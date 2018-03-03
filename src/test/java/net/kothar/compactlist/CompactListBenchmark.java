package net.kothar.compactlist;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.kothar.compactlist.internal.storage.Store;
import net.kothar.compactlist.legacy.CSViewLongList;
import net.kothar.compactlist.legacy.CSViewLongList.Block;

public class CompactListBenchmark {

	interface Test {
		void init(Class<? extends LongList> impl, int size);

		void reset();

		void run();

		void clear();

		long memoryUsage();

		long compactMemoryUsage();
	}

	public static void main(String[] args) throws IOException {

		int maxListSize = 21;
		if (args.length > 0) {
			maxListSize = Integer.parseInt(args[0]);
		}
		System.out.println("Running benchmarks up to lists of " + (1 << maxListSize) + " elements");

		System.out.println("Test\t\tCount\t\tClass\t\t\tElapsed");

		Map<String, Test> tests = new LinkedHashMap<>();
		tests.put("append", appendSequential);
		tests.put("appendRandom", appendRandom);
		tests.put("insert", insertSequential);
		tests.put("set", setSeq);
		tests.put("setRandom", setRand);
		tests.put("remove", removeRandom);

		for (Entry<String, Test> testEntry : tests.entrySet()) {

			Map<Class<? extends LongList>, Map<Integer, Double>> testData = new LinkedHashMap<>();
			Map<Class<? extends LongList>, Map<Integer, Long>> testMemory = new LinkedHashMap<>();
			Map<Integer, Long> compactMemory = new TreeMap<>();

			for (Class<? extends LongList> impl : Arrays.asList(
				CompactList.class,
				ArrayListWrapper.class,
				TroveListWrapper.class)) {

				TreeMap<Integer, Double> classData = new TreeMap<>();
				testData.put(impl, classData);
				TreeMap<Integer, Long> classMemory = new TreeMap<>();
				testMemory.put(impl, classMemory);

				Test test = testEntry.getValue();
				try {
					warmup(impl, test, maxListSize);

					int increment = 10_000;
					for (int size = increment; size < 1 << maxListSize; size += increment, increment *= 1.2) {

						test.init(impl, size);

						double elapsed = time(
							String.format("%s\t%10d\t%s", testEntry.getKey(), size, impl.getSimpleName()),
							test);

						classData.put(size, elapsed);
						classMemory.put(size, test.memoryUsage());

						if (impl.equals(CompactList.class)) {
							compactMemory.put(size, test.compactMemoryUsage());
						}

						if (elapsed > 3_000) {
							break;
						}
					}
				} catch (Exception e) {
					System.err.println("Test not supported for " + impl.getSimpleName());
					e.printStackTrace();
				} finally {
					test.clear();
				}
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
				if (!classData.getValue().isEmpty()) {
					chart.addSeries(classData.getKey().getSimpleName(),
						new ArrayList<Integer>(classData.getValue().keySet()),
						new ArrayList<Double>(classData.getValue().values()));
				}
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
				if (!classData.getValue().isEmpty()) {
					chart.addSeries(classData.getKey().getSimpleName(),
						new ArrayList<Integer>(classData.getValue().keySet()),
						classData.getValue().entrySet().stream()
							.map(entry -> entry.getValue() / entry.getKey())
							.collect(Collectors.toList()));
				}
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

			chart.getStyler().setYAxisLogarithmic(true);

			for (Entry<Class<? extends LongList>, Map<Integer, Long>> classData : testMemory.entrySet()) {
				if (!classData.getValue().isEmpty()) {
					chart.addSeries(classData.getKey().getSimpleName(),
						new ArrayList<Integer>(classData.getValue().keySet()),
						new ArrayList<Long>(classData.getValue().values()));
				}
			}
			chart.addSeries("CompactList (compacted)",
				new ArrayList<Integer>(compactMemory.keySet()),
				new ArrayList<Long>(compactMemory.values()));

			// Save it
			BitmapEncoder.saveBitmap(chart, "img/" + testEntry.getKey() + "_mem.png", BitmapFormat.PNG);
		}
	}

	private static Test appendSequential = new Test() {

		private LongList					list;
		private int							size;
		private Class<? extends LongList>	impl;
		private long[]						values;

		@Override
		public void init(Class<? extends LongList> impl, int size) {
			this.impl = impl;
			this.size = size;
		}

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.addLong(values[i]);
			}
		}

		@Override
		public void reset() {
			try {
				list = impl.newInstance();
				values = new long[size];
				for (int i = 0; i < size; i++) {
					values[i] = i;
				}
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

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static Test appendRandom = new Test() {

		private LongList					list;
		private long[]						values;
		private int							size;
		private Random						r;
		private Class<? extends LongList>	impl;

		@Override
		public void init(Class<? extends LongList> impl, int size) {
			this.impl = impl;
			this.size = size;
		}

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.addLong(values[i]);
			}
		}

		@Override
		public void reset() {
			try {
				r = new Random(size);
				list = impl.newInstance();
				values = new long[size];
				for (int i = 0; i < size; i++) {
					values[i] = r.nextInt();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void clear() {
			list = null;
			r = null;
			values = null;
		}

		@Override
		public long memoryUsage() {
			return sizeof(list);
		}

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static Test insertSequential = new Test() {
		private int							size;
		private Random						r;
		private LongList					list;
		private Class<? extends LongList>	impl;
		private int[]						positions;

		@Override
		public void run() {
			list.addLong(0, 0);
			for (int i = 0; i < size; i++) {
				list.addLong(positions[i], i);
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
				this.positions = new int[size];

				for (int i = 0; i < size; i++) {
					positions[i] = r.nextInt(i + 1);
				}
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

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static Test removeRandom = new Test() {
		private int							size;
		private Random						r;
		private LongList					list;
		private Class<? extends LongList>	impl;
		private int[]						positions;

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.removeLong(positions[i]);
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
				for (long i = 0; i < size; i++) {
					list.addLong(r.nextInt(size));
				}
				positions = new int[size];
				for (int i = 0; i < size; i++) {
					positions[i] = r.nextInt(size - i);
				}
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

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static Test setSeq = new Test() {
		private int							size;
		private Random						r;
		private LongList					list;
		private Class<? extends LongList>	impl;
		private int[]						positions;

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.setLong(positions[i], i);
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
				for (long i = 0; i < size; i++) {
					list.addLong(i);
				}
				this.positions = new int[size];

				for (int i = 0; i < size; i++) {
					positions[i] = r.nextInt(size);
				}
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

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static Test setRand = new Test() {
		private int							size;
		private Random						r;
		private LongList					list;
		private Class<? extends LongList>	impl;
		private int[]						positions;

		@Override
		public void run() {
			for (int i = 0; i < size; i++) {
				list.setLong(positions[i], i);
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
				for (long i = 0; i < size; i++) {
					list.addLong(r.nextInt(size));
				}
				this.positions = new int[size];

				for (int i = 0; i < size; i++) {
					positions[i] = r.nextInt(size);
				}
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

		@Override
		public long compactMemoryUsage() {
			((CompactList) list).compact();
			return memoryUsage();
		}
	};

	private static void warmup(Class<? extends LongList> impl, Test test, int maxListSize) {
		System.out.println("Warming up " + impl.getSimpleName());
		int size = 1 << maxListSize / 2;
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 5_000) {
			test.init(impl, size);
			test.reset();
			test.run();
			size <<= 1;
		}
	}

	private static double time(String desc, Test test) {

		test.reset();

		Runtime.getRuntime().gc();
		long start = System.nanoTime();
		test.run();
		long elapsed = System.nanoTime() - start;
		int runs = 1;

		while (elapsed < 100_000_000 || runs < 3) {
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
				LongHolder size = new LongHolder(16);
				node.walk(leaf -> {
					// This approximates the true size based to the number of leaves, but doesn't
					// account for other nodes
					size.value += 24;
					Store storageStrategy = leaf.getStorageStrategy();
					if (storageStrategy != null) {
						size.value += (long) leaf.size() * storageStrategy.getWidth() / 8;
					}
				});
				return size.value;
			} else if (o instanceof CSViewLongList) {
				Field dataField = CSViewLongList.class.getDeclaredField("root");
				dataField.setAccessible(true);
				Block block = (Block) dataField.get(o);
				return size(block);
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

	private static long size(Block block)
		throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field leftField = Block.class.getDeclaredField("left");
		leftField.setAccessible(true);
		Field rightField = Block.class.getDeclaredField("right");
		rightField.setAccessible(true);
		Field itemsField = Block.class.getDeclaredField("items");
		itemsField.setAccessible(true);

		long size = 24;
		ByteBuffer items = (ByteBuffer) itemsField.get(block);
		if (items != null) {
			size += items.capacity();
		}

		Block left = (Block) leftField.get(block);
		if (left != null) {
			size += size(left);
		}
		Block right = (Block) rightField.get(block);
		if (right != null) {
			size += size(right);
		}

		return size;
	}
}
