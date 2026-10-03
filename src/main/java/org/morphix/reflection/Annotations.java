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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
	 * Returns the given annotation as found on the given method or, if the method does not carry it, on the nearest
	 * declaration of the same method in its class and interface hierarchy.
	 * <p>
	 * Java does not inherit method annotations, so a method overriding a superclass or implementing an interface method
	 * only carries the annotations of its own declaration. This method is meant for the annotations that describe the
	 * method itself, in a contract-like fashion, and where an annotation declared once on the general declaration is
	 * expected to be honored by all the more derived ones. Use {@link Method#getAnnotation(Class)} instead for annotations
	 * describing a particular declaration only.
	 * <p>
	 * The hierarchy is searched starting from the given method's declaring class, up through its super classes and then
	 * through its interfaces, including their super interfaces, so the most derived declaration wins. The class hierarchy
	 * is searched before the interfaces because a class method always overrides an interface method.
	 * <p>
	 * A declaration matches the given method if it has the same name and the same parameter types. The return type is
	 * deliberately ignored, so that covariant overrides and implementations of generic methods are matched as well.
	 * <p>
	 * Note: if several interfaces declare the same annotation on the same method, the one returned is the first found,
	 * following the traversal order of {@link Interfaces#getAll(Class)}, which is rooted in the unspecified
	 * {@link Class#getInterfaces()} order.
	 * <p>
	 * There is no need to qualify the result with a count, as there is for
	 * {@link Methods#getOneDeclaredInHierarchy(String, Class, Class[])}: a single element carries at most one instance of a
	 * given annotation type. The one exception are {@link java.lang.annotation.Repeatable repeatable} annotations, which
	 * are stored in their container and are therefore not visible to this method, not even in the declaration that holds
	 * them.
	 *
	 * @param <T> annotation type
	 *
	 * @param method method to get the annotation for
	 * @param annotationClass annotation class
	 * @return the annotation if it is present in the method's hierarchy, null otherwise
	 */
	public static <T extends Annotation> T getInHierarchy(final Method method, final Class<T> annotationClass) {
		T annotation = method.getAnnotation(annotationClass);
		if (null != annotation) {
			return annotation;
		}
		Class<?> declaringClass = method.getDeclaringClass();
		for (Class<?> cls = declaringClass.getSuperclass(); null != cls && Object.class != cls; cls = cls.getSuperclass()) {
			annotation = getDeclaredIn(method, cls, annotationClass);
			if (null != annotation) {
				return annotation;
			}
		}
		for (Class<?> iface : Interfaces.getAll(declaringClass)) {
			annotation = getDeclaredIn(method, iface, annotationClass);
			if (null != annotation) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Returns the given annotation as declared on the same method, if any, in the given type.
	 *
	 * @param <T> annotation type
	 *
	 * @param method method to look for in the given type
	 * @param type type to look the method up in
	 * @param annotationClass annotation class
	 * @return the annotation if it is present, null otherwise
	 */
	private static <T extends Annotation> T getDeclaredIn(final Method method, final Class<?> type, final Class<T> annotationClass) {
		Method declared = Methods.Safe.getOneDeclared(method.getName(), type, method.getParameterTypes());
		if (null == declared) {
			return null;
		}
		return declared.getAnnotation(annotationClass);
	}

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
		if (null == attribute || attribute.isBlank()) {
			throw new IllegalArgumentException("Attribute name must be non-null and non-blank.");
		}
		if (null == annotation) {
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
