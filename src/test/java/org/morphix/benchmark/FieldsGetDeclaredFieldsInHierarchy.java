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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.morphix.reflection.Fields;
import org.morphix.reflection.predicates.MemberPredicates;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for {@link Fields#getAllDeclaredInHierarchy(Class)}.
 * <p>
 * Run only this benchmark with:
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.FieldsGetDeclaredFieldsInHierarchy
 * </pre>
 *
 * Or run specific methods with:
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks='org.morphix.benchmark.FieldsGetDeclaredFieldsInHierarchy.testConstructionWith.*'
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FieldsGetDeclaredFieldsInHierarchy {

	static class A {

		public int x;
		public String s;
		public Long l;
		public char c;
		public Boolean b;
	}

	static class B extends A {

		public int x2;
		public String s2;
		public Long l2;
		public char c2;
		public Boolean b2;
	}

	static class C extends B {

		public int x3;
		public String s3;
		public Long l3;
		public char c3;
		public Boolean b3;
	}

	static class D extends C {

		public int x4;
		public String s4;
		public Long l4;
		public char c4;
		public Boolean b4;
	}

	@State(Scope.Thread)
	public static class TraversalState {

		List<Field> linkedList;
		List<Field> arrayList;

		@Setup(Level.Trial)
		public void setup() {
			linkedList = getDeclaredFieldsLinkedList(D.class);
			arrayList = new ArrayList<>(linkedList);
		}
	}

	@Benchmark
	public List<Field> testConstructionWithCurrentImplementation() {
		return Fields.getAllDeclaredInHierarchy(D.class);
	}

	@Benchmark
	public List<Field> testConstructionWithLinkedList() {
		return getDeclaredFieldsLinkedList(D.class);
	}

	@Benchmark
	public List<Field> testConstructionWithArrayListAddAll() {
		return getDeclaredFieldsArrayListAddAll(D.class);
	}

	@Benchmark
	public List<Field> testConstructionWithArrayListLoop() {
		return getDeclaredFieldsArrayListLoop(D.class);
	}

	@Benchmark
	public List<Field> testConstructionWithArrayListEnsureCapacity() {
		return getDeclaredFieldsArrayListEnsureCapacity(D.class);
	}

	@Benchmark
	public void testTraversalWithLinkedList(final Blackhole blackhole) {
		List<Field> fields = getDeclaredFieldsLinkedList(D.class);
		for (Field field : fields) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public void testTraversalWithArrayListAddAll(final Blackhole blackhole) {
		List<Field> fields = getDeclaredFieldsArrayListAddAll(D.class);
		for (Field field : fields) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public void testTraversalWithArrayListLoop(final Blackhole blackhole) {
		List<Field> fields = getDeclaredFieldsArrayListLoop(D.class);
		for (Field field : fields) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public void testTraversalWithArrayListEnsureCapacity(final Blackhole blackhole) {
		List<Field> fields = getDeclaredFieldsArrayListEnsureCapacity(D.class);
		for (Field field : fields) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public void testOnlyTraversalWithLinkedList(final TraversalState state, final Blackhole blackhole) {
		for (Field field : state.linkedList) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public void testOnlyTraversalWithArrayList(final TraversalState state, final Blackhole blackhole) {
		for (Field field : state.arrayList) {
			blackhole.consume(field);
		}
	}

	@Benchmark
	public List<Field> testFilterWithLinkedList() {
		return Fields.filter(
				getDeclaredFieldsLinkedList(D.class),
				MemberPredicates.withModifiers(Modifier::isPublic));
	}

	@Benchmark
	public List<Field> testFilterWithArrayListAddAll() {
		return Fields.filter(
				getDeclaredFieldsArrayListAddAll(D.class),
				MemberPredicates.withModifiers(Modifier::isPublic));
	}

	@Benchmark
	public List<Field> testFilterWithArrayListLoop() {
		return Fields.filter(
				getDeclaredFieldsArrayListLoop(D.class),
				MemberPredicates.withModifiers(Modifier::isPublic));
	}

	@Benchmark
	public List<Field> testFilterWithArrayListEnsureCapacity() {
		return Fields.filter(
				getDeclaredFieldsArrayListEnsureCapacity(D.class),
				MemberPredicates.withModifiers(Modifier::isPublic));
	}

	@Benchmark
	public List<Field> testFilterWithArrayListInline() {
		return getDeclaredFieldsArrayListInlineFilter(
				D.class,
				MemberPredicates.withModifiers(Modifier::isPublic));
	}

	static <T> List<Field> getDeclaredFieldsLinkedList(final Class<T> cls) {
		if (null == cls) {
			return List.of();
		}
		Class<?> superClass = cls.getSuperclass();
		List<Field> fields = null == superClass
				? new LinkedList<>()
				: getDeclaredFieldsLinkedList(superClass);
		fields.addAll(0, Fields.getAllDeclared(cls));
		return fields;
	}

	static <T> List<Field> getDeclaredFieldsArrayListAddAll(final Class<T> cls) {
		if (null == cls) {
			return List.of();
		}
		List<Field> fields = new ArrayList<>();
		for (Class<?> type = cls; null != type; type = type.getSuperclass()) {
			fields.addAll(Fields.getAllDeclared(type));
		}
		return fields;
	}

	static <T> List<Field> getDeclaredFieldsArrayListLoop(final Class<T> cls) {
		if (null == cls) {
			return List.of();
		}
		List<Field> fields = new ArrayList<>();
		for (Class<?> type = cls; null != type; type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				fields.add(field);
			}
		}
		return fields;
	}

	static <T> List<Field> getDeclaredFieldsArrayListEnsureCapacity(final Class<T> cls) {
		if (null == cls) {
			return List.of();
		}
		ArrayList<Field> fields = new ArrayList<>();
		for (Class<?> type = cls; null != type; type = type.getSuperclass()) {
			Field[] declaredFields = type.getDeclaredFields();
			fields.ensureCapacity(fields.size() + declaredFields.length);
			for (Field field : declaredFields) {
				fields.add(field);
			}
		}
		return fields;
	}

	static <T> List<Field> getDeclaredFieldsArrayListInlineFilter(
			final Class<T> cls, final Predicate<Field> predicate) {
		if (null == cls || null == predicate) {
			return List.of();
		}
		List<Field> fields = new ArrayList<>();
		for (Class<?> type = cls; null != type; type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				if (predicate.test(field)) {
					fields.add(field);
				}
			}
		}
		return fields;
	}
}
