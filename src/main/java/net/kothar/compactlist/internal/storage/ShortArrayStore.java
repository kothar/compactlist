package net.kothar.compactlist.internal.storage;

public class ShortArrayStore extends CompactStore<short[]> {

	private static final long serialVersionUID = -8685201260630950651L;

	public ShortArrayStore() {
		this(0);
	}

	public ShortArrayStore(long valueOffset) {
		super(valueOffset);
	}

	public ShortArrayStore(long valueOffset, Store elements) {
		super(valueOffset, elements.size(), elements.size());
		copy(elements, 0);
	}

	public ShortArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (short) value;
	}

	@Override
	protected short[] allocateArray(int length) {
		return new short[length];
	}

	@Override
	public boolean inRange(long value) {
		long compactValue = value - valueOffset;
		return compactValue >= 0 && compactValue < 1 << 16;
	}

	@Override
	public int getWidth() {
		return Short.SIZE;
	}

	@Override
	protected ArrayStore<short[]> newInstance() {
		return new ShortArrayStore(valueOffset);
	}

}
