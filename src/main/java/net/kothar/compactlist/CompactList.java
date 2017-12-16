package net.kothar.compactlist;

import java.util.AbstractList;
import java.util.Iterator;

import net.kothar.compactlist.internal.Node;
import net.kothar.compactlist.internal.NodeManager;
import net.kothar.compactlist.internal.QueueingNodeManager;

public class CompactList extends AbstractList<Long> {

	NodeManager manager;
	Node root;

	public CompactList() {
		this(new QueueingNodeManager());
	}

	public CompactList(NodeManager manager) {
		this.manager = manager;
		root = new Node(null, manager);
	}

	@Override
	public void add(int index, Long element) {
		root.addLong(index, element);
	}

	@Override
	public Long remove(int index) {
		return root.remove(index);
	}

	@Override
	public Long get(int index) {
		return root.getLong(index);
	}

	@Override
	public Long set(int index, Long element) {
		return root.setLong(index, element);
	}

	@Override
	public int size() {
		return root.size();
	}

	/**
	 * Tries to find more efficient in-memory representations for each list segment
	 */
	public void compact() {
		manager.reset();
		root.compact();
	}

	/**
	 * The iterator used here maintains its position in the tree, making it slightly
	 * more efficient than repeatedly calling get over the range of indices.
	 */
	@Override
	public Iterator<Long> iterator() {
		return root.iterator();
	}
}
