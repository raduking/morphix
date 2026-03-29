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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.cache.ConcurrentStrictLRUCache;
import org.morphix.lang.cache.ConcurrentThreadLRUCache;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmarks for LRU cache implementations.
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.CacheBenchmark
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(2)
@Threads(16)
@State(Scope.Benchmark)
public class CacheBenchmark {

	private static final int KEY_SPACE = 1000;
	private static final int CACHE_SIZE = 256;

	private ConcurrentStrictLRUCache<Integer, Integer> strict;
	private ConcurrentThreadLRUCache<Integer, Integer> thread;
	private Map<Integer, Integer> synchronizedLinkedHashMap;
	private ConcurrentHashMap<Integer, Integer> chm;

	@Setup(Level.Trial)
	public void setup() {

		strict = new ConcurrentStrictLRUCache<>(CACHE_SIZE);
		thread = new ConcurrentThreadLRUCache<>(CACHE_SIZE);

		chm = new ConcurrentHashMap<>();

		synchronizedLinkedHashMap = Collections.synchronizedMap(
				new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
					@Override
					protected boolean removeEldestEntry(final Map.Entry<Integer, Integer> eldest) {
						return size() > CACHE_SIZE;
					}
				});
	}

	private static int key() {
		return ThreadLocalRandom.current().nextInt(KEY_SPACE);
	}

	private int value(final int k) {
		return k * 31;
	}

	@Benchmark
	public int strictLRUCache() {
		int k = key();
		return strict.computeIfAbsent(k, this::value);
	}

	@Benchmark
	public int threadLRUCache() {
		int k = key();
		return thread.computeIfAbsent(k, this::value);
	}

	@Benchmark
	public int synchronizedLinkedHashMap() {
		int k = key();
		return synchronizedLinkedHashMap.computeIfAbsent(k, this::value);
	}

	@Benchmark
	public int concurrentHashMap() {
		int k = key();
		return chm.computeIfAbsent(k, this::value);
	}
}
