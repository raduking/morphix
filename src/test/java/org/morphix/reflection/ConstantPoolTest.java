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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.morphix.lang.function.InstanceFunction.instanceFunction;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.morphix.reflection.ConstantPool.ConstantPoolIterator;

/**
 * Test class for {@link ConstantPool}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstantPoolTest {

	@Mock
	private ConstantPoolAccessor constantPoolAccessor;

	@Mock
	private Object constantPoolObject;

	private ConstantPool<String> constantPool;

	private AutoCloseable mocks;

	@BeforeEach
	void setUp() {
		mocks = openMocks(this);

		doReturn(constantPoolObject).when(constantPoolAccessor).getConstantPool(String.class);
		constantPool = new ConstantPool<>(String.class, instanceFunction(constantPoolAccessor));
		Fields.IgnoreAccess.set(constantPool, "constantPoolObject", constantPoolObject);
	}

	@AfterEach
	void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	void shouldInitializeAllMembers() {
		ConstantPool<String> cp = new ConstantPool<>(String.class, instanceFunction(constantPoolAccessor));

		assertNotNull(Fields.IgnoreAccess.get(cp, "constantPoolObject"));
		assertNotNull(Fields.IgnoreAccess.get(cp, "constantPoolAccessor"));
	}

	@Test
	void shouldDelegateCallsToConstantPoolAccessor() {
		constantPool.getSize();
		verify(constantPoolAccessor).getSize(constantPoolObject);

		constantPool.getMemberAt(0);
		verify(constantPoolAccessor).getMethodAt(constantPoolObject, 0);
	}

	@Test
	void shouldThrowExceptionWhenGettingNextOnNoElementsInPool() {
		doReturn(0).when(constantPoolAccessor).getSize(constantPool);

		Iterator<Member> iterator = constantPool.iterator();
		assertThrows(NoSuchElementException.class, iterator::next);
	}

	@Test
	void shouldReturnMemberOnNextInPool() throws Exception {
		Method expected = ConstantPoolTest.class.getDeclaredMethod("shouldReturnMemberOnNextInPool");
		doReturn(1).when(constantPoolAccessor).getSize(constantPoolObject);
		doReturn(expected).when(constantPoolAccessor).getMethodAt(constantPoolObject, 0);

		Iterator<Member> iterator = constantPool.iterator();
		Method result = (Method) iterator.next();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldDecrementIndexOnNextElement() {
		ConstantPoolIterator<Member> iterator = (ConstantPoolIterator<Member>) constantPool.iterator();
		int index = iterator.nextIndex();
		int nextIndex = iterator.nextIndex();

		assertThat(nextIndex, lessThan(index));
	}

	private static Stream<Arguments> provideIndexesTestParameters() {
		return Stream.of(
				Arguments.of(1, -1, false),
				Arguments.of(1, 2, false),
				Arguments.of(1, 1, false),
				Arguments.of(2, 1, true));
	}

	@ParameterizedTest
	@MethodSource("provideIndexesTestParameters")
	void shouldVerifyAllIteratorIndexes(final int size, final int index, final boolean hasNext) {
		ConstantPoolIterator<Member> iterator = (ConstantPoolIterator<Member>) constantPool.iterator();
		Fields.IgnoreAccess.set(iterator, "size", size);
		Fields.IgnoreAccess.set(iterator, "index", index);

		assertThat(iterator.hasNext(), equalTo(hasNext));
	}

	@Test
	void shouldReturnCorrectTargetClass() {
		ConstantPool<String> cp = new ConstantPool<>(String.class, instanceFunction(constantPoolAccessor));

		Class<?> result = cp.getTargetClass();

		assertThat(result, equalTo(String.class));
	}

	@Test
	void shouldCallProtectedConstructor() {
		Supplier<ConstantPoolAccessor> saveInstanceFn = Fields.IgnoreAccess.getStatic(ConstantPool.class, "instanceFunction");
		Class<?> result;
		try {
			Fields.IgnoreAccess.setStatic(ConstantPool.class, "instanceFunction", instanceFunction(constantPoolAccessor));
			ConstantPool<String> cp = new ConstantPool<>(String.class);
			result = cp.getTargetClass();
		} finally {
			Fields.IgnoreAccess.setStatic(ConstantPool.class, "instanceFunction", saveInstanceFn);
		}
		assertThat(result, equalTo(String.class));
	}

	@Test
	void shouldCallProtectedConstructorWithInstanceFunctionSetter() {
		Supplier<ConstantPoolAccessor> saveInstanceFn = Fields.IgnoreAccess.getStatic(ConstantPool.class, "instanceFunction");
		Class<?> result;
		try {
			ConstantPool.setInstanceFunction(instanceFunction(constantPoolAccessor));
			ConstantPool<String> cp = new ConstantPool<>(String.class);
			result = cp.getTargetClass();
		} finally {
			ConstantPool.setInstanceFunction(saveInstanceFn);
		}
		assertThat(result, equalTo(String.class));
	}
}
