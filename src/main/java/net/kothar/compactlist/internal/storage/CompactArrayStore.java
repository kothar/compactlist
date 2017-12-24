package net.kothar.compactlist.internal.storage;

import net.kothar.compactlist.internal.compaction.CompactionStrategy;
import net.kothar.compactlist.internal.compaction.OffsetCompactionStrategy;
import net.kothar.compactlist.internal.compaction.PositionIndependentCompactionStrategy;

public abstract class CompactArrayStore<T> extends ArrayStore<T> {

	protected CompactionStrategy strategy;

	protected abstract boolean inRange(long value);

	public CompactArrayStore(CompactionStrategy strategy) {
		super();
		this.strategy = strategy;
	}

	public CompactArrayStore(CompactionStrategy strategy, int size, int capactity) {
		super(size, capactity);
		this.strategy = strategy;
	}

	public CompactArrayStore(StorageStrategy elements, int offset, int size) {
		super(size, size);
		if (elements instanceof CompactArrayStore) {
			strategy = ((CompactArrayStore<?>) elements).strategy;
		} else {
			strategy = new OffsetCompactionStrategy(0);
		}
		copy(elements, 0, offset, size);
	}

	protected CompactArrayStore() {
	}

	@Override
	public boolean isPositionIndependent() {
		return strategy instanceof PositionIndependentCompactionStrategy;
	};

	@Override
	public boolean inRange(int index, long value, boolean positionIndependent) {
		if (index < size && !(positionIndependent || isPositionIndependent())) {
			return false;
		}

		long compactValue = strategy.getCompactValue(index, value);
		return inRange(compactValue);
	}

}