package org.morphix.reflection.predicates;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.reflection.predicates.Predicates.not;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.morphix.Conversion;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link TypePredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypePredicatesTest {

	@Test
	void shouldTestCollectionsPredicates() {
		boolean result = TypePredicates.isQueue().test(Queue.class);
		assertThat(result, equalTo(true));

		result = TypePredicates.isSet().test(Set.class);
		assertThat(result, equalTo(true));
	}

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<TypePredicates> defaultConstructor = TypePredicates.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<TypePredicates>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo("This class shouldn't be instantiated."));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldCombinePredicatesForArrays() {
		Type type = Integer[].class;
		boolean result = TypePredicates.isArray().test(type);

		assertThat(result, equalTo(true));

		type = Integer.class;
		result = TypePredicates.isArray().test(type);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldCombinePredicatesForCharSequence() {
		Type type = String.class;
		boolean result = TypePredicates.isCharSequence().test(type);

		assertThat(result, equalTo(true));

		type = Integer.class;
		result = TypePredicates.isCharSequence().test(type);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldCombinePredicatesForSrc() {
		Type type = Conversion.class;
		Predicate<Type> predicate = TypePredicates.isAClassAnd(not(ClassPredicates.isA(Conversion.class)));
		boolean result = predicate.test(type);

		assertThat(result, equalTo(false));

		predicate = TypePredicates.isAClass();
		result = predicate.test(type);

		assertThat(result, equalTo(true));

		type = Type.class;
		result = predicate.test(type);

		assertThat(result, equalTo(true));
	}

}
