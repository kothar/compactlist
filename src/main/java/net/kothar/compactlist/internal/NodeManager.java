package net.kothar.compactlist.internal;

import java.io.Serializable;

public interface NodeManager extends Serializable {

	void mark(Node node);

	void unmark(Node node);

	void maintain();

	void compactAll();

	void reset();

}