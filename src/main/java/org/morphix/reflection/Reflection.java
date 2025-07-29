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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.morphix.lang.JavaObjects;

/**
 * Utility reflection methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Reflection {

	/**
	 * Unwraps an exception returning the cause. If the cause is {@code null} no unwrapping is done and the exception is
	 * returned as is.
	 *
	 * @param e input exception
	 * @return the cause, if the cause is {@code null} the input exception is returned
	 */
	static Throwable unwrapException(final Throwable e) {
		return null == e.getCause() || e == e.getCause() ? e : unwrapException(e.getCause());
	}

	/**
	 * Unwraps an {@link InvocationTargetException} returning the {@link InvocationTargetException#getCause()}. If the cause
	 * is {@code null} no unwrapping is done and the exception is returned as is.
	 *
	 * @param e input exception
	 * @return the cause, if the cause is {@code null} the input exception is returned
	 */
	static Throwable unwrapInvocationTargetException(final InvocationTargetException e) {
		return null != e.getCause() ? e.getCause() : e;
	}

	/**
	 * Returns the class with the given prefix.
	 *
	 * @param <T> the type of the class with a prefix
	 *
	 * @param clazz the base class
	 * @param prefix the prefix which is added to the class
	 * @return returns the class which has the name composed of the prefix + {@link Class#getSimpleName()}. In case that
	 * class is not found, a {@link ReflectionException}
	 */
	static <T> Class<T> getClassWithPrefix(final Class<?> clazz, final String prefix) {
		String classWithPrefixName = clazz.getName().replace(clazz.getSimpleName(), prefix + clazz.getSimpleName());
		try {
			return JavaObjects.cast(Class.forName(classWithPrefixName));
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not find class with prefix '" + classWithPrefixName + "'", e);
		}
	}

	/**
	 * Returns the value on the given field from the given object. It first tries to call a getter method if there is one
	 * otherwise will access the field directly.
	 *
	 * @param <T> field value type
	 *
	 * @param obj object on which the operation will be executed
	 * @param field field to get value from
	 * @return value of the field in the source object
	 */
	static <T> T getFieldValue(final Object obj, final Field field) {
		// try a getter method first
		try {
			String getterMethodName = MethodType.GETTER.getMethodName(field);
			Method getterMethod = Methods.getOneDeclaredInHierarchy(getterMethodName, obj.getClass());
			return Methods.IgnoreAccess.invoke(getterMethod, obj);
		} catch (NoSuchMethodException e) {
			// if no getter is found, just get the value from the field
			return Fields.IgnoreAccess.get(obj, field);
		}
	}

	/**
	 * Sets the value for the given field on the given object. It first tries to call a setter method if there is one
	 * otherwise will access the field directly.
	 *
	 * @param obj object on which the operation will be executed
	 * @param field field to set value on
	 * @param value value to set on field
	 */
	static void setFieldValue(final Object obj, final Field field, final Object value) {
		// try a setter method first
		try {
			String setterMethodName = MethodType.SETTER.getMethodName(field);
			Method setterMethod = Methods.getOneDeclaredInHierarchy(setterMethodName, obj.getClass(), field.getType());
			Methods.IgnoreAccess.invoke(setterMethod, obj, value);
		} catch (NoSuchMethodException e) {
			// if no setter found just set the value to field
			Fields.IgnoreAccess.set(obj, field, value);
		}
	}

	/**
	 * Sets the value for the given field on the given object. It first tries to call a setter method if there is one
	 * otherwise will access the field directly.
	 *
	 * @param <T> object type
	 * @param <F> field type
	 * @param <U> value type
	 *
	 * @param obj object on which the operation will be executed
	 * @param fieldName field name to set value on
	 * @param fieldType field type
	 * @param value value to set on field
	 */
	static <T, F, U> void setFieldValue(final T obj, final String fieldName, final Class<F> fieldType, final U value) {
		// try a setter method first
		try {
			String setterMethodName = MethodType.SETTER.getMethodName(fieldName);
			Method setterMethod = Methods.getOneDeclaredInHierarchy(setterMethodName, obj.getClass(), fieldType);
			Methods.IgnoreAccess.invoke(setterMethod, obj, value);
		} catch (NoSuchMethodException e) {
			// if no setter found just set the value to field
			Field field = Fields.getOneDeclaredInHierarchy(obj, fieldName);
			if (null != fieldName && null != field && field.getType().equals(fieldType)) {
				Fields.IgnoreAccess.set(obj, field, value);
			}
		}
	}

	/**
	 * Returns the subclass of the expected parent.
	 *
	 * @param expectedParent expected parent
	 * @param child some child class
	 * @return the subclass of the expected parent
	 */
	static Class<?> findSubclass(final Class<?> expectedParent, final Class<?> child) {
		Class<?> parent = child.getSuperclass();
		if (Object.class == parent) {
			throw new ReflectionException("The parent of " + child.getCanonicalName() + " is not a " + expectedParent.getCanonicalName());
		}
		if (expectedParent == parent) {
			return child;
		}
		return findSubclass(expectedParent, parent);
	}

	/**
	 * Returns a class based on a class name.
	 *
	 * @param <T> returned type
	 *
	 * @param className class name
	 * @return a class based on a class name
	 */
	static <T> Class<T> getClass(final String className) {
		try {
			return JavaObjects.cast(Class.forName(className));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

}
