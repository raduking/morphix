package org.morphix.reflection.predicates;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.reflection.predicates.MemberPredicates.hasName;
import static org.morphix.reflection.predicates.MemberPredicates.nameIn;
import static org.morphix.reflection.predicates.MemberPredicates.withAllModifiers;
import static org.morphix.reflection.predicates.MemberPredicates.withAnyModifiers;
import static org.morphix.reflection.predicates.MemberPredicates.withModifiers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.MemberAccessor;
import org.morphix.reflection.Methods;

/**
 * Test class for {@link MemberPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class MemberPredicatesTest {

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<MemberPredicates> defaultConstructor = MemberPredicates.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<MemberPredicates>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo("This class shouldn't be instantiated."));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldReturnTheCorrectAnyModifiersPredicate() {
		Method staticMethod = Methods.getSafeDeclaredMethodInHierarchy("fooStatic", A.class);

		boolean result = withAnyModifiers(Modifier::isStatic, Modifier::isPrivate).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectAllModifiersPredicate() {
		Method staticMethod = Methods.getSafeDeclaredMethodInHierarchy("fooStatic", A.class);

		boolean result = withAllModifiers(Modifier::isStatic, Modifier::isPublic).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectAllModifiersPredicateAlias() {
		Method staticMethod = Methods.getSafeDeclaredMethodInHierarchy("fooStatic", A.class);

		boolean result = withModifiers(Modifier::isStatic, Modifier::isPublic).test(staticMethod);

		assertTrue(result);
	}

	@Test
	void shouldReturnTheCorrectMethodsForPublicPredicate() {
		List<Method> methods = Methods.getDeclaredMethodsInHierarchy(A.class, withAllModifiers(Modifier::isStatic, Modifier::isPublic));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("fooStatic"));
	}

	@Test
	void shouldReturnTheCorrectMethodsForNamePredicate() {
		List<Method> methods = Methods.getDeclaredMethodsInHierarchy(A.class, hasName("foo"));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("foo"));
	}

	@Test
	void shouldReturnMethodsWithNames() {
		List<Method> methods = Methods.getDeclaredMethodsInHierarchy(A.class, nameIn(List.of("foo")));

		assertThat(methods, hasSize(1));
		assertThat(methods.get(0).getName(), equalTo("foo"));
	}

	public static class A {

		public static void fooStatic() {
			// empty
		}

		public void foo() {
			// empty
		}

	}

}
