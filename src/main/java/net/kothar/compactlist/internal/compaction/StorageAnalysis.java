package net.kothar.compactlist.internal.compaction;

import java.util.List;

public class StorageAnalysis {
	public long	first, last;
	public long	min	= Long.MAX_VALUE, max = Long.MIN_VALUE;
	public int	size;

	public StorageAnalysis(List<Long> storage) {
		size = storage.size();
		first = storage.get(0);
		last = storage.get(size - 1);

		for (int i = 0; i < size; i++) {
			long v = storage.get(i);
			if (v < min) {
				min = v;
			}
			if (v > max) {
				max = v;
			}
		}
	}
}
