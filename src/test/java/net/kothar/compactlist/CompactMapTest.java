package net.kothar.compactlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

public class CompactMapTest {

	@Test
	public void add_elements() {
		CompactMap map = new CompactMap();
		map.put(-25L, 1L);
		map.put(0L, 2L);
		map.put(4L, 3L);
		map.put(5L, 4L);
		map.put(6L, 5L);
		map.put(4L, 6L);

		assertTrue(map.containsKey(4L));
		assertTrue(map.keySet().containsAll(Arrays.asList(-25L, 0, 4, 5, 6)));
		assertFalse(map.containsKey(8L));
		assertTrue(map.containsValue(1L));
		assertTrue(map.containsValue(6L));
		assertEquals((Long) 6L, map.get(4L));
	}

	@Test
	public void count_elements() {
		CompactMap map = new CompactMap();
		map.put(4L, 1L);
		map.put(5L, 2L);
		map.put(6L, 3L);
		map.put(4L, 6L);

		assertEquals(3, map.size());
	}

	@Test
	public void remove_elements() {
		CompactMap map = new CompactMap();
		map.put(4L, 1L);
		map.put(5L, 2L);
		map.put(6L, 3L);
		map.put(4L, 6L);

		map.remove(5L);

		assertTrue(map.keySet().containsAll(Arrays.asList(4L, 6)));
		assertFalse(map.containsKey(5L));
	}

	@Test
	public void clear_set() {
		CompactMap map = new CompactMap();
		map.put(4L, 1L);
		map.put(5L, 2L);
		map.put(6L, 3L);

		map.clear();

		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void iterate_elements() {
		CompactMap map = new CompactMap();
		map.put(4L, 1L);
		map.put(5L, 2L);
		map.put(6L, 3L);

		Iterator<Entry<Long, Long>> i = map.entrySet().iterator();

		assertTrue(i.hasNext());
		assertEquals((Long) 4L, i.next().getKey());
		assertEquals((Long) 5L, i.next().getKey());
		assertEquals((Long) 3L, i.next().getValue());
		assertFalse(i.hasNext());
	}

	@Test
	public void large_map() {
		Map<Long, Long> javaMap = new TreeMap<>();
		CompactMap compactMap = new CompactMap();

		System.out.println("Building java tree map");
		long start = System.currentTimeMillis();
		for (long i = 0; i < 1 << 14; i++) {
			javaMap.put(i, i);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms\n");

		System.out.println("Building compact map");
		start = System.currentTimeMillis();
		for (long i = 0; i < 1 << 14; i++) {
			compactMap.put(i, i);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms\n");
	}

}
