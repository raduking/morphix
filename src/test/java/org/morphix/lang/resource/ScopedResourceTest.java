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
package org.morphix.lang.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.leak.LeakDetectionLevel;
import org.morphix.lang.leak.ResourceLeakDetector;
import org.morphix.lang.leak.ResourceLeakTracker;
import org.morphix.reflection.Fields;
import org.morphix.utils.ConcurrentSystem;

/**
 * Test class for {@link ScopedResource}.
 *
 * @author Radu Sebastian LAZIN
 */
class ScopedResourceTest {

	@Nested
	class CreationTests {

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAManagedResourceWithConstructor() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isManaged());
			assertFalse(scopedResource.isNotManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAnUnmanagedResourceWithConstructor() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource, Lifecycle.UNMANAGED);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isNotManaged());
			assertFalse(scopedResource.isManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAManagedResourceWithFactoryMethod() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isManaged());
			assertFalse(scopedResource.isNotManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAManagedResourceWithOwnedFactoryMethod() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.owned(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isManaged());
			assertFalse(scopedResource.isNotManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAnUnmanagedResourceWithFactoryMethod() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isNotManaged());
			assertFalse(scopedResource.isManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCreateAnUnmanagedResourceWithExternalFactoryMethod() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.external(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
			assertTrue(scopedResource.isNotManaged());
			assertFalse(scopedResource.isManaged());
		}
	}

	@Nested
	class AccessTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnTheSameResourceOnUnwrap() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource);

			TestResource retrievedResource = scopedResource.unwrap();

			assertThat(resource, equalTo(retrievedResource));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnTheSameResourceOnGet() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource);

			TestResource retrievedResource = scopedResource.get();

			assertThat(resource, equalTo(retrievedResource));
		}
	}

	@Nested
	class LifecycleTests {

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResource() throws Exception {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

			scopedResource.close();

			assertTrue(resource.isClosed());
			assertThat(resource.getCloseCount(), equalTo(1));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResourceEvenIfAlreadyClosed() throws Exception {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);
			resource.close();

			scopedResource.close();

			assertTrue(resource.isClosed());
			assertThat(resource.getCloseCount(), equalTo(2));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResourceOnCloseIfManaged() throws Exception {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

			scopedResource.closeIfManaged();

			assertTrue(resource.isClosed());
			assertThat(resource.getCloseCount(), equalTo(1));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResourceAndResourceLeakTrackerOnCloseWhenAdvancedTrackingIsEnabled() throws Exception {
			ConcurrentSystem.withProperty(LeakDetectionLevel.PROPERTY, LeakDetectionLevel.ADVANCED.name(), () -> {
				TestResource resource = new TestResource();
				ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

				scopedResource.closeIfManaged();

				assertTrue(resource.isClosed());
				assertTrue(scopedResource.getLeakTracker().isClosed());
				assertThat(resource.getCloseCount(), equalTo(1));
			});
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResourceAndResourceLeakTrackerOnCloseWhenSimpleTrackingIsEnabled() throws Exception {
			ConcurrentSystem.withProperty(LeakDetectionLevel.PROPERTY, LeakDetectionLevel.SIMPLE.name(), () -> {
				TestResource resource = new TestResource();
				ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

				scopedResource.closeIfManaged();

				assertTrue(resource.isClosed());
				assertTrue(scopedResource.getLeakTracker().isClosed());
				assertThat(resource.getCloseCount(), equalTo(1));
			});
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotCloseUnmanagedResourceOnCloseIfManaged() throws Exception {
			try (TestResource resource = new TestResource()) {
				ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

				scopedResource.closeIfManaged();

				assertFalse(resource.isClosed());
				assertThat(resource.getCloseCount(), equalTo(0));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotCloseUnmanagedResourceButCloseTrackerWhenSimpleTrackingIsEnabled() throws Exception {
			ConcurrentSystem.withProperty(LeakDetectionLevel.PROPERTY, LeakDetectionLevel.SIMPLE.name(), () -> {
				try (TestResource resource = new TestResource()) {
					ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

					scopedResource.close();

					assertFalse(resource.isClosed());
					assertThat(resource.getCloseCount(), equalTo(0));
					// this should be the DISABLED tracker, which is always closed
					assertTrue(scopedResource.getLeakTracker().isClosed());
				}
			});
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotCloseUnmanagedResourceButCloseTrackerWhenAdvancedTrackingIsEnabled() throws Exception {
			ConcurrentSystem.withProperty(LeakDetectionLevel.PROPERTY, LeakDetectionLevel.ADVANCED.name(), () -> {
				try (TestResource resource = new TestResource()) {
					ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

					scopedResource.close();

					assertFalse(resource.isClosed());
					assertThat(resource.getCloseCount(), equalTo(0));
					// this should be the DISABLED tracker, which is always closed
					assertTrue(scopedResource.getLeakTracker().isClosed());
				}
			});
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotCloseUnmanagedResourceOrTrackerWhenSimpleTrackingIsEnabledAndTrackerSetWithReflection() throws Exception {
			ConcurrentSystem.withProperty(LeakDetectionLevel.PROPERTY, LeakDetectionLevel.SIMPLE.name(), () -> {
				try (TestResource resource = new TestResource()) {
					ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);
					ResourceLeakTracker leakTracker = ResourceLeakDetector.track(resource, "Expected leak");
					Fields.IgnoreAccess.set(scopedResource, "leakTracker", leakTracker);

					scopedResource.close();

					assertFalse(resource.isClosed());
					assertFalse(scopedResource.getLeakTracker().isClosed());
					assertThat(resource.getCloseCount(), equalTo(0));
				}
			});
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseManagedResourceWithExceptionHandler() {
			TestResource resource = new TestResource();
			ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

			assertDoesNotThrow(() -> scopedResource.closeIfManaged(e -> {
				throw new RuntimeException("Should not reach here!", e);
			}));

			assertTrue(resource.isClosed());
			assertThat(resource.getCloseCount(), equalTo(1));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldNotCloseUnmanagedResourceWithExceptionHandler() {
			try (TestResource resource = new TestResource()) {
				ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

				assertDoesNotThrow(() -> scopedResource.closeIfManaged(e -> {
					throw new RuntimeException("Should not reach here!", e);
				}));

				assertFalse(resource.isClosed());
				assertThat(resource.getCloseCount(), equalTo(0));
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCallExceptionHandlerWhenClosingManagedResourceFails() {
			TestResourceWithExceptionOnClose resource = new TestResourceWithExceptionOnClose();
			ScopedResource<TestResourceWithExceptionOnClose> scopedResource = ScopedResource.managed(resource);

			AtomicBoolean handlerCalled = new AtomicBoolean(false);

			scopedResource.closeIfManaged(e -> handlerCalled.set(true));

			assertTrue(handlerCalled.get());
			assertThat(resource.getCloseCount(), equalTo(1));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldCloseResourcesInReverseOrder() throws Exception {
			TrackingResource.reset();
			try (ScopedResource<TrackingResource> r1 =
					ScopedResource.managed(new TrackingResource("conn"));
					ScopedResource<TrackingResource> r2 =
							r1.derive(r -> new TrackingResource("stmt"));
					ScopedResource<TrackingResource> r3 =
							r2.derive(r -> new TrackingResource("rset"))) {
				// empty
			}

			assertThat(TrackingResource.openOrder(), contains(
					"conn#1",
					"stmt#2",
					"rset#3"));

			assertThat(TrackingResource.closeOrder(), contains(
					"rset#3",
					"stmt#2",
					"conn#1"));
		}
	}

	@Nested
	class EnsureSingleManagerTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnUnmanagedWhenBothManagedAndSameResource() {
			TestResource c = new TestResource();

			ScopedResource<TestResource> a = ScopedResource.managed(c);
			ScopedResource<TestResource> b = ScopedResource.managed(c);

			ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

			assertSame(c, result.unwrap());
			assertTrue(result.isNotManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnOriginalWhenResourcesAreDifferent() {
			ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
			ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

			ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

			assertSame(a, result);
			assertTrue(result.isManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnOriginalWhenFirstIsAlreadyUnmanaged() {
			ScopedResource<TestResource> a = ScopedResource.unmanaged(new TestResource());
			ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

			ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

			assertSame(a, result);
			assertTrue(result.isNotManaged());
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnOriginalWhenSecondIsAlreadyUnmanaged() {
			ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
			ScopedResource<TestResource> b = ScopedResource.unmanaged(new TestResource());

			ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

			assertSame(a, result);
			assertTrue(a.isManaged());
		}
	}

	@Nested
	class SafeCloseTests {

		@Test
		@SuppressWarnings("resource")
		void shouldSafelyCloseTheResourceWithSafeClose() {
			TestResource resource = new TestResource();

			assertDoesNotThrow(() -> ScopedResource.safeClose(resource, Consumers.noConsumer()));

			assertTrue(resource.isClosed());
		}

		@Test
		void shouldNotThrowExceptionOnSafeCloseWithNull() {
			assertDoesNotThrow(() -> ScopedResource.safeClose(null, Consumers.noConsumer()));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldSafelyCloseTheResourceWithSafeCloseAndHandleException() {
			TestResourceWithExceptionOnClose resource = new TestResourceWithExceptionOnClose();
			AtomicReference<Exception> caughtException = new AtomicReference<>();

			assertDoesNotThrow(() -> ScopedResource.safeClose(resource, caughtException::set));

			assertTrue(resource.isClosed());
			assertThat(caughtException.get().getMessage(), equalTo(TestResourceWithExceptionOnClose.CLOSE_FAILED));
		}
	}

	@Nested
	class DeriveTests {

		@Test
		@SuppressWarnings("resource")
		void shouldDeriveManagedChildWhenParentIsManaged() throws Exception {
			TestResource parent = new TestResource();

			try (ScopedResource<TestResource> scopedParent = ScopedResource.managed(parent);
					ScopedResource<TestResource> child = scopedParent.derive(p -> new TestResource())) {

				assertTrue(child.isManaged());
			}
		}

		@Test
		void shouldDeriveUnanagedChildWhenParentIsUnmanaged() throws Exception {
			try (TestResource parent = new TestResource();
					ScopedResource<TestResource> scopedParent = ScopedResource.unmanaged(parent);
					ScopedResource<TestResource> child = scopedParent.derive(p -> new TestResource())) {

				assertFalse(child.isManaged());
			}
		}

		@Test
		@SuppressWarnings("resource")
		void shouldPropagateExceptionFromFactory() {
			TestResource parent = new TestResource();
			ScopedResource<TestResource> scopedParent = ScopedResource.managed(parent);

			RuntimeException exception = new RuntimeException("boom");
			RuntimeException thrown = null;

			try {
				scopedParent.derive(p -> {
					throw exception;
				});
			} catch (Exception e) {
				thrown = (RuntimeException) e;
			}

			assertThat(thrown, is(exception));
		}
	}

	static class TestResource implements AutoCloseable {

		private boolean closed = false;
		private int closeCount = 0;

		@Override
		public void close() {
			closed = true;
			++closeCount;
		}

		public boolean isClosed() {
			return closed;
		}

		public int getCloseCount() {
			return closeCount;
		}
	}

	static class TestResourceWithExceptionOnClose extends TestResource {

		private static final String CLOSE_FAILED = "Close failed!";

		@Override
		public void close() {
			super.close();
			throw new RuntimeException(CLOSE_FAILED);
		}
	}

	static class TrackingResource implements AutoCloseable {

		private static final List<String> OPEN_ORDER = Collections.synchronizedList(new ArrayList<>());
		private static final List<String> CLOSE_ORDER = Collections.synchronizedList(new ArrayList<>());

		private static final AtomicInteger COUNTER = new AtomicInteger();

		private final String name;
		private final int id;
		private volatile boolean closed;

		public TrackingResource(final String name) {
			this.name = name;
			this.id = COUNTER.incrementAndGet();
			OPEN_ORDER.add(name + "#" + id);
		}

		@Override
		public void close() {
			closed = true;
			CLOSE_ORDER.add(name + "#" + id);
		}

		public boolean isClosed() {
			return closed;
		}

		public String identifier() {
			return name + "#" + id;
		}

		public static void reset() {
			CLOSE_ORDER.clear();
			COUNTER.set(0);
		}

		public static List<String> openOrder() {
			return new ArrayList<>(OPEN_ORDER);
		}

		public static List<String> closeOrder() {
			return new ArrayList<>(CLOSE_ORDER);
		}
	}
}
