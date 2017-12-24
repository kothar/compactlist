package net.kothar.compactlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class CompactSetTest {

	@Test
	public void add_elements() {
		CompactSet set = new CompactSet();
		set.add(-25L);
		set.add(0L);
		set.add(4L);
		set.add(5L);
		set.add(6L);
		set.add(4L);

		assertTrue(set.containsAll(Arrays.asList(-25L, 0, 4, 5, 6)));
		assertFalse(set.contains(8L));
	}

	@Test
	public void count_elements() {
		CompactSet set = new CompactSet();
		set.add(4L);
		set.add(5L);
		set.add(6L);
		set.add(4L);

		assertEquals(3, set.size());
	}

	@Test
	public void remove_elements() {
		CompactSet set = new CompactSet();
		set.add(4L);
		set.add(5L);
		set.add(6L);
		set.add(4L);

		set.remove(5L);

		assertTrue(set.containsAll(Arrays.asList(4L, 6)));
		assertFalse(set.contains(5L));
	}

	@Test
	public void clear_set() {
		CompactSet set = new CompactSet();
		set.add(4L);
		set.add(5L);
		set.add(6L);
		set.add(4L);

		set.clear();

		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	public void iterate_elements() {
		CompactSet set = new CompactSet();
		set.add(4L);
		set.add(5L);
		set.add(6L);
		set.add(4L);

		Iterator<Long> i = set.iterator();

		assertTrue(i.hasNext());
		assertEquals((Long) 4L, i.next());
		assertEquals((Long) 5L, i.next());
		assertEquals((Long) 6L, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	public void large_set() {
		Set<Long> javaSet = new TreeSet<>();
		CompactSet compactSet = new CompactSet();

		System.out.println("Building java tree set");
		long start = System.currentTimeMillis();
		for (long i = 0; i < 1 << 14; i++) {
			javaSet.add(i);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms\n");

		System.out.println("Building compact set");
		start = System.currentTimeMillis();
		for (long i = 0; i < 1 << 14; i++) {
			compactSet.add(i);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms\n");
	}

}
