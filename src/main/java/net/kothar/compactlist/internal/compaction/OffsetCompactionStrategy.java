package net.kothar.compactlist.internal.compaction;

public class OffsetCompactionStrategy implements CompactionStrategy, PositionIndependentCompactionStrategy {

	private long offset;

	public OffsetCompactionStrategy(StorageAnalysis analysis) {
		this.offset = analysis.min + (analysis.max - analysis.min) / 2;
	}

	@Override
	public long getCompactValue(int index, long value) {
		return value - offset;
	}

	@Override
	public long getRealValue(int index, long compactValue) {
		return compactValue + offset;
	}

	@Override
	public void adjustOffset(long minValue) {
		offset += minValue;
	}

}
