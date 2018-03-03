package net.kothar.compactlist;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Iterator;

import net.kothar.compactlist.internal.Node;

public class CompactList extends AbstractList<Long> implements LongList, Serializable {

	private static final long serialVersionUID = -3558458042495888205L;

	private static final long MAINTENANCE_CYCLE = 1 << 18;

	long	operation	= 0;
	Node	root;

	public CompactList() {
		root = new Node();
	}

	private void maintain() {
		if (++operation % MAINTENANCE_CYCLE == 0) {
			root.maintain();
		}
	}

	@Override
	public void add(int index, Long element) {
		addLong(index, element);
	}

	@Override
	public void addLong(int index, long element) {
		if (index > size() || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		root.addLong(index, element);
		maintain();
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
		long result = root.removeLong(index);
		maintain();
		return result;
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
		long result = root.getLong(index);
		maintain();
		return result;
	}

	@Override
	public Long set(int index, Long element) {
		return setLong(index, element);
	}

	@Override
	public long setLong(int index, long element) {
		if (index >= size() || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		long result = root.setLong(index, element);
		maintain();
		return result;
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
	 * Performs a search in the list to locate the index of the given value.
	 * <p>
	 * Assumes that the values are in ascending order.
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
