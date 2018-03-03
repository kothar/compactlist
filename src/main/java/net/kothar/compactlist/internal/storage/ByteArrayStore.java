package net.kothar.compactlist.internal.storage;

public class ByteArrayStore extends CompactStore<byte[]> {

	private static final long serialVersionUID = -1702416986721114622L;

	public ByteArrayStore() {
		this(0);
	}

	public ByteArrayStore(long valueOffset) {
		super(valueOffset);
	}

	public ByteArrayStore(long valueOffset, Store elements) {
		super(valueOffset, elements.size(), elements.size());
		copy(elements, 0);
	}

	public ByteArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (byte) value;
	}

	@Override
	protected byte[] allocateArray(int length) {
		return new byte[length];
	}

	@Override
	public boolean inRange(long value) {
		long compactValue = value - valueOffset;
		return compactValue >= 0 && compactValue < 1 << 8;
	}

	@Override
	public int getWidth() {
		return Byte.SIZE;
	}

	@Override
	protected ArrayStore<byte[]> newInstance() {
		return new ByteArrayStore(valueOffset);
	}

}
