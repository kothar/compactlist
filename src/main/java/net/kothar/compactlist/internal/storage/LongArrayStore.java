package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class LongArrayStore extends ArrayStore<long[]> {

	/**
	 * Creates a new empty store
	 */
	public LongArrayStore() {
		super();
	}

	public LongArrayStore(CompactionStrategy strategy) {
		super();
	}

	public LongArrayStore(int size) {
		super(size, size + ALLOCATION_BUFFER);
	}

	public LongArrayStore(Store elements) {
		this(elements, 0, elements.size());
	}

	/**
	 * Creates a new store from a subrange of an existing store
	 * 
	 * @param elements
	 *            An existing store to copy elements from
	 * @param offset
	 *            The location to start copying elements
	 * @param size
	 *            The number of elements to copy
	 */
	public LongArrayStore(Store elements, int offset, int size) {
		super(size, size + ALLOCATION_BUFFER);

		if (elements instanceof LongArrayStore) {
			LongArrayStore longStore = (LongArrayStore) elements;
			System.arraycopy(longStore.store, offset + longStore.offset, store, 0, size);
		} else {
			for (int i = 0; i < size; i++) {
				setElement(i, elements.get(i + offset));
			}
		}
	}

	@Override
	public long getArrayElement(int index) {
		return store[index];
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = value;
	}

	@Override
	public boolean inRange(int index, long value) {
		return true;
	}

	@Override
	protected long[] allocateArray(int length) {
		return new long[length];
	}

	/**
	 * Copy the provided elements into this store at the given offset
	 * 
	 */
	@Override
	public void copy(Store src, int dstOffset, int srcOffset, int length) {
		if (src instanceof LongArrayStore) {
			LongArrayStore longStore = (LongArrayStore) src;
			System.arraycopy(longStore.store, srcOffset + longStore.offset, store, dstOffset + offset, length);
		} else {
			super.copy(src, dstOffset, srcOffset, length);
		}
	}

	@Override
	public int getWidth() {
		return Long.SIZE;
	}

	@Override
	protected ArrayStore<long[]> newInstance() {
		return new LongArrayStore();
	}

}
