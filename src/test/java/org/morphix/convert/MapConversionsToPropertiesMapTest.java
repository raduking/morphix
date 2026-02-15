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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MapConversions#toPropertiesMap(Object)}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapConversionsToPropertiesMapTest {

	enum Status {
		ACTIVE,
		INACTIVE
	}

	static class Nested {

		private String nestedValue;

		public String getNestedValue() {
			return nestedValue;
		}

		public void setNestedValue(final String nestedValue) {
			this.nestedValue = nestedValue;
		}
	}

	static class Everything {

		private String stringValue;
		private StringBuilder charSequenceValue;
		private Integer numberValue;
		private Boolean booleanValue;
		private Status enumValue;
		private UUID uuidValue;
		private Optional<String> optionalValue;
		private Optional<String> emptyOptional;
		private Map<String, Object> mapValue;
		private List<Object> collectionValue;
		private Object[] arrayValue;
		private Nested nestedObject;
		private String nullValue;

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(final String stringValue) {
			this.stringValue = stringValue;
		}

		public StringBuilder getCharSequenceValue() {
			return charSequenceValue;
		}

		public void setCharSequenceValue(final StringBuilder charSequenceValue) {
			this.charSequenceValue = charSequenceValue;
		}

		public Integer getNumberValue() {
			return numberValue;
		}

		public void setNumberValue(final Integer numberValue) {
			this.numberValue = numberValue;
		}

		public Boolean getBooleanValue() {
			return booleanValue;
		}

		public void setBooleanValue(final Boolean booleanValue) {
			this.booleanValue = booleanValue;
		}

		public Status getEnumValue() {
			return enumValue;
		}

		public void setEnumValue(final Status enumValue) {
			this.enumValue = enumValue;
		}

		public UUID getUuidValue() {
			return uuidValue;
		}

		public void setUuidValue(final UUID uuidValue) {
			this.uuidValue = uuidValue;
		}

		public Optional<String> getOptionalValue() {
			return optionalValue;
		}

		public void setOptionalValue(final Optional<String> optionalValue) {
			this.optionalValue = optionalValue;
		}

		public Optional<String> getEmptyOptional() {
			return emptyOptional;
		}

		public void setEmptyOptional(final Optional<String> emptyOptional) {
			this.emptyOptional = emptyOptional;
		}

		public Map<String, Object> getMapValue() {
			return mapValue;
		}

		public void setMapValue(final Map<String, Object> mapValue) {
			this.mapValue = mapValue;
		}

		public List<Object> getCollectionValue() {
			return collectionValue;
		}

		public void setCollectionValue(final List<Object> collectionValue) {
			this.collectionValue = collectionValue;
		}

		public Object[] getArrayValue() {
			return arrayValue;
		}

		public void setArrayValue(final Object[] arrayValue) {
			this.arrayValue = arrayValue;
		}

		public Nested getNestedObject() {
			return nestedObject;
		}

		public void setNestedObject(final Nested nestedObject) {
			this.nestedObject = nestedObject;
		}

		public String getNullValue() {
			return nullValue;
		}

		public void setNullValue(final String nullValue) {
			this.nullValue = nullValue;
		}
	}

	static Everything createEverything() {
		Nested nested = new Nested();
		nested.setNestedValue("nested");

		Map<String, Object> innerMap = Map.of(
				"innerKey1", "innerValue",
				"innerKey2", 42,
				"innerKey3", Status.ACTIVE);

		Everything e = new Everything();
		e.setStringValue("hello");
		e.setCharSequenceValue(new StringBuilder("builder"));
		e.setNumberValue(123);
		e.setBooleanValue(true);
		e.setEnumValue(Status.INACTIVE);
		e.setUuidValue(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
		e.setOptionalValue(Optional.of("optional"));
		e.setEmptyOptional(Optional.empty());
		e.setMapValue(innerMap);
		e.setCollectionValue(List.of("listValue", 99, Status.ACTIVE));
		e.setArrayValue(new Object[] { "arrayValue", 77, false });
		e.setNestedObject(nested);
		e.setNullValue(null);

		return e;
	}

	@Test
	void shouldConvertComplexObject() {
		Everything everything = createEverything();

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("stringValue", "hello");
		expected.put("charSequenceValue", "builder");
		expected.put("numberValue", "123");
		expected.put("booleanValue", "true");
		expected.put("enumValue", "INACTIVE");
		expected.put("uuidValue", "123e4567-e89b-12d3-a456-426614174000");
		expected.put("optionalValue", "optional");
		expected.put("emptyOptional", null);
		expected.put("mapValue", Map.of(
				"innerKey1", "innerValue",
				"innerKey2", "42",
				"innerKey3", "ACTIVE"));
		expected.put("collectionValue", List.of(
				"listValue",
				"99",
				"ACTIVE"));
		expected.put("arrayValue", List.of(
				"arrayValue",
				"77",
				"false"));
		expected.put("nestedObject", Map.of(
				"nestedValue", "nested"));
		expected.put("nullValue", null);

		Map<String, Object> params = MapConversions.toPropertiesMap(everything);

		assertThat(params, equalTo(expected));
	}

	enum Mode {
		FAST,
		SLOW
	}

	static class InnerPojo {

		private String name;
		private Optional<Integer> count;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public Optional<Integer> getCount() {
			return count;
		}

		public void setCount(final Optional<Integer> count) {
			this.count = count;
		}
	}

	static class Stress {

		private Map<String, Object> map;
		private List<Object> list;
		private Optional<Object> optional;
		private Object[] array;

		public Map<String, Object> getMap() {
			return map;
		}

		public void setMap(final Map<String, Object> map) {
			this.map = map;
		}

		public List<Object> getList() {
			return list;
		}

		public void setList(final List<Object> list) {
			this.list = list;
		}

		public Optional<Object> getOptional() {
			return optional;
		}

		public void setOptional(final Optional<Object> optional) {
			this.optional = optional;
		}

		public Object[] getArray() {
			return array;
		}

		public void setArray(final Object[] array) {
			this.array = array;
		}
	}

	static Stress createStress() {
		InnerPojo pojo = new InnerPojo();
		pojo.setName("deepPojo");
		pojo.setCount(Optional.of(5));

		Map<String, Object> nestedMap = new LinkedHashMap<>();
		nestedMap.put("pojo", pojo);
		nestedMap.put("mode", Mode.FAST);
		nestedMap.put("numbers", List.of(1, 2, 3));

		List<Object> crazyList = List.of(
				Optional.of("wrapped"),
				nestedMap,
				List.of(Optional.of(pojo), Optional.empty()));

		Stress s = new Stress();

		Map<String, Object> innerMap = new LinkedHashMap<>();
		innerMap.put("level1", Map.of("level2", List.of(
				Optional.of(Map.of(
						"arrayInside", new Object[] {
								Mode.SLOW,
								99,
								pojo
						})))));
		innerMap.put("nullInside", null);
		s.setMap(innerMap);

		s.setList(crazyList);

		s.setOptional(Optional.of(List.of(
				Map.of("inner", Optional.of(pojo)))));

		s.setArray(new Object[] {
				Optional.of(Map.of(
						"collectionInside", List.of(pojo, Mode.FAST))),
				null
		});

		return s;
	}

	@Test
	void shouldHandleDeeplyNestedStructures() {
		Map<String, Object> expected = new LinkedHashMap<>();

		Map<String, Object> innerMap = new LinkedHashMap<>();
		innerMap.put("level1", Map.of(
				"level2", List.of(
						Map.of(
								"arrayInside", List.of(
										"SLOW",
										"99",
										Map.of(
												"name", "deepPojo",
												"count", "5"))))));
		innerMap.put("nullInside", null);

		expected.put("map", innerMap);

		expected.put("list", List.of(
				"wrapped",
				Map.of(
						"pojo", Map.of(
								"name", "deepPojo",
								"count", "5"),
						"mode", "FAST",
						"numbers", List.of("1", "2", "3")),
				Arrays.asList(
						Map.of(
								"name", "deepPojo",
								"count", "5"),
						null)));

		expected.put("optional", List.of(
				Map.of(
						"inner", Map.of(
								"name", "deepPojo",
								"count", "5"))));

		expected.put("array", Arrays.asList(
				Map.of(
						"collectionInside", List.of(
								Map.of(
										"name", "deepPojo",
										"count", "5"),
								"FAST")),
				null));

		Stress stress = createStress();

		Map<String, Object> params = MapConversions.toPropertiesMap(stress);

		assertThat(params, equalTo(expected));
	}

	static class Node {

		private Node next;

		public Node getNext() {
			return next;
		}

		public void setNext(final Node next) {
			this.next = next;
		}
	}

	@Test
	void shouldHandleCyclicReferences() {
		Node node1 = new Node();
		Node node2 = new Node();
		node1.setNext(node2);
		node2.setNext(node1);

		Map<String, Object> expected = Map.of(
				"next", Map.of(
						"next", Map.of("_cyclic_ref", Node.class.getSimpleName())));

		Map<String, Object> params = MapConversions.toPropertiesMap(node1);

		assertThat(params, equalTo(expected));
	}

	@Test
	void shouldHandleNullInput() {
		Map<String, Object> params = MapConversions.toPropertiesMap(null);

		assertThat(params, equalTo(null));
	}

	record A(Optional<B> b) {
		// empty
	}

	record B(Optional<A> a) {
		// empty
	}

	@Test
	void shouldHandleCyclicOptionals() {
		A a = new A(Optional.empty());
		B b = new B(Optional.of(a));
		a = new A(Optional.of(b));

		Map<String, Object> innerMap = new LinkedHashMap<>();
		innerMap.put("b", null);
		Map<String, Object> expected = Map.of(
				"b", Map.of(
						"a", innerMap));

		Map<String, Object> params = MapConversions.toPropertiesMap(a);

		assertThat(params, equalTo(expected));
	}
}
