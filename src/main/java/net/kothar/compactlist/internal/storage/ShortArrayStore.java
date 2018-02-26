package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ShortArrayStore extends CompactStore<short[]> {

	private ShortArrayStore() {
		super();
	}

	public ShortArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public ShortArrayStore(CompactionStrategy strategy, Store elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	public ShortArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (short) value;
	}

	@Override
	protected short[] allocateArray(int length) {
		return new short[length];
	}

	@Override
	public boolean inRange(long value) {
		return value >= 0 && value < 1 << 16;
	}

	@Override
	public int getWidth() {
		return Short.SIZE;
	}

	@Override
	protected ArrayStore<short[]> newInstance() {
		return new ShortArrayStore();
	}

}
