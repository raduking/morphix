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

import java.util.Map;

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

}
