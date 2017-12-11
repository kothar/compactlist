package net.kothar.compactlist;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import net.kothar.compactlist.LongList;

public class LongListTest {

	@Test
	public void testAdd() {
		LongList list = new LongList();

		list.add(1L);
		list.add(2L);
		list.add(3L);
		list.add(4L);

		list.add(2, 5L);

		System.out.println(list);
		assertTrue(Arrays.asList(1L, 2L, 5L, 3L, 4L).equals(list));
	}

}
