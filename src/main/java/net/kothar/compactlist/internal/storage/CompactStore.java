package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;
import net.kothar.compactlist.internal.compaction.OffsetCompactionStrategy;

public abstract class CompactStore<T> extends ArrayStore<T> {

	private CompactionStrategy strategy;

	public abstract boolean inRange(long value);

	public CompactStore(CompactionStrategy strategy) {
		super();
		this.strategy = strategy;
	}

	public CompactStore(CompactionStrategy strategy, int size, int capactity) {
		super(size, capactity);
		this.strategy = strategy;
	}

	public CompactStore(Store elements, int offset, int size) {
		super(size, size);
		if (elements instanceof CompactStore) {
			strategy = ((CompactStore<?>) elements).strategy;
		} else {
			strategy = new OffsetCompactionStrategy(0);
		}
		copy(elements, 0, offset, size);
	}

	protected CompactStore() {
	}

	@Override
	public boolean inRange(int index, long value) {
		return strategy.inRange(index, value, this);
	}

	@Override
	protected final long getElement(int index) {
		return strategy.getRealValue(index, getArrayElement(index + offset));
	}

	@Override
	protected final void setElement(int index, long value) {
		setArrayElement(index + offset, strategy.getCompactValue(index, value));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Store[] split(int index) {
		CompactionStrategy[] splitStrategies = strategy.split(index);
		Store[] branches = super.split(index);

		((CompactStore<T>) branches[0]).strategy = splitStrategies[0];
		((CompactStore<T>) branches[1]).strategy = splitStrategies[1];
		return branches;
	}

}