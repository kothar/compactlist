package net.kothar.compactlist.internal.storage;

public class LongArrayStore extends ArrayStore<long[]> {

	/**
	 * Creates a new empty store
	 */
	public LongArrayStore() {
		super();
	}

	public LongArrayStore(int size) {
		super(size, size + ALLOCATION_BUFFER);
	}

	public LongArrayStore(StorageStrategy elements) {
		this(elements, 0, elements.size());
	}

	/**
	 * Creates a new store from a subrange of an existing store
	 * 
	 * @param elements
	 * @param offset
	 * @param size
	 */
	public LongArrayStore(StorageStrategy elements, int offset, int size) {
		super(size, size + ALLOCATION_BUFFER);

		if (elements instanceof LongArrayStore) {
			System.arraycopy(((LongArrayStore) elements).store, offset, store, 0, size);
		} else {
			for (int i = 0; i < size; i++) {
				setElement(i, elements.get(i + offset));
			}
		}
	}

	@Override
	public long get(int index) {
		return store[index];
	}

	@Override
	protected void setElement(int index, long value) {
		store[index] = value;
	}

	@Override
	public boolean inRange(int index, long value, boolean positionIndependent) {
		return true;
	}

	@Override
	protected long[] allocateArray(int length) {
		return new long[length];
	}

	@Override
	protected int length(long[] array) {
		return array.length;
	}

	/**
	 * Copy the provided elements into this store at the given offset
	 * 
	 */
	@Override
	public void copy(StorageStrategy src, int dstOffset, int srcOffset, int length) {
		if (src instanceof LongArrayStore) {
			System.arraycopy(((LongArrayStore) src).store, srcOffset, store, dstOffset, length);
		} else {
			super.copy(src, dstOffset, srcOffset, length);
		}
	}

	@Override
	public boolean isPositionIndependent() {
		return true;
	}

	@Override
	public int getWidth() {
		return Long.SIZE;
	}

}
