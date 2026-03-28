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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ResourceLeakReference}.
 *
 * @author Radu Sebastian LAZIN
 */
class ResourceLeakReferenceTest {

	private static final String TEST = "test";

	@Test
	@SuppressWarnings("resource")
	void shouldReportOnlyOnce() {
		ResourceLeakReporter reporter = ResourceLeakLogger.instance();
		ResourceLeakReference reference = ResourceLeakReference.of(LeakDetectionLevel.SIMPLE, Object.class, reporter);

		reference.reportLeak(TEST);

		assertTrue(reference.isReported());
		assertDoesNotThrow(() -> reference.reportLeak(TEST));
		assertDoesNotThrow(() -> reference.reportLeak(TEST));
		assertFalse(reference.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotReportAfterClose() {
		ResourceLeakReporter reporter = ResourceLeakLogger.instance();
		ResourceLeakReference ref = ResourceLeakReference.of(LeakDetectionLevel.SIMPLE, Object.class, reporter);

		ref.close();
		assertTrue(ref.isClosed());
		assertDoesNotThrow(() -> ref.reportLeak(TEST));
		assertFalse(ref.isReported());
	}
}
