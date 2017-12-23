package net.kothar.compactlist.internal.storage;

public abstract class ArrayStore<T> extends AbstractStore {

	protected T store;

	protected abstract T allocateArray(int length);

	protected abstract int length(T array);

	protected abstract void setElement(int index, long value);

	public ArrayStore() {
		this(0, ALLOCATION_BUFFER);
	}

	protected ArrayStore(int size, int capactity) {
		this.size = size;
		store = allocateArray(capactity);
	}

	@Override
	public void allocate(int size) {
		store = allocateArray(size + ALLOCATION_BUFFER);
	}

	@Override
	public int capacity() {
		return length(store);
	}

	@Override
	public void add(int index, long value) {
		expand(index);
		setElement(index, value);
	}

	@Override
	public long set(int index, long value) {
		long oldValue = get(index);
		setElement(index, value);
		return oldValue;
	}

	@Override
	public long remove(int index) {
		long oldValue = get(index);
		System.arraycopy(store, index + 1, store, index, size - index - 1);
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
		T newStore = store;
		if (length(store) == size) {
			newStore = allocateArray((int) (size * EXPANSION_FACTOR));
			if (index > 0) {
				System.arraycopy(store, 0, newStore, 0, index);
			}
		}
		if (index < size) {
			System.arraycopy(store, index, newStore, index + 1, size - index);
		}
		store = newStore;
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
			str.append(get(i));
		}
		return String.format("%s: [%s]", getClass().getSimpleName(), str);
	}
}
