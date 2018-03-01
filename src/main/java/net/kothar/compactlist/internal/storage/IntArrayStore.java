package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class IntArrayStore extends CompactStore<int[]> {

	private IntArrayStore() {
		super();
	}

	public IntArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public IntArrayStore(CompactionStrategy strategy, Store elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	public IntArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFFF_FFFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (int) value;
	}

	@Override
	protected int[] allocateArray(int length) {
		return new int[length];
	}

	@Override
	public boolean inRange(long value) {
		return value >= 0 && value < 1 << 32;
	}

	@Override
	public int getWidth() {
		return Integer.SIZE;
	}

	@Override
	protected ArrayStore<int[]> newInstance() {
		return new IntArrayStore();
	}

}
