package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ByteArrayStore extends CompactArrayStore<byte[]> {

	public ByteArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public ByteArrayStore(CompactionStrategy strategy, StorageStrategy elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	@Override
	public long get(int index) {
		return strategy.getRealValue(index, store[index] & 0xFFL);
	}

	@Override
	protected void setElement(int index, long value) {
		store[index] = (byte) (strategy.getCompactValue(index, value));
	}

	@Override
	protected byte[] allocateArray(int length) {
		return new byte[length];
	}

	@Override
	protected int length(byte[] array) {
		return array.length;
	}

	@Override
	protected boolean inRange(long compactValue) {
		return compactValue >= 0 && compactValue < 1 << 8;
	}

	@Override
	public int getWidth() {
		return Byte.SIZE;
	}

}
