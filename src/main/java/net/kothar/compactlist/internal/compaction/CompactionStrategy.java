package net.kothar.compactlist.internal.compaction;

public abstract class CompactionStrategy {

	public abstract long getCompactValue(int index, long value);

	public abstract long getRealValue(int index, long compactValue);

	public abstract void adjustOffset(long minValue);
}
