package net.kothar.compactlist.internal;

public interface NodeManager {

	void mark(Node<?> node);

	void unmark(Node<?> node);

}