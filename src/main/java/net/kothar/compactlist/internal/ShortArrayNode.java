package net.kothar.compactlist.internal;

public class ShortArrayNode extends ArrayNode<short[]> {

	long offset;

	public ShortArrayNode(Long offset, Node<?> parent, NodeManager manager) {
		super(parent, manager);
		this.offset = offset;
	}

	public ShortArrayNode(long offset, Node<?> node) {
		super(node.parent, node.manager);
		this.offset = offset;
		size = node.size;
		elements = new short[size];
		for (int i = 0; i < size; i++) {
			elements[i] = (short) (node.getLongElement(i) - offset);
		}
	}

	@Override
	protected long getLongElement(int index) {
		return offset + (elements[index] & 0xFFFF);
	}

	@Override
	protected short[] allocateElements(int length) {
		return new short[length];
	}

	@Override
	protected int length(short[] elements) {
		return elements.length;
	}

	@Override
	protected void set(short[] elements, int index, long element) {
		elements[index] = (short) (element - offset);
	}

	@Override
	protected void createChildren() {
		left = new ShortArrayNode(offset, this, manager);
		right = new ShortArrayNode(offset, this, manager);
	}

	@Override
	protected boolean elementInRange(long element) {
		long v = element - offset;
		return v >= 0 && v < 1 << 16;
	}

}
