package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class IntArrayStore extends CompactArrayStore<int[]> {

	public IntArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public IntArrayStore(CompactionStrategy strategy, StorageStrategy elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	public IntArrayStore(StorageStrategy elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getLong(int index) {
		return strategy.getRealValue(index, store[index] & 0xFFFF_FFFFL);
	}

	@Override
	protected void setElement(int index, long value) {
		store[index] = (int) (strategy.getCompactValue(index, value));
	}

	@Override
	protected int[] allocateArray(int length) {
		return new int[length];
	}

	@Override
	protected int length(int[] array) {
		return array.length;
	}

	@Override
	public boolean inRange(long value) {
		return value >= 0 && value < 1 << 32;
	}

	@Override
	public int getWidth() {
		return Integer.SIZE;
	}

}
