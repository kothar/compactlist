package net.kothar.compactlist;

import java.io.Serializable;

import gnu.trove.list.array.TLongArrayList;

public class TroveListWrapper implements LongList, Serializable {

	private static final long serialVersionUID = 1L;

	TLongArrayList list = new TLongArrayList();

	@Override
	public long removeLong(int index) {
		return list.remove(index) ? 0 : 1;
	}

	@Override
	public long getLong(int index) {
		return list.get(index);
	}

	@Override
	public long setLong(int index, long element) {
		return list.set(index, element);
	}

	@Override
	public void addLong(int index, long element) {
		list.insert(index, element);
	}

	@Override
	public void addLong(long element) {
		list.add(element);
	}

	@Override
	public int size() {
		return list.size();
	}

}
