package net.kothar.compactlist;

import java.util.AbstractSet;
import java.util.Iterator;

import net.kothar.compactlist.internal.Node;

public class CompactSet extends AbstractSet<Long> {

	Node root;

	public CompactSet() {
		root = new Node();
	}

	@Override
	public Iterator<Long> iterator() {
		return root.iterator();
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Number)) {
			return false;
		}

		int index = root.searchLong(((Number) o).longValue());
		return index >= 0;
	}

	@Override
	public boolean add(Long e) {
		int index = root.searchLong(e);
		if (index >= 0) {
			return false;
		}
		root.addLong(-(1 + index), e);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Number)) {
			return false;
		}

		int index = root.searchLong(((Number) o).longValue());
		if (index < 0) {
			return false;
		}
		root.removeLong(index);
		return true;
	}

	@Override
	public void clear() {
		root = new Node();
	}

}
