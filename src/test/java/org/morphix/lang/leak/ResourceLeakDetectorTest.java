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
package org.morphix.lang.leak;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.morphix.utils.Tests.waitUntil;
import static org.morphix.utils.matcher.MorphixMatchers.containsAtLeastTimes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.utils.ConcurrentSystem;
import org.morphix.utils.Tests;

/**
 * Test class for {@link ResourceLeakDetector}.
 *
 * @author Radu Sebastian LAZIN
 */
class ResourceLeakDetectorTest {

	private static final String REASON = "reason";
	private static final String GC_WITHOUT_CLOSE = "GC without close()";
	private static final String TEST_MESSAGE = "Test message";

	private static final int ADVANCED_REPORTED_FRAMES = 10;
	private static final Set<String> IGNORED_FRAMES = Set.of(
			ResourceLeakReference.class.getName() + ".",
			ResourceLeakTracker.class.getName() + ".",
			ResourceLeakDetector.class.getName() + ".");

	private String originalProperty;

	@BeforeEach
	void setup() {
		originalProperty = ConcurrentSystem.getAndSetProperty(LeakDetectionLevel.PROPERTY, null);
	}

	@AfterEach
	void cleanup() {
		ConcurrentSystem.getAndSetProperty(LeakDetectionLevel.PROPERTY, originalProperty);
	}

	private static Set<ResourceLeakReference> references() {
		return Fields.IgnoreAccess.getStatic(ResourceLeakDetector.class, "REFERENCES");
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotTrackWhenDisabled() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "DISABLED");

		ResourceLeakTracker tracker = ResourceLeakDetector.track(new Object());

		try {
			assertSame(ResourceLeakTracker.DISABLED, tracker);
			assertTrue(references().isEmpty());
		} finally {
			tracker.close();
		}
	}

	@Test
	void shouldTrackObject() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "SIMPLE");

		try (ResourceLeakTracker tracker = ResourceLeakDetector.track(new Object())) {
			assertFalse(references().isEmpty());
		}
	}

	@Test
	void shouldRemoveReferenceOnClose() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "SIMPLE");

		try (ResourceLeakTracker tracker = ResourceLeakDetector.track(new Object())) {
			// no assertion here, just ensuring that the reference is added
		}
		assertTrue(references().isEmpty());
	}

	@Test
	@Timeout(5)
	@SuppressWarnings("resource")
	void shouldReportLeakWhenNotClosed() throws Exception {
		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakLogger.NAME);

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		Object obj = new Object();
		ResourceLeakTracker tracker = ResourceLeakDetector.track(obj);

		obj = null; // NOSONAR - allow GC

		System.gc();

		try {
			waitUntil(() -> !logs.isEmpty());

			String expectedMessage = tracker.getReference().getReport(GC_WITHOUT_CLOSE);

			assertThat(expectedMessage, containsString(Object.class.getName()));
			assertThat(logs.get(0).getMessage(), equalTo(expectedMessage));
		} finally {
			logger.removeHandler(handler);
			// cleanup so other tests are not affected
			tracker.close();
		}
	}

	@Test
	@Timeout(5)
	@SuppressWarnings("resource")
	void shouldReportAllLeaksWhenNotClosed() throws Exception {
		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakLogger.NAME);

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		Object obj1 = new A();
		Object obj2 = new B();
		ResourceLeakTracker tracker1 = ResourceLeakDetector.track(obj1);
		ResourceLeakTracker tracker2 = ResourceLeakDetector.track(obj2);

		obj1 = null; // NOSONAR - allow GC
		obj2 = null; // NOSONAR - allow GC

		System.gc();

		try {
			// wait until both leaks are reported
			waitUntil(() -> logs.size() >= 2);

			String expectedMessage1 = tracker1.getReference().getReport(GC_WITHOUT_CLOSE);
			String expectedMessage2 = tracker2.getReference().getReport(GC_WITHOUT_CLOSE);
			List<String> logMessages = logs.stream().map(LogRecord::getMessage).toList();

			assertThat(expectedMessage1, containsString(A.class.getName()));
			assertThat(expectedMessage2, containsString(B.class.getName()));
			assertThat(logMessages, hasSize(2));
			assertThat(logMessages, containsInAnyOrder(expectedMessage1, expectedMessage2));
		} finally {
			logger.removeHandler(handler);
			// cleanup so other tests are not affected
			tracker1.close();
			tracker2.close();
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "ADVANCED", "PARANOID" })
	@Timeout(5)
	@SuppressWarnings("resource")
	void shouldReportLeakWithStackTraceWhenNotClosed(final String level) throws Exception {
		System.setProperty(LeakDetectionLevel.PROPERTY, level);

		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakLogger.NAME);

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		TestResource obj = new TestResource();
		ResourceLeakTracker tracker = obj.leakTracker;

		obj = null; // NOSONAR - allow GC

		System.gc();

		try {
			// wait until the leak is reported
			waitUntil(() -> !logs.isEmpty());

			String expectedMessage = tracker.getReference().getReport(GC_WITHOUT_CLOSE);

			assertThat(logs.get(0).getMessage(), equalTo(expectedMessage));
			assertThat(expectedMessage, containsAtLeastTimes("  at ", ADVANCED_REPORTED_FRAMES));
			assertThat(expectedMessage, containsString(TestResource.class.getName()));
			for (String ignoredFrame : IGNORED_FRAMES) {
				assertThat(expectedMessage, not(containsString(ignoredFrame)));
			}
		} finally {
			logger.removeHandler(handler);
			// cleanup so other tests are not affected
			tracker.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReportLeaksOnShutdown() throws Exception {
		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakLogger.NAME);

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		TestResource obj = new TestResource();
		ResourceLeakTracker tracker = obj.leakTracker;

		try {
			for (ResourceLeakReference ref : references()) {
				ref.reportLeak("JVM shutdown");
			}

			assertThat(logs.get(0).getMessage(), containsString(TestResource.class.getName()));
			assertThat(logs.get(0).getMessage(), containsString("JVM shutdown"));
		} finally {
			logger.removeHandler(handler);
			// cleanup so other tests are not affected
			tracker.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReportLeaksOnShutdownWithShutdownThread() throws Exception {
		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakLogger.NAME);

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		TestResource obj = new TestResource();
		ResourceLeakTracker tracker = obj.leakTracker;

		Thread leakReporterThread = ResourceLeakDetector.newLeakReporterThread();
		leakReporterThread.start();
		leakReporterThread.join();

		try {
			assertThat(logs.get(0).getMessage(), containsString(TestResource.class.getName()));
			assertThat(logs.get(0).getMessage(), containsString("JVM shutdown"));
		} finally {
			logger.removeHandler(handler);
			// cleanup so other tests are not affected
			tracker.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallReportLeakOnAllReferences() {
		ResourceLeakReference reference1 = mock(ResourceLeakReference.class);
		ResourceLeakReference reference2 = mock(ResourceLeakReference.class);

		Set<ResourceLeakReference> references = Set.of(reference1, reference2);

		ResourceLeakDetector.reportLeaks(references, TEST_MESSAGE);

		verify(reference1).reportLeak(TEST_MESSAGE);
		verify(reference2).reportLeak(TEST_MESSAGE);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(ResourceLeakDetector.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldBuildMessageWithNullHint() {
		String message = ResourceLeakDetector.message(null, REASON);

		assertThat(message, equalTo(REASON));
	}

	@Test
	void shouldBuildMessageWithHint() {
		String message = ResourceLeakDetector.message(TEST_MESSAGE, REASON);

		assertThat(message, equalTo(TEST_MESSAGE + " - " + REASON));
	}

	static class TestResource implements AutoCloseable {

		final ResourceLeakTracker leakTracker = ResourceLeakDetector.track(this);

		@Override
		public void close() {
			leakTracker.close();
		}
	}

	static class LogsHandler extends Handler {

		private final List<LogRecord> logs;

		LogsHandler(final List<LogRecord> logs) {
			this.logs = logs;
		}

		@Override
		public void publish(final LogRecord logRecord) {
			logs.add(logRecord);
		}

		@Override
		public void flush() {
			// empty
		}

		@Override
		public void close() throws SecurityException {
			// empty
		}
	}

	static class A {
		// empty
	}

	static class B {
		// empty
	}
}
