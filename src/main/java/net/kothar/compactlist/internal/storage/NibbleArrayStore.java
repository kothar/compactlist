package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class NibbleArrayStore extends CompactArrayStore<byte[]> {

	public NibbleArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public NibbleArrayStore(CompactionStrategy strategy, StorageStrategy elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	@Override
	public long get(int index) {
		byte value = store[index / 2];
		long nibble;
		if (index % 2 == 0) {
			nibble = value & 0xFL;
		} else {
			nibble = (value & 0xFFL) >> 4;
		}
		return strategy.getRealValue(index, nibble);
	}

	@Override
	protected void setElement(int index, long value) {
		long nibble = strategy.getCompactValue(index, value) & 0xFL;
		byte existingValue = store[index / 2];
		if (index % 2 == 0) {
			existingValue &= 0XF0L;
		} else {
			existingValue &= 0XFL;
			nibble <<= 4;
		}
		store[index / 2] = (byte) (nibble | existingValue);
	}

	@Override
	protected byte[] allocateArray(int length) {
		return new byte[length / 2 + length % 2];
	}

	@Override
	protected int length(byte[] array) {
		return array.length * 2;
	}

	@Override
	protected boolean inRange(long compactValue) {
		return compactValue >= 0 && compactValue < 1 << 4;
	}

	@Override
	public int getWidth() {
		return 4;
	}
}
