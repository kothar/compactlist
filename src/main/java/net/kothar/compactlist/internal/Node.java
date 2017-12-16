package net.kothar.compactlist.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.kothar.compactlist.LongList;
import net.kothar.compactlist.internal.compaction.CompactionEvaluator;
import net.kothar.compactlist.internal.compaction.CompactionStrategy;
import net.kothar.compactlist.internal.compaction.LinearPredictionCompactionStrategy;
import net.kothar.compactlist.internal.compaction.OffsetCompactionStrategy;
import net.kothar.compactlist.internal.compaction.StorageAnalysis;
import net.kothar.compactlist.internal.storage.AbstractStore;
import net.kothar.compactlist.internal.storage.ByteArrayStore;
import net.kothar.compactlist.internal.storage.ConstantStore;
import net.kothar.compactlist.internal.storage.IntArrayStore;
import net.kothar.compactlist.internal.storage.LongArrayStore;
import net.kothar.compactlist.internal.storage.NibbleArrayStore;
import net.kothar.compactlist.internal.storage.ShortArrayStore;
import net.kothar.compactlist.internal.storage.StorageStrategy;

public class Node implements Iterable<Long>, LongList, Serializable {

	private static final long serialVersionUID = 3105932349913959938L;

	private static final int MAX_LEAF_SIZE = 1 << 16;
	private static final int MIN_SUBTREE_SIZE = 1_000;

	protected int size;
	protected StorageStrategy elements;
	protected Node left, right;
	protected int height;

	protected NodeManager manager;
	protected Node parent;

	public Node(Node parent, NodeManager manager) {
		this(parent, manager, new LongArrayStore());
	}

	public Node(Node parent, NodeManager manager, StorageStrategy elements) {
		this.parent = parent;
		this.manager = manager;
		this.elements = elements;
		this.size = elements.size();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public long getLong(int index) {
		if (index >= size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Leaf
		if (elements != null) {
			return elements.get(index);
		}

		// Left branch
		if (index < left.size) {
			return left.getLong(index);
		}

		// Right branch
		return right.getLong(index - left.size);
	}

	@Override
	public long setLong(int index, long element) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Replace with non-compact representation if out of range
		if (elements != null && !elements.inRange(index, element, true)) {
			elements = new LongArrayStore(elements, 0, size);
			manager.mark(this);
		}

		// Leaf
		if (elements != null) {
			return elements.set(index, element);
		} else if (index <= left.size) {
			// Left branch
			return left.setLong(index, element);
		} else {
			// Right branch
			return right.setLong(index - left.size, element);
		}
	}

	@Override
	public void addLong(int index, long element) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Split
		if (elements != null && size >= MAX_LEAF_SIZE) {
			if (index == 0 || index == size) {
				split(size / 2);
			} else {
				split(index);
			}
		}

		// Replace with non-compact representation if out of range
		if (elements != null && !elements.inRange(index, element, false)) {
			LongArrayStore newElements = new LongArrayStore(size + 1);
			newElements.copy(elements, 0, 0, index);
			newElements.set(index, element);
			newElements.copy(elements, index + 1, index, size - index);
			elements = newElements;
			manager.mark(this);
		}

		// Leaf
		else if (elements != null) {
			elements.add(index, element);
		} else if (index <= left.size) {
			// Left branch
			left.addLong(index, element);
		} else {
			// Right branch
			right.addLong(index - left.size, element);
		}

		size++;
		balance();
	}

	@Override
	public long removeLong(int index) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// If we can't safely remove an element, decompact the store
		if (elements != null && !elements.isPositionIndependent()) {
			elements = new LongArrayStore(elements);
			manager.mark(this);
		}

		// Leaf
		size--;
		long oldValue;
		if (elements != null) {
			return elements.remove(index);
		} else if (index < left.size) {
			// Left branch
			oldValue = left.removeLong(index);
		} else {
			// Right branch
			oldValue = right.removeLong(index - left.size);
		}

		if (size < MIN_SUBTREE_SIZE) {
			merge();
		}

		balance();
		return oldValue;
	}

	protected void split(int pivot) {
		if (elements != null) {
			// Right node takes a copy of data after pivot
			right = new Node(this, manager, new LongArrayStore(elements, pivot, size - pivot));

			// Left re-uses the current elements array
			elements.setSize(pivot);
			left = new Node(this, manager, elements);

			elements = null;
			height = 1;

			manager.unmark(this);
			manager.mark(left);
			manager.mark(right);

		} else if (pivot < left.size) {
			left.split(pivot);
			balance();
		} else {
			right.split(pivot - left.size);
			balance();
		}
	}

	protected void merge() {
		LongArrayStore target = new LongArrayStore(size);

		left.mergeElements(target, 0);
		right.mergeElements(target, left.size);

		elements = target;
		left = null;
		right = null;
		height = 0;

		balanceTree();

		manager.mark(this);
	}

	protected void mergeElements(LongArrayStore target, int offset) {
		if (elements != null) {
			if (size > 0) {
				target.copy(elements, offset);
			}
			manager.unmark(this);
		} else {
			left.mergeElements(target, offset);
			right.mergeElements(target, offset + left.size);
		}
	}

	private void balanceTree() {
		if (parent != null) {
			parent.balance();
			parent.balanceTree();
		}
	}

	protected void balance() {
		if (elements != null) {
			return;
		}

		while (right.height - left.height > 1) {
			// Rotate left
			Node alpha = left, beta = right.left, gamma = right.right;
			left = right;
			left.left = alpha;
			left.right = beta;
			right = gamma;

			left.update();

		}

		while (left.height - right.height > 1) {
			// Rotate right
			Node alpha = left.left, beta = left.right, gamma = right;
			right = left;
			left = alpha;
			right.left = beta;
			right.right = gamma;

			right.update();
		}

		update();
	}

	private void update() {
		if (elements == null) {
			height = Math.max(left.height, right.height) + 1;
			size = left.size + right.size;
		}
	}

	public void compact() {
		if (size == 0) {
			// TODO edge case where all elements have been removed from a leaf
			return;
		}

		if (elements == null) {
			if (size < MIN_SUBTREE_SIZE) {
				merge();
			} else {
				left.compact();
				right.compact();
				return;
			}
		}

		// Analyse the values in this node
		StorageAnalysis analysis = new StorageAnalysis();
		analysis.size = size;
		analysis.first = elements.get(0);
		analysis.last = elements.get(size - 1);

		for (int i = 0; i < size; i++) {
			long v = elements.get(i);
			if (v < analysis.min) {
				analysis.min = v;
			}
			if (v > analysis.max) {
				analysis.max = v;
			}
		}

		CompactionStrategy[] strategies = new CompactionStrategy[] {
				new OffsetCompactionStrategy(analysis),
				new LinearPredictionCompactionStrategy(analysis)
		};
		List<CompactionEvaluator> evaluators = Arrays.stream(strategies).map(CompactionEvaluator::new)
				.collect(Collectors.toList());

		// Evaluate available compaction strategies
		for (int i = 0; i < size && !evaluators.isEmpty(); i++) {
			long v = elements.get(i);
			for (Iterator<CompactionEvaluator> si = evaluators.iterator(); si.hasNext();) {
				CompactionEvaluator strategy = si.next();
				if (!strategy.evaluate(i, v)) {
					si.remove();
				}
			}
		}

		if (evaluators.isEmpty()) {
			// We may still be able to truncate the storage
			if (elements instanceof LongArrayStore
					&& elements.capacity() > elements.size() + AbstractStore.ALLOCATION_BUFFER) {
				elements = new LongArrayStore(elements);
			}
			return;
		}

		long range = Long.MAX_VALUE;
		CompactionStrategy strategy = null;
		for (CompactionEvaluator evaluator : evaluators) {
			evaluator.normalize();
			if (strategy == null || evaluator.range() < range) {
				strategy = evaluator.getStrategy();
				range = evaluator.range();
			}
		}

		// Choose an appropriate storage strategy for the range of compact values
		// observed
		// TODO do we want to allow any headroom?
		if (range == 0) {
			elements = new ConstantStore(strategy, elements);
		} else if (range < 1L << 4) {
			elements = new NibbleArrayStore(strategy, elements);
		} else if (range < 1L << 8) {
			elements = new ByteArrayStore(strategy, elements);
		} else if (range < 1L << 16) {
			elements = new ShortArrayStore(strategy, elements);
		} else if (range < 1L << 32) {
			elements = new IntArrayStore(strategy, elements);
		}
	}

	public void print(String prefix, String indent) {
		if (elements != null) {
			System.out.println(prefix + "h: 0 " + elements);
		} else {
			System.out.println(prefix + "h: " + height);
			left.print(prefix + indent, indent);
			right.print(prefix + indent, indent);
		}
	}

	@Override
	public String toString() {
		if (elements == null) {
			return String.format("Node: height %d, size %d", height, size);
		} else {
			return String.format("Node: size %d, elements %s", size, elements);
		}
	}

	@Override
	public Iterator<Long> iterator() {
		return new NodeIterator();
	}

	public class NodeIterator implements Iterator<Long> {

		ArrayList<Node> stack = new ArrayList<>();

		int pos, currentPos;
		Node current;

		public NodeIterator() {
			current = Node.this;
		}

		@Override
		public boolean hasNext() {
			return pos < size;
		}

		@Override
		public Long next() {

			// Only true if we've depleted this node
			if (currentPos >= current.size) {
				// Move to the right sibling node
				current = stack.remove(stack.size() - 1).right;
				currentPos = 0;
			}

			// Move to the leftmost child
			while (current.elements == null) {
				stack.add(current);
				current = current.left;
			}

			// Iterate over current node
			long v = current.elements.get(currentPos++);
			pos++;
			return v;

		}

		@Override
		public void remove() {
			pos--;
			currentPos--;
			current.elements.remove(currentPos);
			Node n = current;
			while (n != null) {
				n.size--;
				n = n.parent;
			}
		}
	}

	public void walk(Consumer<Node> leafConsumer) {
		ArrayList<Node> stack = new ArrayList<>();
		stack.add(this);

		while (!stack.isEmpty()) {
			Node current = stack.remove(stack.size() - 1);

			// Move to the leftmost child
			while (current.elements == null) {
				stack.add(current.right);
				current = current.left;
			}

			leafConsumer.accept(current);
		}
	}

	public StorageStrategy getStorageStrategy() {
		return elements;
	}
}