package net.kothar.compactlist.internal;

public class ByteArrayNode extends ArrayNode<byte[]> {

	Long offset;

	public ByteArrayNode(Long offset, NodeContainer parent, NodeManager manager) {
		super(parent, manager);
		this.offset = offset;
	}

	public ByteArrayNode(long offset, AbstractNode<?> node) {
		super(node.parent, node.manager);
		this.offset = offset;
		size = node.size;
		elements = new byte[size];
		for (int i = 0; i < size; i++) {
			elements[i] = (byte) (node.getLongElement(i) - offset);
		}
	}

	@Override
	protected long getLongElement(int index) {
		return offset + (elements[index] & 0xFF);
	}

	@Override
	protected byte[] allocateElements(int length) {
		return new byte[length];
	}

	@Override
	protected int length(byte[] elements) {
		return elements.length;
	}

	@Override
	protected void set(byte[] elements, int index, long element) {
		elements[index] = (byte) (element - offset);
	}

	@Override
	protected void createChildren() {
		left = new ByteArrayNode(offset, parent, manager);
		right = new ByteArrayNode(offset, parent, manager);
	}

}
