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
package org.morphix.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Properties;
import java.util.function.BooleanSupplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.retry.Wait;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Utility methods for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Tests {

	static <T extends Throwable> T verifyDefaultConstructorThrows(final Class<?> cls) {
		ReflectionException reflectionException =
				assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(cls));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		return JavaObjects.cast(invocationTargetException.getCause());
	}

	static Properties loadProperties(final String filePath) {
		try (FileInputStream input = new FileInputStream(filePath)) {
			Properties properties = new Properties();
			properties.load(input);
			return properties;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load project properties", e);
		}
	}

	static void waitUntil(final BooleanSupplier condition, final Duration timeout, final Duration pollInterval) {
		boolean conditionMet = Threads.waitUntil(condition, timeout, pollInterval);
		if (!conditionMet) {
			fail("Condition not met within: " + timeout);
		}
	}

	static void waitUntil(final BooleanSupplier condition, final Duration timeout) {
		waitUntil(condition, timeout, Wait.Default.POLL_INTERVAL);
	}

	static void waitUntil(final BooleanSupplier condition) {
		waitUntil(condition, Duration.ZERO, Wait.Default.POLL_INTERVAL);
	}
}
