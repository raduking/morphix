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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.context.CyclicReferencesContext;
import org.morphix.convert.strategy.PropertyArrayStrategy;
import org.morphix.convert.strategy.PropertyBeanStrategy;
import org.morphix.convert.strategy.PropertyCollectionStrategy;
import org.morphix.convert.strategy.PropertyLeafStrategy;
import org.morphix.convert.strategy.PropertyMapStrategy;
import org.morphix.convert.strategy.PropertyOptionalStrategy;

/**
 * Test class for {@link PropertyConversionEngine}.
 *
 * @author Radu Sebastian LAZIN
 */
class PropertyConversionEngineTest {

	private static final String TEST = "test";

	@Test
	void shouldThrowIllegalStateExceptionWhenNoStrategiesAreFound() {
		PropertyConversionEngine engine = new PropertyConversionEngine(List.of());

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> engine.convert(TEST, new CyclicReferencesContext()));

		assertThat(e.getMessage(), equalTo("No property conversion strategy found for type: " + String.class.getName()));
	}

	@Test
	void shouldHaveCorrectDefaultStrategyOrder() {
		PropertyConversionEngine engine = PropertyConversionEngine.getDefault();

		assertThat(engine.getStrategies().get(0).getClass(), equalTo(PropertyLeafStrategy.class));
		assertThat(engine.getStrategies().get(1).getClass(), equalTo(PropertyOptionalStrategy.class));
		assertThat(engine.getStrategies().get(2).getClass(), equalTo(PropertyMapStrategy.class));
		assertThat(engine.getStrategies().get(3).getClass(), equalTo(PropertyCollectionStrategy.class));
		assertThat(engine.getStrategies().get(4).getClass(), equalTo(PropertyArrayStrategy.class));
		assertThat(engine.getStrategies().get(5).getClass(), equalTo(PropertyBeanStrategy.class));
		assertThat(engine.getStrategies().size(), equalTo(6));
	}
}
