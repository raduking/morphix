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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.convert.DefaultStrategies.STRATEGY_BASIC_NAME;
import static org.morphix.convert.DefaultStrategies.STRATEGY_NAME_PATH;
import static org.morphix.convert.DefaultStrategies.STRATEGY_PATH;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.morphix.convert.strategy.ConversionStrategy;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link DefaultStrategies}.
 *
 * @author Radu Sebastian LAZIN
 */
class DefaultStrategiesTest {

	static final ConversionStrategy[] STRATEGIES = {
			STRATEGY_BASIC_NAME,
			STRATEGY_PATH,
			STRATEGY_NAME_PATH
	};

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<DefaultStrategies> defaultConstructor = DefaultStrategies.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<DefaultStrategies>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldKeepTheHandlersOrder() {
		for (int i = 0; i < STRATEGIES.length; ++i) {
			assertThat(STRATEGIES[i], equalTo(DefaultStrategies.STRATEGIES_CHAIN[i]));
		}
	}

	@Test
	void shouldReturnTheStrategyList() {
		for (int i = 0; i < STRATEGIES.length; ++i) {
			assertThat(STRATEGIES[i], equalTo(DefaultStrategies.list().get(i)));
		}
	}

}
