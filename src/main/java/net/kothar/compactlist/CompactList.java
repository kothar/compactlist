package net.kothar.compactlist;

import java.util.AbstractList;
import java.util.Iterator;

import net.kothar.compactlist.internal.LongArrayNode;
import net.kothar.compactlist.internal.Node;
import net.kothar.compactlist.internal.NodeManager;
import net.kothar.compactlist.internal.NoopNodeManager;

public class CompactList extends AbstractList<Long> {

	NodeManager manager;
	Node<?> root;

	public CompactList() {
		manager = new NoopNodeManager();
		root = new LongArrayNode(null, manager);
	}

	@Override
	public void add(int index, Long element) {
		root = root.addLong(index, element);
	}

	@Override
	public Long remove(int index) {
		long v = root.getLong(index);
		root = root.remove(index);
		return v;
	}

	@Override
	public Long get(int index) {
		return root.getLong(index);
	}

	@Override
	public Long set(int index, Long element) {
		long v = root.getLong(index);
		root = root.setLong(index, element);
		return v;
	}

	@Override
	public int size() {
		return root.size();
	}

	/**
	 * Tries to find more efficient in-memory representations for each list segment
	 */
	public void compact() {
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
