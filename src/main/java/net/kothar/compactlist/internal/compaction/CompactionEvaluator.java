package net.kothar.compactlist.internal.compaction;

public class CompactionEvaluator {

	private CompactionStrategy strategy;

	private long compactMin = Long.MAX_VALUE, compactMax = Long.MIN_VALUE;

	public CompactionEvaluator(CompactionStrategy strategy) {
		this.strategy = strategy;
	}

	public long range() {
		return compactMax - compactMin;
	}

	public boolean evaluate(int index, long value) {

		long compactValue = strategy.getCompactValue(index, value);
		if (compactValue < compactMin) {
			compactMin = compactValue;
		}
		if (compactValue > compactMax) {
			compactMax = compactValue;
		}
		return range() < (1L << 32);
	}

	public CompactionStrategy getStrategy() {
		return strategy;
	}

	/**
	 * Use information about the evaluated data to normalise the strategy if
	 * possible
	 */
	public void normalize() {
		strategy.adjustOffset(compactMin);
	}
}
