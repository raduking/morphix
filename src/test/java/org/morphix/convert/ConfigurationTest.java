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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Configuration.Default;
import org.morphix.convert.extras.ExcludedFields;
import org.morphix.convert.extras.ExpandableFields;
import org.morphix.convert.extras.SimpleConverters;
import org.morphix.convert.strategy.ConversionStrategy;

/**
 * Test class for {@link Configuration}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConfigurationTest {

	@Test
	void shouldFailEqualsOnWrongClass() {
		Configuration config = Configuration.defaults();
		@SuppressWarnings("unlikely-arg-type")
		boolean result = config.equals("config");

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldUseAllMembersOnHashCode() {
		List<FieldHandler> fieldHandlers = Collections.singletonList(DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY);
		List<ConversionStrategy> strategies = Collections.singletonList(DefaultStrategies.STRATEGY_BASIC_NAME);
		ExcludedFields excludedFields = ExcludedFields.of(Collections.singletonList("y"));
		ExpandableFields expandableFields = ExpandableFields.of(Collections.singletonList("x"));
		SimpleConverters simpleConverters = SimpleConverters.empty();

		Configuration config = Configuration.of(fieldHandlers, strategies, excludedFields, expandableFields, simpleConverters);
		int result = config.hashCode();
		int expected = Objects.hash(
				config.getFieldHandlers(),
				config.getStrategies(),
				config.getExcludedFields(),
				config.getExpandableFields(),
				config.getSimpleConverters(),
				config.getGenericTypesMap());

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnEqualOnDefaultConfigurations() {
		boolean result = Configuration.defaults().equals(Configuration.defaults());
		assertThat(result, equalTo(true));
	}

	@Test
	void shouldCheckForDefaultConfiguration() {
		Configuration configuration = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);

		System.out.println("" + configuration.getGenericTypesMap() + " - " + Configuration.defaults().getGenericTypesMap());

		assertTrue(configuration.isDefault());
	}

	@Test
	void shouldReturnFalseOnEqualsWithNull() {
		Configuration configuration = Configuration.defaults();

		boolean result = configuration.equals(null);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnEqualsWithTheSameObject() {
		Configuration configuration = Configuration.defaults();

		boolean result = configuration.equals(configuration);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenFieldHandlersAreDifferent() {
		Configuration configuration1 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);
		Configuration configuration2 = Configuration.of(
				Collections.emptyList(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenStrategiesAreDifferent() {
		Configuration configuration1 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);
		Configuration configuration2 = Configuration.of(
				DefaultFieldHandlers.list(),
				Collections.emptyList(),
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenExcludedFieldsAreDifferent() {
		Configuration configuration1 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);
		Configuration configuration2 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				ExcludedFields.excludeAll(),
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenExpandableFieldsAreDifferent() {
		Configuration configuration1 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);
		Configuration configuration2 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				ExpandableFields.of("bubu"),
				Default.SIMPLE_CONVERTERS);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenSimpleConvertersAreDifferent() {
		Configuration configuration1 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);

		SimpleConverters simpleConverters = SimpleConverters.of(String::valueOf);
		Configuration configuration2 = Configuration.of(
				DefaultFieldHandlers.list(),
				DefaultStrategies.STRATEGIES_LIST,
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				simpleConverters);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenGenericTypesMapsAreDifferent() {
		Configuration configuration1 = Configuration.of(
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS,
				Class.class);
		Configuration configuration2 = Configuration.of(
				Default.EXCLUDED_FIELDS,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS,
				String.class);

		boolean result = configuration1.equals(configuration2);

		assertFalse(result);
	}

}
