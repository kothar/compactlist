package net.kothar.compactlist.internal.storage;

public abstract class CompactStore<T> extends ArrayStore<T> {

	private static final long serialVersionUID = -2150017141000225835L;

	protected long valueOffset;

	public CompactStore(long valueOffset) {
		super();
		this.valueOffset = valueOffset;
	}

	public CompactStore(long valueOffset, int size, int capactity) {
		super(size, capactity);
		this.valueOffset = valueOffset;
	}

	public CompactStore(Store elements, int pos, int size) {
		super(size, size);
		if (elements instanceof CompactStore) {
			valueOffset = ((CompactStore<?>) elements).valueOffset;
		} else {
			valueOffset = 0;
		}
		copy(elements, 0, pos, size);
	}

	@Override
	protected final long getElement(int index) {
		return getArrayElement(index + offset) + valueOffset;
	}

	@Override
	protected final void setElement(int index, long value) {
		setArrayElement(index + offset, value - valueOffset);
	}

}