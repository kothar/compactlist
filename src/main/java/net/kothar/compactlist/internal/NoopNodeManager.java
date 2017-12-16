package net.kothar.compactlist.internal;

public class NoopNodeManager implements NodeManager {

	@Override
	public void mark(Node<?> node) {
		// no-op
	}

	@Override
	public void unmark(Node<?> node) {
		// no-op
	}

}
