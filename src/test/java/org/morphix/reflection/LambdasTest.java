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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.morphix.reflection.predicates.MethodPredicates.isMethodWith;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.convert.function.SimpleConverter;

/**
 * Test class for {@link Lambdas}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class LambdasTest {

	@Mock
	private ConstantPool<String> constantPool;

	public static class A {

		public A a;

		public void foo() {
			// empty
		}

	}

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<Lambdas> defaultConstructor = Lambdas.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<Lambdas>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldReturnNullIfConstantPoolHasMethodsWithNoArgs() throws Exception {
		doReturn(1).when(constantPool).getSize();
		doReturn(A.class.getDeclaredMethod("foo")).when(constantPool).getMemberAt(anyInt());
		doReturn(B.class).when(constantPool).getTargetClass();
		doReturn(new ConstantPoolIterator<>(constantPool)).when(constantPool).iterator();

		Method result = Lambdas.getLambdaMethod(constantPool, isMethodWith(Class.class, Class.class));

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfConstantPoolHasFields() throws Exception {
		doReturn(1).when(constantPool).getSize();
		doReturn(A.class.getDeclaredField("a")).when(constantPool).getMemberAt(anyInt());
		doReturn(B.class).when(constantPool).getTargetClass();
		doReturn(new ConstantPoolIterator<>(constantPool)).when(constantPool).iterator();

		Method result = Lambdas.getLambdaMethod(constantPool, isMethodWith(Class.class, Class.class));

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfConstantPoolHasANullMember() {
		doReturn(1).when(constantPool).getSize();
		doReturn(null).when(constantPool).getMemberAt(anyInt());
		doReturn(new ConstantPoolIterator<>(constantPool)).when(constantPool).iterator();

		Method result = Lambdas.getLambdaMethod(constantPool, isMethodWith(Class.class, Class.class));

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullIfConstantPoolHasSameClassMethod() throws Exception {
		doReturn(1).when(constantPool).getSize();
		doReturn(A.class.getDeclaredMethod("foo")).when(constantPool).getMemberAt(anyInt());
		doReturn(A.class).when(constantPool).getTargetClass();
		doReturn(new ConstantPoolIterator<>(constantPool)).when(constantPool).iterator();

		Method result = Lambdas.getLambdaMethod(constantPool, isMethodWith(Class.class, Class.class));

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnMethodIfConstantPoolHasMethodWithPredicate() throws Exception {
		Method expected = A.class.getDeclaredMethod("foo");

		doReturn(1).when(constantPool).getSize();
		doReturn(expected).when(constantPool).getMemberAt(anyInt());
		doReturn(B.class).when(constantPool).getTargetClass();
		doReturn(new ConstantPoolIterator<>(constantPool)).when(constantPool).iterator();

		Method result = Lambdas.getLambdaMethod(constantPool, method -> true);

		assertThat(result, equalTo(expected));
	}

	public static class B {

		@SuppressWarnings("unused")
		public void foo1(final B b1, final B b2) {
			// empty
		}

		public C foo2(final C c) {
			return c;
		}

	}

	public static class C {
		public C foo(final C c) {
			return c;
		}
	}

	public static class D {

		public C foo3() {
			return null;
		}

		public B foo4(@SuppressWarnings("unused") final C c) {
			return null;
		}

	}

	@Test
	void shouldReturnTrueForLambdaWithCorrectParams() {
		SimpleConverter<String, ?> converter = (final String src) -> {
			return "";
		};
		boolean isLambda = Lambdas.isLambdaWithParams(converter, String.class, String.class);

		assertThat(isLambda, equalTo(true));
	}

	@Test
	void shouldReturnFalseForNonLambda() {
		boolean isLambda = Lambdas.isLambdaWithParams(new Object(), String.class, String.class);

		assertThat(isLambda, equalTo(false));
	}

	public interface NonSerializedLambda {
		void foo();
	}

	@Test
	void shouldReturnFalseForNonSerializableLambda() {
		NonSerializedLambda lambda = () -> {
			// empty
		};

		boolean isLambda = Lambdas.isLambdaWithParams(lambda, String.class, String.class);

		assertThat(isLambda, equalTo(false));
	}

	@Test
	void shouldReturnEmptyListIfSerializedLambdaCannotAccessImplClass() {
		SerializedLambda serializedLambda =
				new SerializedLambda(null, null, null, null, 0, "test name", null, null, null, new Object[] { });

		List<Method> methods = Lambdas.getLambdaDeclaredMethods(serializedLambda);

		assertThat(methods, hasSize(0));
	}

	@Test
	void shouldReturnNullIfLambdaHasNoMethods() {
		SerializedLambda serializedLambda =
				new SerializedLambda(null, null, null, null, 0, "test name", null, null, null, new Object[] { });

		Method method = Lambdas.getLambdaMethod(serializedLambda, Objects::nonNull);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullIfLambdaDoesNotMatchThePredicate() {
		SimpleConverter<String, ?> converter = (final String src) -> {
			return "";
		};
		SerializedLambda serializedLambda = Lambdas.getSerializedLambda(converter);

		Method method = Lambdas.getLambdaMethod(serializedLambda, Objects::isNull);

		assertThat(method, equalTo(null));
	}

	@Test
	void shouldReturnNullSerializedLambdaWhenClassIsNull() {
		SimpleConverter<String, ?> converter = (final String src) -> {
			return "";
		};

		SerializedLambda serializedLambda = Lambdas.getSerializedLambda(converter, null);

		assertThat(serializedLambda, equalTo(null));
	}

	@Test
	void shouldReturnNullSerializedLambdaWhenLambdaIsNotSerializableNull() {
		Predicate<String> predicate = s -> {
			return false;
		};

		SerializedLambda serializedLambda = Lambdas.getSerializedLambda(predicate, Predicate.class);

		assertThat(serializedLambda, equalTo(null));
	}

	@FunctionalInterface
	interface WriteReplace {
		String writeReplace();
	}

	@Test
	void shouldReturnNullSerializedLambdaWhenLambdaIsSerializedAndDefinesTheSerializedMethod() {
		WriteReplace foo = () -> "foo";

		SerializedLambda serializedLambda = Lambdas.getSerializedLambda(foo, WriteReplace.class);

		assertThat(serializedLambda, equalTo(null));
	}

	@FunctionalInterface
	public interface WriteReplaceSerialized extends Serializable {
		String writeReplacex();
	}

	@Test
	void shouldReturnNonNullSerializedLambdaWhenLambdaDefinesTheSerializedMethod() {
		SimpleConverter<String, ?> converter = (final String src) -> {
			return "";
		};

		SerializedLambda serializedLambda = Lambdas.getSerializedLambda(converter, converter.getClass());

		assertThat(serializedLambda, notNullValue());

		WriteReplaceSerialized foo = () -> "foo";

		serializedLambda = Lambdas.getSerializedLambda(foo, foo.getClass());

		assertThat(serializedLambda, notNullValue());
		assertThat(serializedLambda.getFunctionalInterfaceMethodName(), equalTo("writeReplacex"));
	}

	@Test
	void shouldReturnSerializationMethodName() {
		assertThat(Lambdas.SERIALIZATION_METHOD_NAME, equalTo("writeReplace"));
	}

	/**
	 * Constant Pool iterator.
	 *
	 * @param <T> type of the class for which this constant pool refers to
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class ConstantPoolIterator<T> implements Iterator<Member> {

		private final ConstantPool<T> constantPool;
		private final int size;
		private int index;

		public ConstantPoolIterator(final ConstantPool<T> constantPool) {
			this.constantPool = constantPool;
			this.size = constantPool.getSize();
			this.index = this.size - 1;
		}

		@Override
		public boolean hasNext() {
			return 0 <= index && index < size;
		}

		@Override
		public Member next() {
			Member result = constantPool.getMemberAt(index);
			if (nextIndex() < -1) {
				throw new NoSuchElementException();
			}
			return result;
		}

		int nextIndex() {
			return --index;
		}
	}
}
