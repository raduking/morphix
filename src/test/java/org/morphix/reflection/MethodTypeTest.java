package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link MethodType}.
 *
 * @author Radu Sebastian LAZIN
 */
class MethodTypeTest {

	public static class A {
		public String x;
	}

	@Test
	void shouldBuildProperMethodName() {
		Field field = Fields.getDeclaredFieldInHierarchy(A.class, "x");
		String result = MethodType.GETTER.getMethodName(field);

		assertThat(result, equalTo("getX"));
	}

	@Test
	void shouldReturnSuffixOnToString() {
		assertThat(MethodType.GETTER.toString(), equalTo(MethodType.GETTER.getDefaultPrefix()));
	}

	@Test
	void shouldKeepValues() {
		MethodType[] methodTypes = { MethodType.GETTER, MethodType.SETTER };

		assertThat(MethodType.values(), equalTo(methodTypes));
	}

	@Test
	void shouldBuildFromString() {
		MethodType getter = MethodType.valueOf("GETTER");

		assertThat(getter, notNullValue());
	}

	public static class B {
		public Boolean b1;
		public boolean b2;
	}

	@Test
	void shouldReturnIsForBooleanClass() throws Exception {
		String getterName = MethodType.GETTER.getMethodName(B.class.getDeclaredField("b1"));

		assertThat(getterName, equalTo("isB1"));
	}

	@Test
	void shouldReturnIsForBooleanPrimitive() throws Exception {
		String getterName = MethodType.GETTER.getMethodName(B.class.getDeclaredField("b2"));

		assertThat(getterName, equalTo("isB2"));
	}

	@Test
	void shouldReturnCorrectValuesOnTypeConventionGetters() {
		MethodType.TypePrefixConvention<String> tsc = new MethodType.TypePrefixConvention<>(String.class, "of");

		assertThat(tsc.getPrefix(), equalTo("of"));
		assertThat(tsc.getType(), equalTo(String.class));
	}

	@Test
	void shouldIgnoreNullPrefixOnGetFieldName() {
		String fieldName = MethodType.getFieldName(null, "x");

		assertThat(fieldName, equalTo(null));
	}

	@Test
	void shouldIgnoreNullMethodNameOnGetFieldName() {
		String fieldName = MethodType.getFieldName("x", null);

		assertThat(fieldName, equalTo(null));
	}

	@Test
	void shouldIgnoreBadPrefixOnGetFieldName() {
		String fieldName = MethodType.getFieldName("x", "y");

		assertThat(fieldName, equalTo(null));
	}

	@Test
	void shouldIgnoreSamePrefixAndMethodNameOnGetFieldName() {
		String fieldName = MethodType.getFieldName("x", "x");

		assertThat(fieldName, equalTo(null));
	}

	@Test
	void shouldIgnoreNullPrefixOnGetMethodName() {
		String methodName = MethodType.getMethodName(null, "x");

		assertThat(methodName, equalTo(null));
	}

	@Test
	void shouldIgnoreNullFieldNameOnGetMethodName() {
		String methodName = MethodType.getMethodName("x", (String) null);

		assertThat(methodName, equalTo(null));
	}

	@Test
	void shouldIgnoreNullFieldNameOnGetMethodNameTypes() {
		for (MethodType type : MethodType.values()) {
			String methodName = type.getMethodName((String) null);
			assertThat(methodName, equalTo(null));
		}
	}

	@Test
	void shouldIgnoreNullFieldNameOnGetMethodNameTypesField() {
		for (MethodType type : MethodType.values()) {
			String methodName = type.getMethodName((Field) null);
			assertThat(methodName, equalTo(null));
		}
	}

	@Test
	void shouldAlwaysReturnDefaultPrefixWithoutField() {
		for (MethodType type : MethodType.values()) {
			String methodName = type.getPrefix((Field) null);
			assertThat(methodName, equalTo(type.getDefaultPrefix()));
		}
	}

	@Test
	void shouldAlwaysReturnDefaultPrefixWithoutType() {
		for (MethodType type : MethodType.values()) {
			String methodName = type.getPrefix((Class<?>) null);
			assertThat(methodName, equalTo(type.getDefaultPrefix()));
		}
	}

	@Test
	void shouldReturnCorrectParameterCountOnGetter() {
		assertThat(MethodType.GETTER.getParameterCount(), equalTo(0));
	}

	@Test
	void shouldReturnCorrectParameterCountOnSetter() {
		assertThat(MethodType.SETTER.getParameterCount(), equalTo(1));
	}

	@Test
	void shouldReturnCorrectDefaultPrefixOnGetter() {
		assertThat(MethodType.GETTER.getDefaultPrefix(), equalTo("get"));
	}

	@Test
	void shouldReturnCorrectDefaultPrefixOnSetter() {
		assertThat(MethodType.SETTER.getDefaultPrefix(), equalTo("set"));
	}

	public static class C {
		public String getC() {
			return null;
		}

		public Boolean isX() {
			return null;
		}

		public String popX() {
			return null;
		}

		public String getY(@SuppressWarnings("unused") final int x) {
			return null;
		}
	}

	private static Stream<Arguments> provideValuesForGetterParameterizedTest() {
		return Stream.of(
				Arguments.of("getC", "c"),
				Arguments.of("isX", "x"),
				Arguments.of("popX", null)
		);
	}

	@ParameterizedTest
	@MethodSource("provideValuesForGetterParameterizedTest")
	void shouldReturnCorrectFieldNameBasedOnMethod(final String methodName, final String fieldName) throws Exception {
		Method method = C.class.getDeclaredMethod(methodName);

		String result = MethodType.GETTER.getFieldName(method);

		assertThat(result, equalTo(fieldName));
	}

	@Test
	void shouldReturnTrueForGetterPredicate() throws Exception {
		Method method = C.class.getDeclaredMethod("getC");

		boolean isGetter = MethodType.GETTER.getPredicate().test(method);

		assertTrue(isGetter);
	}

	@Test
	void shouldReturnTrueForIsGetterPredicate() throws Exception {
		Method method = C.class.getDeclaredMethod("isX");

		boolean isGetter = MethodType.GETTER.getPredicate().test(method);

		assertTrue(isGetter);
	}

	@Test
	void shouldReturnFalseForGetterPredicateOnNonGetter() throws Exception {
		Method method = C.class.getDeclaredMethod("popX");

		boolean isGetter = MethodType.GETTER.getPredicate().test(method);

		assertFalse(isGetter);
	}

	@Test
	void shouldReturnFalseForGetterPredicateOnNonGetterWithParameters() throws Exception {
		Method method = C.class.getDeclaredMethod("getY", int.class);

		boolean isGetter = MethodType.GETTER.getPredicate().test(method);

		assertFalse(isGetter);
	}

}
