package net.kothar.compactlist.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import net.kothar.compactlist.CompactList;
import net.kothar.compactlist.internal.storage.ArrayStore;
import net.kothar.compactlist.internal.storage.LongArrayStore;
import net.kothar.compactlist.internal.storage.ShortArrayStore;

public class NodeTest {

	@Before
	public void setup() {
	}

	@Test
	public void test_add() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);

		assertEquals(2, node.size());
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void test_set() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);
		node.setLong(0, 1);

		assertEquals(2, node.size());
		assertEquals(1, node.getLong(0));
	}

	@Test
	public void test_remove() {
		Node node = new Node();

		node.addLong(0, 4);
		node.addLong(1, 5);
		node.addLong(2, 6);
		// Should be 4, 5, 6

		node.removeLong(1);
		// Should be 4, 6

		assertEquals(2, node.size);
		assertEquals(4, node.getLong(0));
		assertEquals(6, node.getLong(1));

		node.compact();
		assertEquals(2, node.size);
		assertEquals(4, node.getLong(0));
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void test_split() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);

		node.split(1);

		assertEquals(2, node.size);
		assertEquals(1, node.height);
		assertEquals(1, node.left.size);
		assertEquals(6, node.getLong(1));

		node.addLong(1, 4);
		assertEquals(3, node.size);
		assertEquals(4, node.getLong(1));
	}

	@Test
	public void node_tree_correctly_balanced() {
		Node node = new Node();

		node.addLong(0, 0);
		node.addLong(1, 1);
		node.addLong(2, 2);
		node.addLong(3, 3);
		node.addLong(4, 4);
		node.addLong(5, 5);

		node.split(1);
		node.split(2);
		node.split(3);
		node.split(4);

		node.print("", "  ");

		assertEquals(3, node.height);
		assertEquals(6, node.size);
		assertEquals(4, node.getLong(4));
	}

	@Test
	public void test_merge() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);

		node.split(1);

		node.merge();

		assertEquals(2, node.size);
		assertEquals(0, node.height);
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void compacting_a_node_uses_appropriate_store() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);

		node.compact();
		assertEquals(2, node.size());
		assertEquals(6, node.getLong(1));
		assertFalse(node.elements instanceof LongArrayStore);

		node.addLong(0, -512);
		node.addLong(1, Short.MAX_VALUE);
		node.addLong(2, Short.MIN_VALUE);
		node.compact();
		assertEquals(5, node.size());
		assertEquals(-512, node.getLong(0));
		assertEquals(Short.MAX_VALUE, node.getLong(1));
		assertEquals(Short.MIN_VALUE, node.getLong(2));
		assertTrue(node.elements instanceof ShortArrayStore);
	}

	@Test
	public void out_of_range_insert_triggers_decompaction() {
		Node node = new Node();

		node.addLong(0, 6);
		node.addLong(0, 5);
		node.compact();

		assertFalse(node.elements.inRange(0, -512));

		node.addLong(0, -512);
		assertEquals(3, node.size());
		assertEquals(-512, node.getLong(0));
		assertTrue(node.elements.inRange(0, -512));
	}

	@Test
	public void shared_store_returned_to_neighbouring_stores() {
		Node node = new Node();

		// List of 5 elements
		node.addLong(1);
		node.addLong(2);
		node.addLong(3);
		node.addLong(4);
		node.addLong(5);
		assertEquals(5, node.size);
		assertEquals(5, node.elements.size());

		// Split into [1, 2, 3] and [4, 5]
		node.split(3);

		System.out.println(node.left);
		System.out.println(node.right);
		assertEquals(5, node.size);
		assertEquals(3, node.left.size);
		assertEquals(3, node.left.elements.size());
		assertEquals(2, node.right.size);
		assertEquals(2, node.right.elements.size());

		assertNotNull(((ArrayStore<?>) node.left.elements).right);
		assertNotNull(((ArrayStore<?>) node.right.elements).left);

		// Insert element on left branch forcing reallocation
		node.addLong(2, 100);

		System.out.println(node.left);
		System.out.println(node.right);
		assertEquals(6, node.size);
		assertEquals(4, node.left.size);
		assertEquals(4, node.left.elements.size());
		assertEquals(2, node.right.size);
		assertEquals(2, node.right.elements.size());

		assertNull(((ArrayStore<?>) node.left.elements).right);
		assertNull(((ArrayStore<?>) node.right.elements).left);
	}

	@Test
	public void testRandomOperations() {
		Random r = new Random(29239);
		List<Long> list = new ArrayList<>();
		CompactList compactList = new CompactList();

		String lastOp = "";

		for (int i = 0; i < 5_000; i++) {
			long v;
			int index;
			Long a;
			Long b;
			switch (r.nextInt(5)) {
			case 0:
				if (!list.isEmpty()) {
					// Insert
					v = r.nextLong();
					index = r.nextInt(list.size());
					list.add(index, v);
					compactList.add(index, v);
					lastOp = "insert " + v + " at " + index;
					break;
				}
			case 1:
				// Set
				if (!list.isEmpty()) {
					v = r.nextLong();
					index = r.nextInt(list.size());
					a = list.set(index, v);
					b = compactList.set(index, v);
					assertEquals(a, b);
					lastOp = "set " + v + " at " + index;
					break;
				}
			case 2:
				// Append
				v = r.nextLong();
				list.add(v);
				compactList.add(v);
				lastOp = "append " + " at " + (list.size() - 1);
				break;
			case 3:
				// Remove
				if (!list.isEmpty()) {
					index = r.nextInt(list.size());
					a = list.remove(index);
					b = compactList.remove(index);
					assertEquals(a, b);
					lastOp = "remove " + a + " from " + index;
				}
				break;
			case 4:
				// Get
				if (!list.isEmpty()) {
					index = r.nextInt(list.size());
					a = list.get(index);
					b = compactList.get(index);
					assertEquals(a, b);
					lastOp = "get " + a + " at " + index;
					break;
				}
			}

			assertEquals(list.size(), compactList.size());
			for (int j = 0; j < list.size(); j++) {
				long listVal = list.get(j);
				long clistVal = compactList.getLong(j);
				if (listVal != clistVal) {
					System.out.println("Iteration " + i + ": " + lastOp);
					System.out.println(" " + j + ": " + listVal + " != " + clistVal);
				}
				assertEquals("Index " + j, listVal, clistVal);
			}

			assertTrue("Iteration " + i + ": " + lastOp, list.equals(compactList));
		}
	}
}
