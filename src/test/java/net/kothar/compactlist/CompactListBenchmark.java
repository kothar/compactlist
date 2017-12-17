package net.kothar.compactlist;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.omg.CORBA.LongHolder;

import gnu.trove.list.array.TLongArrayList;
import net.kothar.compactlist.internal.Node;
import net.kothar.compactlist.internal.storage.StorageStrategy;

public class CompactListBenchmark {

	interface Test {
		void init(Class<? extends LongList> impl, int size);

		void reset();

		void run();

		void clear();

		long memoryUsage();
	}

	public static void main(String[] args) throws IOException {

		System.out.println("Test\t\tCount\t\tClass\t\t\tElapsed");

		Map<String, Test> tests = new LinkedHashMap<>();
		tests.put("appendseq", appendSequential);
		tests.put("insertseq", insertSequential);

		for (Entry<String, Test> testEntry : tests.entrySet()) {

			Map<Class<? extends LongList>, Map<Integer, Double>> testData = new HashMap<>();
			Map<Class<? extends LongList>, Map<Integer, Long>> testMemory = new HashMap<>();

			for (Class<? extends LongList> impl : Arrays.asList(
				ArrayListWrapper.class,
				TroveListWrapper.class,
				CompactList.class)) {

				TreeMap<Integer, Double> classData = new TreeMap<>();
				testData.put(impl, classData);
				TreeMap<Integer, Long> classMemory = new TreeMap<>();
				testMemory.put(impl, classMemory);

				Test test = testEntry.getValue();

				int increment = 10_000;
				for (int size = increment; size < 1 << 26; size += increment, increment *= 1.2) {

					test.init(impl, size);

					double elapsed = time(
						String.format("%s\t%10d\t%s", testEntry.getKey(), size, impl.getSimpleName()),
						test);

					classData.put(size, elapsed);
					classMemory.put(size, test.memoryUsage());

					if (elapsed > 10_000) {
						break;
					}
				}

				test.clear();
			}

			// Create Chart
			XYChart chart = new XYChartBuilder()
				.title("Total test time: " + testEntry.getKey())
				.xAxisTitle("List size")
				.yAxisTitle("Total time (ms)")
				.width(800)
				.height(400)
				.build();

			for (Entry<Class<? extends LongList>, Map<Integer, Double>> classData : testData.entrySet()) {
				chart.addSeries(classData.getKey().getSimpleName(),
					new ArrayList<Integer>(classData.getValue().keySet()),
					new ArrayList<Double>(classData.getValue().values()));
			}

			// Save it
			BitmapEncoder.saveBitmap(chart, "img/" + testEntry.getKey() + ".png", BitmapFormat.PNG);

			// Per op chart
			chart = new XYChartBuilder()
				.title("Average operation time: " + testEntry.getKey())
				.xAxisTitle("List size")
				.yAxisTitle("Average operation time (ms)")
				.width(800)
				.height(400)
				.build();

			chart.getStyler().setYAxisLogarithmic(true);

			for (Entry<Class<? extends LongList>, Map<Integer, Double>> classData : testData.entrySet()) {
				chart.addSeries(classData.getKey().getSimpleName(),
					new ArrayList<Integer>(classData.getValue().keySet()),
					classData.getValue().entrySet().stream()
						.map(entry -> entry.getValue() / entry.getKey())
						.collect(Collectors.toList()));
			}

			// Save it
			BitmapEncoder.saveBitmap(chart, "img/" + testEntry.getKey() + "_op.png", BitmapFormat.PNG);
			System.err.println("Saved charts");

			// Memory chart
			chart = new XYChartBuilder()
				.title("Memory usage: " + testEntry.getKey())
				.xAxisTitle("List size")
				.yAxisTitle("Memory usage (bytes)")
				.width(800)
				.height(400)
				.build();

			for (Entry<Class<? extends LongList>, Map<Integer, Long>> classData : testMemory.entrySet()) {
				chart.addSeries(classData.getKey().getSimpleName(),
					new ArrayList<Integer>(classData.getValue().keySet()),
					new ArrayList<Long>(classData.getValue().values()));
			}

			// Save it
			BitmapEncoder.saveBitmap(chart, "img/" + testEntry.getKey() + "_mem.png", BitmapFormat.PNG);
		}
	}

	private static Test appendSequential = new Test() {

		private LongList					list;
		private int							size;
		private Class<? extends LongList>	impl;

		@Override
		public void init(Class<? extends LongList> impl, int size) {
			this.impl = impl;
			this.size = size;
		}

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.addLong(i);
			}
		}

		@Override
		public void reset() {
			try {
				list = impl.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void clear() {
			list = null;
		}

		@Override
		public long memoryUsage() {
			return sizeof(list);
		}
	};

	private static Test insertSequential = new Test() {
		private int							size;
		private Random						r;
		private LongList					list;
		private Class<? extends LongList>	impl;

		@Override
		public void run() {
			list.addLong(0, 0);
			for (long i = 1; i < size; i++) {
				list.addLong(r.nextInt(list.size()), i);
			}
		}

		@Override
		public void init(Class<? extends LongList> impl, int size) {
			this.impl = impl;
			this.size = size;
		}

		@Override
		public void reset() {
			try {
				r = new Random(size);
				list = impl.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void clear() {
			list = null;
			r = null;
		}

		@Override
		public long memoryUsage() {
			return sizeof(list);
		}
	};

	private static double time(String desc, Test test) {

		test.reset();

		Runtime.getRuntime().gc();
		long start = System.nanoTime();
		test.run();
		long elapsed = System.nanoTime() - start;
		int runs = 1;

		while (elapsed < 10_000_000) {
			test.reset();
			runs++;
			test.run();
			elapsed = System.nanoTime() - start;
		}

		double perRun = elapsed / 1000000.0 / runs;
		System.out.printf("%s\t%5.2f\n", desc, perRun);
		return perRun;
	}

	protected static long sizeof(Object o) {
		try {
			if (o instanceof ArrayListWrapper) {
				ArrayList<Long> list = ((ArrayListWrapper) o).list;
				Field dataField = ArrayList.class.getDeclaredField("elementData");
				dataField.setAccessible(true);
				Object[] elements = (Object[]) dataField.get(list);
				return (long) elements.length * 4 + (long) list.size() * 28;
			} else if (o instanceof TroveListWrapper) {
				TLongArrayList list = ((TroveListWrapper) o).list;
				Field dataField = TLongArrayList.class.getDeclaredField("_data");
				dataField.setAccessible(true);
				long[] elements = (long[]) dataField.get(list);
				return (long) elements.length * Long.BYTES;
			} else if (o instanceof CompactList) {
				Node node = ((CompactList) o).root;
				LongHolder size = new LongHolder();
				node.walk(leaf -> {
					StorageStrategy storageStrategy = leaf.getStorageStrategy();
					if (storageStrategy != null) {
						size.value += (long) leaf.size() * storageStrategy.getWidth() / 8;
					}
				});
				return size.value;
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}
}
