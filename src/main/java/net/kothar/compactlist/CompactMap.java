package net.kothar.compactlist;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.kothar.compactlist.internal.Node;

public class CompactMap extends AbstractMap<Long, Long> {

	Node keys, values;

	public CompactMap() {
		keys = new Node();
		values = new Node();
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public boolean containsValue(Object value) {
		if (!(value instanceof Number)) {
			return false;
		}
		long match = ((Number) value).longValue();

		// TODO implement a primitive long valued iterator
		for (Iterator<Long> i = values.iterator(); i.hasNext();) {
			if (i.next().equals(match)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof Number)) {
			return false;
		}
		int index = keys.searchLong(((Number) key).longValue());
		return index >= 0;
	}

	@Override
	public Long get(Object key) {
		if (!(key instanceof Number)) {
			return null;
		}
		int index = keys.searchLong(((Number) key).longValue());
		if (index < 0) {
			return null;
		}
		return values.getLong(index);
	}

	@Override
	public Long put(Long key, Long value) {
		int index = keys.searchLong(((Number) key).longValue());
		if (index < 0) {
			keys.addLong(-(1 + index), key);
			values.addLong(-(1 + index), value);
			return null;
		}
		return values.setLong(index, value);
	}

	@Override
	public Long remove(Object key) {
		int index = keys.searchLong(((Number) key).longValue());
		if (index < 0) {
			return null;
		}

		keys.removeLong(index);
		return values.removeLong(index);
	}

	@Override
	public void clear() {
		keys = new Node();
		values = new Node();
	}

	@Override
	public Set<Map.Entry<Long, Long>> entrySet() {
		return new AbstractSet<Map.Entry<Long, Long>>() {

			@Override
			public Iterator<Map.Entry<Long, Long>> iterator() {
				return new Iterator<Map.Entry<Long, Long>>() {

					Iterator<Long>	keyIterator		= keys.iterator();
					Iterator<Long>	valueIterator	= values.iterator();

					@Override
					public boolean hasNext() {
						return keyIterator.hasNext();
					}

					@Override
					public Map.Entry<Long, Long> next() {
						return new Entry<Long, Long>() {

							long	key		= keyIterator.next();
							long	value	= valueIterator.next();

							@Override
							public Long getKey() {
								return key;
							}

							@Override
							public Long getValue() {
								return value;
							}

							@Override
							public Long setValue(Long value) {
								throw new UnsupportedOperationException();
							}
						};
					}
				};
			}

			@Override
			public int size() {
				return keys.size();
			}

			@Override
			public void clear() {
				CompactMap.this.clear();
			}

		};
	}

}
