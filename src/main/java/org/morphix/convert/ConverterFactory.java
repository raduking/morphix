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

import java.util.List;
import java.util.Map;

import org.morphix.convert.handler.AnyToAny;
import org.morphix.convert.handler.AnyToAnyFromConversionMethod;
import org.morphix.convert.handler.ArrayToArray;
import org.morphix.convert.handler.ArrayToIterable;
import org.morphix.convert.handler.ExcludedFieldHandler;
import org.morphix.convert.handler.ExpandableFieldHandler;
import org.morphix.convert.handler.IterableToArray;
import org.morphix.convert.handler.IterableToIterable;
import org.morphix.convert.handler.MapToMap;

/**
 * Convenience static methods for creating new converters.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConverterFactory {

	/**
	 * Field handler constructor with configuration functional interface.
	 *
	 * @param <T> field handler type
	 */
	@FunctionalInterface
	interface ConfigurationConstructor<T extends FieldHandler> {

		/**
		 * Returns a field handler with the given configuration.
		 *
		 * @param configuration conversion configuration
		 * @return field handler instance
		 */
		T instance(Configuration configuration);
	}

	/**
	 * Instance functions constants.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class Constants {

		/**
		 * Field handlers to put into configuration before the default ones.
		 */
		protected static final List<ConfigurationConstructor<FieldHandler>> FIELD_HANDLERS_BEFORE_DEFAULT = List.of(
				ExcludedFieldHandler::new,
				ExpandableFieldHandler::new,
				AnyToAnyFromConversionMethod::new,
				IterableToIterable::new,
				ArrayToArray::new,
				IterableToArray::new,
				ArrayToIterable::new,
				MapToMap::new);

		/**
		 * Field handlers to put into configuration after the default ones.
		 */
		protected static final List<ConfigurationConstructor<FieldHandler>> FIELD_HANDLERS_AFTER_DEFAULT = List.of(
				AnyToAny::new);

		/**
		 * Private constructor.
		 */
		private Constants() {
			// empty
		}
	}

	/**
	 * Convenience static method which returns a new {@link ObjectConverter} which can handle conversions with a supplied
	 * conversion method and expandable fields.
	 *
	 * @param <S> source type
	 * @param <D> destination type
	 *
	 * @param configuration conversion configuration
	 * @return a new {@link ObjectConverter}
	 */
	static <S, D> ObjectConverter<S, D> newObjectConverter(final Configuration configuration) {
		return new ObjectConverter<>(configuration);
	}

	/**
	 * Convenience static method for creating map conversions.
	 *
	 * @param <V> map key type
	 * @param <D> destination type
	 *
	 * @return a new {@link ObjectConverter} for map conversions
	 */
	static <V, D> ObjectConverter<Map<String, V>, D> newMapObjectConverter() {
		return ConverterFactory.newObjectConverter(Configuration.defaultConfiguration());
	}
}
