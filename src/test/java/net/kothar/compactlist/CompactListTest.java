package net.kothar.compactlist;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CompactListTest {

	private static final int BENCHMARK_COUNT = 6_000_000;

	@Before
	public void setup() {
	}

	@Test
	public void testAdd() {
		CompactList list = new CompactList();

		list.add(1L);
		list.add(2L);
		list.add(3L);
		list.add(4L);

		list.add(2, 5L);

		System.out.println(list);
		assertTrue(Arrays.asList(1L, 2L, 5L, 3L, 4L).equals(list));
	}

	@Test
	public void benchmarkArrayList() {
		ArrayList<Long> list = new ArrayList<>();
		System.gc();
		long start = System.currentTimeMillis();

		for (int i = 0; i < BENCHMARK_COUNT; i++) {
			list.add((long) i);
		}

		long elapsed = System.currentTimeMillis() - start;

		System.gc();
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Inserted " + BENCHMARK_COUNT + " longs into ArrayList in " + elapsed + " ms. Using "
				+ (usedMem >> 20) + " mb");

		start = System.currentTimeMillis();
		long total = 0;
		for (int i = 0; i < BENCHMARK_COUNT; i++) {
			total += list.get(i);
		}
		elapsed = System.currentTimeMillis() - start;
		System.out.println("Summed " + BENCHMARK_COUNT + " longs to " + total + " in " + elapsed + " ms.");

		start = System.currentTimeMillis();
		total = 0;
		for (Long v : list) {
			total += v;
		}
		elapsed = System.currentTimeMillis() - start;
		System.out
				.println("Summed " + BENCHMARK_COUNT + " longs with iterator to " + total + " in " + elapsed + " ms.");
	}

	@Test
	public void benchmarkCompactList() {
		CompactList list = new CompactList();
		System.gc();
		long start = System.currentTimeMillis();

		for (int i = 0; i < BENCHMARK_COUNT; i++) {
			list.add(list.size(), (long) i);
		}

		long elapsed = System.currentTimeMillis() - start;

		System.gc();
		long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Inserted " + BENCHMARK_COUNT + " longs into CompactList in " + elapsed + " ms. Using "
				+ (usedMem >> 20) + " mb");

		start = System.currentTimeMillis();
		long total = 0;
		for (int i = 0; i < BENCHMARK_COUNT; i++) {
			total += list.get(i);
		}
		elapsed = System.currentTimeMillis() - start;
		System.out.println("Summed " + BENCHMARK_COUNT + " longs to " + total + " in " + elapsed + " ms.");

		System.gc();
		usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Using " + (usedMem >> 20) + " mb after summation");

		// Compact
		start = System.currentTimeMillis();
		list.compact();
		elapsed = System.currentTimeMillis() - start;
		System.out.println("Compacted " + BENCHMARK_COUNT + " longs in " + elapsed + " ms.");

		System.gc();
		usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Using " + (usedMem >> 20) + " mb after compaction");

		start = System.currentTimeMillis();
		total = 0;
		for (int i = 0; i < BENCHMARK_COUNT; i++) {
			total += list.get(i);
		}
		elapsed = System.currentTimeMillis() - start;
		System.out.println("Summed " + BENCHMARK_COUNT + " compacted longs to " + total + " in " + elapsed + " ms.");
		start = System.currentTimeMillis();

		total = 0;
		for (Long v : list) {
			total += v;
		}
		elapsed = System.currentTimeMillis() - start;
		System.out.println(
				"Summed " + BENCHMARK_COUNT + " longs with iterator to " + total + " in " + elapsed + " ms.");
	}

	@After
	public void cleanup() {
	}
}
