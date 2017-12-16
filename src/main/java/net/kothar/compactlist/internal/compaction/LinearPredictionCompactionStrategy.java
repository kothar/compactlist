package net.kothar.compactlist.internal.compaction;

public class LinearPredictionCompactionStrategy implements CompactionStrategy {

	private long offset;
	private double step;

	public LinearPredictionCompactionStrategy(StorageAnalysis analysis) {
		offset = analysis.first;
		int increments = analysis.size - 1;
		if (increments < 1) {
			increments = 1;
		}

		if (analysis.first < analysis.last) {
			step = (analysis.last - analysis.first) / (double) increments;
		} else {
			step = -(analysis.first - analysis.last) / (double) increments;
		}
	}

	@Override
	public long getCompactValue(int index, long value) {
		return value - offset - Math.round(step * index);
	}

	@Override
	public long getRealValue(int index, long compactValue) {
		return compactValue + offset + Math.round(step * index);
	}

	@Override
	public void adjustOffset(long offsetAdjustment) {
		this.offset += offsetAdjustment;
	}

}
