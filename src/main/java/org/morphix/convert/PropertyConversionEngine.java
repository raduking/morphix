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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.morphix.convert.context.ConversionContext;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.convert.strategy.PropertyArrayStrategy;
import org.morphix.convert.strategy.PropertyBeanStrategy;
import org.morphix.convert.strategy.PropertyCollectionStrategy;
import org.morphix.convert.strategy.PropertyConversionStrategy;
import org.morphix.convert.strategy.PropertyLeafStrategy;
import org.morphix.convert.strategy.PropertyMapStrategy;
import org.morphix.convert.strategy.PropertyOptionalStrategy;

/**
 * A {@link ConversionEngine} implementation that uses a list of {@link PropertyConversionStrategy} instances to convert
 * property values. The engine iterates through the strategies and applies the first one that supports the given value.
 *
 * @author Radu Sebastian LAZIN
 */
public class PropertyConversionEngine implements ConversionEngine {

	/**
	 * A cache to store the resolved property conversion strategy for each type. This improves performance by avoiding
	 * repeated resolution of strategies for the same types.
	 */
	private final Map<Class<?>, PropertyConversionStrategy> strategyCache = new ConcurrentHashMap<>();

	/**
	 * The list of property conversion strategies used by this engine.
	 */
	private final List<? extends PropertyConversionStrategy> strategies;

	/**
	 * A simple converter to convert property names. This can be used by strategies that need to convert property names as
	 * part of their conversion process. The converter can be customized to apply specific naming conventions or
	 * transformations to property names.
	 */
	private final SimpleConverter<String, String> propertyNameConverter;

	/**
	 * Constructs a new {@code PropertyConversionEngine} with the specified list of strategies.
	 *
	 * @param strategies the list of property conversion strategies to use
	 */
	public PropertyConversionEngine(final List<? extends PropertyConversionStrategy> strategies) {
		this(strategies, getDefaultPropertyNameConverter());
	}

	/**
	 * Constructs a new {@code PropertyConversionEngine} with the specified list of strategies.
	 *
	 * @param strategies the list of property conversion strategies to use
	 * @param propertyNameConverter a simple converter to convert property names, used by strategies that need to convert
	 */
	public PropertyConversionEngine(final List<? extends PropertyConversionStrategy> strategies,
			final SimpleConverter<String, String> propertyNameConverter) {
		this.strategies = Objects.requireNonNull(strategies, "Strategies list cannot be null");
		this.propertyNameConverter = Objects.requireNonNull(propertyNameConverter, "Property name converter cannot be null");
	}

	/**
	 * Converts the given value using the first supported strategy from the list. If no strategy supports the value, an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param value the value to convert
	 * @param ctx the conversion context
	 * @return the converted value, or {@code null} if the input value is {@code null}
	 * @throws IllegalStateException if no property conversion strategy supports the given value
	 */
	@Override
	public Object convert(final Object value, final ConversionContext ctx) {
		if (null == value) {
			return null;
		}
		Class<?> type = value.getClass();
		PropertyConversionStrategy strategy = strategyCache.computeIfAbsent(type, this::resolve);
		return strategy.convert(value, this, ctx);
	}

	/**
	 * Returns the default instance of {@code PropertyConversionEngine} with a predefined set of strategies. The default
	 * instance includes strategies for leaf properties, beans, collections, maps, arrays and optionals.
	 *
	 * @return the default instance of {@code PropertyConversionEngine}
	 */
	public static PropertyConversionEngine getDefault() {
		return InstanceHolder.DEFAULT;
	}

	/**
	 * Returns the default list of property conversion strategies used by the default instance of
	 * {@code PropertyConversionEngine}.
	 *
	 * @return the default list of property conversion strategies
	 */
	public static List<PropertyConversionStrategy> getDefaultStrategies() {
		return InstanceHolder.DEFAULT_STRATEGIES;
	}

	/**
	 * Returns the default property name converter used by the default instance of {@code PropertyConversionEngine}. The
	 * default converter simply returns the input key as is, without applying any transformations. This can be used when no
	 * custom property name converter is provided by the conversion engine.
	 *
	 * @return the default property name converter
	 */
	public static SimpleConverter<String, String> getDefaultPropertyNameConverter() {
		return InstanceHolder.DEFAULT_PROPERTY_NAME_CONVERTER;
	}

	/**
	 * Resolves the appropriate property conversion strategy for the given type by iterating through the list of strategies
	 * and checking if each one supports the type. If a supporting strategy is found, it is returned. If no strategy
	 * supports the type, an {@link IllegalStateException} is thrown.
	 *
	 * @param type the class type for which to resolve a property conversion strategy
	 * @return the resolved property conversion strategy for the given type
	 * @throws IllegalStateException if no property conversion strategy supports the given type
	 */
	private PropertyConversionStrategy resolve(final Class<?> type) {
		for (var strategy : strategies) {
			if (strategy.supportsType(type)) {
				return strategy;
			}
		}
		throw new IllegalStateException("No property conversion strategy found for type: " + type.getName());
	}

	/**
	 * Returns the list of property conversion strategies used by this engine.
	 *
	 * @return the list of property conversion strategies used by this engine
	 */
	public List<? extends PropertyConversionStrategy> getStrategies() {
		return strategies;
	}

	/**
	 * Returns the simple converter used for converting property names. This converter can be used by strategies that need
	 * to convert property names as part of their conversion process. The converter can be customized to apply specific
	 * naming conventions or transformations to property names.
	 *
	 * @return the simple converter used for converting property names
	 */
	public SimpleConverter<String, String> getPropertyNameConverter() {
		return propertyNameConverter;
	}

	/**
	 * The default instance is lazily initialized when the getDefault() method is called for the first time. This approach
	 * ensures thread safety and avoids unnecessary initialization if the default instance is never used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * A default property name converter that simply returns the input key as is. This can be used when no custom property
		 * name converter is provided by the conversion engine.
		 */
		private static final SimpleConverter<String, String> DEFAULT_PROPERTY_NAME_CONVERTER = k -> k;

		/**
		 * The default list of property conversion strategies used by the default instance of {@code PropertyConversionEngine}.
		 * The strategies are ordered to ensure that more specific strategies (like leaf and optional) are applied before more
		 * general ones (like collections and maps). This order is important to ensure that the most appropriate strategy is
		 * applied for each type of property value.
		 */
		private static final List<PropertyConversionStrategy> DEFAULT_STRATEGIES = List.of(
				new PropertyLeafStrategy(),
				new PropertyOptionalStrategy(),
				new PropertyMapStrategy(),
				new PropertyCollectionStrategy(),
				new PropertyArrayStrategy(),
				new PropertyBeanStrategy());

		/**
		 * The default instance of {@code PropertyConversionEngine} initialized with a predefined set of strategies. The
		 * strategies are ordered to ensure that more specific strategies (like leaf and optional) are applied before more
		 * general ones (like collections and maps).
		 */
		private static final PropertyConversionEngine DEFAULT = new PropertyConversionEngine(DEFAULT_STRATEGIES);
	}
}
