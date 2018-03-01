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

	/**
	 * Stores sharing the same backing array which may be handed a range when this store allocates a
	 * new backing store.
	 */
	public ArrayStore<T>	left;
	public ArrayStore<T>	right;

	protected abstract T allocateArray(int length);

	protected abstract void setArrayElement(int index, long value);

	protected abstract long getArrayElement(int index);

	protected abstract ArrayStore<T> newInstance();

	public ArrayStore() {
		this(0, ALLOCATION_BUFFER);
	}

	protected ArrayStore(int size, int capactity) {
		this.size = size;
		limit = capactity;
		store = allocateArray(capactity);
	}

	@Override
	public void allocate(int capacity) {
		release();

		size = 0;
		base = 0;
		offset = 0;
		limit = capacity;
		store = allocateArray(capacity);

	}

	/**
	 * Release existing backing store claim. Further reads or writes will fain unless
	 * {@link #allocate(int)} is called
	 */
	@Override
	public void release() {

		if (left != null) {
			left.limit = limit;
			left.right = right;

			if (right != null) {
				right.left = left;
			}
		} else if (right != null) {
			right.base = base;
			right.left = null;
		}

		left = null;
		right = null;
		store = null;
	}

	@Override
	public int capacity() {
		return limit - base;
	}

	@Override
	public int prependCapacity() {
		return offset - base;
	}

	@Override
	public int appendCapacity() {
		return limit - offset - size;
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
		if (prependCapacity() > 0 && (index < size / 2 || appendCapacity() == 0)) {
			// Add by prefix
			offset--;
			if (index > 0) {
				// Shift range [0, index) to the left
				System.arraycopy(store, offset + 1, store, offset, index);
			}
		} else {
			// Add by suffix
			if (appendCapacity() == 0) {
				// Re-allocate
				int capacity = (int) (size * EXPANSION_FACTOR + ALLOCATION_BUFFER);
				T newStore = allocateArray(capacity);
				if (index > 0) {
					System.arraycopy(store, offset, newStore, 0, index);
				}
				if (index < size) {
					System.arraycopy(store, offset + index, newStore, index + 1, size - index);
				}

				release();
				store = newStore;
				base = 0;
				offset = 0;
				limit = capacity;
			} else if (index < size) {
				System.arraycopy(store, offset + index, store, offset + index + 1, size - index);
			}
		}
		size++;
	}

	@Override
	public Store[] split(int index) {
		ArrayStore<T> that = newInstance();

		that.store = store;
		that.base = offset + index;
		that.offset = that.base;
		that.size = size - index;
		that.limit = limit;

		that.left = this;
		if (this.right != null) {
			this.right.left = that;
			that.right = right;
		}

		this.size = index;
		this.limit = that.base;

		this.right = that;

		assert this.size == that.offset - this.offset;

		return new Store[] { this, that };
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
