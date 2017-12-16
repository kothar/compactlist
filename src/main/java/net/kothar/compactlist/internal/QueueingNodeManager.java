package net.kothar.compactlist.internal;

import java.util.HashMap;

public class QueueingNodeManager implements NodeManager {

	private static final long COMPACT_DELAY = 32;
	private static final long COMPACT_INTERVAL = 4;

	HashMap<Node, DirtyNode> dirtyNodes = new HashMap<>();
	DirtyNode head, tail;
	long opcounter;
	long lastCompact;
	long compactcount;

	class DirtyNode {
		Node node;
		DirtyNode previous, next;
		public long modifiedOp;

		public DirtyNode(Node node) {
			this.node = node;
		}

		public Node remove() {
			if (previous == null) {
				head = next;
			} else {
				previous.next = next;
			}

			if (next == null) {
				tail = previous;
			} else {
				next.previous = previous;
			}
			return node;
		}

		public Node append() {
			previous = tail;
			if (previous != null) {
				previous.next = this;
			} else {
				head = this;
			}
			tail = this;
			return node;
		}
	}

	@Override
	public void mark(Node node) {
		DirtyNode dirtyNode = dirtyNodes.get(node);
		if (dirtyNode == null) {
			dirtyNode = new DirtyNode(node);
		} else {
			dirtyNode.remove();
		}
		dirtyNode.modifiedOp = opcounter;
		dirtyNode.append();

		maintain();
	}

	@Override
	public void unmark(Node node) {
		DirtyNode dirtyNode = dirtyNodes.remove(node);
		if (dirtyNode != null) {
			dirtyNode.remove();
		}
	}

	@Override
	public void compactAll() {
		while (head != null) {
			compact();
		}
	}

	private void compact() {
		Node dirtyNode = head.remove();
		dirtyNodes.remove(dirtyNode);
		dirtyNode.compact();
		lastCompact = opcounter;
	}

	@Override
	public void maintain() {
		opcounter++;

		if (head == null) {
			return;
		}

		// If we somehow reach maximum count opcounter will be negative, but difference
		// should still end up positive
		if (opcounter - lastCompact > COMPACT_INTERVAL &&
				opcounter - head.modifiedOp > COMPACT_DELAY) {
			compact();
		}
	}

	@Override
	public void reset() {
		dirtyNodes.clear();
		head = null;
		tail = null;
		opcounter = 0;
		lastCompact = 0;
		compactcount = 0;
	}
}
