package net.kothar.compactlist.internal.storage;

public class IntArrayStore extends CompactStore<int[]> {

	private static final long serialVersionUID = 775028421757439454L;

	public IntArrayStore() {
		this(0);
	}

	public IntArrayStore(long valueOffset) {
		super(valueOffset);
	}

	public IntArrayStore(long valueOffset, Store elements) {
		super(valueOffset, elements.size(), elements.size());
		copy(elements, 0);
	}

	public IntArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFFF_FFFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (int) value;
	}

	@Override
	protected int[] allocateArray(int length) {
		return new int[length];
	}

	@Override
	public boolean inRange(long value) {
		long compactValue = value - valueOffset;
		return compactValue >= 0 && compactValue < 1 << 32;
	}

	@Override
	public int getWidth() {
		return Integer.SIZE;
	}

	@Override
	protected ArrayStore<int[]> newInstance() {
		return new IntArrayStore(valueOffset);
	}

}
