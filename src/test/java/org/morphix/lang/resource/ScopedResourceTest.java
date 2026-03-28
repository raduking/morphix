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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;

/**
 * Test class for {@link ScopedResource}.
 *
 * @author Radu Sebastian LAZIN
 */
class ScopedResourceTest {

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

	@Test
	@SuppressWarnings("resource")
	void shouldCloseManagedResource() throws Exception {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		scopedResource.closeIfManaged();

		assertTrue(resource.isClosed());
	}

	@Test
	void shouldNotCloseUnmanagedResource() throws Exception {
		try (TestResource resource = new TestResource()) {
			ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

			scopedResource.closeIfManaged();

			assertFalse(resource.isClosed());
		}
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
	}

	@Test
	void shouldNotCloseUnmanagedResourceWithExceptionHandler() {
		try (TestResource resource = new TestResource()) {
			ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

			assertDoesNotThrow(() -> scopedResource.closeIfManaged(e -> {
				throw new RuntimeException("Should not reach here!", e);
			}));

			assertFalse(resource.isClosed());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallExceptionHandlerWhenClosingManagedResourceFails() {
		ScopedResource<TestResource> scopedResource = ScopedResource.managed(new TestResource() {
			@Override
			public void close() {
				throw new RuntimeException("Close failed!");
			}
		});

		AtomicBoolean handlerCalled = new AtomicBoolean(false);

		scopedResource.closeIfManaged(e -> handlerCalled.set(true));

		assertTrue(handlerCalled.get());
	}

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
		TestResourceExceptionOnClose resource = new TestResourceExceptionOnClose();

		AtomicReference<Exception> caughtException = new AtomicReference<>();

		assertDoesNotThrow(() -> ScopedResource.safeClose(resource, caughtException::set));

		assertTrue(resource.isClosed());
		assertThat(caughtException.get().getMessage(), equalTo(TestResourceExceptionOnClose.CLOSE_FAILED));
	}

	static class TestResource implements AutoCloseable {

		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
		}

		public boolean isClosed() {
			return closed;
		}
	}

	static class TestResourceExceptionOnClose implements AutoCloseable {

		private static final String CLOSE_FAILED = "Close failed!";

		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
			throw new RuntimeException(CLOSE_FAILED);
		}

		public boolean isClosed() {
			return closed;
		}
	}
}
