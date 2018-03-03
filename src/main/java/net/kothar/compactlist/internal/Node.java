package net.kothar.compactlist.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.kothar.compactlist.LongList;
import net.kothar.compactlist.internal.compaction.StorageAnalysis;
import net.kothar.compactlist.internal.storage.ByteArrayStore;
import net.kothar.compactlist.internal.storage.ConstantStore;
import net.kothar.compactlist.internal.storage.IntArrayStore;
import net.kothar.compactlist.internal.storage.LongArrayStore;
import net.kothar.compactlist.internal.storage.ShortArrayStore;
import net.kothar.compactlist.internal.storage.Store;

public class Node implements Iterable<Long>, LongList, Serializable {

	private static final long serialVersionUID = 3418582865627235043L;

	private static final Logger		log		= LoggerFactory.getLogger(Node.class);
	private static final boolean	trace	= log.isTraceEnabled();

	private static final int	TARGET_LEAF_SIZE	= (1 << 16) - 1;
	private static final int	MAX_LEAF_SIZE		= 1 << 20;

	protected int		size;
	protected Store		elements;
	protected Node		left, right;
	protected int		height;
	protected boolean	dirty	= true;

	public Node() {
		this(new ShortArrayStore(0));
	}

	public Node(Store elements) {
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
			return elements.getLong(index);
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
		// TODO evaluate storage range
		if (isLeaf() && !elements.inRange(element)) {
			elements = new LongArrayStore(elements, 0, size);
			dirty = true;
		}

		// Leaf
		if (isLeaf()) {
			return elements.setLong(index, element);
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
		if (isLeaf()
			&& size >= TARGET_LEAF_SIZE
			&& (index < size // Insert
				|| size >= MAX_LEAF_SIZE // Too big
				|| !elements.inRange(element) // Out of range
				|| elements.capacity() == 0 // Allocation required
			)) {
			split(index);
		}

		if (isLeaf()) {
			// Leaf
			// Replace with non-compact representation if out of range
			// TODO perform storage analysis
			if (!elements.inRange(element)) {
				LongArrayStore newElements = new LongArrayStore(size + 1);
				newElements.copy(elements, 0, 0, index);
				newElements.set(index, element);
				newElements.copy(elements, index + 1, index, size - index);
				elements = newElements;
				dirty = true;
			} else {
				elements.addLong(index, element);
			}
			size++;
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

		long oldValue;
		if (isLeaf()) {
			if (index == 0 || index == size - 1) {
				oldValue = elements.removeLong(index);
			} else {
				split(index + 1);
				oldValue = left.removeLong(index);
			}
		} else if (index < left.size) {
			// Left branch
			oldValue = left.removeLong(index);
		} else {
			// Right branch
			oldValue = right.removeLong(index - left.size);
		}

		size--;
		if (!isLeaf() && size == 0) {
			release();
			elements = new ConstantStore(0, 0);
			height = 0;
			dirty = true;
		} else {
			balance();
		}
		return oldValue;
	}

	private void release() {
		if (!isLeaf()) {
			left.release();
			right.release();
			left = null;
			right = null;
		} else {
			elements.release();
		}
	}

	protected void split(int pivot) {

		if (trace)
			log.trace("split {}: {}", pivot, this);

		if (isLeaf()) {
			if (pivot == size) {
				left = new Node(elements);
				right = new Node(new ShortArrayStore(elements.getLong(pivot - 1)));
			} else if (pivot == 0) {
				left = new Node(new ShortArrayStore(elements.getLong(0)));
				right = new Node(elements);
			} else {
				Store[] splitElements = elements.split(pivot);
				left = new Node(splitElements[0]);
				right = new Node(splitElements[1]);
			}

			elements = null;
			height = 1;

			assert left.size == pivot;
			assert right.size == size - pivot;
		} else if (pivot < left.size) {
			left.split(pivot);
			balance();
		} else {
			right.split(pivot - left.size);
			balance();
		}
	}

	protected void merge() {

		if (trace)
			log.trace("merge: {}", this);

		// TODO analyse elements to determine range required
		LongArrayStore target = new LongArrayStore(size);

		left.mergeElements(target, 0);
		right.mergeElements(target, left.size);

		elements = target;
		left = null;
		right = null;
		height = 0;
		dirty = true;
	}

	protected void mergeElements(LongArrayStore target, int offset) {
		if (isLeaf()) {
			if (size > 0) {
				target.copy(elements, offset);
			}
			elements.release();
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

		if (trace)
			log.trace("compact: {}", this);

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
				elements.release();
				elements = new ConstantStore(0, 0);
				return;
			}

			// Analyse the values in this node
			StorageAnalysis analysis = new StorageAnalysis(elements);
			long range = analysis.max - analysis.min;

			// Choose an appropriate storage strategy for the range of compact values
			// observed
			if (range >= 0) {
				Store newElements = null;
				if (range == 0) {
					newElements = new ConstantStore(elements);
				} else if (range < 1L << 8) {
					newElements = new ByteArrayStore(analysis.min, elements);
				} else if (range < 1L << 16) {
					newElements = new ShortArrayStore(analysis.min, elements);
				} else if (range < 1L << 32) {
					newElements = new IntArrayStore(analysis.min, elements);
				}

				if (newElements != null) {
					elements.release();
					elements = newElements;
				}
			}
		} finally {
			dirty = false;
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

			while (current != null) {
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
				if (currentPos < current.size) {
					long v = current.elements.getLong(currentPos++);
					pos++;
					return v;
				}
			}
			throw new IllegalStateException();

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

	public Store getStorageStrategy() {
		return elements;
	}

	/**
	 * Performs a binary search in the node to locate the index of the given value.
	 * <p>
	 * Assumes that the values are in ascending order.
	 * 
	 * @param value
	 *            The value to search for
	 * @return The index of the value. If not found, returns -1 minus the index at which it should
	 *         be inserted.
	 */
	public int searchLong(long value) {
		if (isLeaf()) {
			return Collections.binarySearch(elements, value, Comparator.naturalOrder());
		}

		if (right.size > 0 && right.getLong(0) <= value) {
			int index = right.searchLong(value);
			index += index < 0 ? -left.size : left.size;
			return index;
		} else if (left.size > 0) {
			return left.searchLong(value);
		}
		return -1;
	}
}
