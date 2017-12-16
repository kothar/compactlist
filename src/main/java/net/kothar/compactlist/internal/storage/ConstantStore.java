package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;

public class ConstantStore extends AbstractStore {

	private long compactValue;
	private CompactionStrategy strategy;

	public ConstantStore(CompactionStrategy strategy, StorageStrategy elements) {
		super();
		this.strategy = strategy;
		this.compactValue = strategy.getCompactValue(0, elements.get(0));
	}

	@Override
	public void allocate(int size) {
		// nothing to do
	}

	@Override
	public long get(int index) {
		return strategy.getRealValue(index, compactValue);
	}

	@Override
	public void add(int index, long value) {
		assert strategy.getCompactValue(index, value) == this.compactValue;
		size++;
	}

	@Override
	public long set(int index, long value) {
		assert strategy.getCompactValue(index, value) == this.compactValue;
		return value;
	}

	@Override
	public long remove(int index) {
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

}
