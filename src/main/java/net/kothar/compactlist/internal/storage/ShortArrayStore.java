package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ShortArrayStore extends CompactArrayStore<short[]> {

	public ShortArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public ShortArrayStore(CompactionStrategy strategy, StorageStrategy elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	public ShortArrayStore(StorageStrategy elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getLong(int index) {
		return strategy.getRealValue(index, store[index] & 0xFFFFL);
	}

	@Override
	protected void setElement(int index, long value) {
		store[index] = (short) strategy.getCompactValue(index, value);
	}

	@Override
	protected short[] allocateArray(int length) {
		return new short[length];
	}

	@Override
	protected int length(short[] array) {
		return array.length;
	}

	@Override
	public boolean inRange(long value) {
		return value >= 0 && value < 1 << 16;
	}

	@Override
	public int getWidth() {
		return Short.SIZE;
	}

}
