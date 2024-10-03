package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.morphix.function.InstanceFunction.instanceFunction;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.morphix.function.InstanceFunction;
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
		Fields.setIgnoreAccess(constantPool, "constantPoolObject", constantPoolObject);
	}

	@AfterEach
	void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	void shouldInitializeAllMembers() {
		ConstantPool<String> constantPool = new ConstantPool<>(String.class, instanceFunction(constantPoolAccessor));

		assertNotNull(Fields.getIgnoreAccess(constantPool, "constantPoolObject"));
		assertNotNull(Fields.getIgnoreAccess(constantPool, "constantPoolAccessor"));
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
				Arguments.of(2, 1, true)
		);
	}

	@ParameterizedTest
	@MethodSource("provideIndexesTestParameters")
	void shouldVerifyAllIteratorIndexes(final int size, final int index, final boolean hasNext) {
		ConstantPoolIterator<Member> iterator = (ConstantPoolIterator<Member>) constantPool.iterator();
		Fields.setIgnoreAccess(iterator, "size", size);
		Fields.setIgnoreAccess(iterator, "index", index);

		assertThat(iterator.hasNext(), equalTo(hasNext));
	}

	@Test
	void shouldReturnCorrectTargetClass() {
		ConstantPool<String> constantPool = new ConstantPool<>(String.class, instanceFunction(constantPoolAccessor));

		Class<?> result = constantPool.getTargetClass();

		assertThat(result, equalTo(String.class));
	}

	@Test
	void shouldCallProtectedConstructor() {
		InstanceFunction<ConstantPoolAccessor> saveInstanceFn = Fields.getStaticIgnoreAccess(ConstantPool.class, "instanceFunction");
		Class<?> result;
		try {
			Fields.setStaticIgnoreAccess(ConstantPool.class, "instanceFunction", instanceFunction(constantPoolAccessor));
			ConstantPool<String> constantPool = new ConstantPool<>(String.class);
			result = constantPool.getTargetClass();
		} finally {
			Fields.setStaticIgnoreAccess(ConstantPool.class, "instanceFunction", saveInstanceFn);
		}
		assertThat(result, equalTo(String.class));
	}

	@Test
	void shouldCallProtectedConstructorWithInstanceFunctionSetter() {
		InstanceFunction<ConstantPoolAccessor> saveInstanceFn = Fields.getStaticIgnoreAccess(ConstantPool.class, "instanceFunction");
		Class<?> result;
		try {
			ConstantPool.setInstanceFunction(instanceFunction(constantPoolAccessor));
			ConstantPool<String> constantPool = new ConstantPool<>(String.class);
			result = constantPool.getTargetClass();
		} finally {
			ConstantPool.setInstanceFunction(saveInstanceFn);
		}
		assertThat(result, equalTo(String.class));
	}
}
