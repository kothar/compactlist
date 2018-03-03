package net.kothar.compactlist.internal.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

public class ArrayStoreTest {

	private static final List<Class<? extends ArrayStore<?>>> strategies = Arrays.asList(
		ByteArrayStore.class,
		ShortArrayStore.class,
		IntArrayStore.class,
		LongArrayStore.class);

	@Test
	public void stored_values_can_be_recovered() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();

			store.addAll(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L));

			assertEquals("Store size", store.size(), 10);
			assertEquals("Capacity remains unchanged", initialCapacity, store.capacity());

			for (int i = 0; i < 10; i++) {
				assertEquals("Value retrieved", i, store.getLong(i));
			}
		});
	}

	@Test
	public void stored_values_can_be_iterated() {
		testEach(store -> {
			store.allocate(10);

			store.addAll(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L));

			Iterator<Long> iterator = store.iterator();
			for (int i = 0; i < 10; i++) {
				assertEquals("Value retrieved", (long) i, (long) iterator.next());
			}
			assertFalse("Iterator has more entries", iterator.hasNext());
		});
	}

	@Test
	public void expand_increases_size_without_reallocating() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();

			store.addAll(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
			store.expand(5);

			assertEquals("Store size", store.size(), 10);
			assertEquals("Capacity remains unchanged", initialCapacity, store.capacity());

			for (int i = 0; i < 5; i++) {
				assertEquals("Value before expansion index", i, store.getLong(i));
			}
			for (int i = 5; i < 9; i++) {
				assertEquals("Value after expansion index", i, store.getLong(i + 1));
			}
		});
	}

	@Test
	public void array_is_reallocated_when_capacity_is_reached() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}
			assertEquals("Capacity matches size", store.size(), store.capacity());

			store.add((long) store.size());
			assertTrue("Capacity has been increased", store.capacity() > initialCapacity);
		});
	}

	@Test
	public void insertion_of_prefix_exapnds_array() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}
			store.addLong(0, 200);
			assertTrue("Capacity has been increased", store.capacity() > initialCapacity);
			assertEquals("Offset reinitialised", 0, store.offset);
		});
	}

	@Test
	public void removal_of_prefix_moves_offset() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();
			int initialOffset = store.offset;

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}
			store.remove(0);
			assertNotEquals("Offset moved", initialOffset, store.offset);
		});
	}

	@Test
	public void insertion_at_index_0_uses_prefix_if_available() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();
			int initialOffset = store.offset;

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}
			store.remove(0);
			store.addLong(0, 200);

			assertEquals("Capacity matches size", store.size(), store.capacity());
			assertEquals("Inserted value", 200, store.getLong(0));
			assertEquals("Offset restored to original", initialOffset, store.offset);
		});
	}

	@Test
	public void append_uses_prefix_if_available_and_no_capacity() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();
			int initialOffset = store.offset;

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}
			store.remove(0);
			store.add(200L);

			assertEquals("Capacity matches size", store.size(), store.capacity());
			assertEquals("Inserted value", 200, store.getLong(store.size() - 1));
			assertEquals("Offset restored to original", initialOffset, store.offset);
		});
	}

	@Test
	public void insertion_at_index_0_extends_if_prefix_not_available() {
		testEach(store -> {
			store.allocate(10);
			int initialCapacity = store.capacity();

			for (long i = 0; i < 1000 && store.size() < initialCapacity; i++) {
				store.add(i);
			}

			store.addLong(0, 200);

			assertNotEquals("Capacity increased", initialCapacity, store.capacity());
			assertEquals("Inserted value", 200, store.getLong(0));
			assertEquals("Offset reinitialised", 0, store.offset);
		});
	}

	private void testEach(Consumer<ArrayStore<?>> test) {
		try {
			for (Class<? extends ArrayStore<?>> c : strategies) {
				ArrayStore<?> store = c.newInstance();
				test.accept(store);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
