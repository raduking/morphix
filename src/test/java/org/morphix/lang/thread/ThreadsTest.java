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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.thread.Threads.ExecutionType;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Threads}.
 *
 * @author Radu Sebastian LAZIN
 */
class ThreadsTest {

	private static final String TEST_STRING = "testString";
	private static final int THREAD_COUNT = 10;
	private static final int TIME = 10;
	private static final long TIME_LONG = TIME;

	@ParameterizedTest
	@EnumSource(value = ExecutionType.class)
	void shouldExecuteWithExecutionType(final ExecutionType excecutionType) {
		Queue<Integer> queue = new ConcurrentLinkedQueue<>();
		List<Runnable> runnables = IntStream.range(0, THREAD_COUNT)
				.mapToObj(i -> (Runnable) () -> {
					for (int j = 1; j <= THREAD_COUNT; ++j) {
						queue.add(i * THREAD_COUNT + j);
					}
				})
				.toList();
		Threads.execute(runnables, excecutionType);

		Set<Integer> set = new HashSet<>(queue);
		assertThat(queue, hasSize(THREAD_COUNT * THREAD_COUNT));
		assertThat(set, hasSize(THREAD_COUNT * THREAD_COUNT));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateDefaultClass() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(Threads.Default.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Nested
	class SafeSleepTest {

		@Test
		void shouldSafeSleepForTheGivenAmount() {
			TimeUnit timeUnit = mock(TimeUnit.class);

			Threads.safeSleep(TIME, timeUnit);

			try {
				verify(timeUnit).sleep(TIME); // NOSONAR - we verify the correct call
			} catch (InterruptedException e) {
				Threads.handleInterruptedException();
			}
		}

		@Test
		void shouldSafeSleepForTheGivenDuration() {
			Duration duration = mock(Duration.class);
			doReturn(TIME_LONG).when(duration).toMillis();

			try (MockedStatic<Threads> threadsMock = Mockito.mockStatic(Threads.class)) {
				threadsMock.when(() -> Threads.safeSleep(duration)).thenCallRealMethod();
				Threads.safeSleep(duration);

				verify(duration).toMillis();
				threadsMock.verify(() -> Threads.safeSleep(TIME_LONG, TimeUnit.MILLISECONDS));
			}
		}

		@Test
		void shouldNotSafeSleepIfTheGivenAmountIsZero() {
			TimeUnit timeUnit = mock(TimeUnit.class);

			Threads.safeSleep(0, timeUnit);

			verifyNoInteractions(timeUnit);
		}
	}

	@Nested
	class ExecuteWithTimeoutTest {

		@Test
		void shouldThrowExeptionOnTimeoutWhenRunningRunnableWithTimeout() {
			Runnable runnable = () -> {
				Threads.safeSleep(10, TimeUnit.SECONDS);
			};

			assertThrows(TimeoutException.class, () -> Threads.execute(Duration.ofMillis(10), runnable));
		}

		@Test
		void shouldThrowExeptionOnTimeoutWhenRunningSupplierWithTimeout() {
			Supplier<String> supplier = () -> {
				Threads.safeSleep(10, TimeUnit.SECONDS);
				return TEST_STRING;
			};

			Exception exception = null;
			String result = null;
			try {
				result = Threads.execute(Duration.ofMillis(10), supplier);
			} catch (Exception e) {
				exception = e;
			}

			assertNull(result);
			assertNotNull(exception);
			assertTrue(TimeoutException.class.isInstance(exception));
		}

		@Test
		void shouldThrowExecutionExeptionOnExceptionWhenRunningRunnableWithTimeout() {
			Runnable runnable = () -> {
				throw new RuntimeException(TEST_STRING);
			};

			Throwable exception = assertThrows(ExecutionException.class, () -> Threads.execute(Duration.ofHours(TIME), runnable));

			exception = exception.getCause();
			assertThat(exception.getMessage(), equalTo(TEST_STRING));
		}

		@Test
		void shouldNotThrowExeptionOnTimeoutWhenRunningSupplierWithTimeout() {
			Supplier<String> supplier = () -> TEST_STRING;

			String result = Threads.execute(Duration.ofSeconds(10), supplier);

			assertThat(result, equalTo(TEST_STRING));
		}

		@Test
		void shouldNotThrowExeptionOnTimeoutWhenRunningRunnableWithTimeout() {
			assertDoesNotThrow(() -> Threads.execute(Duration.ofSeconds(10), Runnables.doNothing()));
		}
	}

	@Nested
	class ExecuteForEachInTest {

		@ParameterizedTest
		@EnumSource(value = ExecutionType.class, mode = Mode.EXCLUDE, names = "VIRTUAL")
		void shouldExecuteForEachInWithExecutionType(final ExecutionType executionType) {
			Queue<Integer> queue = new ConcurrentLinkedQueue<>();
			List<Integer> integers = IntStream.range(0, THREAD_COUNT).boxed().toList();
			Threads.executeForEachIn(integers, i -> {
				for (int j = 1; j <= THREAD_COUNT; ++j) {
					queue.add(i * THREAD_COUNT + j);
				}
			}, executionType);

			assertThat(queue, hasSize(THREAD_COUNT * THREAD_COUNT));
			Set<Integer> set = new HashSet<>(queue);
			assertThat(set, hasSize(THREAD_COUNT * THREAD_COUNT));
		}

		@Test
		void shouldExecuteForEachInWithExecutor() {
			Queue<Integer> queue = new ConcurrentLinkedQueue<>();
			List<Integer> integers = IntStream.range(0, THREAD_COUNT).boxed().toList();
			@SuppressWarnings("resource")
			ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

			Threads.executeForEachIn(integers, i -> {
				for (int j = 1; j <= THREAD_COUNT; ++j) {
					queue.add(i * THREAD_COUNT + j);
				}
			}, executor);
			executor.close();

			assertThat(queue, hasSize(THREAD_COUNT * THREAD_COUNT));
			Set<Integer> set = new HashSet<>(queue);
			assertThat(set, hasSize(THREAD_COUNT * THREAD_COUNT));
		}
	}

	@Nested
	class SafeJoinTest {

		@Test
		void shouldSafelyWaitForCountdownLatch() {
			CountDownLatch latch = new CountDownLatch(1);
			AtomicInteger i = new AtomicInteger(0);

			Thread thread = Thread.ofPlatform().start(() -> {
				i.incrementAndGet();
				Threads.safeWait(latch);
			});

			latch.countDown();
			Threads.safeJoin(thread);

			assertThat(i.get(), equalTo(1));
		}

		@Test
		void shouldSafelyWaitForCountdownLatchAndHandleInterruptedException() {
			CountDownLatch latch = new CountDownLatch(1);
			AtomicInteger i = new AtomicInteger(0);

			Thread thread = Thread.ofPlatform().start(() -> {
				i.incrementAndGet();
				Threads.safeWait(latch);
			});

			while (i.get() == 0) {
				// empty
			}
			thread.interrupt();
			Threads.safeJoin(thread);

			assertThat(i.get(), equalTo(1));
		}

		@Test
		void shouldSafelyWaitForCountdownLatchWithDurationAndCountReached() {
			CountDownLatch latch = new CountDownLatch(1);
			AtomicInteger i = new AtomicInteger(0);
			AtomicBoolean countReached = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				i.incrementAndGet();
				countReached.set(Threads.safeWait(latch, Duration.ofSeconds(10)));
			});

			latch.countDown();
			Threads.safeJoin(thread);

			assertThat(i.get(), equalTo(1));
			assertTrue(countReached.get());
		}

		@Test
		void shouldSafelyWaitForCountdownLatchWithDurationAndCountNotReached() {
			CountDownLatch latch = new CountDownLatch(1);
			AtomicInteger i = new AtomicInteger(0);
			AtomicBoolean countReached = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				i.incrementAndGet();
				countReached.set(Threads.safeWait(latch, Duration.ofSeconds(10)));
			});

			thread.interrupt();
			Threads.safeJoin(thread);

			assertThat(i.get(), equalTo(1));
			assertFalse(countReached.get());
		}
	}

	@Nested
	class HandleInterruptedExceptionTest {

		@Test
		void shouldInterruptCurrentThreadWhenHandlingInterruptedException() {
			Thread thread = Thread.ofPlatform().start(Threads::handleInterruptedException);
			Threads.safeJoin(thread);

			assertTrue(thread.isInterrupted());
		}

		@Test
		void shouldInterruptCurrentThreadWhenHandlingInterruptedExceptionWithInterruptedException() {
			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.handleInterruptedException(new InterruptedException());
			});
			Threads.safeJoin(thread);

			assertTrue(thread.isInterrupted());
		}

		@Test
		void shouldInterruptCurrentThreadWhenHandlingInterruptedExceptionWithAnyException() {
			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.handleInterruptedException(new RuntimeException());
			});
			Threads.safeJoin(thread);

			assertFalse(thread.isInterrupted());
		}

		@Test
		void shouldHandleInterruptedExceptionOnJoin() {
			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.safeSleep(Duration.ofHours(TIME));
			});

			Thread testingThread = Thread.ofPlatform().start(() -> {
				Thread.currentThread().interrupt();
				Threads.safeJoin(thread);

				assertTrue(Thread.currentThread().isInterrupted());
			});

			Threads.safeJoin(testingThread);
		}
	}

	@Nested
	class IsCurrentInterruptedTest {

		@Test
		void shouldReturnFalseWhenThreadIsNotInterrupted() {
			// clear any existing interrupt status
			Thread.interrupted();

			assertThat(Threads.isCurrentInterrupted(), is(equalTo(false)));
		}

		@Test
		void shouldReturnTrueWhenThreadIsInterrupted() {
			Thread.currentThread().interrupt();

			assertThat(Threads.isCurrentInterrupted(), is(equalTo(true)));
		}
	}

	@Nested
	class WaitUntilTest {

		@Test
		@Timeout(5)
		void shouldWaitUntilConditionIsTrue() {
			AtomicBoolean condition = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get);
			});
			Threads.waitUntil(thread::isAlive);
			condition.set(true);

			boolean result = Threads.waitUntil(() -> !thread.isAlive());

			assertFalse(thread.isAlive());
			assertTrue(result);
		}

		@Test
		@Timeout(5)
		void shouldWaitUntilConditionIsTrueWithTimeout() {
			AtomicBoolean condition = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get, Duration.ofSeconds(1));
			});
			Threads.waitUntil(thread::isAlive);
			condition.set(true);
			Threads.waitUntil(() -> !thread.isAlive());

			assertFalse(thread.isAlive());
		}

		@Test
		@Timeout(5)
		void shouldWaitUntilConditionIsTrueWithTimeoutAndPollInterval() {
			AtomicBoolean condition = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get, Duration.ofSeconds(1), Duration.ofMillis(10));
			});
			Threads.waitUntil(thread::isAlive);
			condition.set(true);
			Threads.waitUntil(() -> !thread.isAlive());

			assertFalse(thread.isAlive());
		}

		@Test
		void shouldReturnImmediatelyIfConditionIsAlreadyTrue() {
			AtomicBoolean condition = new AtomicBoolean(true);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get);
			});
			Threads.safeJoin(thread);

			assertFalse(thread.isAlive());
		}

		@Test
		void shouldReturnImmediatelyIfTimeoutIsNegative() {
			AtomicBoolean condition = new AtomicBoolean(false);

			boolean result = Threads.waitUntil(condition::get, Duration.ofSeconds(-1));

			assertFalse(condition.get());
			assertFalse(result);
		}

		@Test
		void shouldWaitForTimeout() {
			AtomicBoolean condition = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get, Duration.ofMillis(1), Duration.ofMillis(1));
			});
			Threads.safeJoin(thread);

			assertFalse(thread.isAlive());
			assertFalse(condition.get());
		}

		@Test
		void shouldNotWaitForTimeoutIfInterrupted() {
			AtomicBoolean condition = new AtomicBoolean(false);

			Thread thread = Thread.ofPlatform().start(() -> {
				Threads.waitUntil(condition::get, Duration.ofSeconds(3), Duration.ofMillis(10));
			});
			thread.interrupt();

			Threads.waitUntil(() -> !thread.isAlive());

			assertFalse(condition.get());
		}
	}
}
