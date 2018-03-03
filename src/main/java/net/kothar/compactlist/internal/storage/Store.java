package net.kothar.compactlist.internal.storage;

import java.io.Serializable;
import java.util.List;

import net.kothar.compactlist.LongList;

/**
 * Encapsulates a strategy for storing a list of long values in a leaf node
 */
public interface Store extends LongList, List<Long>, Serializable {

	void setSize(int size);

	void allocate(int size);

	void release();

	boolean inRange(long value);

	int capacity();

	int prependCapacity();

	int appendCapacity();

	int getWidth();

	Store[] split(int index);
}
