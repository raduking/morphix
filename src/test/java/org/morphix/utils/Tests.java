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

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.morphix.lang.JavaObjects;
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

}
