package net.kothar.compactlist.internal.storage;

public class ConstantStore extends AbstractStore {

	private static final long serialVersionUID = -6037938423175067220L;

	private long value;

	public ConstantStore(long value, int size) {
		super();
		this.value = value;
		this.size = size;
	}

	public ConstantStore(Store elements) {
		super();
		this.value = elements.get(0);
		this.size = elements.size();
	}

	@Override
	public void allocate(int size) {
		this.size = size;
	}

	@Override
	public long getLong(int index) {
		return value;
	}

	@Override
	public void addLong(int index, long value) {
		assert value == this.value;
		size++;
	}

	@Override
	public long setLong(int index, long value) {
		assert value == this.value;
		return value;
	}

	@Override
	public long removeLong(int index) {
		size--;
		return value;
	}

	@Override
	public boolean inRange(long value) {
		return value == this.value;
	}

	@Override
	public int capacity() {
		return size;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int prependCapacity() {
		return size;
	}

	@Override
	public int appendCapacity() {
		return size;
	}

	@Override
	public Store[] split(int index) {
		ConstantStore that = new ConstantStore(value, size - index);
		size = index;
		return new Store[] { this, that };
	}

	@Override
	public void release() {
		// Nothing to do
	}

}
