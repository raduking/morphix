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

import org.morphix.convert.strategy.BasicNameStrategy;
import org.morphix.convert.strategy.ConversionStrategy;
import org.morphix.convert.strategy.FieldNameMapStrategy;
import org.morphix.convert.strategy.NamePathStrategy;
import org.morphix.convert.strategy.PathStrategy;
import org.morphix.reflection.Constructors;

/**
 * Scope class for instantiating the default strategies and the default strategy order.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DefaultStrategies {

	/**
	 * Basic name to name conversion strategy.
	 */
	static final ConversionStrategy STRATEGY_BASIC_NAME = new BasicNameStrategy();

	/**
	 * Strategy that searches fields in the given path to match.
	 */
	static final ConversionStrategy STRATEGY_PATH = new PathStrategy();

	/**
	 * Strategy that searches fields in the given path with the given name to match.
	 */
	static final ConversionStrategy STRATEGY_NAME_PATH = new NamePathStrategy();

	/**
	 * Strategy that searches fields given a field name map.
	 */
	static final ConversionStrategy STRATEGY_FIELD_NAME_MAP = new FieldNameMapStrategy();

	/**
	 * The default strategy chain as array.
	 */
	static final ConversionStrategy[] STRATEGIES_CHAIN = {
			STRATEGY_BASIC_NAME,
			STRATEGY_FIELD_NAME_MAP,
			STRATEGY_PATH,
			STRATEGY_NAME_PATH
	};

	/**
	 * The default strategy chain as {@link List}.
	 */
	static final List<ConversionStrategy> STRATEGIES_LIST = List.of(STRATEGIES_CHAIN);

	/**
	 * Returns the default strategies list.
	 *
	 * @return the default strategies list
	 */
	static List<ConversionStrategy> list() {
		return STRATEGIES_LIST;
	}

	/**
	 * Private constructor.
	 */
	private DefaultStrategies() {
		throw Constructors.unsupportedOperationException();
	}

}
