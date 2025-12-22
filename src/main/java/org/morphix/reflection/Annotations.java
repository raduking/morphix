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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Utility reflection methods for annotations.
 *
 * @author Radu Sebastian LAZIN
 */
public class Annotations {

	/**
	 * The name of the internal field holding annotation member values.
	 */
	static final String FIELD_NAME_MEMBER_VALUES = "memberValues";

	/**
	 * Overrides a specific attribute value of an annotation instance.
	 * <p>
	 * This class uses deep reflection to modify the values of annotation attributes by accessing the internal data
	 * structures of the JDK's annotation proxy implementation.
	 * <p>
	 * To use this class, applications <strong>must</strong> provide:
	 *
	 * <pre>
	 *   --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED
	 * </pre>
	 *
	 * Without this JVM argument, any attempt to access the {@code memberValues} field will fail.
	 *
	 * @param <A> the type of the annotation
	 *
	 * @param annotation the annotation instance
	 * @param attribute the attribute name to override
	 * @param value the new value to set
	 * @throws ReflectionException if the internal map cannot be accessed or modified (typically due to missing
	 *     {@code --add-opens} flags)
	 */
	public static <A extends Annotation> void overrideValue(final A annotation, final String attribute, final Object value) {
		if (attribute == null || attribute.isBlank()) {
			throw new IllegalArgumentException("Attribute name must be non-null and non-blank.");
		}
		if (annotation == null) {
			throw new ReflectionException("Failed to override annotation: annotation instance is null (possibly not retained at runtime).");
		}
		try {
			InvocationHandler handler = Proxy.getInvocationHandler(annotation);
			Map<String, Object> memberValues = Fields.IgnoreAccess.get(handler, FIELD_NAME_MEMBER_VALUES);
			memberValues.put(attribute, value);
		} catch (Exception e) {
			throw new ReflectionException(e, "Failed to override annotation: {}.{}() value.",
					annotation.annotationType().getCanonicalName(), attribute);
		}
	}

	/**
	 * Hide constructor.
	 */
	private Annotations() {
		throw Constructors.unsupportedOperationException();
	}
}
