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
import java.util.concurrent.ConcurrentHashMap;

import org.morphix.convert.context.ConversionContext;
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
	 * Constructs a new {@code PropertyConversionEngine} with the specified list of strategies.
	 *
	 * @param strategies the list of property conversion strategies to use
	 */
	public PropertyConversionEngine(final List<? extends PropertyConversionStrategy> strategies) {
		this.strategies = strategies;
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
		throw new IllegalStateException("No property conversion strategy found for type: " + type);
	}

	/**
	 * The default instance is lazily initialized when the getDefault() method is called for the first time. This approach
	 * ensures thread safety and avoids unnecessary initialization if the default instance is never used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * The default instance of {@code PropertyConversionEngine} initialized with a predefined set of strategies. The
		 * strategies are ordered to ensure that more specific strategies (like leaf and optional) are applied before more
		 * general ones (like collections and maps).
		 */
		private static final PropertyConversionEngine DEFAULT = new PropertyConversionEngine(List.of(
				new PropertyLeafStrategy(),
				new PropertyOptionalStrategy(),
				new PropertyMapStrategy(),
				new PropertyCollectionStrategy(),
				new PropertyArrayStrategy(),
				new PropertyBeanStrategy()));
	}
}
