package net.kothar.compactlist.internal.storage;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates a strategy for storing a list of long values in a leaf node
 */
public interface StorageStrategy extends List<Long>, Serializable {

	void setSize(int size);

	void allocate(int size);

	long getLong(int index);

	void addLong(int index, long value);

	long setLong(int index, long value);

	long removeLong(int index);

	boolean inRange(int index, long value, boolean positionIndependent);

	boolean isPositionIndependent();

	int capacity();

	int prependCapacity();

	int appendCapacity();

	int getWidth();
}
