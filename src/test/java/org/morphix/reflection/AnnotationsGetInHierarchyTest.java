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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Annotations#getInHierarchy(Method, Class)}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnnotationsGetInHierarchyTest {

	private static final String GET_VALUE_METHOD_NAME = "getValue";

	private static final String OWN = "own";
	private static final String SUPER = "super";
	private static final String IFACE = "iface";
	private static final String ROOT = "root";
	private static final String COVARIANT = "covariant";
	private static final String LEFT = "left";
	private static final String TOP = "top";
	private static final String RETURN_VALUE = "returnValue";

	@Retention(RetentionPolicy.RUNTIME)
	@interface TestAnnotation {
		String value();
	}

	static class WithOwnAnnotation {

		@TestAnnotation(OWN)
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	static class BaseWithAnnotation {

		@TestAnnotation(SUPER)
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Repeatable(RepeatableTestAnnotations.class)
	@interface RepeatableTestAnnotation {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface RepeatableTestAnnotations {
		RepeatableTestAnnotation[] value();
	}

	static class WithRepeatableAnnotations {

		@RepeatableTestAnnotation(OWN)
		@RepeatableTestAnnotation(SUPER)
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	static class OverridesRepeatableAnnotations extends BaseWithAnnotation {

		@RepeatableTestAnnotation(OWN)
		@RepeatableTestAnnotation(SUPER)
		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	static class DerivedWithoutAnnotation extends BaseWithAnnotation {

		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	interface WithAnnotation {

		@TestAnnotation(IFACE)
		String getValue();
	}

	static class ImplementsInterfaceWithoutAnnotation implements WithAnnotation {

		@Override
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	interface CovariantWithAnnotation {

		@TestAnnotation(COVARIANT)
		Object getValue();
	}

	static class ImplementsCovariantInterface implements CovariantWithAnnotation {

		@Override
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	interface RootWithAnnotation {

		@TestAnnotation(ROOT)
		Object getValue();
	}

	interface MiddleWithoutAnnotation extends RootWithAnnotation {

		@Override
		Object getValue();
	}

	static class ExtendsBaseAndImplementsInterface extends BaseWithAnnotation implements WithAnnotation {

		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	interface CommonWithAnnotation {

		@TestAnnotation(LEFT)
		String getValue();
	}

	interface LeftWithAnnotation extends CommonWithAnnotation {
		// empty
	}

	interface RightWithAnnotation extends CommonWithAnnotation {
		// empty
	}

	static class Diamond implements LeftWithAnnotation, RightWithAnnotation {

		@Override
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	static class TopWithAnnotation {

		@TestAnnotation(TOP)
		public String getValue() {
			return RETURN_VALUE;
		}
	}

	static class PlainMiddle extends TopWithAnnotation {

		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	static class PlainBottom extends PlainMiddle {

		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	static class BaseWithoutAnnotation {

		public String getValue() {
			return RETURN_VALUE;
		}
	}

	static class DerivedFromBaseWithoutAnnotation extends BaseWithoutAnnotation implements WithAnnotation {

		@Override
		public String getValue() {
			return super.getValue();
		}
	}

	static class Unrelated {

		public String getValue() {
			return RETURN_VALUE;
		}
	}

	@Test
	void shouldReturnTheAnnotationOnTheMethodItself() {
		Method method = nonBridgeMethod(WithOwnAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(OWN));
	}

	@Test
	void shouldReturnTheAnnotationFromTheSuperclassDeclaration() {
		Method method = nonBridgeMethod(DerivedWithoutAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(SUPER));
	}

	@Test
	void shouldReturnTheAnnotationFromTheInterfaceDeclaration() {
		Method method = nonBridgeMethod(ImplementsInterfaceWithoutAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(IFACE));
	}

	@Test
	void shouldReturnTheAnnotationFromTheCovariantInterfaceDeclaration() {
		Method method = nonBridgeMethod(ImplementsCovariantInterface.class, GET_VALUE_METHOD_NAME);

		assertThat(method.getReturnType(), equalTo(String.class));
		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(COVARIANT));
	}

	@Test
	void shouldReturnTheAnnotationFromTheSuperinterfaceOfAnInterfaceDeclaration() {
		Method method = nonBridgeMethod(MiddleWithoutAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(ROOT));
	}

	@Test
	void shouldPreferTheSuperclassDeclarationOverTheInterfaceOne() {
		Method method = nonBridgeMethod(ExtendsBaseAndImplementsInterface.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(SUPER));
	}

	@Test
	void shouldReturnTheAnnotationFromASuperinterfaceSharedByDiamondInterfaces() {
		Method method = nonBridgeMethod(Diamond.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(LEFT));
	}

	@Test
	void shouldKeepSearchingWhenAnIntermediateSuperclassHasNoAnnotation() {
		Method method = nonBridgeMethod(PlainBottom.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(TOP));
	}

	@Test
	void shouldFallBackToTheInterfaceWhenNoSuperclassCarriesTheAnnotation() {
		Method method = nonBridgeMethod(DerivedFromBaseWithoutAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(IFACE));
	}

	@Test
	void shouldReturnNullWhenNoDeclarationCarriesTheAnnotation() {
		Method method = nonBridgeMethod(Unrelated.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheMethodIsNotDeclaredInTheHierarchy() {
		Method method = nonBridgeMethod(WithOwnAnnotation.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, Deprecated.class), nullValue());
	}

	@Test
	void shouldNotSeeRepeatableAnnotationsStoredInTheirContainer() {
		Method method = nonBridgeMethod(WithRepeatableAnnotations.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, RepeatableTestAnnotation.class), nullValue());
	}

	@Test
	void shouldFallBackToTheHierarchyWhenOnlyTheMethodCarriesRepeatableAnnotations() {
		Method method = nonBridgeMethod(OverridesRepeatableAnnotations.class, GET_VALUE_METHOD_NAME);

		assertThat(Annotations.getInHierarchy(method, TestAnnotation.class).value(), equalTo(SUPER));
	}

	private static Method nonBridgeMethod(final Class<?> cls, final String name) {
		return Arrays.stream(cls.getDeclaredMethods())
				.filter(candidate -> name.equals(candidate.getName()) && !candidate.isBridge())
				.findFirst()
				.orElseThrow();
	}
}
