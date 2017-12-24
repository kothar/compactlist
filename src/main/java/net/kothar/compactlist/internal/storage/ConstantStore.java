package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ConstantStore extends AbstractStore {

	private long				compactValue;
	private CompactionStrategy	strategy;

	public ConstantStore(CompactionStrategy strategy, long compactValue, int size) {
		super();
		this.strategy = strategy;
		this.compactValue = compactValue;
		this.size = size;
	}

	public ConstantStore(CompactionStrategy strategy, StorageStrategy elements) {
		super();
		this.strategy = strategy;
		this.compactValue = strategy.getCompactValue(0, elements.get(0));
		this.size = elements.size();
	}

	@Override
	public void allocate(int size) {
		this.size = size;
	}

	@Override
	public long getLong(int index) {
		return strategy.getRealValue(index, compactValue);
	}

	@Override
	public void addLong(int index, long value) {
		assert strategy.getCompactValue(index, value) == this.compactValue;
		size++;
	}

	@Override
	public long setLong(int index, long value) {
		assert strategy.getCompactValue(index, value) == this.compactValue;
		return value;
	}

	@Override
	public long removeLong(int index) {
		size--;
		return compactValue;
	}

	@Override
	public boolean inRange(int index, long value, boolean positionIndependent) {
		return strategy.getCompactValue(index, value) == this.compactValue;
	}

	@Override
	public boolean isPositionIndependent() {
		return true;
	}

	@Override
	public int capacity() {
		return size;
	}

	@Override
	public int getWidth() {
		return 0;
	}

}
