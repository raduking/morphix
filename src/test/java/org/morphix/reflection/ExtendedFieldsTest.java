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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverter;
import org.morphix.convert.annotation.Src;

/**
 * Test class for {@link ExtendedFields}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExtendedFieldsTest {

	private static final String VALUE = "value";
	private static final String SHADOWED = "shadowed";
	private static final String WITH_GETTER = "withGetter";
	private static final String COMPUTED = "computed";
	private static final String BASE_MAPPING = "baseMapping";
	private static final String DERIVED_MAPPING = "derivedMapping";

	@Test
	void shouldReturnAllNonStaticFieldsAndGetters() {
		List<ExtendedField> fields = ExtendedFields.findAllNonStatic(new Simple());

		assertThat(namesOf(fields), contains(VALUE, COMPUTED));
	}

	@Test
	void shouldNotReturnStaticFields() {
		List<ExtendedField> fields = ExtendedFields.findAllNonStatic(new WithStaticField());

		assertThat(namesOf(fields), contains(VALUE));
	}

	@Test
	void shouldPreferTheMostDerivedFieldWhenFieldsAreShadowed() {
		ExtendedField field = findByName(ExtendedFields.findAllNonStatic(new Derived()), SHADOWED);

		assertThat(field.getField().getDeclaringClass(), equalTo(Derived.class));
	}

	@Test
	void shouldPreferTheMostDerivedGetterWhenFieldAndGettersArePresent() {
		ExtendedField field = findByName(ExtendedFields.findAllNonStatic(new Derived()), WITH_GETTER);

		assertThat(field.getGetterMethod().getDeclaringClass(), equalTo(Derived.class));
	}

	@Test
	void shouldKeepTheFieldAndTheGetterOnTheSameEntry() {
		ExtendedField field = findByName(ExtendedFields.findAllNonStatic(new Derived()), WITH_GETTER);

		assertThat(field.getField(), not(nullValue()));
		assertThat(field.getGetterMethod(), not(nullValue()));
		assertThat(field.getFieldValue(), equalTo(Long.valueOf(20L)));
	}

	@Test
	void shouldPreferTheMostDerivedGetterWhenOnlyGettersArePresent() {
		List<ExtendedField> fields = ExtendedFields.findAllNonStatic(new DerivedWithOnlyGetters());

		assertThat(namesOf(fields), contains(COMPUTED));
		ExtendedField field = fields.get(0);
		assertThat(field.getField(), nullValue());
		assertThat(field.getGetterMethod().getDeclaringClass(), equalTo(DerivedWithOnlyGetters.class));
	}

	@Test
	void shouldPreferTheMostDerivedGetterAnnotation() {
		ExtendedField field = findByName(ExtendedFields.findAllNonStatic(new DerivedWithSrc()), COMPUTED);

		assertThat(field.getGetterMethod().getAnnotation(Src.class).value(), equalTo(DERIVED_MAPPING));
	}

	@Test
	void shouldUseTheMostDerivedGetterAnnotationAsSourceFieldName() {
		DerivedWithSrc object = new DerivedWithSrc();
		ExtendedField field = findByName(ExtendedFields.findAllNonStatic(object), COMPUTED);

		assertThat(ObjectConverter.getSourceFieldName(field, object), equalTo(DERIVED_MAPPING));
	}

	@Test
	void shouldReturnGetterOnlyEntryWhenFieldIsFilteredOut() {
		Simple object = new Simple();

		List<ExtendedField> fields = ExtendedFields.findAllNonStatic(object, field -> null == field.getField());

		assertThat(namesOf(fields), contains(VALUE, COMPUTED));
		ExtendedField field = findByName(fields, VALUE);
		assertThat(field.getField(), nullValue());
		assertThat(field.getGetterMethod(), not(nullValue()));
		assertThat(field.getFieldValue(), equalTo(Long.valueOf(10L)));
	}

	@Test
	void shouldReturnNoFieldsWhenEverythingIsFilteredOut() {
		assertThat(ExtendedFields.findAllNonStatic(new Simple(), field -> false), equalTo(List.of()));
	}

	private static List<String> namesOf(final List<ExtendedField> fields) {
		return fields.stream().map(ExtendedField::getName).toList();
	}

	private static ExtendedField findByName(final List<ExtendedField> fields, final String name) {
		return fields.stream()
				.filter(field -> name.equals(field.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("no field named " + name + " in " + namesOf(fields)));
	}

	public static class Simple {

		Long value = 1L;

		public Long getValue() {
			return 10L;
		}

		public Long getComputed() {
			return 100L;
		}
	}

	public static class WithStaticField {

		static Long hidden = 1L;

		Long value = 2L;
	}

	public static class Base {

		Long shadowed = 1L;

		Long withGetter = 2L;

		public Long getWithGetter() {
			return 10L;
		}
	}

	public static class Derived extends Base {

		Long shadowed = 3L;

		@Override
		public Long getWithGetter() {
			return 20L;
		}
	}

	public static class BaseWithOnlyGetters {

		public Long getComputed() {
			return 1L;
		}
	}

	public static class DerivedWithOnlyGetters extends BaseWithOnlyGetters {

		@Override
		public Long getComputed() {
			return 2L;
		}
	}

	public static class BaseWithSrc {

		@Src(BASE_MAPPING)
		public Long getComputed() {
			return 1L;
		}
	}

	public static class DerivedWithSrc extends BaseWithSrc {

		@Src(DERIVED_MAPPING)
		@Override
		public Long getComputed() {
			return 2L;
		}
	}
}
