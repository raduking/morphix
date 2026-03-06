package org.morphix.convert.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.morphix.convert.PropertyConversionEngine;
import org.morphix.convert.function.SimpleConverter;

/**
 * Test class for {@link PropertyBeanStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class PropertyBeanStrategyTest {

	@Test
	void shouldReturnDefaultIfConversionEngineIsNotPropertyConversionEngine() {
		SimpleConverter<String, String> propertyNameConverter = PropertyBeanStrategy.getPropertyNameConverter(null);

		assertThat(propertyNameConverter, notNullValue());
		assertThat(propertyNameConverter, equalTo(PropertyConversionEngine.getDefaultPropertyNameConverter()));
	}
}
