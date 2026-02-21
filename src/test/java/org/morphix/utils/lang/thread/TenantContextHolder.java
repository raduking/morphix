package org.morphix.utils.lang.thread;

import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

import org.morphix.lang.thread.StackedContextHolder;

/**
 * Context holder for current tenant ID.
 * <p>
 * Singleton class. The {@link #instance()} method is not needed to be accessible from the outside since only the
 * following methods
 *
 * <pre>
 * {@link #onTenant(String, Runnable)}
 * {@link #onTenant(String, Statements)}
 * {@link #onTenant(String, Function, Object)}
 * {@link #getTenantId()}
 * {@link #changeTenantId(String)}</pre>
 *
 * are actually used from outside to set/change/return the current tenant in the current threads' context.
 * <p>
 *
 * @author Radu Sebastian LAZIN
 */
public class TenantContextHolder extends StackedContextHolder<String> {

	/**
	 * Holds the current tenant ID. The {@link ThreadLocal#remove()} is handled correctly by {@link StackedContextHolder} so
	 * no memory leak problems occur.
	 */
	private static final ThreadLocal<Stack<String>> tenantStack = new ThreadLocal<>();

	/**
	 * Set the name to {@code "Tenant"}.
	 */
	protected TenantContextHolder() {
		super("Tenant", TenantContextHolder::isValidTenantId);
	}

	/**
	 * Returns the thread local stack for tenant IDs.
	 *
	 * @return the thread local stack for tenant IDs
	 */
	@Override
	public ThreadLocal<Stack<String>> getThreadStack() {
		return tenantStack;
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	private static TenantContextHolder instance() {
		return TenantContextHolder.InstanceHolder.INSTANCE;
	}

	/**
	 * Returns the current tenant ID.
	 *
	 * @return the current tenant ID
	 */
	public static String getTenantId() {
		return instance().getElement();
	}

	/**
	 * Changes the current tenant.
	 *
	 * @param tenantId new tenant
	 */
	public static void changeTenantId(final String tenantId) {
		instance().changeElement(tenantId);
	}

	/**
	 * Runs the given statements on the given tenant and returns the result from the statements.
	 *
	 * @param <T> return type
	 * @param tenantId tenant ID
	 * @param statements statements to run
	 * @return statements result
	 */
	public static <T> T onTenant(final String tenantId, final Supplier<T> statements) {
		return on(tenantId, instance(), statements::get);
	}

	/**
	 * Runs the given function on the given tenant and returns its result.
	 *
	 * @param <T> function parameter type
	 * @param <R> function return type
	 *
	 * @param tenantId tenant ID
	 * @param function function to run
	 * @param arg function argument
	 * @return function result
	 */
	public static <T, R> R onTenant(final String tenantId, final Function<T, R> function, final T arg) {
		return on(tenantId, instance(), function, arg);
	}

	/**
	 * Runs the given runnable on the given tenant.
	 *
	 * @param tenantId tenant ID
	 * @param runnable runnable to run
	 */
	public static void onTenant(final String tenantId, final Runnable runnable) {
		on(tenantId, instance(), runnable);
	}

	/**
	 * Checks if the given tenant ID is valid.
	 *
	 * @param tenantId tenant ID to check
	 * @return true if the tenant ID is valid, false otherwise
	 */
	private static boolean isValidTenantId(final String tenantId) {
		return tenantId != null && !tenantId.isBlank();
	}

	/**
	 * Instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance. The class is loaded only when the {@link TenantContextHolder#instance()} method is called for the
		 * first time, so the instance is created lazily.
		 */
		private static final TenantContextHolder INSTANCE = new TenantContextHolder();
	}
}
