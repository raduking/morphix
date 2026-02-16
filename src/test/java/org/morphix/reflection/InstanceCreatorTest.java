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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link InstanceCreator}.
 *
 * @author Radu Sebastian LAZIN
 */
class InstanceCreatorTest {

	@BeforeEach
	void setUp() throws Exception {
		assumeTrue(InstanceCreator.getInstance() != null);
	}

	@Test
	void shouldReturnInstance() {
		assertThat(InstanceCreator.getInstance(), notNullValue());
	}

	@Test
	void shouldSetInstanceCreatorToNull() {
		InstanceCreator saved = InstanceCreator.getInstance();
		InstanceCreator.setInstance(null);
		InstanceCreator result = InstanceCreator.getInstance();
		InstanceCreator.setInstance(saved);

		assertThat(result, nullValue());
	}

	@Test
	void shouldHavePrivateDefaultConstructorInInstanceHolder() throws Exception {
		Class<?> instanceHolderClass = Class.forName(InstanceCreator.class.getName() + "$InstanceHolder");
		Constructor<?> constructor = instanceHolderClass.getDeclaredConstructor();

		assertThat(constructor.canAccess(null), equalTo(false));

		try (MemberAccessor<Constructor<?>> ignored = new MemberAccessor<>(null, constructor)) {
			Object obj = constructor.newInstance();
			assertNotNull(obj);
		}
	}

	@Test
	void shouldReturnUnusableIfUsingBaseClass() {
		InstanceCreator instanceCreator = new InstanceCreator();

		assertThat(instanceCreator.isUsable(), equalTo(false));
	}

	@Test
	void shouldThrowExceptionOnNewInstanceIfUsingBaseClass() {
		InstanceCreator instanceCreator = new InstanceCreator();
		assertThrows(ReflectionException.class, () -> instanceCreator.newInstance(String.class));
	}

}
