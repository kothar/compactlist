package net.kothar.compactlist.internal;

public class LongArrayNode extends ArrayNode<long[]> {

	public LongArrayNode(NodeContainer parent, NodeManager manager) {
		super(parent, manager);
	}

	@Override
	protected long getLongElement(int index) {
		return elements[index];
	}

	/**
	 * As the target type matches this container, we can do a more efficient memcopy
	 * into the target array
	 */
	@Override
	protected void mergeElements(long[] target, int offset) {
		if (left == null) {
			if (size > 0) {
				System.arraycopy(elements, 0, target, offset, size);
			}
		} else {
			left.mergeElements(target, offset);
			right.mergeElements(target, offset + left.size);
		}
	}

	@Override
	protected long[] allocateElements(int length) {
		return new long[length];
	}

	@Override
	protected int length(long[] elements) {
		return elements.length;
	}

	@Override
	protected void set(long[] elements, int index, long element) {
		elements[index] = element;
	}

	@Override
	protected void createChildren() {
		left = new LongArrayNode(parent, manager);
		right = new LongArrayNode(parent, manager);
	}

}
