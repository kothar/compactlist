package net.kothar.compactlist;

public interface LongList {

	int size();

	long removeLong(int index);

	long getLong(int index);

	long setLong(int index, long element);

	void addLong(int index, long element);

	default void addLong(long element) {
		addLong(size(), element);
	}
}
