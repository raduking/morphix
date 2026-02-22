package org.morphix.lang.thread;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.logging.LogManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.utils.lang.thread.TenantContextHolder;

/**
 * Test class for {@link StackedContextHolder}.
 *
 * @author Radu Sebastian LAZIN
 */
class StackedContextHolderTest {

	private static final String PROPERTY_JAVA_UTIL_LOGGING_CONFIG_FILE = "java.util.logging.config.file";

	private static final String TENANT_ID = "bubu";
	private static final String TEST_STRING = "testString";

	@BeforeAll
	static void beforeAll() throws SecurityException, IOException {
		// Set the system property to point to your test logging config
		System.setProperty(PROPERTY_JAVA_UTIL_LOGGING_CONFIG_FILE,
				"src/test/resources/test-logging.properties");

		// Force LogManager to reinitialize with the new property
		LogManager.getLogManager().readConfiguration();
	}

	@Test
	void shouldCallRemoveOnThreadLocal() {
		String result = DummyTenantContextHolder.onTenant(TENANT_ID, () -> TEST_STRING);

		assertThat(result, equalTo(TEST_STRING));
		assertThat(DummyTenantContextHolder.stack.removeCalls, equalTo(1));
	}

	@Test
	void shouldSetTenantIdWithStatements() {
		String result = TenantContextHolder.onTenant(TENANT_ID, () -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
			return TEST_STRING;
		});

		assertThat(result, equalTo(TEST_STRING));
	}

	@Test
	void shouldSetTenantIdWithFunction() {
		String result = TenantContextHolder.onTenant(TENANT_ID, value -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
			return value;
		}, TEST_STRING);

		assertThat(result, equalTo(TEST_STRING));
	}

	@Test
	void shouldSetTenantIdWithRunnable() {
		TenantContextHolder.onTenant(TENANT_ID, () -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
		});
	}

	@Test
	void shouldChangeTenantId() {
		TenantContextHolder.onTenant(TENANT_ID, () -> {
			TenantContextHolder.changeTenantId(TENANT_ID + "Changed");
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID + "Changed"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "\t", "\n" })
	void shouldHandleWrongTenantId(final String wrongTenantId) {
		ThreadContextException e = assertThrows(ThreadContextException.class, () -> TenantContextHolder.onTenant(wrongTenantId, () -> {
			// do nothing
		}));

		assertThat(e.getMessage(), equalTo("Tenant cannot be: " + wrongTenantId));
	}

	@Test
	void shouldHandleWrongTenantIdOnRecursiveCalls() {
		String wrongTenantId = " ";
		Executable executable = () -> DummyTenantContextHolder.onTenant(TENANT_ID, () -> {
			DummyTenantContextHolder.onTenant(TENANT_ID + "Nested", () -> {
				DummyTenantContextHolder.onTenant(wrongTenantId, () -> {
					// do nothing
				});
			});
		});
		ThreadContextException e = assertThrows(ThreadContextException.class, executable);

		assertThat(e.getMessage(), equalTo("Tenant cannot be: " + wrongTenantId));

		assertThat(DummyTenantContextHolder.INSTANCE.dummyStack.operations, equalTo(List.of(
				"push: " + TENANT_ID,
				"push: " + TENANT_ID + "Nested",
				"pop: " + TENANT_ID + "Nested",
				"pop: " + TENANT_ID)));
	}

	@Test
	void shouldChangeTenantIdAndCleanStack() {
		DummyTenantContextHolder.onTenant(TENANT_ID, () -> {
			DummyTenantContextHolder.changeTenantId(TENANT_ID + "Changed");
			assertThat(DummyTenantContextHolder.getTenantId(), equalTo(TENANT_ID + "Changed"));
		});

		assertThat(DummyTenantContextHolder.INSTANCE.dummyStack.operations, equalTo(List.of(
				"push: " + TENANT_ID,
				"change: " + TENANT_ID + "Changed",
				"pop: " + TENANT_ID + "Changed")));
	}

	@Test
	void shouldHandleChangeTenantIdWithWrongTenantId() {
		String wrongTenantId = " ";
		Executable executable = () -> DummyTenantContextHolder.onTenant(TENANT_ID, () -> {
			DummyTenantContextHolder.changeTenantId(wrongTenantId);
		});
		ThreadContextException e = assertThrows(ThreadContextException.class, executable);

		assertThat(e.getMessage(), equalTo("Tenant cannot be: " + wrongTenantId));

		assertThat(DummyTenantContextHolder.INSTANCE.dummyStack.operations, equalTo(List.of(
				"push: " + TENANT_ID,
				"pop: " + TENANT_ID)));
	}

	@Test
	void shouldReturnNullTenantIdWhenStackIsEmpty() {
		assertThat(TenantContextHolder.getTenantId(), equalTo(null));
	}

	@Test
	void shouldNotChangeTenantIdWhenStackIsEmpty() {
		assertDoesNotThrow(() -> DummyTenantContextHolder.changeTenantId(TENANT_ID));
		assertThat(DummyTenantContextHolder.getTenantId(), equalTo(null));
		assertDoesNotThrow(() -> TenantContextHolder.changeTenantId(TENANT_ID));
		assertThat(TenantContextHolder.getTenantId(), equalTo(null));
	}

	/**
	 * Dummy Context holder example.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class DummyTenantContextHolder extends TenantContextHolder {

		private static final DummyThreadLocal<Stack<String>> stack = new DummyThreadLocal<>();

		private static final DummyTenantContextHolder INSTANCE = new DummyTenantContextHolder();

		private DummyStack<String> dummyStack;

		public static <T> T onTenant(final String tenantId, final Supplier<T> statements) {
			return on(tenantId, INSTANCE, statements::get);
		}

		public static void onTenant(final String tenantId, final Runnable statements) {
			on(tenantId, INSTANCE, statements::run);
		}

		public static void changeTenantId(final String tenantId) {
			INSTANCE.changeElement(tenantId);
		}

		public static String getTenantId() {
			return INSTANCE.getElement();
		}

		@Override
		public ThreadLocal<Stack<String>> getThreadStack() {
			return stack;
		}

		@Override
		protected Stack<String> newStack() {
			DummyStack<String> newStack = new DummyStack<>();
			this.dummyStack = newStack;
			return newStack;
		}
	}

	/**
	 * Dummy thread local class to allow testing of {@link #remove()} method call.
	 *
	 * @param <T> value type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class DummyThreadLocal<T> extends ThreadLocal<T> {

		private int removeCalls = 0;

		@Override
		public void remove() {
			++removeCalls;
			super.remove();
		}
	}

	/**
	 * Dummy stack class to allow testing of push and pop operations.
	 *
	 * @param <T> value type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class DummyStack<T> extends Stack<T> {

		private static final long serialVersionUID = 5387964986959157681L;

		private List<String> operations = new ArrayList<>();

		@Override
		public T push(final T item) {
			operations.add("push: " + item);
			return super.push(item);
		}

		@Override
		public synchronized T pop() {
			operations.add("pop: " + peek());
			return super.pop();
		}

		@Override
		public synchronized T set(final int index, final T element) {
			operations.add("change: " + element);
			return super.set(index, element);
		}
	}
}
