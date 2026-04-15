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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.morphix.lang.function.ExecutionWrapper;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.function.Runnables;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.lang.retry.Retry;
import org.morphix.lang.retry.WaitCounter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;
import org.morphix.utils.Tests;
import org.morphix.utils.logging.JulLoggerAdapter;

/**
 * Test class for {@link ReschedulingTask}.
 *
 * @author Radu Sebastian LAZIN
 */
class ReschedulingTaskTest {

	private static final LoggerAdapter LOGGER = JulLoggerAdapter.of(ReschedulingTaskTest.class);

	private static final String TASK_NAME = "test-task";
	private static final Duration DELAY = Duration.ofMillis(100);

	private ScheduledExecutorService executor;

	private ScopedResource<ScheduledExecutorService> scheduler() {
		executor = Executors.newSingleThreadScheduledExecutor();
		return ScopedResource.unmanaged(executor);
	}

	@BeforeAll
	static void setup() throws Exception {
		Tests.configureLogging("src/test/resources/test-logging.properties");
	}

	@AfterEach
	void tearDown() {
		if (null != executor) {
			executor.shutdownNow();
		}
	}

	@Nested
	class DefaultValuesTests {

		@Test
		void shouldThrowExceptionWhenTryingToInstantiate() {
			UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(ReschedulingTask.Default.class);

			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseDefaultValuesWhenNotProvided() {
			ScopedResource<ScheduledExecutorService> scheduler = scheduler();

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler)
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			assertThat(task.getTaskCancelRetry(), is(Retry.noRetry()));
			assertThat(task.getMinDelay(), is(ReschedulingTask.Default.MIN_DELAY));
			assertThat(task.getLogger(), is(LoggerAdapter.none()));
			assertThat(task.isInterruptOnCancel(), is(ReschedulingTask.Default.INTERRUPT_ON_CANCEL));
			assertThat(task.getExecutionWrapper(), is(ExecutionWrapper.EMPTY));
		}
	}

	@Nested
	class BuilderValidationTests {

		@Test
		@SuppressWarnings("resource")
		void shouldBuildSuccessfullyWhenAllRequiredFieldsAreProvided() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			assertThat(task, is(notNullValue()));
			assertThat(task.getName(), is(TASK_NAME));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowExceptionWhenNameIsNull() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY);

			NullPointerException ex = assertThrows(NullPointerException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("name must not be null"));
		}

		@Test
		void shouldThrowExceptionWhenSchedulerIsNull() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.name(TASK_NAME)
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY);

			NullPointerException ex = assertThrows(NullPointerException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("scheduler must not be null"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowExceptionWhenTaskIsNull() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.name("test-task")
					.scheduler(scheduler())
					.nextDelay(() -> Duration.ofMillis(100));

			NullPointerException ex = assertThrows(NullPointerException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("refreshTask must not be null"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowExceptionWhenNextDelayIsNull() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.name("test-task")
					.scheduler(scheduler())
					.task(Runnables.doNothing());

			NullPointerException ex = assertThrows(NullPointerException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("nextDelaySupplier must not be null"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowExceptionWhenMinDelayIsNegative() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.minDelay(Duration.ofMillis(-1));

			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("minDelay must be positive"));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldThrowExceptionWhenMinDelayIsZero() {
			ReschedulingTask.Builder taskBuilder = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.minDelay(Duration.ZERO);

			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, taskBuilder::build);

			assertThat(ex.getMessage(), is("minDelay must be positive"));
		}
	}

	@Nested
	class InitialStateTests {

		@Test
		@SuppressWarnings("resource")
		void shouldHaveDeterministicInitialState() {
			ScopedResource<ScheduledExecutorService> scheduler = scheduler();

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler)
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			assertThat(task.getTaskCancelRetry(), is(Retry.noRetry()));
			assertThat(task.getMinDelay(), is(ReschedulingTask.Default.MIN_DELAY));
			assertThat(task.getLogger(), is(LoggerAdapter.none()));
			assertThat(task.isInterruptOnCancel(), is(ReschedulingTask.Default.INTERRUPT_ON_CANCEL));

			assertThat(task.getName(), is(TASK_NAME));
			assertThat(task.getScheduler(), is(scheduler));
			assertThat(task.getRefreshTask(), sameInstance(Runnables.doNothing()));
			assertThat(task.getLogger(), is(LoggerAdapter.none()));
			assertThat(task.getNextDelay(), is(DELAY));

			assertThat(task.isEnabled(), is(false));
			assertThat(task.isDisabled(), is(true));
			assertThat(task.isScheduled(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldBeDisabledByDefault() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			assertThat(task.isEnabled(), is(false));
			assertThat(task.isDisabled(), is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotBeScheduledInitially() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			assertThat(task.isScheduled(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseCustomLogger() {
			LoggerAdapter logger = (level, message, args) -> {
				// empty
			};

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.logger(logger)
					.build();

			assertThat(task.getLogger(), is(logger));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseCustomMinDelay() {
			Duration minDelay = Duration.ofSeconds(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.minDelay(minDelay)
					.build();

			assertThat(task.getMinDelay(), is(minDelay));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseCustomTaskCancelRetry() {
			Retry retry = Retry.defaultRetry();

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.taskCancelRetry(retry)
					.build();

			assertThat(task.getTaskCancelRetry(), is(retry));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseCustomInterruptOnCancel() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.interruptOnCancel(true)
					.build();

			assertThat(task.isInterruptOnCancel(), is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseFixedDelay() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(DELAY)
					.build();

			assertThat(task.getNextDelay(), is(DELAY));
		}
	}

	@Nested
	class LifecycleTests {

		@Test
		@SuppressWarnings("resource")
		void shouldEnableTaskAndExecuteAtLeastOnce() throws InterruptedException {
			CountDownLatch latch = new CountDownLatch(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(latch::countDown)
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();
			boolean enabled = task.enable();

			boolean executed = latch.await(1, TimeUnit.SECONDS);

			assertThat(enabled, is(true));
			assertThat(task.isEnabled(), is(true));
			assertThat(executed, is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotEnableTaskTwice() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();

			assertThat(task.enable(), is(true));
			assertThat(task.enable(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldDisableTaskSuccessfully() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();

			task.enable();
			boolean disabled = task.disable();

			assertThat(disabled, is(true));
			assertThat(task.isDisabled(), is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotDisableTaskTwice() {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();

			task.enable();
			assertThat(task.disable(), is(true));
			assertThat(task.disable(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldRescheduleTaskMultipleTimes() throws InterruptedException {
			AtomicInteger counter = new AtomicInteger();
			CountDownLatch latch = new CountDownLatch(3);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						counter.incrementAndGet();
						latch.countDown();
					})
					.nextDelay(() -> Duration.ofMillis(50))
					.minDelay(Duration.ofMillis(10))
					.logger(LOGGER)
					.build();

			task.enable();

			boolean completed = latch.await(2, TimeUnit.SECONDS);
			task.disable();

			assertThat(completed, is(true));
			assertThat(counter.get(), greaterThanOrEqualTo(3));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldRespectMinimumDelay() throws InterruptedException {
			long minDelayMillis = 100;

			AtomicInteger counter = new AtomicInteger();
			CountDownLatch latch = new CountDownLatch(2);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						counter.incrementAndGet();
						latch.countDown();
					})
					.nextDelay(() -> Duration.ZERO)
					.minDelay(Duration.ofMillis(minDelayMillis))
					.logger(LOGGER)
					.build();

			long start = System.nanoTime();
			task.enable();

			boolean completed = latch.await(2, TimeUnit.SECONDS);
			long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

			task.disable();

			assertThat(completed, is(true));
			assertThat(elapsedMillis, greaterThanOrEqualTo(minDelayMillis));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldHandleExceptionsAndContinueScheduling() throws InterruptedException {
			AtomicInteger counter = new AtomicInteger();
			CountDownLatch latch = new CountDownLatch(2);
			RuntimeException simulatedException = new RuntimeException("Simulated failure");

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						counter.incrementAndGet();
						latch.countDown();
						throw simulatedException;
					})
					.nextDelay(() -> Duration.ofMillis(10))
					.logger(LOGGER)
					.build();
			task.enable();

			boolean completed = latch.await(1, TimeUnit.SECONDS);
			task.disable();

			assertThat(completed, is(true));
			assertThat(counter.get(), greaterThanOrEqualTo(2));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReportScheduledState() throws InterruptedException {
			CountDownLatch latch = new CountDownLatch(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						Threads.safeSleep(Duration.ofMillis(50));
						latch.countDown();
						Threads.safeSleep(Duration.ofMillis(50));
					})
					.nextDelay(() -> Duration.ofHours(2))
					.logger(LOGGER)
					.build();

			assertThat(task.isScheduled(), is(false));

			task.enable();

			assertThat(task.isScheduled(), is(true));

			latch.await(1, TimeUnit.SECONDS);

			assertThat(task.isScheduled(), is(true));

			task.disable();

			assertThat(task.isScheduled(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		@Timeout(5)
		void shouldNotScheduleTaskWhenDisabled() {
			AtomicInteger counter = new AtomicInteger();
			CountDownLatch taskLatch = new CountDownLatch(1);
			CountDownLatch disableLatch = new CountDownLatch(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						counter.incrementAndGet();
						taskLatch.countDown();
						Threads.safeWait(disableLatch);
					})
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();

			Thread.ofVirtual().start(() -> {
				Threads.safeWait(taskLatch);
				task.disable();
				disableLatch.countDown();
			});

			task.enable();

			Threads.safeWait(disableLatch, Duration.ofSeconds(2));

			assertThat(counter.get(), is(1));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSkipExecutionWhenDisabled() {
			AtomicInteger counter = new AtomicInteger();

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(counter::incrementAndGet)
					.nextDelay(() -> DELAY)
					.logger(LOGGER)
					.build();

			assertThat(task.isDisabled(), is(true));

			Method executeTaskMethod = Methods.getOneDeclared("execute", ReschedulingTask.class);
			assertThat(executeTaskMethod, is(notNullValue()));

			Methods.IgnoreAccess.invoke(executeTaskMethod, task);

			assertThat(counter.get(), is(0));
			assertThat(task.isScheduled(), is(false));
		}
	}

	@Nested
	class CloseAndResourceManagementTests {

		@Test
		@SuppressWarnings("resource")
		void shouldDisableTaskWhenClosed() throws Exception {
			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			task.enable();
			assertThat(task.isEnabled(), is(true));

			task.close();

			assertThat(task.isDisabled(), is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCancelScheduledTaskWhenClosed() throws Exception {
			CountDownLatch latch = new CountDownLatch(1);

			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			ScopedResource<ScheduledExecutorService> scheduler = ScopedResource.managed(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler)
					.task(latch::countDown)
					.nextDelay(() -> Duration.ofSeconds(5))
					.build();
			task.enable();
			Threads.safeWait(latch);

			assertThat(task.isScheduled(), is(true));

			task.close();

			assertThat(task.isScheduled(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotShutdownUnmanagedSchedulerOnClose() throws Exception {
			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			ScopedResource<ScheduledExecutorService> resource = ScopedResource.unmanaged(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			task.enable();
			task.close();

			assertThat(scheduledExecutor.isShutdown(), is(false));

			scheduledExecutor.shutdownNow();
		}

		@Test
		@SuppressWarnings("resource")
		void shouldShutdownManagedSchedulerOnClose() throws Exception {
			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			ScopedResource<ScheduledExecutorService> resource = ScopedResource.managed(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> DELAY)
					.build();

			task.enable();
			task.close();

			assertThat(scheduledExecutor.isShutdown(), is(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldRetryCancellationOnDisableWhenRetryIsConfigured() {
			int retryAttempts = 3;

			ScheduledExecutorService scheduledExecutor = mock(ScheduledExecutorService.class);
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(scheduledFuture).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
			doReturn(false).when(scheduledFuture).cancel(anyBoolean());

			ScopedResource<ScheduledExecutorService> resource = ScopedResource.unmanaged(scheduledExecutor);

			Retry retry = Retry.of(WaitCounter.of(retryAttempts, Duration.ofMillis(1)));

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> Duration.ofSeconds(5))
					.taskCancelRetry(retry)
					.build();

			task.enable();
			task.disable();

			verify(scheduledFuture, times(retryAttempts)).cancel(anyBoolean());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldRetryCancellationOnCloseWhenRetryIsConfigured() throws Exception {
			int retryAttempts = 3;

			ScheduledExecutorService scheduledExecutor = mock(ScheduledExecutorService.class);
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(scheduledFuture).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
			doReturn(false).when(scheduledFuture).cancel(anyBoolean());

			ScopedResource<ScheduledExecutorService> resource = ScopedResource.managed(scheduledExecutor);

			Retry retry = Retry.of(WaitCounter.of(retryAttempts, Duration.ofMillis(10)));

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> Duration.ofSeconds(5))
					.taskCancelRetry(retry)
					.build();

			task.enable();
			task.close();

			verify(scheduledFuture, times(retryAttempts)).cancel(anyBoolean());
			verify(scheduledExecutor, times(1)).shutdownNow();
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotRetryCancellationOnDisableWhenRetryIsNotConfigured() {
			ScheduledExecutorService scheduledExecutor = mock(ScheduledExecutorService.class);
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(scheduledFuture).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
			doReturn(false).when(scheduledFuture).cancel(anyBoolean());

			ScopedResource<ScheduledExecutorService> resource = ScopedResource.managed(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> Duration.ofSeconds(5))
					.build();

			task.enable();
			task.disable();

			verify(scheduledFuture, times(1)).cancel(anyBoolean());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotRetryCancellationOnCloseWhenRetryIsNotConfigured() throws Exception {
			ScheduledExecutorService scheduledExecutor = mock(ScheduledExecutorService.class);
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(scheduledFuture).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
			doReturn(false).when(scheduledFuture).cancel(anyBoolean());

			ScopedResource<ScheduledExecutorService> resource = ScopedResource.managed(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(Runnables.doNothing())
					.nextDelay(() -> Duration.ofSeconds(5))
					.build();

			task.enable();
			task.close();

			verify(scheduledFuture, times(1)).cancel(anyBoolean());
			verify(scheduledExecutor, times(1)).shutdownNow();
		}

		@Test
		@SuppressWarnings("resource")
		void shouldInterruptTaskOnCancelWhenInterruptOnCancelIsTrue() throws InterruptedException {
			AtomicInteger counter = new AtomicInteger(0);
			CountDownLatch latch = new CountDownLatch(1);
			CountDownLatch taskDisableLatch = new CountDownLatch(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						if (counter.incrementAndGet() == 1) {
							// first execution, disable only on the next execution to ensure the task
							// is scheduled and running when disable is called
						} else {
							taskDisableLatch.countDown();
							while (!Thread.currentThread().isInterrupted()) {
								Threads.safeSleep(Duration.ofMillis(10));
							}
							latch.countDown();
							// preserve interrupt status
							Thread.currentThread().interrupt();
						}
					})
					.nextDelay(() -> Duration.ofMillis(10))
					.interruptOnCancel(true)
					.build();

			Thread.ofVirtual().start(() -> {
				Threads.safeWait(taskDisableLatch);
				task.disable();
			});

			task.enable();

			boolean interrupted = latch.await(5, TimeUnit.SECONDS);

			assertThat(interrupted, is(true));
			assertThat(Thread.currentThread().isInterrupted(), is(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldRescheduleAndCloseFinishedTaskMultipleTimes() throws InterruptedException {
			int executionCount = 3;
			AtomicInteger counter = new AtomicInteger();
			CountDownLatch latch = new CountDownLatch(executionCount);
			CountDownLatch disableLatch = new CountDownLatch(1);

			ScopedResource<ScheduledExecutorService> scheduler = scheduler();

			ScheduledExecutorService scheduledExecutor = spy(scheduler.unwrap());
			List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

			doAnswer(invocation -> {
				ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) invocation.callRealMethod();
				scheduledFuture = spy(scheduledFuture);
				scheduledFutures.add(scheduledFuture);
				return scheduledFuture;
			}).when(scheduledExecutor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

			ScopedResource<ScheduledExecutorService> resource = ScopedResource.unmanaged(scheduledExecutor);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(resource)
					.task(() -> {
						int count = counter.incrementAndGet();
						latch.countDown();
						if (count == executionCount) {
							Threads.safeWait(disableLatch);
						}
					})
					.nextDelay(Duration.ofMillis(10))
					.minDelay(Duration.ofMillis(10))
					.logger(LOGGER)
					.build();
			task.enable();

			Thread.ofVirtual().start(() -> {
				Threads.safeWait(latch);
				task.disable();
				disableLatch.countDown();
			});

			boolean completed = disableLatch.await(2, TimeUnit.SECONDS);

			for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
				verify(scheduledFuture, times(1)).cancel(anyBoolean());
			}
			assertThat(completed, is(true));
			assertThat(counter.get(), equalTo(executionCount));
		}
	}

	@Nested
	class DoneTests {

		@Test
		void shouldReturnTrueOnIsDoneForDoneTask() {
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(true).when(scheduledFuture).isDone();

			boolean isDone = ReschedulingTask.isDone(scheduledFuture);

			assertThat(isDone, is(true));
		}

		@Test
		void shouldReturnFalseOnIsDoneForNotDoneTask() {
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(false).when(scheduledFuture).isDone();

			boolean isDone = ReschedulingTask.isDone(scheduledFuture);

			assertThat(isDone, is(false));
		}

		@Test
		void shouldReturnTrueOnIsDoneForNullFuture() {
			boolean isDone = ReschedulingTask.isDone(null);

			assertThat(isDone, is(true));
		}

		@Test
		void shouldReturnFalseOnIsNotDoneForDoneTask() {
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(true).when(scheduledFuture).isDone();

			boolean isNotDone = ReschedulingTask.isNotDone(scheduledFuture);

			assertThat(isNotDone, is(false));
		}

		@Test
		void shouldReturnTrueOnIsNotDoneForNotDoneTask() {
			ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
			doReturn(false).when(scheduledFuture).isDone();

			boolean isNotDone = ReschedulingTask.isNotDone(scheduledFuture);

			assertThat(isNotDone, is(true));
		}

		@Test
		void shouldReturnFalseOnIsNotDoneForNullFuture() {
			boolean isNotDone = ReschedulingTask.isNotDone(null);

			assertThat(isNotDone, is(false));
		}
	}

	@Nested
	class ExecutionWrapperTests {

		@Test
		@SuppressWarnings("resource")
		void shouldExecuteTaskAndHandleExceptions() throws InterruptedException {
			int executionCount = 3;

			AtomicInteger wrapperCounter = new AtomicInteger();
			AtomicInteger executionCounter = new AtomicInteger();

			CountDownLatch latch = new CountDownLatch(3);
			CountDownLatch disableLatch = new CountDownLatch(1);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(() -> {
						int executions = executionCounter.incrementAndGet();
						LOGGER.info("Executing task, execution count: {}", executions);
						latch.countDown();
						if (executions == executionCount) {
							Threads.safeWait(disableLatch);
						}
					})
					.wrap(wrapped -> () -> transaction("test-transaction", wrapped, wrapperCounter))
					.nextDelay(() -> Duration.ofMillis(10))
					.minDelay(Duration.ofMillis(10))
					.logger(LOGGER)
					.build();
			task.enable();

			Thread.ofVirtual().start(() -> {
				Threads.safeWait(latch);
				task.disable();
				disableLatch.countDown();
			});

			boolean executed = latch.await(5, TimeUnit.SECONDS);

			assertThat(executed, is(true));
			assertThat(executionCounter.get(), is(wrapperCounter.get()));
			assertThat(executionCounter.get(), is(executionCount));
		}

		private static <T> T transaction(final String name, final Supplier<T> supplier, final AtomicInteger counter) {
			counter.incrementAndGet();
			try {
				LOGGER.info("Starting transaction '{}'", name);
				T result = supplier.get();
				LOGGER.info("Transaction '{}' completed successfully", name);
				return result;
			} catch (Exception e) {
				LOGGER.error("Transaction '{}' failed", name, e);
				throw e;
			}
		}
	}

	@Nested
	class LoggingTests {

		@Test
		@SuppressWarnings("resource")
		void shouldEnableAndDisableTaskAndLogExecution() throws InterruptedException {
			int executionCount = 3;

			CountDownLatch latch = new CountDownLatch(executionCount);

			LoggerAdapter logger = mock(LoggerAdapter.class);

			ReschedulingTask task = ReschedulingTask.builder()
					.name(TASK_NAME)
					.scheduler(scheduler())
					.task(latch::countDown)
					.nextDelay(() -> DELAY)
					.minDelay(Duration.ofMillis(10))
					.logger(logger)
					.build();
			task.enable();

			latch.await(1, TimeUnit.SECONDS);

			task.disable();

			verify(logger).debug("[{}] Enabling rescheduling task.", TASK_NAME);
			verify(logger, atLeast(executionCount - 1)).debug("[{}] Scheduling next execution in {}ms.", TASK_NAME, DELAY.toMillis());
			verify(logger).debug("[{}] Disabling rescheduling task.", TASK_NAME);
		}

	}
}
