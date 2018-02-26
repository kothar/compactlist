package net.kothar.compactlist.internal.compaction;

public class LinearPredictionCompactionStrategy implements CompactionStrategy {

	private long	valueOffset;
	private long	indexOffset;
	private double	step;

	private LinearPredictionCompactionStrategy() {
	}

	public LinearPredictionCompactionStrategy(StorageAnalysis analysis) {
		valueOffset = analysis.first;
		indexOffset = 0;
		int increments = analysis.size - 1;
		if (increments < 1) {
			increments = 1;
		}

		step = (analysis.last - analysis.first) / (double) increments;
	}

	@Override
	public long getCompactValue(int index, long value) {
		return value - valueOffset - Math.round(step * (index + indexOffset));
	}

	@Override
	public long getRealValue(int index, long compactValue) {
		return compactValue + valueOffset + Math.round(step * (index + indexOffset));
	}

	@Override
	public void adjustOffset(long offsetAdjustment) {
		this.valueOffset += offsetAdjustment;
	}

	@Override
	public CompactionStrategy[] split(int index) {
		LinearPredictionCompactionStrategy that = new LinearPredictionCompactionStrategy();

		that.step = step;
		that.valueOffset = valueOffset;
		that.indexOffset = indexOffset + index;

		return new CompactionStrategy[] { this, that };
	}

}
