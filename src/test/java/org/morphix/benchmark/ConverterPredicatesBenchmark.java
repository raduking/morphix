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

import java.util.concurrent.TimeUnit;

import org.morphix.convert.Converter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks for converter predicates.
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.ConverterPredicatesBenchmark
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ConverterPredicatesBenchmark {

	static class Src {

		private A a;

		public A getA() {
			return a;
		}

		public void setA(final A a) {
			this.a = a;
		}
	}

	static class Dst {

		private B a;

		public B getA() {
			return a;
		}

		public void setA(final B a) {
			this.a = a;
		}
	}

	static class A {
		// empty
	}

	static class B {
		// empty
	}

	private Src src;

	@Setup
	public void setup() {
		src = new Src();
	}

	/**
	 * Test the performance of converting an object using the {@link Converter}.
	 * <p>
	 * This benchmark will measure the time taken to convert an instance of {@link Src} to an instance of {@link Dst} using
	 * the {@link Converter#convert(Object)} method and the {@link Converter.To#to(Class)} method. The conversion process
	 * will involve mapping the fields of the source object to the corresponding fields of the destination object, which may
	 * include type conversions and handling of nested objects. The benchmark will help to evaluate the efficiency of the
	 * conversion process and identify any potential bottlenecks or performance issues in the implementation of the
	 * {@link Converter}.
	 *
	 * @return the converted object of type {@link Dst}
	 */
	@Benchmark
	public Dst testConvertObject() {
		return Converter.convert(src).to(Dst.class);
	}
}
