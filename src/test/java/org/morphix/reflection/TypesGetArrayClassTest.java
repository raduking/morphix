package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Types#getArrayClass(ParameterizedType)}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypesGetArrayClassTest {

	public static class ParameterizedTypeImpl implements ParameterizedType {

		@Override
		public Type[] getActualTypeArguments() {
			return null;
		}

		@Override
		public Type getRawType() {
			return new TypeImpl();
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

	}

	public static class TypeImpl implements Type {
		// empty
	}

	@Test
	void shouldReturnNullIfRawTypeIsNotAClass() {
		ParameterizedType parameterizedType = new ParameterizedTypeImpl();

		Class<?> cls = Types.getArrayClass(parameterizedType);

		assertThat(cls, nullValue());
	}

}
