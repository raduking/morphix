package org.morphix;

import java.util.List;

import org.morphix.strategy.BasicNameStrategy;
import org.morphix.strategy.FieldNameMapStrategy;
import org.morphix.strategy.NamePathStrategy;
import org.morphix.strategy.PathStrategy;
import org.morphix.strategy.Strategy;

/**
 * Scope class for instantiating the default strategies and the default strategy
 * order.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DefaultStrategies {

	/**
	 * Strategies.
	 */
	static final Strategy STRATEGY_BASIC_NAME = new BasicNameStrategy();
	static final Strategy STRATEGY_PATH = new PathStrategy();
	static final Strategy STRATEGY_NAME_PATH = new NamePathStrategy();
	static final Strategy STRATEGY_FIELD_NAME_MAP = new FieldNameMapStrategy();

	static final Strategy[] STRATEGIES_CHAIN = {
			STRATEGY_BASIC_NAME,
			STRATEGY_PATH,
			STRATEGY_NAME_PATH
	};

	static final List<Strategy> STRATEGIES_LIST = List.of(STRATEGIES_CHAIN);

	/**
	 * Private constructor.
	 */
	private DefaultStrategies() {
		// empty
	}

}
