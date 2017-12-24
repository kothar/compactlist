package net.kothar.compactlist.internal.storage;

import java.util.AbstractList;
import java.util.Iterator;

public abstract class AbstractStore extends AbstractList<Long> implements StorageStrategy {

	private static final long serialVersionUID = -4353672868307014379L;

	/** When allocating a new store, add an extra buffer for future expansion */
	public static final int ALLOCATION_BUFFER = 1 << 4;

	/** When expanding a store, use this factor to allocate the new size */
	protected static final double EXPANSION_FACTOR = 2;

	/** How many elements are currently present in this store */
	protected int size;

	@Override
	public int size() {
		return size;
	}

	@Override
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public Iterator<Long> iterator() {
		return new Iterator<Long>() {

			int pos;

			@Override
			public boolean hasNext() {
				return pos < size;
			}

			@Override
			public Long next() {
				return AbstractStore.this.get(pos++);
			}

			@Override
			public void remove() {
				AbstractStore.this.remove(--pos);
			}
		};
	}

	public void copy(StorageStrategy src, int dstOffset) {
		copy(src, dstOffset, 0, src.size());
	}

	public void copy(StorageStrategy src, int dstOffset, int srcOffset, int length) {
		for (int i = 0; i < length; i++) {
			set(dstOffset + i, src.get(srcOffset + i));
		}
	}

	@Override
	public Long get(int index) {
		return getLong(index);
	}

	@Override
	public Long set(int index, Long element) {
		return setLong(index, element);
	}

	@Override
	public void add(int index, Long element) {
		addLong(index, element);
	}

	@Override
	public Long remove(int index) {
		return removeLong(index);
	}

	@Override
	public void clear() {
		setSize(0);
	}

}
