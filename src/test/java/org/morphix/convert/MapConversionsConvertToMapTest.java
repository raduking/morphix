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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Test class for map conversions via {@link MapConversions} class.
 *
 * @author Radu Sebastian LAZIN
 */
class MapConversionsConvertToMapTest {

	static class A {

		private String key1;
		private String key2;

		public String getKey1() {
			return key1;
		}

		public void setKey1(final String param1) {
			this.key1 = param1;
		}

		public String getKey2() {
			return key2;
		}

		public void setKey2(final String param2) {
			this.key2 = param2;
		}
	}

	@Test
	void shouldConvertFromObject() {
		A a = new A();
		a.setKey1("value1");
		a.setKey2("value2");

		Map<String, String> params = MapConversions.convertToMap(a, k -> k, String::valueOf);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("value1"));
		assertThat(params.get("key2"), equalTo("value2"));
	}

	@Test
	void shouldConvertFromObjectWithNullValues() {
		A a = new A();
		a.setKey1(null);
		a.setKey2("value2");

		Map<String, String> params = MapConversions.convertToMap(a, k -> k, String::valueOf);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("null"));
		assertThat(params.get("key2"), equalTo("value2"));
	}

	static class B {

		private Integer key1;
		private Long key2;

		public Integer getKey1() {
			return key1;
		}

		public void setKey1(final Integer param1) {
			this.key1 = param1;
		}

		public Long getKey2() {
			return key2;
		}

		public void setKey2(final Long param2) {
			this.key2 = param2;
		}
	}

	@Test
	void shouldConvertFromObjectWithNonStringFields() {
		B b = new B();
		b.setKey1(100);
		b.setKey2(200L);

		Map<String, String> params = MapConversions.convertToMap(b, k -> k, String::valueOf);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("100"));
		assertThat(params.get("key2"), equalTo("200"));
	}

	@Test
	void shouldConvertFromEmptyObject() {
		B b = new B();

		Map<String, String> params = MapConversions.convertToMap(b, k -> k, String::valueOf);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("null"));
		assertThat(params.get("key2"), equalTo("null"));
	}

	@Test
	void shouldConvertFromNullObject() {
		Map<String, String> params = MapConversions.convertToMap(null, k -> k, String::valueOf);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenKeyMapperIsNull() {
		B b = new B();

		NullPointerException e = assertThrows(NullPointerException.class,
				() -> MapConversions.convertToMap(b, null, String::valueOf));

		assertThat(e.getMessage(), equalTo("Key converter cannot be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenValueMapperIsNull() {
		B b = new B();

		NullPointerException e = assertThrows(NullPointerException.class,
				() -> MapConversions.convertToMap(b, k -> k, null));

		assertThat(e.getMessage(), equalTo("Value converter cannot be null"));
	}

	static class C {

		private String key1;
		private D key2;

		public String getKey1() {
			return key1;
		}

		public void setKey1(final String param1) {
			this.key1 = param1;
		}

		public D getKey2() {
			return key2;
		}

		public void setKey2(final D param2) {
			this.key2 = param2;
		}
	}

	static class D {

		private String subKey1;

		public String getSubKey1() {
			return subKey1;
		}

		public void setSubKey1(final String param1) {
			this.subKey1 = param1;
		}
	}

	@Test
	void shouldConvertDeepObject() {
		C c = new C();
		c.setKey1("value1");
		D d = new D();
		d.setSubKey1("subValue1");
		c.setKey2(d);

		Map<String, Object> params = MapConversions.toPropertiesMap(c);

		assertThat(params.entrySet(), hasSize(2));

		Map<String, Object> expectedMap = Map.of(
				"key1", "value1",
				"key2", Map.of(
						"subKey1", "subValue1"));

		assertThat(params, equalTo(expectedMap));
	}

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
}
