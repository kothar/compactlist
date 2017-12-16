package net.kothar.compactlist.internal;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Node<T> implements Iterable<Long> {

	private static final int MAX_LEAF_SIZE = 1 << 16;
	private static final int MIN_SUBTREE_SIZE = 1_000;

	protected int size;
	protected T elements;
	protected Node<?> left, right;
	protected int height;

	protected NodeManager manager;
	protected Node<?> parent;

	protected abstract long getLongElement(int index);

	protected abstract void addLongElement(int index, long element);

	protected abstract void setLongElement(int index, long element);

	protected abstract void splitElements(int pivot);

	protected abstract boolean elementInRange(long element);

	public Node(Node<?> parent, NodeManager manager) {
		this.parent = parent;
		this.manager = manager;
	}

	public int size() {
		return size;
	}

	public long getLong(int index) {
		if (index >= size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Leaf
		if (left == null) {
			return getLongElement(index);
		}

		// Left branch
		if (index < left.size) {
			return left.getLong(index);
		}

		// Right branch
		return right.getLong(index - left.size);
	}

	public Node<?> setLong(int index, long element) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Replace with non-compact representation if out of range
		if (left == null && !elementInRange(element)) {
			long[] newElements = new long[size + 16];
			for (int i = 0; i < index; i++) {
				newElements[i] = getLongElement(i);
			}
			newElements[index] = element;
			for (int i = index + 1; i < size; i++) {
				newElements[i] = getLongElement(i);
			}
			return new LongArrayNode(parent, manager, newElements, size);
		}

		// Leaf
		if (left == null) {
			setLongElement(index, element);
		} else if (index <= left.size) {
			// Left branch
			left = left.setLong(index, element);
		} else {
			// Right branch
			right = right.setLong(index - left.size, element);
		}

		return this;
	}

	public Node<?> addLong(int index, long element) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Split
		if (left == null && size >= MAX_LEAF_SIZE) {
			if (index == 0 || index == size) {
				split(size / 2);
			} else {
				split(index);
			}
		}

		// Replace with non-compact representation if out of range
		if (left == null && !elementInRange(element)) {
			long[] newElements = new long[size + 16];
			for (int i = 0; i < index; i++) {
				newElements[i] = getLongElement(i);
			}
			newElements[index] = element;
			for (int i = index; i < size; i++) {
				newElements[i + 1] = getLongElement(i);
			}
			return new LongArrayNode(parent, manager, newElements, size + 1);
		}

		// Leaf
		if (left == null) {
			addLongElement(index, element);
			size++;
			return this;
		} else if (index <= left.size) {
			// Left branch
			left = left.addLong(index, element);
		} else {
			// Right branch
			right = right.addLong(index - left.size, element);
		}

		balance();
		return this;
	}

	public Node<?> remove(int index) {
		if (index > size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		// Leaf
		if (left == null) {
			removeElement(index);
			size--;
			return this;
		} else if (index < left.size) {
			// Left branch
			left = left.remove(index);

		} else {
			// Right branch
			right = right.remove(index - left.size);
		}

		if (size < MIN_SUBTREE_SIZE) {
			return merge();
		}

		balance();
		return this;
	}

	protected void removeElement(int index) {
		if (index < size - 1) {
			System.arraycopy(elements, index + 1, elements, index, size - index - 1);
		}
	}

	protected void split(int pivot) {
		if (left == null) {
			splitElements(pivot);
			height = 1;
		} else if (pivot < left.size) {
			left.split(pivot);
			balance();
		} else {
			right.split(pivot - left.size);
			balance();
		}
	}

	protected LongArrayNode merge() {
		LongArrayNode mergedNode = new LongArrayNode(parent, manager);
		mergedNode.size = size;
		mergedNode.elements = new long[size];

		left.mergeElements(mergedNode.elements, 0);
		right.mergeElements(mergedNode.elements, left.size);

		balanceTree();
		return mergedNode;
	}

	protected void mergeElements(long[] target, int offset) {
		if (left == null) {
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					target[i + offset] = getLongElement(i);
				}
			}
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
		while (right.height - left.height > 1) {
			// Rotate left
			Node<?> alpha = left, beta = right.left, gamma = right.right;
			left = right;
			left.left = alpha;
			left.right = beta;
			right = gamma;

			left.update();

		}

		while (left.height - right.height > 1) {
			// Rotate right
			Node<?> alpha = left.left, beta = left.right, gamma = right;
			right = left;
			left = alpha;
			right.left = beta;
			right.right = gamma;

			right.update();
		}
		update();
	}

	private void update() {
		if (left != null) {
			height = Math.max(left.height, right.height) + 1;
			size = left.size + right.size;
		}
	}

	public Node<?> compact() {
		if (size == 0) {
			return this;
		}

		if (left != null) {
			left = left.compact();
			right = right.compact();
			return this;
		}

		// Decide on the range of values in this node
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;

		for (int i = 0; i < size; i++) {
			long v = getLong(i);
			min = Math.min(min, v);
			max = Math.max(max, v);
		}

		long range = max - min;
		if (range < 1L << 8) {
			return new ByteArrayNode(min, this);
		} else if (range < 1L << 16) {
			return new ShortArrayNode(min, this);
		} else if (range < 1L << 32) {
			return new IntArrayNode(min, this);
		} else {
			return this;
		}
	}

	public void print(String prefix, String indent) {
		if (left == null) {
			System.out.println(prefix + "h: 0 " + elements);
		} else {
			System.out.println(prefix + "h: " + height);
			left.print(prefix + indent, indent);
			right.print(prefix + indent, indent);
		}
	}

	@Override
	public Iterator<Long> iterator() {
		return new NodeIterator();
	}

	public class NodeIterator implements Iterator<Long> {

		ArrayList<Node<?>> stack = new ArrayList<>();

		int pos, currentPos;
		Node<?> current;

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
			while (current.left != null) {
				stack.add(current);
				current = current.left;
			}

			// Iterate over current node
			long v = current.getLongElement(currentPos++);
			pos++;
			return v;

		}

		@Override
		public void remove() {
			pos--;
			currentPos--;
			current.removeElement(currentPos);
			Node<?> n = current;
			while (n != null) {
				n.size--;
				n = n.parent;
			}
		}
	}
}