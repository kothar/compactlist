package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ByteArrayStore extends CompactStore<byte[]> {

	private ByteArrayStore() {
		super();
	}

	public ByteArrayStore(CompactionStrategy strategy) {
		super(strategy);
	}

	public ByteArrayStore(CompactionStrategy strategy, Store elements) {
		super(strategy, elements.size(), elements.size());
		copy(elements, 0);
	}

	public ByteArrayStore(Store elements, int offset, int size) {
		super(elements, offset, size);
	}

	@Override
	public long getArrayElement(int index) {
		return store[index] & 0xFFL;
	}

	@Override
	protected void setArrayElement(int index, long value) {
		store[index] = (byte) value;
	}

	@Override
	protected byte[] allocateArray(int length) {
		return new byte[length];
	}

	@Override
	protected boolean inRange(long compactValue) {
		return compactValue >= 0 && compactValue < 1 << 8;
	}

	@Override
	public int getWidth() {
		return Byte.SIZE;
	}

	@Override
	protected ArrayStore<byte[]> newInstance() {
		return new ByteArrayStore();
	}

}
