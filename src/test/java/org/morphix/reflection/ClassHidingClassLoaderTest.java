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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Test class for {@link ClassHidingClassLoader}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassHidingClassLoaderTest {

	private static final String STRING = String.class.getName();

	private static final String ARRAY_LIST = ArrayList.class.getName();

	private static final String EXTENSION_CONTEXT = ExtensionContext.class.getName();

	@Test
	void shouldReturnNewBuilder() {
		assertNotSame(ClassHidingClassLoader.builder(), ClassHidingClassLoader.builder());
	}

	@Test
	void shouldUseOwningClassLoaderAsDefaultParent() {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder().build();

		assertSame(ClassHidingClassLoader.class.getClassLoader(), classLoader.getParent());
	}

	@Test
	void shouldUseGivenParent() {
		ClassLoader parent = ClassHidingClassLoaderTest.class.getClassLoader();

		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder().parent(parent).build();

		assertSame(parent, classLoader.getParent());
	}

	@Test
	void shouldThrowNullPointerExceptionWhenParentIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> ClassHidingClassLoader.builder().parent(null));

		assertThat(exception.getMessage(), equalTo("parent must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenHiddenClassNameIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> ClassHidingClassLoader.builder().hide((String) null));

		assertThat(exception.getMessage(), equalTo("className must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenHiddenClassIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> ClassHidingClassLoader.builder().hide((Class<?>) null));

		assertThat(exception.getMessage(), equalTo("type must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenLocalClassNameIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> ClassHidingClassLoader.builder().loadLocally((String) null));

		assertThat(exception.getMessage(), equalTo("className must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenLocalClassIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> ClassHidingClassLoader.builder().loadLocally((Class<?>) null));

		assertThat(exception.getMessage(), equalTo("type must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenHiddenClassNamesIsNull() {
		ClassLoader parent = ClassHidingClassLoaderTest.class.getClassLoader();

		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> new ClassHidingClassLoader(parent, null, Set.of()));

		assertThat(exception.getMessage(), equalTo("hiddenClassNames collection must not be null"));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenLocalClassNamesIsNull() {
		ClassLoader parent = ClassHidingClassLoaderTest.class.getClassLoader();

		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> new ClassHidingClassLoader(parent, Set.of(), null));

		assertThat(exception.getMessage(), equalTo("localClassNames collection must not be null"));
	}

	@Test
	void shouldCopyClassNamesWhenBuilt() throws ClassNotFoundException {
		ClassLoader parent = ClassHidingClassLoaderTest.class.getClassLoader();
		Set<String> hiddenClassNames = new HashSet<>();
		Set<String> localClassNames = new HashSet<>();

		ClassHidingClassLoader classLoader = new ClassHidingClassLoader(parent, hiddenClassNames, localClassNames);

		hiddenClassNames.add(STRING);
		localClassNames.add(EXTENSION_CONTEXT);

		assertSame(String.class, classLoader.loadClass(STRING));
		assertSame(ExtensionContext.class, classLoader.loadClass(EXTENSION_CONTEXT));
	}

	@Test
	void shouldThrowClassNotFoundExceptionForHiddenClass() {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.hide(STRING)
				.build();

		ClassNotFoundException exception = assertThrows(ClassNotFoundException.class,
				() -> classLoader.loadClass(STRING));

		assertThat(exception.getMessage(), equalTo(STRING));
	}

	@Test
	void shouldThrowClassNotFoundExceptionForHiddenClassGivenAsClass() {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.hide(String.class)
				.build();

		assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(STRING));
	}

	@Test
	void shouldHideClassBeforeLoadingItLocally() {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.hide(ExtensionContext.class)
				.loadLocally(ExtensionContext.class)
				.build();

		assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(EXTENSION_CONTEXT));
	}

	@Test
	void shouldHideExactNameOnly() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.hide(ClassHidingClassLoader.class)
				.build();

		assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(ClassHidingClassLoader.class.getName()));
		assertSame(ClassHidingClassLoader.Builder.class,
				classLoader.loadClass(ClassHidingClassLoader.class.getName() + "$Builder"));
	}

	@Test
	void shouldDelegateClassesToParent() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder().build();

		assertSame(String.class, classLoader.loadClass(STRING));
		assertSame(ArrayList.class, classLoader.loadClass(ARRAY_LIST));
	}

	@Test
	void shouldLoadClassLocally() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(EXTENSION_CONTEXT)
				.build();

		Class<?> firstLoad = classLoader.loadClass(EXTENSION_CONTEXT);
		Class<?> secondLoad = classLoader.loadClass(EXTENSION_CONTEXT);

		assertNotSame(ExtensionContext.class, firstLoad);
		assertSame(classLoader, firstLoad.getClassLoader());
		assertSame(firstLoad, secondLoad);
	}

	@Test
	void shouldLoadNestedClassLocally() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(ExtensionContext.class)
				.build();

		Class<?> nested = classLoader.loadClass(ExtensionContext.Namespace.class.getName());

		assertNotSame(ExtensionContext.Namespace.class, nested);
		assertSame(classLoader, nested.getClassLoader());
	}

	@Test
	void shouldNotLoadClassLocallyWhenNameOnlyHasSamePrefix() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(ClassHidingClassLoader.class)
				.build();

		assertSame(ClassHidingClassLoaderTest.class,
				classLoader.loadClass(ClassHidingClassLoaderTest.class.getName()));
	}

	@Test
	void shouldKeepDistinctStaticStateForEachClassLoader() throws ClassNotFoundException {
		ClassHidingClassLoader first = ClassHidingClassLoader.builder()
				.loadLocally(ExtensionContext.class)
				.build();
		ClassHidingClassLoader second = ClassHidingClassLoader.builder()
				.loadLocally(ExtensionContext.class)
				.build();

		Class<?> firstLoad = first.loadClass(EXTENSION_CONTEXT);
		Class<?> secondLoad = second.loadClass(EXTENSION_CONTEXT);

		// each class loader gets its own Class instance, and with it its own static state
		assertNotSame(firstLoad, secondLoad);
		assertSame(first, firstLoad.getClassLoader());
		assertSame(second, secondLoad.getClassLoader());
	}

	@Test
	void shouldResolveClassWhenResolveFlagIsSet() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(ExtensionContext.class)
				.build();

		Class<?> type = classLoader.loadClass(EXTENSION_CONTEXT, true);

		assertNotSame(ExtensionContext.class, type);
		assertSame(classLoader, type.getClassLoader());
	}

	@Test
	void shouldThrowClassNotFoundExceptionWhenLocalClassBytecodeIsMissing() {
		String className = "org.morphix.does.not.Exist";
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(className)
				.build();

		assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass(className));
	}

	@Test
	void shouldThrowClassNotFoundExceptionWhenBytecodeCannotBeRead() {
		ClassLoader failingParent = new ClassLoader(ClassHidingClassLoaderTest.class.getClassLoader()) {

			@Override
			public InputStream getResourceAsStream(final String name) {
				if (name.endsWith("ExtensionContext.class")) {
					return new InputStream() {

						@Override
						public int read() throws IOException {
							throw new IOException("cannot read");
						}
					};
				}
				return super.getResourceAsStream(name);
			}
		};
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.parent(failingParent)
				.loadLocally(ExtensionContext.class)
				.build();

		ClassNotFoundException exception = assertThrows(ClassNotFoundException.class,
				() -> classLoader.loadClass(EXTENSION_CONTEXT));

		assertTrue(exception.getCause() instanceof IOException);
	}

	@Test
	void shouldUseParentProtectionDomainForLocallyLoadedClass() throws ClassNotFoundException {
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.loadLocally(ExtensionContext.class)
				.build();

		Class<?> type = classLoader.loadClass(EXTENSION_CONTEXT);

		assertThat(type.getProtectionDomain(), equalTo(ExtensionContext.class.getProtectionDomain()));
	}

	@Test
	void shouldFallBackToOwnProtectionDomainWhenParentCannotLoadClass() throws ClassNotFoundException {
		ClassLoader resourceOnlyParent = new ClassLoader(ClassHidingClassLoaderTest.class.getClassLoader()) {

			@Override
			protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
				if (EXTENSION_CONTEXT.equals(name)) {
					throw new ClassNotFoundException(name);
				}
				return super.loadClass(name, resolve);
			}

			@Override
			public Class<?> loadClass(final String name) throws ClassNotFoundException {
				if (EXTENSION_CONTEXT.equals(name)) {
					throw new ClassNotFoundException(name);
				}
				return super.loadClass(name);
			}
		};
		ClassHidingClassLoader classLoader = ClassHidingClassLoader.builder()
				.parent(resourceOnlyParent)
				.loadLocally(ExtensionContext.class)
				.build();

		Class<?> type = classLoader.loadClass(EXTENSION_CONTEXT);

		assertSame(classLoader, type.getClassLoader());
		assertThat(type.getProtectionDomain(), equalTo(ClassHidingClassLoader.class.getProtectionDomain()));
	}
}
