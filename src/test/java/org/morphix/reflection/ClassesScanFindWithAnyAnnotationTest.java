/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes.Scan#findWithAnyAnnotation(Set, Path, Set)}</li>
 * <li>{@link Classes.Scan#findWithAnyAnnotation(Set, Path, Set, Consumer)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesScanFindWithAnyAnnotationTest {

	private static final String TARGET_TEST_CLASSES = "target/test-classes";

	@interface TestAnnotation {

		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface RuntimeTestAnnotation {

		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface RuntimeTestAnnotation2 {

		String value();
	}

	@RuntimeTestAnnotation("example")
	@TestAnnotation("example")
	@RuntimeTestAnnotation2("example")
	static class A {
		// empty
	}

	@Test
	void shouldFindZeroResultsOnNullPackages() {
		var classes = Classes.Scan.findWithAnyAnnotation(null, Path.of("some/empty/path"), Set.of(SuppressWarnings.class));

		assertThat(classes, hasSize(0));
	}

	@Test
	void shouldFindZeroResultsOnEmptyPackages() {
		var classes = Classes.Scan.findWithAnyAnnotation(Set.of(), Path.of("some/empty/path"), Set.of(SuppressWarnings.class));

		assertThat(classes, hasSize(0));
	}

	@Test
	void shouldFindZeroResultsOnEmptyPath() {
		var classes = Classes.Scan.findWithAnyAnnotation(Set.of("some.package"), Path.of("some/empty/path"), Set.of(SuppressWarnings.class));

		assertThat(classes, hasSize(0));
	}

	@Test
	void shouldFindZeroResultsWhenAnnotationIsNotPresent() {
		var packages = Set.of("org.morphix.reflection.testdata", "org.morphix.reflection.predicates");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), Set.of(SuppressWarnings.class));

		assertThat(classes, hasSize(0));
	}

	@Test
	void shouldFindZeroResultsWithAnnotationNotRetainedAtRuntime() {
		var classes = Classes.Scan.findWithAnyAnnotation(Set.of("org.morphix"), Path.of(TARGET_TEST_CLASSES), Set.of(TestAnnotation.class));

		assertThat(classes, hasSize(0));
	}

	@Test
	void shouldFindResultsWithAnnotationRetainedAtRuntime() {
		var packages = Set.of("org.morphix.reflection");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), Set.of(RuntimeTestAnnotation.class));

		assertThat(classes, hasSize(1));
	}

	@Test
	void shouldFindZeroResultsOnNullPackagesWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var classes = Classes.Scan.findWithAnyAnnotation(null, Path.of("some/empty/path"), Set.of(SuppressWarnings.class), logger);

		assertThat(classes, hasSize(0));
		assertThat(logCount.get(), equalTo(0));
	}

	@Test
	void shouldFindZeroResultsOnEmptyPackagesWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var classes = Classes.Scan.findWithAnyAnnotation(Set.of(), Path.of("some/empty/path"), Set.of(SuppressWarnings.class), logger);

		assertThat(classes, hasSize(0));
		assertThat(logCount.get(), equalTo(0));
	}

	@Test
	void shouldFindZeroResultsOnEmptyPathWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var packages = Set.of("some.package");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of("some/empty/path"), Set.of(SuppressWarnings.class), logger);

		assertThat(classes, hasSize(0));
		assertThat(logCount.get(), equalTo(packages.size()));
	}

	@Test
	void shouldFindZeroResultsWhenAnnotationIsNotPresentWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var packages = Set.of("org.morphix.reflection.testdata", "org.morphix.reflection.predicates");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), Set.of(SuppressWarnings.class), logger);

		assertThat(classes, hasSize(0));
		assertThat(logCount.get(), equalTo(packages.size()));
	}

	@Test
	void shouldFindZeroResultsWithAnnotationNotRetainedAtRuntimeWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var packages = Set.of("org.morphix");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), Set.of(TestAnnotation.class), logger);

		assertThat(classes, hasSize(0));
		assertThat(logCount.get(), equalTo(packages.size()));
	}

	@Test
	void shouldFindResultsWithAnnotationRetainedAtRuntimeWithLoggerConsumer() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var packages = Set.of("org.morphix.reflection");
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), Set.of(RuntimeTestAnnotation.class), logger);

		assertThat(classes, hasSize(1));
		assertThat(logCount.get(), equalTo(packages.size() + 1));
	}

	@Test
	void shouldFindResultsWithAnnotationRetainedAtRuntimeWithLoggerConsumerAndLogAllAnnotations() {
		AtomicInteger logCount = new AtomicInteger(0);
		Consumer<String> logger = msg -> {
			logCount.incrementAndGet();
		};
		var packages = Set.of("org.morphix.reflection");
		var annotatations = Set.of(RuntimeTestAnnotation.class, RuntimeTestAnnotation2.class);
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), annotatations, logger);

		assertThat(classes, hasSize(1));
		assertThat(logCount.get(), equalTo(packages.size() + annotatations.size()));
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldFindResultsWithAnnotationRetainedAtRuntimeWithLoggerConsumerAndLogAllAnnotationsText() {
		Consumer<String> logger = mock(Consumer.class);
		var packages = Set.of("org.morphix.reflection");
		var annotatations = Set.of(RuntimeTestAnnotation.class, RuntimeTestAnnotation2.class);
		var classes = Classes.Scan.findWithAnyAnnotation(packages, Path.of(TARGET_TEST_CLASSES), annotatations, logger);

		assertThat(classes, hasSize(1));
		verify(logger).accept("Scanning package: org.morphix.reflection");
		verify(logger).accept(
				"Found annotated class: " + A.class.getCanonicalName() + " with annotation: " + RuntimeTestAnnotation.class.getCanonicalName());
		verify(logger).accept(
				"Found annotated class: " + A.class.getCanonicalName() + " with annotation: " + RuntimeTestAnnotation2.class.getCanonicalName());
	}

}
