package net.kothar.compactlist.internal.compaction;

import java.io.Serializable;

import net.kothar.compactlist.internal.storage.CompactStore;

public interface CompactionStrategy extends Serializable {

	long getCompactValue(int index, long value);

	long getRealValue(int index, long compactValue);

	void adjustOffset(long minValue);

	CompactionStrategy[] split(int index);

	boolean inRange(int index, long realValue, CompactStore<?> store);
}
