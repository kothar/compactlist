package net.kothar.compactlist.internal.storage;

import java.util.Iterator;

public abstract class AbstractStore implements StorageStrategy {

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

	protected void copy(StorageStrategy elements, int offset) {
		int n = elements.size();
		for (int i = 0; i < n; i++) {
			set(i + offset, elements.get(i));
		}
	}
}
