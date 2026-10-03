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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Fields.Safe#getByPath(Object, String)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldsSafeGetByPathTest {

	private static final String FIELD_VALUE = "fieldValue";

	private static final String GETTER_VALUE = "getterValue";

	private static final String NON_EXISTENT_FIELD = "$NonExistentField$";

	public static class WithTransformingGetter {

		public String value = FIELD_VALUE;

		public String getValue() {
			return GETTER_VALUE;
		}
	}

	public static class WithNestedField {

		public WithTransformingGetter inner = new WithTransformingGetter();
	}

	@Test
	void shouldGetFieldAtFirstLevel() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner");

		assertThat(result, equalTo(obj.inner));
	}

	@Test
	void shouldGetNestedField() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner.value");

		assertThat(result, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnNullIfFieldDoesNotExistAtFirstLevel() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, NON_EXISTENT_FIELD);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullIfNestedFieldDoesNotExist() {
		WithNestedField obj = new WithNestedField();

		Object result = Fields.Safe.getByPath(obj, "inner." + NON_EXISTENT_FIELD);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullIfIntermediateFieldIsNull() {
		WithNestedField obj = new WithNestedField();
		obj.inner = null;

		Object result = Fields.Safe.getByPath(obj, "inner.value");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullForEmptyPath() {
		Object result = Fields.Safe.getByPath(new WithNestedField(), "");

		assertThat(result, nullValue());
	}

	@Test
	void shouldReadTheFieldAndNotTheGetterOnASingleSegmentPath() {
		WithTransformingGetter obj = new WithTransformingGetter();

		Object result = Fields.Safe.getByPath(obj, "value");

		assertThat(result, equalTo(FIELD_VALUE));
	}

	@Test
	void shouldReturnNullIfObjectIsNull() {
		Object result = Fields.Safe.getByPath(null, "value");

		assertThat(result, nullValue());
	}
}
