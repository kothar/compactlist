package net.kothar.compactlist.internal.storage;

import java.io.Serializable;

/**
 * Encapsulates a strategy for storing a list of long values in a leaf node
 */
public interface StorageStrategy extends Iterable<Long>, Serializable {
	int size();

	void setSize(int size);

	void allocate(int size);

	long get(int index);

	void add(int index, long value);

	long set(int index, long value);

	long remove(int index);

	boolean inRange(int index, long value, boolean positionIndependent);

	boolean isPositionIndependent();

	int capacity();

	int getWidth();
}
