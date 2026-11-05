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
package org.morphix.lang.thread;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Synchronized}.
 *
 * @author Radu Sebastian LAZIN
 */
class SynchronizedTest {

	private static final String VALUE = "value";
	private static final String DEFAULT = "default";

	@Test
	void shouldRunSupplierWithLock() {
		ReentrantLock lock = new ReentrantLock();

		assertThat(Synchronized.with(lock, () -> VALUE), equalTo(VALUE));
		assertFalse(lock.isLocked());
	}

	@Test
	void shouldUnlockAfterSupplierThrows() {
		ReentrantLock lock = new ReentrantLock();

		assertThrows(IllegalStateException.class, () -> Synchronized.with(lock, () -> {
			throw new IllegalStateException();
		}));

		assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	void shouldRunRunnableWithLock() {
		ReentrantLock lock = new ReentrantLock();
		AtomicBoolean ran = new AtomicBoolean();

		Synchronized.with(lock, () -> ran.set(true));

		assertTrue(ran.get());
		assertFalse(lock.isLocked());
	}

	@Test
	void shouldTrySupplierWithLongTimeoutAndDefault() throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();

		assertThat(Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> VALUE, () -> DEFAULT), equalTo(VALUE));
		whileLocked(lock, () -> {
			assertThat(
					Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> VALUE, () -> DEFAULT),
					equalTo(DEFAULT));
		});
	}

	@Test
	void shouldTrySupplierWithLongTimeoutAndNullDefault() throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();

		assertThat(Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> VALUE), equalTo(VALUE));
		whileLocked(lock, () -> assertThat(Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> VALUE), equalTo(null)));
	}

	@Test
	void shouldTrySupplierWithDuration() {
		ReentrantLock lock = new ReentrantLock();

		assertThat(Synchronized.tryWith(lock, Duration.ofMillis(1), () -> VALUE, () -> DEFAULT), equalTo(VALUE));
		assertThat(Synchronized.tryWith(lock, Duration.ofMillis(1), () -> VALUE), equalTo(VALUE));
		assertThat(Synchronized.tryWith(lock, () -> VALUE), equalTo(VALUE));
	}

	@Test
	void shouldTryRunnableWithTimeout() throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();
		AtomicBoolean ran = new AtomicBoolean();

		Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> ran.set(true));
		Synchronized.tryWith(lock, () -> ran.set(true));
		assertTrue(ran.get());

		ran.set(false);
		whileLocked(lock, () -> {
			Synchronized.tryWith(lock, Duration.ofMillis(1), () -> ran.set(true));
			Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> ran.set(true));
			assertFalse(ran.get());
		});
	}

	@Test
	void shouldReturnDefaultWhenInterrupted() throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();
		AtomicReference<String> result = new AtomicReference<>();
		AtomicBoolean interrupted = new AtomicBoolean();
		lock.lock();
		try {
			Thread thread = new Thread(() -> result.set(Synchronized.tryWith(
					lock,
					1,
					TimeUnit.DAYS,
					() -> VALUE,
					() -> {
						interrupted.set(Thread.currentThread().isInterrupted());
						return DEFAULT;
					})));
			thread.start();
			while (thread.getState() != Thread.State.TIMED_WAITING) {
				Thread.yield();
			}
			thread.interrupt();
			thread.join();
		} finally {
			lock.unlock();
		}

		assertThat(result.get(), equalTo(DEFAULT));
		assertTrue(interrupted.get());
	}

	@Test
	void shouldNotThrowWhenRunnableTimesOut() {
		ReentrantLock lock = new ReentrantLock();
		assertDoesNotThrow(() -> whileLocked(lock, () -> Synchronized.tryWith(lock, 1, TimeUnit.MILLISECONDS, () -> {
			throw new AssertionError("must not run");
		})));
	}

	private static void whileLocked(final ReentrantLock lock, final Runnable action) throws InterruptedException {
		CountDownLatch acquired = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		Thread holder = new Thread(() -> {
			lock.lock();
			try {
				acquired.countDown();
				release.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		});
		holder.start();
		acquired.await();
		try {
			action.run();
		} finally {
			release.countDown();
			holder.join();
		}
	}
}
