package net.kothar.compactlist.internal.compaction;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.Test;

import net.kothar.compactlist.internal.storage.LongArrayStore;

public class CompactionTest {
	private static final List<Class<? extends CompactionStrategy>> strategies = Arrays.asList(
		OffsetCompactionStrategy.class);

	@Test
	public void constant_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add(10L);
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void low_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add((long) i);
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void high_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add(Long.MAX_VALUE - 100 + i);
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void descending_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add(1000L - i);
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void ranged_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add((long) (i * 83472723.2));
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void random_values() {
		Random r = new Random(10);
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			values.add(r.nextLong());
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	@Test
	public void many_values() {
		ArrayList<Long> values = new ArrayList<>();
		for (int i = 0; i < 100_000; i++) {
			values.add(i * 7l);
		}
		testEach(strategy -> {
			verifyRecovery(values, strategy);
		}, values);
	}

	protected void verifyRecovery(ArrayList<Long> values, CompactionStrategy strategy) {
		// Generate compact values
		ArrayList<Long> compactValues = new ArrayList<>();
		int size = values.size();
		for (int i = 0; i < size; i++) {
			compactValues.add(strategy.getCompactValue(i, values.get(i)));
		}

		// Generate raw values
		for (int i = 0; i < size; i++) {
			long realValue = strategy.getRealValue(i, compactValues.get(i));
			assertEquals(strategy.getClass().getSimpleName() + " at index " + i, (long) values.get(i), realValue);
		}

		int pivot = size / 2;
		CompactionStrategy[] strategies = strategy.split(pivot);

		// Check left branch
		for (int i = 0; i < pivot; i++) {
			long realValue = strategies[0].getRealValue(i, compactValues.get(i));
			assertEquals(strategies[0].getClass().getSimpleName() + " at left index " + i, (long) values.get(i),
				realValue);
		}

		// Check right branch
		for (int i = 0; i < size - pivot; i++) {
			long realValue = strategies[1].getRealValue(i, compactValues.get(i + pivot));
			assertEquals(strategies[1].getClass().getSimpleName() + " at right index " + i,
				(long) values.get(i + pivot), realValue);
		}
	}

	private void testEach(Consumer<CompactionStrategy> test, List<Long> values) {
		LongArrayStore store = new LongArrayStore();
		store.addAll(values);
		StorageAnalysis analysis = new StorageAnalysis(store);
		try {
			for (Class<? extends CompactionStrategy> c : strategies) {
				CompactionStrategy strategy = c.getConstructor(StorageAnalysis.class).newInstance(analysis);
				test.accept(strategy);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
