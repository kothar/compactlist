package net.kothar.compactlist;

import java.util.AbstractList;

import net.kothar.compactlist.internal.LongArrayNode;
import net.kothar.compactlist.internal.Node;
import net.kothar.compactlist.internal.NodeContainer;
import net.kothar.compactlist.internal.NodeManager;

public class LongList extends AbstractList<Long> implements NodeContainer {

	NodeManager manager = new NodeManager();
	Node root = new LongArrayNode(this, manager);

	@Override
	public void add(int index, Long element) {
		root.addLong(index, element);
	}

	@Override
	public Long get(int index) {
		return root.getLong(index);
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public void swap(Node dirtyNode, Node cleanNode) {
		if (root == dirtyNode) {
			root = cleanNode;
		}
	}

}
