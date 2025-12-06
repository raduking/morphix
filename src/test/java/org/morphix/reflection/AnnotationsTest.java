/*
 * Copyright 2025 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Annotations}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnnotationsTest {

	@Retention(RetentionPolicy.RUNTIME)
	@interface TestAnnotation {
		String value();
	}

	@interface NonRuntimeAnnotation {
		String name();
	}

	static class A {

		@Deprecated(since = "0.9", forRemoval = true)
		void oldMethod() {
			// empty method
		}

		@TestAnnotation(value = "originalValue")
		void foo() {
			// empty
		}

		@NonRuntimeAnnotation(name = "test")
		void bar() {
			// empty
		}
	}

	@Test
	void shouldOverrideAnnotationValue() {
		Method method = Methods.Safe.getOneDeclared("oldMethod", A.class);
		Deprecated deprecated = method.getAnnotation(Deprecated.class);

		Annotations.overrideValue(deprecated, "since", "1.0");
		deprecated = method.getAnnotation(Deprecated.class);

		assertNotNull(deprecated);
		assertEquals("1.0", deprecated.since());
	}

	@Test
	void shouldOverrideLocalAnnotationValue() {
		Method method = Methods.Safe.getOneDeclared("foo", A.class);
		TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);

		Annotations.overrideValue(annotation, "value", "newValue");
		annotation = method.getAnnotation(TestAnnotation.class);

		assertNotNull(annotation);
		assertEquals("newValue", annotation.value());
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldThrowExceptionIfCannotAddValuesToMemberValuesMap() {
		Method method = Methods.Safe.getOneDeclared("foo", A.class);
		TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);

		RuntimeException expectedCause = new RuntimeException("Map put failed");

		Map<String, Object> throwingMap = mock(Map.class);
		doThrow(expectedCause).when(throwingMap).put(anyString(), any());

		String attributeName = "value";
		ReflectionException exception = null;
		try (MockedStatic<Fields.IgnoreAccess> mockedStatic = mockStatic(Fields.IgnoreAccess.class)) {
			mockedStatic.when(() -> Fields.IgnoreAccess.get(any(), eq("memberValues"))).thenReturn(throwingMap);

			exception = assertThrows(ReflectionException.class, () -> Annotations.overrideValue(annotation, attributeName, "newValue"));
		}

		assertEquals("Failed to override annotation: " + annotation.annotationType().getCanonicalName() + "." + attributeName + "() value.",
				exception.getMessage());
		assertSame(expectedCause, exception.getCause());
	}

	@Test
	void shouldThrowExceptionIfAnnotationIsNotRuntime() {
		Method method = Methods.Safe.getOneDeclared("bar", A.class);
		NonRuntimeAnnotation annotation = method.getAnnotation(NonRuntimeAnnotation.class);

		ReflectionException exception = assertThrows(ReflectionException.class,
				() -> Annotations.overrideValue(annotation, "name", "newName"));

		assertThat(exception.getMessage(), equalTo("Failed to override annotation: annotation instance is null (possibly not retained at runtime)."));
	}

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Annotations.class);

		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

}
