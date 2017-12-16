package net.kothar.compactlist.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LongArrayNodeTest {

	NodeContainer parent = new NodeContainer() {
		@Override
		public void swap(Node dirtyNode, Node cleanNode) {
		}
	};

	NodeManager manager;

	@Before
	public void setup() {
		manager = new NodeManager();
	}

	@Test
	public void testAdd() {
		LongArrayNode node = new LongArrayNode(parent, manager);

		node.addLong(0, 6);
		node.addLong(0, 5);

		assertEquals(2, node.size());
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void testRemove() {
		LongArrayNode node = new LongArrayNode(parent, manager);

		node.addLong(0, 4);
		node.addLong(1, 5);
		node.addLong(2, 6);
		// Should be 4, 5, 6

		node.remove(1);
		// Should be 4, 6

		assertEquals(2, node.size);
		assertEquals(4, node.getLong(0));
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void testSplit() {
		LongArrayNode node = new LongArrayNode(parent, manager);

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
	public void testBalance() {
		LongArrayNode node = new LongArrayNode(parent, manager);

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
	public void testMerge() {
		LongArrayNode node = new LongArrayNode(parent, manager);

		node.addLong(0, 6);
		node.addLong(0, 5);

		node.split(1);

		node = node.merge();

		assertEquals(2, node.size);
		assertEquals(0, node.height);
		assertEquals(6, node.getLong(1));
	}

	@Test
	public void testCompact() {
		Node node = new LongArrayNode(parent, manager);

		node.addLong(0, 6);
		node.addLong(0, 5);

		Node byteNode = node.compact();
		assertEquals(2, byteNode.size());
		assertEquals(6, byteNode.getLong(1));
		assertTrue(byteNode instanceof ByteArrayNode);

		node.addLong(0, -512);
		Node intNode = node.compact();
		assertEquals(3, intNode.size());
		assertEquals(-512, intNode.getLong(0));
		assertTrue(intNode instanceof IntArrayNode);
	}

}
