package net.kothar.compactlist.internal.storage;

/**
 * A store based on a backing array of some kind
 * 
 * @author mhouston
 *
 * @param <T>
 */
public abstract class ArrayStore<T> extends AbstractStore {

	protected T store;

	/** The lowest index in the array which may be used by this store */
	protected int base;

	/** The array index corresponding to element 0 */
	protected int offset;

	/** The highest index in the array which may be used by this store */
	protected int limit;

	protected abstract T allocateArray(int length);

	protected abstract void setArrayElement(int index, long value);

	protected abstract long getArrayElement(int index);

	public ArrayStore() {
		this(0, ALLOCATION_BUFFER);
	}

	protected ArrayStore(int size, int capactity) {
		this.size = size;
		limit = capactity;
		store = allocateArray(capactity);
	}

	@Override
	public void allocate(int size) {
		limit = size;
		offset = 0;
		store = allocateArray(limit);
	}

	@Override
	public int capacity() {
		return limit - base;
	}

	@Override
	public int prependCapacity() {
		return offset - base + size;
	}

	@Override
	public int appendCapacity() {
		return limit - offset;
	}

	protected void setElement(int index, long value) {
		setArrayElement(index + offset, value);
	}

	protected long getElement(int index) {
		return getArrayElement(index + offset);
	}

	@Override
	public void addLong(int index, long value) {
		expand(index);
		setElement(index, value);
	}

	@Override
	public final long setLong(int index, long value) {
		long oldValue = getLong(index);
		setElement(index, value);
		return oldValue;
	}

	@Override
	public final long getLong(int index) {
		return getElement(index);
	}

	@Override
	public long removeLong(int index) {
		long oldValue = getLong(index);
		if (index == 0) {
			offset++;
		} else if (index == size - 1) {
			// Nothing to do
		} else {
			System.arraycopy(store, index + 1, store, index, size - index - 1);
		}
		size--;
		return oldValue;
	}

	/**
	 * Expands the store if necessary and leaves a new slot at the given index
	 * 
	 * @param index
	 *            The position to insert a gap
	 */
	protected void expand(int index) {
		if (offset > base && (index < size / 2 || offset + size == limit)) {
			// Add by prefix
			offset--;
			if (index > 0) {
				// Shift range [0, index) to the left
				System.arraycopy(store, offset + 1, store, offset, index);
			}
		} else {
			// Add by suffix
			T newStore = store;
			int newOffset = offset;
			if (offset + size == limit) {
				// Re-allocate
				limit = (int) (size * EXPANSION_FACTOR + ALLOCATION_BUFFER);
				base = 0;
				newStore = allocateArray(limit);
				newOffset = 0;
				if (index > 0) {
					System.arraycopy(store, offset, newStore, 0, index);
				}
			}
			if (index < size) {
				System.arraycopy(store, offset + index, newStore, newOffset + index + 1, size - index);
			}
			store = newStore;
			offset = newOffset;
		}
		size++;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if (str.length() > 10) {
				str.append("...");
				break;
			}
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(getLong(i));
		}
		return String.format("%s: [%s]", getClass().getSimpleName(), str);
	}
}
