package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParameterizedTypeImpl}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterizedTypeImplTest {

	@Test
	void shouldSetAllFields() {
		ParameterizedType type = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);

		assertThat(type.getRawType(), equalTo(String.class));
		assertThat(type.getActualTypeArguments(), equalTo(new Type[] { Integer.class }));
		assertThat(type.getOwnerType(), equalTo(Long.class));
	}

	@Test
	void shouldReturnTrueOnEqualsWhenAllFieldsAreEqual() {
		ParameterizedType type1 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);

		boolean result = type1.equals(type2);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueOnEqualsForTheSameInstance() {
		ParameterizedType type = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);

		boolean result = type.equals(type);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenRawTypeIsNotEqual() {
		ParameterizedType type1 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = ParameterizedTypeImpl.make(Integer.class, new Type[] { Integer.class }, Long.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenRawTypeActualArgumentTypeIsNotEqual() {
		ParameterizedType type1 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = ParameterizedTypeImpl.make(String.class, new Type[] { Long.class }, Long.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenOwnerTypeIsNotEqual() {
		ParameterizedType type1 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);
		ParameterizedType type2 = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, String.class);

		boolean result = type1.equals(type2);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseOnEqualsWhenOtherIsNotParameterizedType() {
		ParameterizedType type = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);
		Type other = Object.class;

		boolean result = type.equals(other);

		assertFalse(result);
	}

	@Test
	void shouldHaveTheSameHashCodeAsRawType() {
		ParameterizedType type = ParameterizedTypeImpl.make(String.class, new Type[] { Integer.class }, Long.class);

		assertThat(type.hashCode(), equalTo(String.class.hashCode()));
	}

}
