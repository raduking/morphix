package org.morphix.lang.leak;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.morphix.utils.matcher.MorphixMatchers.containsAtLeastTimes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.utils.Tests;

/**
 * Test class for {@link ResourceLeakDetector}.
 *
 * @author Radu Sebastian LAZIN
 */
class ResourceLeakDetectorTest {

	private static final String GC_WITHOUT_CLOSE = "GC without close()";
	private static final String TEST_MESSAGE = "Test message";

	@AfterEach
	void cleanup() {
		System.clearProperty(LeakDetectionLevel.PROPERTY);
	}

	private static Set<ResourceLeakReference> references() {
		return Fields.IgnoreAccess.getStatic(ResourceLeakDetector.class, "REFERENCES");
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotTrackWhenDisabled() {
		System.setProperty(LeakDetectionLevel.PROPERTY, "DISABLED");

		ResourceLeakTracker tracker = ResourceLeakDetector.track(new Object());

		assertSame(ResourceLeakTracker.DISABLED, tracker);
		assertTrue(references().isEmpty());
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
		Logger logger = Logger.getLogger(ResourceLeakReference.class.getName());

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		Object obj = new Object();
		ResourceLeakTracker tracker = ResourceLeakDetector.track(obj);

		obj = null; // NOSONAR - allow GC

		System.gc();
		while (logs.isEmpty()) {
			Threads.safeSleep(Duration.ofMillis(100));
		}
		String expectedMessage = tracker.getReference().message(GC_WITHOUT_CLOSE);

		try {
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
		Logger logger = Logger.getLogger(ResourceLeakReference.class.getName());

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		Object obj1 = new A();
		Object obj2 = new B();
		ResourceLeakTracker tracker1 = ResourceLeakDetector.track(obj1);
		ResourceLeakTracker tracker2 = ResourceLeakDetector.track(obj2);

		obj1 = null; // NOSONAR - allow GC
		obj2 = null; // NOSONAR - allow GC

		System.gc();
		while (logs.isEmpty()) {
			Threads.safeSleep(Duration.ofMillis(100));
		}
		String expectedMessage1 = tracker1.getReference().message(GC_WITHOUT_CLOSE);
		String expectedMessage2 = tracker2.getReference().message(GC_WITHOUT_CLOSE);
		String logMessages = logs.get(0).getMessage() + logs.get(1).getMessage();

		try {
			assertThat(expectedMessage1, containsString(A.class.getName()));
			assertThat(expectedMessage2, containsString(B.class.getName()));

			assertThat(logMessages, containsString(expectedMessage1));
			assertThat(logMessages, containsString(expectedMessage2));
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
		Logger logger = Logger.getLogger(ResourceLeakReference.class.getName());

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		TestResource obj = new TestResource();
		ResourceLeakTracker tracker = obj.leakTracker;

		obj = null; // NOSONAR - allow GC

		System.gc();
		while (logs.isEmpty()) {
			Threads.safeSleep(Duration.ofMillis(100));
		}
		String expectedMessage = tracker.getReference().message(GC_WITHOUT_CLOSE);

		try {
			assertThat(expectedMessage, containsAtLeastTimes("  at ", 10));
			assertThat(expectedMessage, containsString(TestResource.class.getName()));
			assertThat(logs.get(0).getMessage(), equalTo(expectedMessage));
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
		Logger logger = Logger.getLogger(ResourceLeakReference.class.getName());

		LogsHandler handler = new LogsHandler(logs);
		logger.addHandler(handler);

		TestResource obj = new TestResource();
		ResourceLeakTracker tracker = obj.leakTracker;

		for (ResourceLeakReference ref : references()) {
			ref.reportLeak("JVM shutdown");
		}
		try {
			assertThat(logs.get(0).getMessage(), containsString(TestResource.class.getName()));
			assertThat(logs.get(0).getMessage(), containsString("JVM shutdown"));
		} finally {
			logger.removeHandler(handler);
			tracker.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReportLeaksOnShutdownWithShutdownThread() throws Exception {
		List<LogRecord> logs = new ArrayList<>();
		Logger logger = Logger.getLogger(ResourceLeakReference.class.getName());

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
