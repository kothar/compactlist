package net.kothar.compactlist.internal.compaction;

public class OffsetCompactionStrategy implements CompactionStrategy, PositionIndependentCompactionStrategy {

	private static final long serialVersionUID = -4967610547335861332L;

	private long offset;

	public OffsetCompactionStrategy(long offset) {
		this.offset = offset;
	}

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

	@Override
	public CompactionStrategy[] split(int index) {
		return new CompactionStrategy[] { this, this };
	}

}
