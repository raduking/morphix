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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.morphix.convert.ConverterFactory.Constants;
import org.morphix.convert.extras.ExcludedFields;
import org.morphix.convert.extras.ExpandableFields;
import org.morphix.convert.extras.SimpleConverters;
import org.morphix.convert.strategy.FieldFinderStrategy;

/**
 * Configuration class for conversions.
 * <p>
 * The default configuration is used when no parameterized types are converted so no extra instantiation of field
 * handlers are needed.
 * <p>
 * TODO: add builder so that we can build a configuration with any parameters and the ones that weren't supplied will
 * fallback to the defaults
 * <p>
 * TODO: don't build a new configuration object for default configurations
 *
 * @author Radu Sebastian LAZIN
 */
public final class Configuration {

	/**
	 * List of field handlers.
	 */
	private final List<FieldHandler> fieldHandlers;

	/**
	 * List of name finding strategies.
	 */
	private final List<FieldFinderStrategy> strategies;

	/**
	 * Excluded fields object.
	 */
	private final ExcludedFields excludedFields;

	/**
	 * Expandable fields object.
	 */
	private final ExpandableFields expandableFields;

	/**
	 * Simple converters object.
	 */
	private final SimpleConverters simpleConverters;

	/**
	 * Generic types map.
	 */
	private final Map<String, Type> genericTypesMap;

	/**
	 * Private constructor with all parameters.
	 *
	 * @param fieldHandlers list of field handlers
	 * @param strategies name finding strategies
	 * @param excludedFields excluded fields object
	 * @param expandableFields expandable fields object
	 * @param simpleConverters simple converters object
	 * @param genericTypesMap generic types map
	 */
	private Configuration(
			final List<FieldHandler> fieldHandlers,
			final List<FieldFinderStrategy> strategies,
			final ExcludedFields excludedFields,
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters,
			final Map<String, Type> genericTypesMap) {
		Objects.requireNonNull(fieldHandlers);
		this.strategies = Collections.unmodifiableList(Objects.requireNonNull(strategies));
		this.excludedFields = Objects.requireNonNull(excludedFields);
		this.expandableFields = Objects.requireNonNull(expandableFields);
		this.simpleConverters = Objects.requireNonNull(simpleConverters);
		this.genericTypesMap = genericTypesMap;

		this.fieldHandlers = isCustom() ? initCustom(fieldHandlers) : initDefault(fieldHandlers);
	}

	/**
	 * Private constructor without generic types map.
	 *
	 * @param fieldHandlers list of field handlers
	 * @param strategies name finding strategies
	 * @param excludedFields excluded fields object
	 * @param expandableFields expandable fields object
	 * @param simpleConverters simple converters object
	 */
	private Configuration(
			final List<FieldHandler> fieldHandlers,
			final List<FieldFinderStrategy> strategies,
			final ExcludedFields excludedFields,
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters) {
		this(fieldHandlers, strategies, excludedFields, expandableFields, simpleConverters, new HashMap<>());
	}

	/**
	 * Returns true if this is a custom configuration.
	 *
	 * @return true if this is a custom configuration
	 */
	private boolean isCustom() {
		return !Default.EXCLUDED_FIELDS.equals(excludedFields) ||
				!Default.EXPANDABLE_FIELDS.equals(expandableFields) ||
				!Default.SIMPLE_CONVERTERS.equals(simpleConverters) ||
				!genericTypesMap.isEmpty();
	}

	/**
	 * Initializes default field handlers.
	 *
	 * @param fieldHandlers field handlers
	 * @return initialized default field handlers
	 */
	private static List<FieldHandler> initDefault(final List<FieldHandler> fieldHandlers) {
		final List<FieldHandler> newFieldHandlers = new LinkedList<>(fieldHandlers);
		newFieldHandlers.add(DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY);
		return Collections.unmodifiableList(newFieldHandlers);
	}

	/**
	 * Initializes custom field handlers.
	 *
	 * @param fieldHandlers field handlers
	 * @return initialized custom field handlers
	 */
	private List<FieldHandler> initCustom(final List<FieldHandler> fieldHandlers) {
		final List<FieldHandler> newFieldHandlers = new LinkedList<>();
		Constants.FIELD_HANDLERS_BEFORE_DEFAULT.forEach(handler -> newFieldHandlers.add(handler.instance(this)));
		newFieldHandlers.addAll(fieldHandlers);
		Constants.FIELD_HANDLERS_AFTER_DEFAULT.forEach(handler -> newFieldHandlers.add(handler.instance(this)));
		return Collections.unmodifiableList(newFieldHandlers);
	}

	/**
	 * Returns the excluded fields object from the configuration.
	 *
	 * @return the excluded fields object from the configuration
	 */
	public ExcludedFields getExcludedFields() {
		return excludedFields;
	}

	/**
	 * Returns the expandable fields object from the configuration.
	 *
	 * @return the expandable fields object from the configuration
	 */
	public ExpandableFields getExpandableFields() {
		return expandableFields;
	}

	/**
	 * Returns the simple converters object from the configuration.
	 *
	 * @return the simple converters object from the configuration
	 */
	public SimpleConverters getSimpleConverters() {
		return simpleConverters;
	}

	/**
	 * Returns the field handlers list from the configuration.
	 *
	 * @return the field handlers list from the configuration
	 */
	public List<FieldHandler> getFieldHandlers() {
		return fieldHandlers;
	}

	/**
	 * Returns the strategies list.
	 *
	 * @return the strategies list
	 */
	public List<FieldFinderStrategy> getStrategies() {
		return strategies;
	}

	/**
	 * Returns true if this is a default configuration.
	 *
	 * @return true if this is a default configuration
	 */
	public boolean isDefault() {
		return equals(Default.CONFIGURATION);
	}

	/**
	 * Builds a configuration object with the given parameters.
	 *
	 * @param fieldHandlers list of field handlers
	 * @param strategies field finding strategies
	 * @param excludedFields excluded fields
	 * @param expandableFields expandable fields
	 * @param simpleConverters simple converters
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final List<FieldHandler> fieldHandlers,
			final List<FieldFinderStrategy> strategies,
			final ExcludedFields excludedFields,
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters) {
		Configuration configuration =
				new Configuration(fieldHandlers, strategies, excludedFields, expandableFields, simpleConverters);
		return configuration.isDefault() ? Configuration.defaults() : configuration;
	}

	/**
	 * Builds a configuration object with the given parameters.
	 *
	 * @param excludedFields excluded fields
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final ExcludedFields excludedFields) {
		return of(
				Default.FIELD_HANDLERS,
				Default.STRATEGIES,
				excludedFields,
				Default.EXPANDABLE_FIELDS,
				Default.SIMPLE_CONVERTERS);
	}

	/**
	 * Builds a configuration object with the given parameters.
	 *
	 * @param excludedFields excluded fields
	 * @param expandableFields expandable fields
	 * @param simpleConverters simple converters
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final ExcludedFields excludedFields,
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters) {
		return of(
				Default.FIELD_HANDLERS,
				Default.STRATEGIES,
				excludedFields,
				expandableFields,
				simpleConverters);
	}

	/**
	 * Builds a configuration object with the given parameters. When a type is provided the configuration will never be a
	 * default one.
	 *
	 * @param excludedFields excluded fields
	 * @param expandableFields expandable fields
	 * @param simpleConverters simple converters
	 * @param type type to convert to
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final ExcludedFields excludedFields,
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters,
			final Type type) {
		Map<String, Type> genericTypesMap = new HashMap<>();
		genericTypesMap.put(type.getTypeName(), type);
		return new Configuration(
				Default.FIELD_HANDLERS,
				Default.STRATEGIES,
				excludedFields,
				expandableFields,
				simpleConverters,
				genericTypesMap);
	}

	/**
	 * Builds a configuration object with the given parameters.
	 *
	 * @param expandableFields expandable fields
	 * @param simpleConverters simple converters
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final ExpandableFields expandableFields,
			final SimpleConverters simpleConverters) {
		return of(Default.EXCLUDED_FIELDS, expandableFields, simpleConverters);
	}

	/**
	 * Builds a configuration object with the given parameters.
	 *
	 * @param expandableFieldNames expandable field names
	 * @param simpleConverters simple converters
	 * @return a configuration object with the given parameters
	 */
	public static Configuration of(
			final List<String> expandableFieldNames,
			final SimpleConverters simpleConverters) {
		return of(ExpandableFields.of(expandableFieldNames), simpleConverters);
	}

	/**
	 * Returns a default configuration object.
	 *
	 * @return a default configuration object
	 */
	public static Configuration defaults() {
		return Default.CONFIGURATION;
	}

	/**
	 * Returns a copy of the given configuration with the new parameterized type.
	 *
	 * @param parameterizedType parameterized type to add to generic types map
	 * @param original original configuration
	 * @return new configuration
	 */
	public static Configuration copyWith(final ParameterizedType parameterizedType, final Configuration original) {
		Map<String, Type> genericTypesMap = new HashMap<>(original.genericTypesMap);
		genericTypesMap.put(parameterizedType.getTypeName(), parameterizedType);
		return new Configuration(
				original.fieldHandlers,
				original.strategies,
				original.excludedFields,
				original.expandableFields,
				original.simpleConverters,
				genericTypesMap);
	}

	/**
	 * see {@link Object#equals(Object)}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Configuration configuration = (Configuration) obj;
		return Objects.equals(fieldHandlers, configuration.fieldHandlers) &&
				Objects.equals(strategies, configuration.strategies) &&
				Objects.equals(excludedFields, configuration.excludedFields) &&
				Objects.equals(expandableFields, configuration.expandableFields) &&
				Objects.equals(simpleConverters, configuration.simpleConverters) &&
				Objects.equals(genericTypesMap, configuration.genericTypesMap);
	}

	/**
	 * see {@link Object#hashCode()}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fieldHandlers, strategies, excludedFields, expandableFields, simpleConverters, genericTypesMap);
	}

	/**
	 * Returns the generic type corresponding to the given type name.
	 *
	 * @param typeName type name
	 * @return the generic type
	 */
	public Type getGenericType(final String typeName) {
		return genericTypesMap.get(typeName);
	}

	/**
	 * Returns the generic type corresponding to the given type name.
	 *
	 * @param typeName type name
	 * @param type type
	 * @return the generic type
	 */
	public Type putGenericType(final String typeName, final Type type) {
		return genericTypesMap.put(typeName, type);
	}

	/**
	 * Returns the generic types map.
	 *
	 * @return the generic types map
	 */
	public Map<String, Type> getGenericTypesMap() {
		return genericTypesMap;
	}

	/**
	 * Default configuration instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default excluded fields.
		 */
		public static final ExcludedFields EXCLUDED_FIELDS = ExcludedFields.excludeNone();

		/**
		 * Default expandable fields.
		 */
		public static final ExpandableFields EXPANDABLE_FIELDS = ExpandableFields.expandAll();

		/**
		 * Default simple converters.
		 */
		public static final SimpleConverters SIMPLE_CONVERTERS = SimpleConverters.empty();

		/**
		 * Default field handlers.
		 */
		private static final List<FieldHandler> FIELD_HANDLERS = DefaultFieldHandlers.list();

		/**
		 * Default strategies.
		 */
		private static final List<FieldFinderStrategy> STRATEGIES = DefaultStrategies.STRATEGIES_LIST;

		/**
		 * Default configuration.
		 */
		private static final Configuration CONFIGURATION = new Configuration(
				FIELD_HANDLERS,
				STRATEGIES,
				EXCLUDED_FIELDS,
				EXPANDABLE_FIELDS,
				SIMPLE_CONVERTERS,
				Collections.emptyMap());

		/**
		 * Private default constructor.
		 */
		private Default() {
			// empty
		}
	}

}
