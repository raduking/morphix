/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.collections.FastArrayList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

/**
 * {@link FastArrayList} benchmark.
 *
 * <pre>
 * mvn clean test-compile
 * </pre>
 *
 * <pre>
 * find target -name BenchmarkList
 * </pre>
 *
 * should return:
 *
 * <pre>
 * target / test - classes / META - INF / BenchmarkList
 * </pre>
 *
 * then:
 *
 * <pre>
 * find target/generated-test-sources -name '*jmh*'
 * </pre>
 *
 * should return a list:
 *
 * <pre>
 * target/generated-test-sources/test-annotations/org/morphix/benchmark/jmh_generated
 * ...
 * </pre>
 *
 * <pre>
 * mvn -Pbenchmark clean test-compile exec:exec \
 *   -Dexec.executable=java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.args="-cp %classpath org.morphix.benchmark.FastArrayListBenchmark"
 * </pre>
 *
 * Or with options:
 *
 * <pre>
 * mvn -Pbenchmark clean test-compile exec:exec \
 *   -Dexec.executable=java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.args="-cp %classpath org.openjdk.jmh.Main FastArrayListBenchmark -p size=1000,100000 -wi 3 -i 3 -f 2 -prof gc"
 * </pre>
 *
 * Or specific ones:
 *
 * <pre>
 * mvn -Pbenchmark clean test-compile exec:exec \
 *   -Dexec.executable=java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.args="-cp %classpath org.openjdk.jmh.Main .*AddPreallocated.* -prof gc"
 *
 * mvn -Pbenchmark clean test-compile exec:exec \
 *   -Dexec.executable=java \
 *   -Dexec.classpathScope=test \
 *   -Dexec.args="-cp %classpath org.openjdk.jmh.Main .*AddWithGrowth.* -prof gc"
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class FastArrayListBenchmark {

	@Param({ "10", "100", "1000", "10000", "100000" })
	private int size;

	private Integer[] array;

	private List<Integer> arrayView;

	private ArrayList<Integer> arrayList;

	private FastArrayList<Integer> fastArrayList;

	public static void main(final String[] args) throws RunnerException {
		BenchmarkRunner.run(FastArrayListBenchmark.class);
	}

	@Setup(Level.Trial)
	public void setup() {
		array = new Integer[size];

		for (int i = 0; i < size; ++i) {
			array[i] = i;
		}

		arrayView = Arrays.asList(array);
		arrayList = new ArrayList<>(arrayView);
		fastArrayList = new FastArrayList<>(array);
	}

	// ------------------------------------------------------------
	// Construction from arrays
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListConstructorFromArray() {
		return new ArrayList<>(Arrays.asList(array));
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListConstructorFromArray() {
		return new FastArrayList<>(array);
	}

	// ------------------------------------------------------------
	// Construction from collections
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListConstructorFromCollection() {
		return new ArrayList<>(arrayList);
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListConstructorFromCollection() {
		return new FastArrayList<>(arrayList);
	}

	// ------------------------------------------------------------
	// Zero-copy wrapping
	// ------------------------------------------------------------

	@Benchmark
	public List<Integer> arraysAsList() {
		return Arrays.asList(array);
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListWrap() {
		return FastArrayList.wrap(array);
	}

	// ------------------------------------------------------------
	// Bulk insertion from arrays
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListAddAllArray() {
		ArrayList<Integer> list = new ArrayList<>(size);
		list.addAll(Arrays.asList(array));
		return list;
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListAddAllArray() {
		FastArrayList<Integer> list = new FastArrayList<>(size);
		list.addAll(array);
		return list;
	}

	// ------------------------------------------------------------
	// Bulk insertion from pre-existing array views
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListAddAllArrayView() {
		ArrayList<Integer> list = new ArrayList<>(size);
		list.addAll(arrayView);
		return list;
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListAddAllArrayView() {
		FastArrayList<Integer> list = new FastArrayList<>(size);
		list.addAll(arrayView);
		return list;
	}

	// ------------------------------------------------------------
	// Bulk insertion from the same list implementation
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListAddAllSameType() {
		ArrayList<Integer> list = new ArrayList<>(size);
		list.addAll(arrayList);
		return list;
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListAddAllSameType() {
		FastArrayList<Integer> list = new FastArrayList<>(size);
		list.addAll(fastArrayList);
		return list;
	}

	// ------------------------------------------------------------
	// Individual insertion with preallocated capacity
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListAddPreallocated() {
		ArrayList<Integer> list = new ArrayList<>(size);

		for (int i = 0; i < size; ++i) {
			list.add(array[i]);
		}

		return list;
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListAddPreallocated() {
		FastArrayList<Integer> list = new FastArrayList<>(size);

		for (int i = 0; i < size; ++i) {
			list.add(array[i]);
		}

		return list;
	}

	// ------------------------------------------------------------
	// Individual insertion with dynamic growth
	// ------------------------------------------------------------

	@Benchmark
	public ArrayList<Integer> arrayListAddWithGrowth() {
		ArrayList<Integer> list = new ArrayList<>();

		for (int i = 0; i < size; ++i) {
			list.add(array[i]);
		}

		return list;
	}

	@Benchmark
	public FastArrayList<Integer> fastArrayListAddWithGrowth() {
		FastArrayList<Integer> list = new FastArrayList<>();

		for (int i = 0; i < size; ++i) {
			list.add(array[i]);
		}

		return list;
	}

	// ------------------------------------------------------------
	// Indexed access
	// ------------------------------------------------------------

	@Benchmark
	public long arrayListGet() {
		long sum = 0;

		for (int i = 0; i < size; ++i) {
			sum += arrayList.get(i);
		}

		return sum;
	}

	@Benchmark
	public long fastArrayListGet() {
		long sum = 0;

		for (int i = 0; i < size; ++i) {
			sum += fastArrayList.get(i);
		}

		return sum;
	}

	// ------------------------------------------------------------
	// Iterator traversal
	// ------------------------------------------------------------

	@Benchmark
	public long arrayListIterator() {
		long sum = 0;

		for (Integer value : arrayList) {
			sum += value;
		}

		return sum;
	}

	@Benchmark
	public long fastArrayListIterator() {
		long sum = 0;

		for (Integer value : fastArrayList) {
			sum += value;
		}

		return sum;
	}
}
