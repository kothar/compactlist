package net.kothar.compactlist.internal.compaction;

import net.kothar.compactlist.LongList;

public class StorageAnalysis {
	public long	first, last;
	public long	min	= Long.MAX_VALUE, max = Long.MIN_VALUE;
	public int	size;

	public StorageAnalysis(LongList storage, int index, boolean set, long newValue) {
		size = storage.size();
		first = index == 0 ? newValue : storage.getLong(0);

		if (set) {
			last = index == size - 1 ? newValue : storage.getLong(size - 1);
			for (int i = 0; i < size; i++) {
				long v = index == i ? newValue : storage.getLong(i);
				if (v < min) {
					min = v;
				}
				if (v > max) {
					max = v;
				}
			}
		} else {
			last = index == size ? newValue : storage.getLong(size - 1);
			for (int i = 0; i < size; i++) {
				long v = storage.getLong(i);
				if (v < min) {
					min = v;
				}
				if (v > max) {
					max = v;
				}
			}
			if (newValue < min) {
				min = newValue;
			}
			if (newValue > max) {
				max = newValue;
			}
			size++;
		}
	}

	public StorageAnalysis(LongList storage) {
		size = storage.size();
		first = storage.getLong(0);
		last = storage.getLong(size - 1);

		for (int i = 0; i < size; i++) {
			long v = storage.getLong(i);
			if (v < min) {
				min = v;
			}
			if (v > max) {
				max = v;
			}
		}
	}
}
