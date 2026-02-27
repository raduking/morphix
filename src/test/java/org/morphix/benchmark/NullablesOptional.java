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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.morphix.lang.Nullables;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks for {@link Nullables#whenNotNull(Object, Function)} and {@link Optional#map(Function)}.
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.NullablesOptional
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class NullablesOptional {

	static class Hotel {

		Images images;

		public Images images() {
			return images;
		}

		public Hotel images(final Images images) {
			this.images = images;
			return this;
		}
	}

	static class Images {

		List<String> names;

		public List<String> names() {
			return names;
		}

		public Images names(final List<String> names) {
			this.names = names;
			return this;
		}
	}

	private Hotel hotel;

	@Setup
	public void setup() {
		hotel = new Hotel().images(new Images().names(List.of("a", "b", "c")));
	}

	@Benchmark
	public List<String> testOptional() {
		return Optional.ofNullable(hotel.images())
				.map(Images::names)
				.orElse(null);
	}

	@Benchmark
	public List<String> testNullables() {
		return Nullables.<Images, List<String>>whenNotNull(hotel.images(), Images::names);
	}

	@Benchmark
	public List<String> testTernary() {
		return hotel.images() != null ? hotel.images().names() : null;
	}

	@Benchmark
	public List<String> testNullablesChain() {
		return Nullables.whenNotNull(hotel.images())
				.thenReturn(Images::names);
	}
}
