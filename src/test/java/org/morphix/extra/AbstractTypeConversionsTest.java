package org.morphix.extra;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link ParameterizedTypeConversions}.
 *
 * @author Radu Sebastian LAZIN
 */
class AbstractTypeConversionsTest {

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<ParameterizedTypeConversions> defaultConstructor = ParameterizedTypeConversions.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<ParameterizedTypeConversions>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo("This class shouldn't be instantiated."));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

}
