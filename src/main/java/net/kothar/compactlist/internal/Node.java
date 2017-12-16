package net.kothar.compactlist.internal;

public interface Node extends NodeContainer, Iterable<Long> {

	int size();

	long getLong(int index);

	void addLong(int index, long element);

	void remove(int index);

	NodeContainer getParent();

	Node compact();

}
