/*
 * Copyright 2025 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.morphix.reflection.Fields;
import org.morphix.reflection.predicates.MemberPredicates;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

/**
 * Benchmarks for {@link Fields#getAllDeclared(Class, Predicate)} and {@link Fields#getAllDeclared(Class)}.
 * <p>
 * Run only this benchmark with:
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.FieldsGetDeclaredFieldsWithPredicate
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FieldsGetDeclaredFieldsWithPredicate {

	static class A {

		public int x;
		public String s;
		public Long l;
		public char c;
		public Boolean b;

	}

	@Benchmark
	public List<Field> testWithStream() {
		return getDeclaredFieldsStream(A.class, MemberPredicates.withModifiers(Modifier::isPublic));
	}

	@Benchmark
	public List<Field> testWithLoop() {
		return getDeclaredFieldsLoop(A.class, MemberPredicates.withModifiers(Modifier::isPublic));
	}

	static <T> List<Field> getDeclaredFieldsStream(final Class<T> cls, final Predicate<Field> predicate) {
		return Fields.getAllDeclared(cls).stream().filter(predicate).toList();
	}

	static <T> List<Field> getDeclaredFieldsLoop(final Class<T> cls, final Predicate<Field> predicate) {
		List<Field> declaredFields = Fields.getAllDeclared(cls);
		List<Field> fieldsMatchingPredicate = new ArrayList<>(declaredFields.size());
		for (Field field : declaredFields) {
			if (predicate.test(field)) {
				fieldsMatchingPredicate.add(field);
			}
		}
		return fieldsMatchingPredicate;
	}
}
