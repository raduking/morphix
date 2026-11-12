package org.morphix.lang.thread;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.morphix.lang.Nullables;

/**
 * Synchronization methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Synchronized {

	/**
	 * Default timeout for acquiring a lock.
	 */
	Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

	/**
	 * Runs a supplier while holding a lock. This method waits until the lock can be acquired.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param supplier value supplier
	 * @return supplier value
	 */
	static <T> T with(final Lock lock, final Supplier<T> supplier) {
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Runs a runnable while holding a lock. This method waits until the lock can be acquired.
	 *
	 * @param lock lock to synchronize with
	 * @param runnable code to execute
	 */
	static void with(final Lock lock, final Runnable runnable) {
		with(lock, () -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Tries to acquire the lock for the specified timeout and runs the supplier while holding it. If the lock cannot be
	 * acquired, or the thread is interrupted while waiting, the default value is returned.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param timeUnit time unit for the timeout
	 * @param supplier value supplier
	 * @param defaultValueSupplier default return type
	 * @return supplier value if lock was acquired, default value otherwise
	 */
	static <T> T tryWith(final Lock lock, final long timeout, final TimeUnit timeUnit, final Supplier<T> supplier,
			final Supplier<T> defaultValueSupplier) {
		boolean isLockAcquired = false;
		try {
			isLockAcquired = lock.tryLock(timeout, timeUnit);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (!isLockAcquired) {
			return defaultValueSupplier.get();
		}
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Tries to acquire the lock for the specified timeout and runs the supplier while holding it. Returns {@code null} if
	 * the lock cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param timeUnit time unit for the timeout
	 * @param supplier value supplier
	 * @return supplier value if lock was acquired, null otherwise
	 */
	static <T> T tryWith(final Lock lock, final long timeout, final TimeUnit timeUnit, final Supplier<T> supplier) {
		return tryWith(lock, timeout, timeUnit, supplier, Nullables.supplyNull());
	}

	/**
	 * Tries to acquire the lock for the specified duration and runs the supplier while holding it. If the lock cannot be
	 * acquired, or the thread is interrupted while waiting, the default value is returned.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param supplier value supplier
	 * @param defaultValueSupplier default return type
	 * @return supplier value if lock was acquired, default value otherwise
	 */
	static <T> T tryWith(final Lock lock, final Duration timeout, final Supplier<T> supplier, final Supplier<T> defaultValueSupplier) {
		return tryWith(lock, timeout.toMillis(), TimeUnit.MILLISECONDS, supplier, defaultValueSupplier);
	}

	/**
	 * Tries to acquire the lock for the specified duration and runs the supplier while holding it. Returns {@code null} if
	 * the lock cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param supplier value supplier
	 * @return supplier value if lock was acquired, null otherwise
	 */
	static <T> T tryWith(final Lock lock, final Duration timeout, final Supplier<T> supplier) {
		return tryWith(lock, timeout.toMillis(), TimeUnit.MILLISECONDS, supplier, Nullables.supplyNull());
	}

	/**
	 * Tries to acquire the lock for the default timeout and runs the supplier while holding it. Returns {@code null} if the
	 * lock cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param <T> supplier return type
	 *
	 * @param lock lock to synchronize with
	 * @param supplier value supplier
	 * @return supplier value if lock was acquired, {@code null} otherwise
	 */
	static <T> T tryWith(final Lock lock, final Supplier<T> supplier) {
		return tryWith(lock, DEFAULT_TIMEOUT, supplier);
	}

	/**
	 * Tries to acquire the lock for the specified timeout and runs the runnable while holding it. Does nothing if the lock
	 * cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param timeUnit time unit for the timeout
	 * @param runnable code to execute
	 */
	static void tryWith(final Lock lock, final long timeout, final TimeUnit timeUnit, final Runnable runnable) {
		tryWith(lock, timeout, timeUnit, Nullables.supplyNull(runnable));
	}

	/**
	 * Tries to acquire the lock for the specified duration and runs the runnable while holding it. Does nothing if the lock
	 * cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param lock lock to synchronize with
	 * @param timeout timeout to acquire lock
	 * @param runnable code to execute
	 */
	static void tryWith(final Lock lock, final Duration timeout, final Runnable runnable) {
		tryWith(lock, timeout.toMillis(), TimeUnit.MILLISECONDS, runnable);
	}

	/**
	 * Tries to acquire the lock for the default timeout and runs the runnable while holding it. Does nothing if the lock
	 * cannot be acquired or the thread is interrupted while waiting.
	 *
	 * @param lock lock to synchronize with
	 * @param runnable code to execute
	 */
	static void tryWith(final Lock lock, final Runnable runnable) {
		tryWith(lock, DEFAULT_TIMEOUT, runnable);
	}
}
