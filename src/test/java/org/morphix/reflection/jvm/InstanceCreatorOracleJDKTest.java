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
package org.morphix.reflection.jvm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.InstanceCreator;
import org.morphix.reflection.MemberAccessor;
import org.morphix.reflection.ReflectionException;
import org.morphix.reflection.testdata.C;

/**
 * Test class for {@link InstanceCreatorOracleJDK}.
 *
 * @author Radu Sebastian LAZIN
 */
class InstanceCreatorOracleJDKTest {

	private static final String UNDEFINED_CLASS_NAME = "__";
	private static final String NEW_CONSTRUCTOR_FOR_SERIALIZATION_METHOD_FIELD_NAME = "newConstructorForSerializationMethod";

	private static final Set<String> EXCLUDED_FIELDS = Set.of("REFLECTION_FACTORY_CLASS_NAME");

	@BeforeEach
	void setUp() {
		assumeTrue(InstanceCreatorOracleJDK.getInstance().isUsable());
	}

	public static class NoDefaultConstructor {
		Integer x;

		public NoDefaultConstructor(final Integer x) {
			this.x = x;
		}
	}

	@Test
	void shouldCreateInstance() {
		NoDefaultConstructor obj = InstanceCreatorOracleJDK.getInstance().newInstance(NoDefaultConstructor.class);

		assertThat(obj, notNullValue());
		assertThat(obj.x, nullValue());
	}

	@Test
	void shouldCreateInstanceWithInstanceCreator() {
		NoDefaultConstructor obj = InstanceCreator.getInstance().newInstance(NoDefaultConstructor.class);

		assertThat(obj, notNullValue());
		assertThat(obj.x, nullValue());
	}

	public abstract static class Abstract {
		// empty
	}

	@Test
	void shouldThrowExceptionIfTryingToInstantiateAbstractClasses() {
		InstanceCreatorOracleJDK instanceCreator = InstanceCreatorOracleJDK.getInstance();
		assertThrows(ReflectionException.class, () -> instanceCreator.newInstance(Abstract.class));
	}

	@Test
	void shouldThrowExceptionIfTryingToInstantiateClassClass() {
		InstanceCreatorOracleJDK instanceCreator = InstanceCreatorOracleJDK.getInstance();
		assertThrows(ReflectionException.class, () -> instanceCreator.newInstance(Class.class));
	}

	public static class A {
		public A() {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	void shouldPropagateExceptionIfNewInstanceFails() throws Exception {
		InstanceCreatorOracleJDK instanceCreator = spy(InstanceCreatorOracleJDK.getInstance());
		Constructor<?> ctor = A.class.getDeclaredConstructor();
		doReturn(ctor).when(instanceCreator).newConstructorForSerialization(NoDefaultConstructor.class);

		assertThrows(ReflectionException.class, () -> instanceCreator.newInstance(NoDefaultConstructor.class));
	}

	@Test
	void shouldReturnTrueOnIsUsable() {
		assertTrue(InstanceCreatorOracleJDK.getInstance().isUsable());
	}

	@Test
	void shouldNotCreateInstanceIfInitializationOfNewSerializationConstructorFailed() {
		Method save = Fields.IgnoreAccess.getStatic(InstanceCreatorOracleJDK.class, NEW_CONSTRUCTOR_FOR_SERIALIZATION_METHOD_FIELD_NAME);
		Fields.IgnoreAccess.setStatic(InstanceCreatorOracleJDK.class, NEW_CONSTRUCTOR_FOR_SERIALIZATION_METHOD_FIELD_NAME, null);

		Exception caughtException = null;
		try {
			InstanceCreatorOracleJDK.getInstance().newInstance(NoDefaultConstructor.class);
		} catch (Exception e) {
			caughtException = e;
			assertThat(caughtException.getClass(), equalTo(ReflectionException.class));
		}
		Fields.IgnoreAccess.setStatic(InstanceCreatorOracleJDK.class, NEW_CONSTRUCTOR_FOR_SERIALIZATION_METHOD_FIELD_NAME, save);

		assertThat(caughtException, notNullValue());
	}

	@Test
	void shouldNotThrowExceptionIfInitFails() {
		for (Field field : InstanceCreatorOracleJDK.class.getDeclaredFields()) {
			if (!EXCLUDED_FIELDS.contains(field.getName())) {
				Fields.IgnoreAccess.setStatic(InstanceCreatorOracleJDK.class, field.getName(), null);
			}
		}

		InstanceCreatorOracleJDK.initializeMethods(UNDEFINED_CLASS_NAME);

		List<Object> staticFieldsValues = new ArrayList<>();
		for (Field field : InstanceCreatorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName)) {
				Object value = Fields.IgnoreAccess.getStatic(InstanceCreatorOracleJDK.class, field.getName());
				staticFieldsValues.add(value);
			}
		}
		assertFalse(InstanceCreatorOracleJDK.getInstance().isUsable());
		for (Object value : staticFieldsValues) {
			assertThat(value, nullValue());
		}

		InstanceCreatorOracleJDK.initializeMethods(InstanceCreatorOracleJDK.REFLECTION_FACTORY_CLASS_NAME);

		assertTrue(InstanceCreatorOracleJDK.getInstance().isUsable());
	}

	@Test
	void shouldReturnReflectionFactoryClassName() {
		assertThat(InstanceCreatorOracleJDK.REFLECTION_FACTORY_CLASS_NAME, equalTo("sun.reflect.ReflectionFactory"));
	}

	@Test
	void shouldHavePrivateDefaultConstructorInInstanceHolder() throws Exception {
		Class<?> instanceHolderClass = Class.forName(InstanceCreatorOracleJDK.class.getName() + "$InstanceHolder");
		Constructor<?> constructor = instanceHolderClass.getDeclaredConstructor();

		assertFalse(constructor.canAccess(null));

		try (MemberAccessor<Constructor<?>> ignored = new MemberAccessor<>(null, constructor)) {
			Object obj = constructor.newInstance();
			assertNotNull(obj);
		}
	}

	@Test
	void shouldKeepAccessModifiersUnchangedAfterCall() throws Exception {
		InstanceCreatorOracleJDK instanceCreator = spy(InstanceCreatorOracleJDK.getInstance());
		Constructor<?> ctor = C.class.getDeclaredConstructor();
		doReturn(ctor).when(instanceCreator).newConstructorForSerialization(NoDefaultConstructor.class);

		instanceCreator.newInstance(NoDefaultConstructor.class);

		assertFalse(ctor.canAccess(null));
	}
}
