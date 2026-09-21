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
package org.morphix.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link LibraryVersion}.
 *
 * @author Radu Sebastian LAZIN
 */
class LibraryVersionTest {

	private static final String LIBRARY_NAME = "test-library";

	@Nested
	class OfTests {

		@Test
		void shouldReturnVersionFromAnchorClassPackage() {
			LibraryVersion libraryVersion = LibraryVersion.of(LIBRARY_NAME, LibraryVersion.class);

			assertThat(libraryVersion.getVersion(), is(LibraryVersion.class.getPackage().getImplementationVersion()));
		}

		@Test
		void shouldReturnVersionFromAnchorClassName() {
			LibraryVersion libraryVersion = LibraryVersion.of(LIBRARY_NAME, LibraryVersion.class.getName());

			assertThat(libraryVersion.getVersion(), is(LibraryVersion.class.getPackage().getImplementationVersion()));
		}

		@Test
		void shouldReturnNullVersionWhenAnchorClassIsNotPresent() {
			LibraryVersion libraryVersion = LibraryVersion.of(LIBRARY_NAME, LibraryVersion.class.getName() + "$NonExistentClass");

			assertThat(libraryVersion.getVersion(), is(nullValue()));
		}

		@Test
		void shouldReturnName() {
			LibraryVersion libraryVersion = LibraryVersion.of(LIBRARY_NAME, LibraryVersion.class);

			assertThat(libraryVersion.getName(), is(LIBRARY_NAME));
		}

		@Test
		void shouldReturnNullVersionWhenAnchorPackageIsNull() {
			LibraryVersion libraryVersion = LibraryVersion.of(LIBRARY_NAME, (Package) null);

			assertThat(libraryVersion.getVersion(), is(nullValue()));
		}
	}

	@Nested
	class IsAtLeastTests {

		@Test
		void shouldConsiderVersionAtLeastMinimumWhenVersionCannotBeDetermined() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, null);

			assertThat(libraryVersion.isAtLeast("999.0"), is(true));
		}

		@Test
		void shouldReturnTrueWhenVersionIsAtLeastMinimum() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(libraryVersion.isAtLeast("5.5"), is(true));
		}

		@Test
		void shouldReturnFalseWhenVersionIsOlderThanMinimum() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, "5.4.4");

			assertThat(libraryVersion.isAtLeast("5.5"), is(false));
		}
	}

	@Nested
	class VerifyAtLeastTests {

		@Test
		void shouldNotThrowWhenVersionCannotBeDetermined() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, null);

			assertDoesNotThrow(() -> libraryVersion.verifyAtLeast("999.0"));
		}

		@Test
		void shouldNotThrowWhenVersionIsAtLeastMinimum() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertDoesNotThrow(() -> libraryVersion.verifyAtLeast("5.5"));
		}

		@Test
		void shouldThrowWhenVersionIsOlderThanMinimum() {
			LibraryVersion libraryVersion = new LibraryVersion(LIBRARY_NAME, "5.4.4");

			IllegalStateException exception = assertThrows(IllegalStateException.class, () -> libraryVersion.verifyAtLeast("5.5"));

			assertThat(exception.getMessage(), is("Unsupported test-library version: 5.4.4, minimum required version is 5.5"));
		}
	}

	@Nested
	class CompareTests {

		@Test
		void shouldReturnZeroForEqualVersions() {
			assertThat(LibraryVersion.compare("5.5.1", "5.5.1"), is(0));
		}

		@Test
		void shouldReturnNegativeWhenFirstVersionIsOlder() {
			assertThat(LibraryVersion.compare("5.4.4", "5.5"), is(-1));
		}

		@Test
		void shouldReturnPositiveWhenFirstVersionIsNewer() {
			assertThat(LibraryVersion.compare("5.6", "5.5.1"), is(1));
		}

		@Test
		void shouldTreatMissingTrailingComponentsAsZero() {
			assertThat(LibraryVersion.compare("5.5", "5.5.0"), is(0));
		}

		@Test
		void shouldIgnoreNonNumericSuffix() {
			assertThat(LibraryVersion.compare("5.5.1-SNAPSHOT", "5.5.1"), is(0));
		}

		@Test
		void shouldCompareMultiDigitNumericComponents() {
			assertThat(LibraryVersion.compare("5.10", "5.9"), is(1));
		}

		@Test
		void shouldCompareEmptyVersionsAsEqual() {
			assertThat(LibraryVersion.compare("", ""), is(0));
		}
	}

	@Nested
	class EqualsTests {

		@Test
		void shouldBeEqualForSameNameAndVersion() {
			LibraryVersion version1 = new LibraryVersion(LIBRARY_NAME, "5.5.1");
			LibraryVersion version2 = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(version1, is(version2));
		}

		@Test
		void shouldBeEqualForSameInstance() {
			LibraryVersion version = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(version, is(version));
		}

		@Test
		void shouldBeEqualForNullVersions() {
			LibraryVersion version1 = new LibraryVersion(LIBRARY_NAME, null);
			LibraryVersion version2 = new LibraryVersion(LIBRARY_NAME, null);

			assertThat(version1, is(version2));
		}

		@Test
		void shouldNotBeEqualForDifferentVersions() {
			LibraryVersion version1 = new LibraryVersion(LIBRARY_NAME, "5.5.1");
			LibraryVersion version2 = new LibraryVersion(LIBRARY_NAME, "5.6");

			assertThat(version1, is(not(version2)));
		}

		@Test
		void shouldNotBeEqualForDifferentNames() {
			LibraryVersion version1 = new LibraryVersion("library-1", "5.5.1");
			LibraryVersion version2 = new LibraryVersion("library-2", "5.5.1");

			assertThat(version1, is(not(version2)));
		}

		@Test
		void shouldNotBeEqualForObjectOfDifferentClass() {
			assertThat(new LibraryVersion(LIBRARY_NAME, "5.5.1"), is(not(new Object())));
		}
	}

	@Nested
	class HashCodeTests {

		@Test
		void shouldHaveSameHashCodeWhenEqual() {
			LibraryVersion version1 = new LibraryVersion(LIBRARY_NAME, "5.5.1");
			LibraryVersion version2 = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(version1.hashCode(), is(version2.hashCode()));
		}
	}

	@Nested
	class CompareToTests {

		@Test
		void shouldReturnNegativeWhenComparedVersionIsOlder() {
			LibraryVersion older = new LibraryVersion(LIBRARY_NAME, "5.4.4");
			LibraryVersion newer = new LibraryVersion(LIBRARY_NAME, "5.5");

			assertThat(older.compareTo(newer), is(lessThan(0)));
		}

		@Test
		void shouldReturnPositiveWhenComparedVersionIsNewer() {
			LibraryVersion newer = new LibraryVersion(LIBRARY_NAME, "5.6");
			LibraryVersion older = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(newer.compareTo(older), is(greaterThan(0)));
		}

		@Test
		void shouldCompareVersionsNumerically() {
			LibraryVersion older = new LibraryVersion(LIBRARY_NAME, "5.9");
			LibraryVersion newer = new LibraryVersion(LIBRARY_NAME, "5.10");

			assertThat(older.compareTo(newer), is(lessThan(0)));
		}

		@Test
		void shouldReturnZeroWhenVersionsAndNamesAreEqual() {
			LibraryVersion version1 = new LibraryVersion(LIBRARY_NAME, "5.5.1");
			LibraryVersion version2 = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(version1.compareTo(version2), is(0));
		}

		@Test
		void shouldCompareByNameWhenVersionsAreEqual() {
			LibraryVersion version1 = new LibraryVersion("library-1", "5.5.1");
			LibraryVersion version2 = new LibraryVersion("library-2", "5.5.1");

			assertThat(version1.compareTo(version2), is(lessThan(0)));
		}

		@Test
		void shouldSortUndeterminedVersionsFirst() {
			LibraryVersion undetermined = new LibraryVersion(LIBRARY_NAME, null);
			LibraryVersion determined = new LibraryVersion(LIBRARY_NAME, "5.5.1");

			assertThat(undetermined.compareTo(determined), is(lessThan(0)));
			assertThat(determined.compareTo(undetermined), is(greaterThan(0)));
		}
	}
}
