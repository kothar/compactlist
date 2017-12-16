package net.kothar.compactlist;

import java.io.Serializable;
import java.util.ArrayList;

public class ArrayListWrapper implements LongList, Serializable {

	private static final long serialVersionUID = 1L;

	ArrayList<Long> list = new ArrayList<>();

	@Override
	public long removeLong(int index) {
		return list.remove(index);
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
		list.add(index, element);
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
