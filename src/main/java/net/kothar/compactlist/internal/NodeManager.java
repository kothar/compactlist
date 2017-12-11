package net.kothar.compactlist.internal;

import java.util.HashMap;

public class NodeManager {

	HashMap<Node, DirtyNode> dirtyNodes = new HashMap<>();
	DirtyNode head, tail;

	class DirtyNode {
		Node node;
		DirtyNode previous, next;

		public DirtyNode(Node node) {
			this.node = node;
		}

		public Node remove() {
			if (previous == null) {
				head = next;
			} else {
				previous.next = next;
				previous = null;
			}

			if (next == null) {
				tail = previous;
			} else {
				next.previous = previous;
				next = null;
			}
			return node;
		}

		public Node append() {
			previous = tail;
			previous.next = this;
			tail = this;
			return node;
		}
	}

	public void mark(Node node) {
		DirtyNode dirtyNode = dirtyNodes.get(node);
		if (dirtyNode == null) {
			dirtyNode = new DirtyNode(node);
		} else {
			dirtyNode.remove();
		}
		dirtyNode.append();

	}

	public void unmark(Node node) {
		DirtyNode dirtyNode = dirtyNodes.get(node);
		if (dirtyNode != null) {
			dirtyNode.remove();
		}
	}

	public void compactAll() {
		while (head != null) {
			compact();
		}
	}

	public void compact() {
		if (head == null) {
			return;
		}

		Node dirtyNode = head.remove();
		Node compactNode = dirtyNode.compact();
		if (compactNode != dirtyNode) {
			dirtyNode.getParent().swap(dirtyNode, compactNode);
		}
	}
}
