package org.morphix.lang.thread;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Stack;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link StackedContextHolder}.
 *
 * @author Radu Sebastian LAZIN
 */
class StackedContextHolderTest {

	private static final String TENANT_ID = "bubu";
	private static final String TEST_STRING = "testString";

	@Test
	void shouldCallRemoveOnThreadLocal() {
		String result = DummyTenantContextHolder.onTenant(TENANT_ID, () -> TEST_STRING);

		assertThat(result, equalTo(TEST_STRING));
		assertThat(DummyTenantContextHolder.stack.removeCalls, equalTo(1));
	}

	@Test
	void shouldSetProjectWithStatements() {
		String result = TenantContextHolder.onTenant(TENANT_ID, () -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
			return TEST_STRING;
		});

		assertThat(result, equalTo(TEST_STRING));
	}

	@Test
	void shouldSetProjectWithFunction() {
		String result = TenantContextHolder.onTenant(TENANT_ID, value -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
			return value;
		}, TEST_STRING);

		assertThat(result, equalTo(TEST_STRING));
	}

	@Test
	void shouldSetProjectWithRunnable() {
		TenantContextHolder.onTenant(TENANT_ID, () -> {
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID));
		});
	}

	@Test
	void shouldChangeProject() {
		TenantContextHolder.onTenant(TENANT_ID, () -> {
			TenantContextHolder.changeTenantId(TENANT_ID + "Changed");
			assertThat(TenantContextHolder.getTenantId(), equalTo(TENANT_ID + "Changed"));
		});
	}

	/**
	 * Dummy Context holder example.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class DummyTenantContextHolder extends TenantContextHolder {

		private static final DummyThreadLocal<Stack<String>> stack = new DummyThreadLocal<>();

		private static final DummyTenantContextHolder INSTANCE = new DummyTenantContextHolder();

		public static <T> T onTenant(final String tenantId, final Supplier<T> statements) {
			return on(tenantId, INSTANCE, statements::get);
		}

		@Override
		public ThreadLocal<Stack<String>> getThreadStack() {
			return stack;
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
}
