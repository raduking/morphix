package org.morphix.reflection.predicates;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.reflection.predicates.MethodPredicates.isGetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.MemberAccessor;
import org.morphix.reflection.Methods;

/**
 * Test class for {@link MethodPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodPredicatesTest {

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<MethodPredicates> defaultConstructor = MethodPredicates.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<MethodPredicates>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo("This class shouldn't be instantiated."));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldReturnTrueForGetter() {
		Field field = Fields.getDeclaredFieldInHierarchy(A.class, "value");

		Predicate<Method> predicate = isGetter(field);
		Method method = Methods.getSafeDeclaredMethodInHierarchy("getValue", A.class);

		assertTrue(predicate.test(method));
	}

	@Test
	void shouldReturnTrueForGetterWithBaseClass() {
		Field field = Fields.getDeclaredFieldInHierarchy(A.class, "type");

		Predicate<Method> predicate = isGetter(field);
		Method method = Methods.getSafeDeclaredMethodInHierarchy("getType", A.class);

		assertTrue(predicate.test(method));
	}

	@Test
	void shouldReturnFalseForGetterWithWrongReturnType() {
		Field field = Fields.getDeclaredFieldInHierarchy(A.class, "i");

		Predicate<Method> predicate = isGetter(field);
		Method method = Methods.getSafeDeclaredMethodInHierarchy("getI", A.class);

		assertFalse(predicate.test(method));
	}

	public static class A {

		private String value;
		private DerivedType type;
		private Integer i;

		public String getValue() {
			return value;
		}

		public BaseType getType() {
			return type;
		}

		public String getI() {
			return String.valueOf(i);
		}

	}

	public static class BaseType {
		// empty
	}

	public static class DerivedType extends BaseType {
		// empty
	}

}
