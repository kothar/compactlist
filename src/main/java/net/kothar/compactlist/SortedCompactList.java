package net.kothar.compactlist;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Iterator;

import net.kothar.compactlist.internal.Node;

/**
 * A sorted list of Longs. Values are sorted on insertion; as such, adding or setting a value at a
 * specific index is unsupported.
 * 
 * @author mhouston
 */
public class SortedCompactList extends AbstractList<Long> implements LongList, Serializable {

	private static final long serialVersionUID = -1956627640749725601L;

	Node root;

	public SortedCompactList() {
		root = new Node();
	}

	@Override
	public boolean add(Long e) {
		addLong(e);
		return true;
	}

	@Override
	public void addLong(long e) {
		int index = root.searchLong(e);
		if (index < 0) {
			root.addLong(-(1 + index), e);
		} else {
			root.addLong(index, e);
		}
	}

	@Override
	@Deprecated
	public void add(int index, Long element) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void addLong(int index, long element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long remove(int index) {
		return removeLong(index);
	}

	@Override
	public long removeLong(int index) {
		if (index >= size() || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return root.removeLong(index);
	}

	@Override
	public Long get(int index) {
		return getLong(index);
	}

	@Override
	public long getLong(int index) {
		if (index >= size() || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return root.getLong(index);
	}

	@Override
	@Deprecated
	public Long set(int index, Long element) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public long setLong(int index, long element) {
		throw new UnsupportedOperationException();
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
	 * The iterator used here maintains its position in the tree, making it slightly more efficient
	 * than repeatedly calling get over the range of indices.
	 */
	@Override
	public Iterator<Long> iterator() {
		return root.iterator();
	}

	/**
	 * Locates the index of the given value.
	 * 
	 * @param value
	 *            The value to search for
	 * @return The index of the value. If not found, returns -1 minus the index at which it should
	 *         be inserted.
	 */
	public long search(long value) {
		return root.searchLong(value);
	}
}
