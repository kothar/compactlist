package net.kothar.compactlist.internal;

public class NoopNodeManager implements NodeManager {

	private static final long serialVersionUID = 1L;

	@Override
	public void mark(Node node) {
		// no-op
	}

	@Override
	public void unmark(Node node) {
		// no-op
	}

	@Override
	public void maintain() {
		// no-op
	}

	@Override
	public void compactAll() {
		// no-op
	}

	@Override
	public void reset() {
		// no-op
	}

}
