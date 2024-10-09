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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.ConstantPool;
import org.morphix.reflection.Fields;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link ConstantPoolAccessorOracleJDK}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class ConstantPoolAccessorOracleJDKTest {

	private static final String GET_CONSTANT_POOL_METHOD = "getConstantPoolMethod";
	private static final String GET_SIZE_METHOD = "getSizeMethod";
	private static final String GET_METHOD_AT_METHOD = "getMethodAtMethod";

	private static final Set<String> EXCLUDED_FIELDS = Set.of(
			"CONSTANT_POOL_CLASS_NAME",
			"SHARED_SECRETS_CLASS_NAME");

	private Method getSizeMethod;

	private Method getMethodAtMethod;

	private Method getConstantPoolMethod;

	@Mock
	private ConstantPool<?> constantPool;

	@BeforeEach
	public void setUp() {
		try {
			getMethodAtMethod = ConstantPoolAccessorOracleJDKTest.class.getDeclaredMethod("getMethodAt", int.class);
			getSizeMethod = ConstantPoolAccessorOracleJDKTest.class.getDeclaredMethod("getSize");
			getConstantPoolMethod = ConstantPoolAccessorOracleJDKTest.class.getDeclaredMethod("getConstantPool");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void shouldNotThrowExceptionIfInitFails() {
		assumeTrue(ConstantPoolAccessorOracleJDK.getInstance().isUsable());

		for (Field field : ConstantPoolAccessorOracleJDK.class.getDeclaredFields()) {
			if (!EXCLUDED_FIELDS.contains(field.getName())) {
				Fields.IgnoreAccess.setStatic(ConstantPoolAccessorOracleJDK.class, field.getName(), null);
			}
		}

		ConstantPoolAccessorOracleJDK.initializeMethods("__");

		List<Object> staticFieldsValues = new ArrayList<>();
		for (Field field : ConstantPoolAccessorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName)) {
				Object value = Fields.IgnoreAccess.getStatic(ConstantPoolAccessorOracleJDK.class, field.getName());
				staticFieldsValues.add(value);
			}
		}
		assertFalse(ConstantPoolAccessorOracleJDK.getInstance().isUsable());
		for (Object value : staticFieldsValues) {
			assertThat(value, nullValue());
		}

		ConstantPoolAccessorOracleJDK.initializeMethods("jdk.internal.reflect.ConstantPool");

		assertTrue(ConstantPoolAccessorOracleJDK.getInstance().isUsable());
	}

	@SuppressWarnings({ "static-method", "unused" })
	private int getSize() {
		return Integer.MIN_VALUE;
	}

	@SuppressWarnings("unused")
	private Method getMethodAt(final int index) {
		return getMethodAtMethod;
	}

	@SuppressWarnings("unused")
	private static Object getConstantPool() {
		return ConstantPoolAccessorOracleJDKTest.class;
	}

	private static void runWithMockMethod(final String methodName, final Method method, final Object poolAccessor, final Runnable runnable) {
		Object previousMethod = Fields.IgnoreAccess.get(poolAccessor, methodName);
		Fields.IgnoreAccess.set(poolAccessor, methodName, method);
		try {
			runnable.run();
		} finally {
			Fields.IgnoreAccess.set(poolAccessor, methodName, previousMethod);
		}
	}

	private static void runWithNullMethod(final String methodName, final Object poolAccessor, final Runnable runnable) {
		runWithMockMethod(methodName, null, poolAccessor, runnable);
	}

	@Test
	void shouldReturnInstance() {
		ConstantPoolAccessorOracleJDK poolAccessor = ConstantPoolAccessorOracleJDK.getInstance();
		assertNotNull(poolAccessor);
	}

	@Test
	void shouldNotThrowExceptionIfMethodGetConstantPoolDoNotExist() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_CONSTANT_POOL_METHOD, poolAccessor, () -> {
			Object cp = poolAccessor.getConstantPool(getClass());
			assertThat(cp, equalTo(null));
		});
	}

	@Test
	void shouldCallGetConstantPoolFromAccessor() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithMockMethod(GET_CONSTANT_POOL_METHOD, getConstantPoolMethod, poolAccessor, () -> {
			Object pool = poolAccessor.getConstantPool(ConstantPoolAccessorOracleJDKTest.class);
			assertThat(pool, equalTo(ConstantPoolAccessorOracleJDKTest.class));
		});
	}

	@Test
	void shouldCallGetSizeMethodFromAccessor() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithMockMethod(GET_SIZE_METHOD, getSizeMethod, poolAccessor, () -> {
			int size = poolAccessor.getSize(this);
			assertThat(size, equalTo(Integer.MIN_VALUE));
		});
	}

	@Test
	void shouldNotThrowExceptionIfMethodGetSizeMethodDoNotExist() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_SIZE_METHOD, poolAccessor, () -> {
			int size = poolAccessor.getSize(constantPool);
			assertThat(size, equalTo(0));
		});
	}

	@Test
	void shouldNotThrowExceptionIfMethodGetMethodAtDoNotExist() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_METHOD_AT_METHOD, poolAccessor, () -> {
			Member member = poolAccessor.getMethodAt(constantPool, 0);
			assertThat(member, equalTo(null));
		});
	}

	@Test
	void shouldMakeAccessorUnusableIfMethodGetMethodAtDoNotExist() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_METHOD_AT_METHOD, poolAccessor, () -> {
			assertThat(poolAccessor.isUsable(), equalTo(false));
		});
	}

	@Test
	void shouldMakeAccessorUsableIfMethodGetMethodAtExists() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithMockMethod(GET_METHOD_AT_METHOD, getMethodAtMethod, poolAccessor, () -> {
			assertThat(poolAccessor.isUsable(), equalTo(true));
		});
	}

	@Test
	void shouldCallGetMethodAtFromAccessor() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithMockMethod(GET_METHOD_AT_METHOD, getMethodAtMethod, poolAccessor, () -> {
			Member method = poolAccessor.getMethodAt(this, 3);
			assertThat(method, equalTo(getMethodAtMethod));
		});
	}

	@Test
	void shouldReturnNotUsableIfGetMethodAtIsNull() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_METHOD_AT_METHOD, poolAccessor, () -> {
			boolean isUsable = poolAccessor.isUsable();
			assertThat(isUsable, equalTo(false));
		});
	}

	@Test
	void shouldNotInitializeMethodsIfConstantPoolNameDoesNotExist() {
		ConstantPoolAccessorOracleJDK poolAccessor = new ConstantPoolAccessorOracleJDK();

		runWithNullMethod(GET_METHOD_AT_METHOD, poolAccessor, () -> {
			ConstantPoolAccessorOracleJDK.initializeMethods("_");

			Object method = Fields.IgnoreAccess.get(poolAccessor, GET_METHOD_AT_METHOD);
			assertThat(method, equalTo(null));
		});

	}

	@Test
	void shouldReturnConstantPoolClassName() {
		assertThat(ConstantPoolAccessorOracleJDK.CONSTANT_POOL_CLASS_NAME, equalTo("jdk.internal.reflect.ConstantPool"));
	}

	@Test
	void shouldHavePrivateDefaultConstructorInInstanceHolder() throws Exception {
		Class<?> instanceHolderClass = Class.forName(ConstantPoolAccessorOracleJDK.class.getName() + "$InstanceHolder");
		Constructor<?> constructor = instanceHolderClass.getDeclaredConstructor();

		assertThat(constructor.canAccess(null), equalTo(false));

		try (MemberAccessor<Constructor<?>> ignored = new MemberAccessor<>(null, constructor)) {
			Object obj = constructor.newInstance();
			assertNotNull(obj);
		}
	}

}
