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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Test class for try-with-resources and finally block behavior with exceptions in both use and close methods.
 *
 * @author Radu Sebastian LAZIN
 */
class ResourceTest {

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
			assertThrows(IllegalStateException.class, resource1::close);
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
