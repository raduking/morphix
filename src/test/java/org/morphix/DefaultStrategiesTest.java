package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.morphix.DefaultStrategies.STRATEGY_BASIC_NAME;
import static org.morphix.DefaultStrategies.STRATEGY_NAME_PATH;
import static org.morphix.DefaultStrategies.STRATEGY_PATH;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.MemberAccessor;
import org.morphix.strategy.Strategy;

/**
 * Test class for {@link DefaultStrategies}.
 *
 * @author Radu Sebastian LAZIN
 */
class DefaultStrategiesTest {

	static final Strategy[] STRATEGIES = {
			STRATEGY_BASIC_NAME,
			STRATEGY_PATH,
			STRATEGY_NAME_PATH
	};

	@Test
	void shouldKeepTheHandlersOrder() {
		for (int i = 0; i < STRATEGIES.length; ++i) {
			assertThat(STRATEGIES[i], equalTo(DefaultStrategies.STRATEGIES_CHAIN[i]));
		}
	}

	@Test
	void shouldHavePrivateConstructor() throws Exception {
		Constructor<DefaultStrategies> constructor = DefaultStrategies.class.getDeclaredConstructor();
		assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));

		try (MemberAccessor<Constructor<DefaultStrategies>> ignored = new MemberAccessor<>(null, constructor)) {
			DefaultStrategies defaultStrategies = constructor.newInstance();
			assertNotNull(defaultStrategies);
		}
	}

}
