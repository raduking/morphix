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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Classes.Scan#findInPackage(String, Path)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ClassesScanFindInPackageTest {

	private static final String TARGET_TEST_CLASSES = "target/test-classes";
	private static final String TARGET_CLASSES = "target/classes";

	@Test
	void shouldThrowExceptionOnNullPackageName() {
		Path path = Path.of("some/path");

		assertThrows(NullPointerException.class, () -> Classes.Scan.findInPackage(null, path));
	}

	@Test
	void shouldThrowExceptionOnNullPath() {
		assertThrows(NullPointerException.class, () -> Classes.Scan.findInPackage("some.package", null));
	}

	@Test
	void shouldFindZeroResultsOnEmptyPath() {
		var classes = Classes.Scan.findInPackage("some.package", Path.of("some/empty/path"));
		assertThat(classes.size(), equalTo(0));
	}

	@Test
	void shouldFindZeroResultsIfPathIsNotADirectory() {
		var classes = Classes.Scan.findInPackage("file", Path.of(TARGET_TEST_CLASSES));

		assertThat(classes.size(), equalTo(0));
	}

	@Test
	void shouldFindClassesInPackage() {
		var classes = Classes.Scan.findInPackage("org.morphix.reflection.testdata", Path.of(TARGET_TEST_CLASSES));

		assertThat(classes.size(), equalTo(3));
	}

	@Test
	void shouldFindClassesInSubpackages() {
		var classes = Classes.Scan.findInPackage("org.morphix.lang", Path.of(TARGET_CLASSES));

		var classesFunction = Classes.Scan.findInPackage("org.morphix.lang.function", Path.of(TARGET_CLASSES));
		var classesThread = Classes.Scan.findInPackage("org.morphix.lang.thread", Path.of(TARGET_CLASSES));

		int langClassesCount = 9;
		// Classes found directly in org.morphix.lang:
		// Comparables
		// Enums
		// JavaArrays
		// JavaObjects
		// Nullables
		// Nullables$Chain
		// Unchecked
		// Unchecked$Undeclared
		// Messages

		assertThat(classes.size(), equalTo(langClassesCount + classesFunction.size() + classesThread.size()));
	}
}
