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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.thread.Threads.ExecutionType;

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

		assertThat(queue, hasSize(THREAD_COUNT * THREAD_COUNT));
		Set<Integer> set = new HashSet<>(queue);
		assertThat(set, hasSize(THREAD_COUNT * THREAD_COUNT));
	}

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

	static class Resource implements AutoCloseable {

		private final AtomicBoolean closed = new AtomicBoolean(false);

		public void use() {
			throw new UnsupportedOperationException("Use method exception");
		}

		@Override
		public void close() {
			closed.set(true);
			throw new IllegalStateException("Close method exception");
		}

		public boolean isClosed() {
			return closed.get();
		}
	}

	private static Resource loadResource() {
		return new Resource();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldValidateTryWithResourcesVsFinally() {
		Resource resource1 = loadResource();
		try {
			resource1.use();
		} catch (Exception e) {
			assertFalse(resource1.isClosed());
			assertInstanceOf(UnsupportedOperationException.class, e);
		} finally {
			assertThrows(IllegalStateException.class, () -> resource1.close());
		}

		// The IllegalStateException from close() is suppressed in try-with-resources
		Resource resource2 = loadResource();
		try (resource2) {
			resource2.use();
		} catch (Exception e) {
			assertTrue(resource2.isClosed());
			assertInstanceOf(UnsupportedOperationException.class, e);
		}
	}
}
