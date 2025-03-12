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

import static org.morphix.reflection.predicates.MemberPredicates.withAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.morphix.lang.JavaObjects;

/**
 * Utility reflection static methods for java methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Methods {

	/**
	 * Returns a method in the class given as parameter, also searching in all it's super classes.
	 *
	 * @param methodName name of the
	 * @param cls class on which the fields are returned
	 * @param parameterTypes types of the method parameters
	 * @return found method
	 * @throws NoSuchMethodException if the method was not found
	 */
	static Method getDeclaredMethodInHierarchy(final String methodName, final Class<?> cls, final Class<?>... parameterTypes)
			throws NoSuchMethodException {
		try {
			return cls.getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			if (null == cls.getSuperclass()) {
				throw e;
			}
			return getDeclaredMethodInHierarchy(methodName, cls.getSuperclass(), parameterTypes);
		}
	}

	/**
	 * Returns a method in the class given as parameter, also searching in all it's super classes.
	 *
	 * @param methodName name of the
	 * @param cls class on which the fields are returned
	 * @param parameterTypes types of the method parameters
	 * @return found method, null otherwise
	 */
	static Method getSafeDeclaredMethodInHierarchy(final String methodName, final Class<?> cls, final Class<?>... parameterTypes) {
		try {
			return cls.getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			if (null == cls.getSuperclass()) {
				return null;
			}
			return getSafeDeclaredMethodInHierarchy(methodName, cls.getSuperclass(), parameterTypes);
		}
	}

	/**
	 * Returns a list with all the fields in the class given as parameter including the ones in all it's super classes.
	 * <p>
	 * {@link LinkedList} is used because:
	 * <ul>
	 * <li>it is more efficient in terms of memory consumption</li>
	 * <li>accessing the first and last has O(1) complexity</li>
	 * <li>more often than not no random access is needed</li>
	 * <li>profiling: ~2 times faster than using {@link java.util.ArrayList}</li>
	 * </ul>
	 * The returned order of the methods are: class -> super class -> ... -> base class and all methods in each class are
	 * returned in the declared order.
	 *
	 * @param cls class on which the fields are returned
	 * @return list of fields
	 */
	static List<Method> getDeclaredMethodsInHierarchy(final Class<?> cls) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Method> methods = getDeclaredMethodsInHierarchy(cls.getSuperclass());
		methods.addAll(0, List.of(cls.getDeclaredMethods()));
		return methods;
	}

	/**
	 * Returns a list with all the methods in the class given as parameter including the ones in all it's super classes
	 * which verify the given method predicate.
	 *
	 * @param cls class on which the fields are returned
	 * @param predicate filter predicate for methods
	 * @return list of fields
	 */
	static List<Method> getDeclaredMethodsInHierarchy(final Class<?> cls, final Predicate<? super Method> predicate) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Method> methods = getDeclaredMethodsInHierarchy(cls.getSuperclass(), predicate);
		for (Method method : cls.getDeclaredMethods()) {
			if (predicate.test(method)) {
				methods.add(method);
			}
		}
		return methods;
	}

	/**
	 * Invokes the given method on the given object with parameters, throws just the original exception message when needed.
	 *
	 * @param <T> object type on which the method is invoked
	 * @param <R> method return type
	 *
	 * @param obj object on which the method is invoked
	 * @param method method to be invoked
	 * @param args method arguments
	 * @return result of the method invocation
	 */
	static <T, R> R invokeWithOriginalException(final Method method, final T obj, final Object... args) {
		try (MemberAccessor<Method> methodAccessor = new MemberAccessor<>(obj, method)) {
			return JavaObjects.cast(method.invoke(obj, args));
		} catch (InvocationTargetException e) {
			// e is just a wrapper on the real exception, escalate the real one
			Throwable cause = Reflection.unwrapInvocationTargetException(e);
			throw new ReflectionException(cause.getMessage(), e);
		} catch (Exception e) {
			// escalate any exception invoking the method
			throw new ReflectionException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the generic return type for a method.
	 * <p>
	 * Example: for the following method:
	 *
	 * <pre>
	 * List&lt;String&gt; foo() {
	 * 	// ...
	 * }
	 * </pre>
	 *
	 * <code>getGenericReturnType(fooMethod, 0)</code> will return <code>String.class</code>.
	 *
	 * @param <T> generic return type
	 *
	 * @param method method for which the generic return type is needed
	 * @param index the zero based index of the type needed (for a Map the 2nd generic parameter has index 1)
	 * @return generic return type
	 */
	static <T extends Type> T getGenericReturnType(final Method method, final int index) {
		Type type = method.getGenericReturnType();
		if (!(type instanceof ParameterizedType parameterizedType)) {
			throw new ReflectionException(type.getTypeName() + " is a raw return type for method " + method.getDeclaringClass().getCanonicalName()
					+ "." + method.getName());
		}
		Type returnType = parameterizedType.getActualTypeArguments()[index];
		return JavaObjects.cast(returnType);
	}

	/**
	 * Returns the generic return type for a method or null if method has no generic return type.
	 *
	 * @param <T> generic return type
	 *
	 * @param method method for which the generic return type is needed
	 * @param index the zero based index of the type needed (for a Map the 2nd generic parameter has index 1)
	 * @return generic return type
	 */
	static <T extends Type> T getSafeGenericReturnType(final Method method, final int index) {
		try {
			return Methods.getGenericReturnType(method, index);
		} catch (ReflectionException e) {
			return null;
		}
	}

	/**
	 * Returns the generic return type for a method.
	 * <p>
	 * Example: for the following method:
	 *
	 * <pre>
	 * List&lt;String&gt; foo() {
	 * 	// ...
	 * }
	 * </pre>
	 *
	 * <code>getGenericReturnClass(fooMethod, 0)</code> will return <code>String.class</code>.
	 *
	 * @param <T> generic return type
	 *
	 * @param method method for which the generic return type is needed
	 * @param index the zero based index of the type needed (for a Map the 2nd generic parameter has index 1)
	 * @return generic return type
	 */
	static <T> Class<T> getGenericReturnClass(final Method method, final int index) {
		Type type = method.getGenericReturnType();
		if (!(type instanceof ParameterizedType parameterizedType)) {
			throw new ReflectionException(type.getTypeName() + " is a raw return type for method " + method.getDeclaringClass().getCanonicalName()
					+ "." + method.getName());
		}
		Type returnType = parameterizedType.getActualTypeArguments()[index];
		try {
			return JavaObjects.cast(returnType);
		} catch (ClassCastException e) {
			throw new ReflectionException("Could not infer actual generic return type argument from " + parameterizedType.getTypeName() +
					" for method " + method.getDeclaringClass().getCanonicalName() + "." + method.getName(), e);
		}
	}

	/**
	 * Invokes all methods that are annotated with the given annotation. The annotated method should have no parameters and
	 * should return <code>void</code>
	 *
	 * @param <T> object type
	 * @param <A> annotation type
	 *
	 * @param obj object on which to invoke the methods
	 * @param annotationClass annotation class
	 */
	static <T, A extends Annotation> void invokeMethodsWithAnnotation(final T obj, final Class<A> annotationClass) {
		List<Method> methods = Methods.getDeclaredMethodsInHierarchy(obj.getClass(), withAnnotation(annotationClass));
		for (Method method : methods) {
			Methods.IgnoreAccess.invoke(method, obj);
		}
	}

	/**
	 * Returns the method string.
	 *
	 * @param method method to transform
	 * @return method string
	 */
	static String toSimpleString(final Method method) {
		List<String> parameterTypes = Stream.of(method.getParameterTypes())
				.map(Class::getName)
				.toList();
		return method.getName() + "(" + String.join(", ", parameterTypes) + ")";
	}

	/**
	 * Returns the currently executing method name. The advantage of this method is that each {@link StackTraceElement} is
	 * fetched lazily, so you don't actually construct the full stack trace before checking the first method.
	 *
	 * @param withClassName flag to prepend class name
	 * @return the currently executing method name
	 */
	static String getCurrentMethodName(final boolean withClassName) {
		return getCurrentMethodName(withClassName, 2);
	}

	/**
	 * Returns the currently executing method name. The advantage of this method is that each {@link StackTraceElement} is
	 * fetched lazily, so you don't actually construct the full stack trace before checking the first method.
	 *
	 * @param withClassName flag to prepend class name
	 * @param depth the depth of the caller method, for the direct caller this should be 1
	 * @return the currently executing method name
	 */
	static String getCurrentMethodName(final boolean withClassName, final int depth) {
		StackWalker walker = StackWalker.getInstance();
		return walker.walk(frames -> frames
				.skip(depth)
				.findFirst()
				.map(stackFrame -> {
					String methodName = stackFrame.getMethodName();
					if (withClassName) {
						methodName = stackFrame.getClassName() + "." + methodName;
					}
					return methodName;
				}))
				.orElse(null);
	}

	/**
	 * Returns the caller method name for a given supplier.
	 *
	 * @param <T> supplier return type
	 *
	 * @param supplier value supplier
	 * @param nameFunction function to build the name from the class name and method name
	 * @return optional method name
	 */
	static <T> Optional<String> getCallerMethodName(final Supplier<T> supplier,
			final BiFunction<? super String, ? super String, String> nameFunction) {
		String packageName = supplier.getClass().getPackageName();
		StackWalker walker = StackWalker.getInstance();
		return walker.walk(frames -> frames
				.skip(1) // skip this method
				.filter(frame -> frame.getClassName().startsWith(packageName))
				.findFirst())
				.map(frame -> nameFunction.apply(frame.getClassName(), frame.getMethodName()));
	}

	/**
	 * Returns the setter method for the given field from the given class.
	 *
	 * @param <T> type containing the setter
	 *
	 * @param cls class from which to search for the setter
	 * @param field field to get the setter from
	 * @return the setter method for the given field
	 */
	static <T> Method getSetterMethod(final Class<T> cls, final Field field) {
		String methodName = MethodType.SETTER.getMethodName(field);
		Class<?> primitiveFieldType;
		try {
			return Methods.getDeclaredMethodInHierarchy(methodName, cls, field.getType());
		} catch (NoSuchMethodException e) {
			try {
				primitiveFieldType = Primitives.toPrimitive(field.getType());
			} catch (ReflectionException re) {
				throw new ReflectionException("Error finding method: "
						+ methodName + "(" + field.getType().getCanonicalName() + ")", re);
			}
		}
		try {
			return Methods.getDeclaredMethodInHierarchy(methodName, cls, primitiveFieldType);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Error finding method: "
					+ methodName + "(" + field.getType().getCanonicalName() + ") or "
					+ methodName + "(" + primitiveFieldType.getCanonicalName() + ")", e);
		}
	}

	/**
	 * Interface which groups all methods that ignore constructor access modifiers.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface IgnoreAccess {

		/**
		 * Invokes the given method on the given object with parameters, ignoring the access modifiers.
		 *
		 * @param <T> object type on which the method is invoked
		 * @param <R> method return type
		 *
		 * @param obj object on which the method is invoked
		 * @param method method to be invoked
		 * @param args method arguments
		 * @return result of the method invocation
		 */
		static <T, R> R invoke(final Method method, final T obj, final Object... args) {
			try (MemberAccessor<Method> methodAccessor = new MemberAccessor<>(obj, method)) {
				return JavaObjects.cast(method.invoke(obj, args));
			} catch (InvocationTargetException e) {
				// e is just a wrapper on the real exception, escalate the real one
				Throwable cause = Reflection.unwrapInvocationTargetException(e);
				String className = method.getDeclaringClass().getCanonicalName();
				if (null != obj) {
					className = obj instanceof Class ? ((Class<?>) obj).getCanonicalName() : obj.getClass().getCanonicalName();
				}
				throw new ReflectionException(cause.getMessage() + ". Error invoking " + className + "." + method.getName(), e);
			} catch (Exception e) {
				// escalate any exception invoking the method
				String className = method.getDeclaringClass().getCanonicalName();
				if (null != obj) {
					className = obj instanceof Class ? ((Class<?>) obj).getCanonicalName() : obj.getClass().getCanonicalName();
				}
				throw new ReflectionException(e.getMessage() + ". Error invoking " + className + "." + method.getName(), e);
			}
		}

	}

}
