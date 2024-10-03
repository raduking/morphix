package org.morphix;

import java.util.List;
import java.util.Map;

import org.morphix.handler.AnyToAny;
import org.morphix.handler.AnyToAnyFromConversionMethod;
import org.morphix.handler.ArrayToArray;
import org.morphix.handler.ArrayToIterable;
import org.morphix.handler.ExcludedFieldHandler;
import org.morphix.handler.ExpandableFieldHandler;
import org.morphix.handler.IterableToArray;
import org.morphix.handler.IterableToIterable;
import org.morphix.handler.MapToMap;

/**
 * Convenience static methods for creating new Generic Converters.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConverterBuilder {

	@FunctionalInterface
	interface ConfigurationConstructor<T extends FieldHandler> {
		T instance(Configuration configuration);
	}

	/**
	 * Instance functions constants.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class Constants {

		protected static final List<ConfigurationConstructor<FieldHandler>> FIELD_HANDLERS_BEFORE_DEFAULT = List.of(
				ExcludedFieldHandler::new,
				ExpandableFieldHandler::new,
				AnyToAnyFromConversionMethod::new,
				IterableToIterable::new,
				ArrayToArray::new,
				IterableToArray::new,
				ArrayToIterable::new,
				MapToMap::new);

		protected static final List<ConfigurationConstructor<FieldHandler>> FIELD_HANDLERS_AFTER_DEFAULT = List.of(
				AnyToAny::new);

		private Constants() {
			// empty
		}
	}

	/**
	 * Convenience static method which returns a new {@link Converter}
	 * which can handle conversions with a supplied conversion method and
	 * expandable fields.
	 *
	 * @return a new {@link Converter}
	 */
	static <S, D> Converter<S, D> newConverter(final Configuration configuration) {
		return new Converter<>(configuration);
	}

	/**
	 * Convenience static method for creating map conversions.
	 *
	 * @return a new {@link Converter} for map conversions
	 */
	static <V, D> Converter<Map<String, V>, D> newMapConverter() {
		Configuration configuration = Configuration.of(
				List.of(DefaultFieldHandlers.FIELD_HANDLER_ANY_FROM_MAP),
				List.of(DefaultStrategies.STRATEGY_FIELD_NAME_MAP),
				Configuration.Default.EXCLUDED_FIELDS,
				Configuration.Default.EXPANDABLE_FIELDS,
				Configuration.Default.SIMPLE_CONVERTERS);
		return new Converter<>(configuration);
	}
}
