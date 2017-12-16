package net.kothar.compactlist.internal;

public class IntArrayNode extends ArrayNode<int[]> {

	long offset;

	public IntArrayNode(Long offset, Node<?> parent, NodeManager manager) {
		super(parent, manager);
		this.offset = offset;
	}

	public IntArrayNode(long offset, Node<?> node) {
		super(node.parent, node.manager);
		this.offset = offset;
		size = node.size;
		elements = new int[size];
		for (int i = 0; i < size; i++) {
			elements[i] = (int) (node.getLongElement(i) - offset);
		}
	}

	@Override
	protected long getLongElement(int index) {
		return offset + (elements[index] & 0xFFFFFFFF);
	}

	@Override
	protected int[] allocateElements(int length) {
		return new int[length];
	}

	@Override
	protected int length(int[] elements) {
		return elements.length;
	}

	@Override
	protected void set(int[] elements, int index, long element) {
		elements[index] = (int) (element - offset);
	}

	@Override
	protected void createChildren() {
		left = new IntArrayNode(offset, this, manager);
		right = new IntArrayNode(offset, this, manager);
	}

	@Override
	protected boolean elementInRange(long element) {
		long v = element - offset;
		return v >= 0 && v < 1 << 32;
	}
}
