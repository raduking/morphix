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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Utility reflection methods for annotations.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Annotations {

	/**
	 * Overrides a specific attribute value of an annotation instance.
	 *
	 * @param <A> the type of the annotation
	 *
	 * @param annotation the annotation instance
	 * @param attribute the attribute name to override
	 * @param value the new value to set
	 */
	static <A extends Annotation> void overrideValue(final A annotation, final String attribute, final Object value) {
		InvocationHandler handler = Proxy.getInvocationHandler(annotation);
		try {
			Field memberValuesField = Fields.getOneDeclared(handler, "memberValues");
			Map<String, Object> memberValues = Fields.IgnoreAccess.get(handler, memberValuesField);
			memberValues.put(attribute, value);
		} catch (Exception e) {
			throw new ReflectionException("Failed to override annotation value", e);
		}
	}
}
