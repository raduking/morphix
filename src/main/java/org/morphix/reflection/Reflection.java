package org.morphix.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.morphix.Converter;

/**
 * Utility reflection methods for the {@link Converter}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Reflection {

	/**
	 * Unwraps an {@link InvocationTargetException} returning the
	 * {@link InvocationTargetException#getCause()}. If the cause is null no
	 * unwrapping is done and the exception is returned as is.
	 *
	 * @param e input exception
	 * @return the cause, if the cause is null the input exception is returned
	 */
	static Throwable unwrapInvocationTargetException(final InvocationTargetException e) {
		return null != e.getCause() ? e.getCause() : e;
	}

	/**
	 * Unwraps an exception returning the cause. If the cause is null no
	 * unwrapping is done and the exception is returned as is.
	 *
	 * @param e input exception
	 * @return the cause, if the cause is null the input exception is returned
	 */
	static Throwable unwrapException(final Throwable e) {
		return null == e.getCause() || e == e.getCause() ? e : unwrapException(e.getCause());
	}

	/**
	 * Returns the class with the given prefix.
	 *
	 * @param clazz the base class
	 * @param prefix the prefix which is added to the class
	 * @param <T> the type of the class with a prefix
	 * @return returns the class which has the name composed of the prefix +
	 *         {@link Class#getSimpleName()}. In case that class is not found, a
	 *         {@link ReflectionException}
	 */
	@SuppressWarnings("unchecked")
	static <T> Class<T> getClassWithPrefix(final Class<?> clazz, final String prefix) {
		String classWithPrefixName = clazz.getName().replace(clazz.getSimpleName(), prefix + clazz.getSimpleName());
		try {
			return (Class<T>) Class.forName(classWithPrefixName);
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Could not find class with prefix '" + classWithPrefixName + "'", e);
		}
	}

	/**
	 * Returns the value on the given field from the given object. It first
	 * tries to call a getter method if there is one otherwise will access the
	 * field directly.
	 *
	 * @param obj object on which the operation will be executed
	 * @param field field to get value from
	 * @return value of the field in source object
	 */
	static <T> T getFieldValue(final Object obj, final Field field) {
		// try a getter method first
		try {
			String getterMethodName = MethodType.GETTER.getMethodName(field);
			Method getterMethod = Methods.getDeclaredMethodInHierarchy(getterMethodName, obj.getClass());
			return Methods.invokeIgnoreAccess(getterMethod, obj);
		} catch (NoSuchMethodException e) {
			// if no getter found just get the value from field
			return Fields.getIgnoreAccess(obj, field);
		}
	}

	/**
	 * Sets the value for the given field on the given object. It first tries to
	 * call a setter method if there is one otherwise will access the field
	 * directly.
	 *
	 * @param obj object on which the operation will be executed
	 * @param field field to set value on
	 * @param value value to set on field
	 */
	static void setFieldValue(final Object obj, final Field field, final Object value) {
		// try a setter method first
		try {
			String setterMethodName = MethodType.SETTER.getMethodName(field);
			Method setterMethod = Methods.getDeclaredMethodInHierarchy(setterMethodName, obj.getClass(), field.getType());
			Methods.invokeIgnoreAccess(setterMethod, obj, value);
		} catch (NoSuchMethodException e) {
			// if no setter found just set the value to field
			Fields.setIgnoreAccess(obj, field, value);
		}
	}

	/**
	 * Sets the value for the given field on the given object. It first tries to
	 * call a setter method if there is one otherwise will access the field
	 * directly.
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
			Method setterMethod = Methods.getDeclaredMethodInHierarchy(setterMethodName, obj.getClass(), fieldType);
			Methods.invokeIgnoreAccess(setterMethod, obj, value);
		} catch (NoSuchMethodException e) {
			// if no setter found just set the value to field
			Field field = Fields.getDeclaredFieldInHierarchy(obj, fieldName);
			if (null != fieldName && null != field && field.getType().equals(fieldType)) {
				Fields.setIgnoreAccess(obj, field, value);
			}
		}
	}

	/**
	 * Casts the parameter to the required type. The advantage of this method is
	 * that no more {@link SuppressWarnings} is necessary and also the type is
	 * inferred by the compiler.
	 *
	 * @param o object to cast
	 * @return object cast to type T
	 */
	@SuppressWarnings("unchecked")
	static <T> T cast(final Object o) {
		return (T) o;
	}

	/**
	 * Returns the generic argument class from a class with a generic type. It
	 * uses a trick where the JRE retains the generic type information for
	 * method return types. The method will try to find a getter for the field
	 * specified and get the return type that keeps the generic information.
	 * <p>
	 * Example: for <code>List&lt;String&gt;</code> the method with index 0 will
	 * return <code>Class&lt;String&gt;</code>
	 *
	 * @param field field
	 * @param cls class
	 * @param index index of the generic argument
	 * @return class of the argument, null otherwise
	 */
	static <T extends Type> T getGenericArgumentType(final Field field, final Class<?> cls, final int index) {
		Method getterMethod;
		try {
			String getterMethodName = MethodType.GETTER.getMethodName(field);
			getterMethod = Methods.getDeclaredMethodInHierarchy(getterMethodName, cls);
		} catch (NoSuchMethodException e) {
			return null;
		}
		return Methods.getSafeGenericReturnType(getterMethod, index);
	}

	/**
	 * Returns a class based on a class name.
	 *
	 * @param className class name
	 * @return a class based on a class name
	 */
	@SuppressWarnings("unchecked")
	static <T> Class<T> getClass(final String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

}
