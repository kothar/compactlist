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

	private static final int	READ_COMPACTION_DELAY	= 1 << 10;
	private static final int	WRITE_COMPACTION_DELAY	= 1 << 13;
	private static final int	TARGET_LEAF_SIZE		= 1 << 16;

	protected int				size;
	protected StorageStrategy	elements;
	protected Node				left, right;
	protected int				height;
	protected boolean			dirty	= true;
	protected long				operation,
		lastRead,
		lastWrite,
		lastCompaction;

	public Node() {
		this(new LongArrayStore());
	}

	public Node(StorageStrategy elements) {
		this.elements = elements;
		this.size = elements.size();
	}

	private boolean isLeaf() {
		return left == null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public long getLong(int index) {
		assert index < size && index >= 0;

		// Leaf
		if (isLeaf()) {
			lastRead = ++operation;
			if (dirty && lastRead - lastWrite > READ_COMPACTION_DELAY) {
				compact();
			}
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
		assert index < size && index >= 0;

		// Replace with non-compact representation if out of range
		if (isLeaf() && !elements.inRange(index, element, true)) {
			elements = new LongArrayStore(elements, 0, size);
			dirty = true;
		}

		// Leaf
		if (isLeaf()) {
			lastWrite = ++operation;
			return elements.set(index, element);
		} else if (index < left.size) {
			// Left branch
			return left.setLong(index, element);
		} else {
			// Right branch
			return right.setLong(index - left.size, element);
		}
	}

	@Override
	public void addLong(int index, long element) {
		assert index <= size && index >= 0;

		if (!isLeaf()) {
			addChild(index, element);
			return;
		}

		// Split
		if (isLeaf() && size >= TARGET_LEAF_SIZE) {
			if (index == size && elements.capacity() > size) {
				// Don't split if we have more capacity to append
			} else {
				split(index);
			}
		}

		if (isLeaf()) {
			// Leaf
			lastWrite = ++operation;

			// Replace with non-compact representation if out of range
			if (!elements.inRange(index, element, false)) {
				LongArrayStore newElements = new LongArrayStore(size + 1);
				newElements.copy(elements, 0, 0, index);
				newElements.set(index, element);
				newElements.copy(elements, index + 1, index, size - index);
				elements = newElements;
				dirty = true;
			} else {
				elements.add(index, element);
			}
			size++;

			if (dirty && lastWrite - lastCompaction > WRITE_COMPACTION_DELAY) {
				compact();
			}
		} else {
			addChild(index, element);
		}
	}

	private void addChild(int index, long element) {
		if (index > left.size || (index == left.size && right.size == 0)) {
			// Right branch
			right.addLong(index - left.size, element);
		} else {
			// Left branch
			left.addLong(index, element);
		}
		size++;
		balance();
	}

	@Override
	public long removeLong(int index) {
		assert index < size && index >= 0;

		// If we can't safely remove an element, decompact the store
		if (isLeaf() && !elements.isPositionIndependent()) {
			elements = new LongArrayStore(elements);
			dirty = true;
		}

		// Leaf
		size--;
		long oldValue;
		if (isLeaf()) {
			lastWrite = ++operation;
			return elements.remove(index);
		} else if (index < left.size) {
			// Left branch
			oldValue = left.removeLong(index);
		} else {
			// Right branch
			oldValue = right.removeLong(index - left.size);
		}

		if (size <= TARGET_LEAF_SIZE) {
			// TODO do this as part of the pre-removal step
			merge();
		} else {
			balance();
		}

		return oldValue;
	}

	protected void split(int pivot) {
		if (isLeaf()) {
			if (pivot == size) {
				left = new Node(elements);
				right = new Node(new LongArrayStore());
				left.compact();
			} else if (pivot == 0) {
				left = new Node(new LongArrayStore());
				right = new Node(elements);
				right.compact();
			} else {
				// Right node takes a copy of data after pivot
				right = new Node(new LongArrayStore(elements, pivot, size - pivot));

				// Left re-uses the current elements array
				elements.setSize(pivot);
				left = new Node(elements);
			}

			elements = null;
			height = 1;

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
		dirty = true;
		lastWrite = lastRead = operation = 0;
	}

	protected void mergeElements(LongArrayStore target, int offset) {
		if (isLeaf()) {
			if (size > 0) {
				target.copy(elements, offset);
			}
		} else {
			left.mergeElements(target, offset);
			right.mergeElements(target, offset + left.size);
		}
	}

	protected void balance() {
		if (isLeaf()) {
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

		updateHeight();
		assert size == left.size + right.size;
	}

	private void update() {
		updateHeight();
		updateSize();
	}

	private void updateSize() {
		size = left.size + right.size;
	}

	private void updateHeight() {
		height = Math.max(left.height, right.height) + 1;
	}

	public void compact() {

		if (!isLeaf()) {
			if (size <= TARGET_LEAF_SIZE) {
				merge();
			} else {
				left.compact();
				right.compact();
				balance();
				return;
			}
		}

		if (!dirty) {
			return;
		}

		try {
			if (size == 0) {
				// Edge case where all elements have been removed from a leaf
				elements = new ConstantStore(new OffsetCompactionStrategy(0), 0, 0);
				return;
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
		} finally {
			dirty = false;
			lastCompaction = operation;
		}
	}

	public void print(String prefix, String indent) {
		if (isLeaf()) {
			System.out.println(prefix + "h: 0 " + elements);
		} else {
			System.out.println(prefix + "h: " + height);
			left.print(prefix + indent, indent);
			right.print(prefix + indent, indent);
		}
	}

	@Override
	public String toString() {
		if (!isLeaf()) {
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

		int		pos, currentPos;
		Node	current;

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
			while (!current.isLeaf()) {
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
			current.removeLong(currentPos);
			for (Node n : stack) {
				n.size--;
			}
		}
	}

	public void walk(Consumer<Node> leafConsumer) {
		ArrayList<Node> stack = new ArrayList<>();
		stack.add(this);

		while (!stack.isEmpty()) {
			Node current = stack.remove(stack.size() - 1);

			// Move to the leftmost child
			while (!current.isLeaf()) {
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