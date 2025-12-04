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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
	 * Returns a list with all the methods declared in the class given as parameter.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param cls class on which the methods are returned
	 * @return list of methods
	 */
	static <T> List<Method> getAllDeclared(final Class<T> cls) {
		return List.of(cls.getDeclaredMethods());
	}

	/**
	 * Returns a method in the class given as parameter, also searching in all its super classes.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param methodName name of the method to find
	 * @param cls class from where to start the search
	 * @param parameterTypes types of the method parameters
	 * @return found method
	 * @throws NoSuchMethodException if the method was not found
	 */
	static <T> Method getOneDeclaredInHierarchy(final String methodName, final Class<T> cls, final Class<?>... parameterTypes)
			throws NoSuchMethodException {
		try {
			return cls.getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			if (null == cls.getSuperclass()) {
				throw e;
			}
			return getOneDeclaredInHierarchy(methodName, cls.getSuperclass(), parameterTypes);
		}
	}

	/**
	 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes. This
	 * method does not return methods from interfaces or from {@link Object} class. This is a simpler version of
	 * {@link Complete#getAllDeclaredInHierarchy(Class)} because most of the times only the class hierarchy is needed.
	 * <p>
	 * {@link LinkedList} is used because:
	 * <ul>
	 * <li>it is more efficient in terms of memory consumption</li>
	 * <li>accessing the first and last has O(1) complexity</li>
	 * <li>more often than not, no random access is needed</li>
	 * <li>profiling: ~2 times faster than using {@link java.util.ArrayList}</li>
	 * </ul>
	 * The returned order of the methods are: class -> super class -> ... -> base class, and all methods in each class are
	 * returned in the declared order.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param cls class on which the methods are returned
	 * @return list of methods
	 */
	static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Method> methods = getAllDeclaredInHierarchy(cls.getSuperclass());

		Method[] declared = cls.getDeclaredMethods();
		for (int i = declared.length - 1; i >= 0; --i) {
			methods.addFirst(declared[i]);
		}
		return methods;
	}

	/**
	 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes that
	 * verify the given method predicate.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param cls class on which the methods are returned
	 * @param predicate filter predicate for methods
	 * @return list of methods
	 */
	static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls, final Predicate<? super Method> predicate) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Method> methods = getAllDeclaredInHierarchy(cls.getSuperclass(), predicate);

		Method[] declared = cls.getDeclaredMethods();
		for (int i = declared.length - 1; i >= 0; --i) {
			if (predicate.test(declared[i])) {
				methods.addFirst(declared[i]);
			}
		}
		return methods;
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
	 * @param index the zero-based index of the type needed (for a Map, the 2nd generic parameter has index 1)
	 * @return generic return type
	 */
	static <T extends Type> T getGenericReturnType(final Method method, final int index) {
		Type type = method.getGenericReturnType();
		if (!(type instanceof ParameterizedType parameterizedType)) {
			throw new ReflectionException(type.getTypeName() + " is a raw return type for method " + method.getDeclaringClass().getCanonicalName()
					+ "." + method.getName());
		}
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (index >= actualTypeArguments.length) {
			throw new ReflectionException("Could not find generic argument at index " + index + " for generic return type "
					+ parameterizedType.getTypeName() + " with " + actualTypeArguments.length + " generic argument(s) for method "
					+ method.getDeclaringClass().getCanonicalName() + "." + method.getName());
		}
		Type returnType = actualTypeArguments[index];
		return JavaObjects.cast(returnType);
	}

	/**
	 * Returns the generic return class for a method.
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
	 * @param index the zero-based index of the type needed (for a Map, the 2nd generic parameter has index 1)
	 * @return generic return class
	 */
	static <T> Class<T> getGenericReturnClass(final Method method, final int index) {
		try {
			return getGenericReturnType(method, index);
		} catch (ClassCastException e) {
			throw new ReflectionException("Could not infer actual generic return type argument from " + method.getGenericReturnType().getTypeName() +
					" for method " + method.getDeclaringClass().getCanonicalName() + "." + method.getName(), e);
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
	 * fetched lazily, so you don't construct the full stack trace before checking the first method.
	 *
	 * @param withClassName flag to prepend class name
	 * @return the currently executing method name
	 */
	static String getCurrentMethodName(final boolean withClassName) {
		return getCurrentMethodName(withClassName, 2);
	}

	/**
	 * Returns the currently executing method name. The advantage of this method is that each {@link StackTraceElement} is
	 * fetched lazily, so you don't construct the full stack trace before checking the first method.
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
	 * Returns the caller method name.
	 *
	 * @param nameFunction function to build the name from the class name and method name
	 * @return optional method name
	 */
	static Optional<String> getCallerMethodName(final BiFunction<? super String, ? super String, String> nameFunction) {
		// skip the calling method, this and the below call to get above
		return getCallerMethodName(3, nameFunction);
	}

	/**
	 * Returns the caller method name.
	 *
	 * @param skipFrames skips the given number of frames starting from the current frame
	 * @param nameFunction function to build the name from the class name and method name
	 * @return optional method name
	 */
	static Optional<String> getCallerMethodName(final int skipFrames, final BiFunction<? super String, ? super String, String> nameFunction) {
		StackWalker walker = StackWalker.getInstance();
		return walker.walk(frames -> frames
				.skip(skipFrames) // skip frames
				.findFirst())
				.map(frame -> nameFunction.apply(frame.getClassName(), frame.getMethodName()));
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
		// skip this and the below call to get above
		return getCallerMethodName(supplier, 2, nameFunction);
	}

	/**
	 * Returns the caller method name for a given supplier.
	 *
	 * @param <T> supplier return type
	 *
	 * @param supplier value supplier
	 * @param skipFrames skips the given number of frames starting from the current frame
	 * @param nameFunction function to build the name from the class name and method name
	 * @return optional method name
	 */
	static <T> Optional<String> getCallerMethodName(final Supplier<T> supplier, final int skipFrames,
			final BiFunction<? super String, ? super String, String> nameFunction) {
		String packageName = supplier.getClass().getPackageName();
		StackWalker walker = StackWalker.getInstance();
		return walker.walk(frames -> frames
				.skip(skipFrames) // skip frames
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
		try {
			return Methods.getOneDeclaredInHierarchy(methodName, cls, field.getType());
		} catch (NoSuchMethodException ignored) {
			// ignored because we try with primitive type next
		}
		Class<?> primitiveFieldType;
		try {
			primitiveFieldType = Primitives.toPrimitive(field.getType());
		} catch (ReflectionException re) {
			throw new ReflectionException("Error finding method: "
					+ methodName + "(" + field.getType().getCanonicalName() + ")", re);
		}
		try {
			return Methods.getOneDeclaredInHierarchy(methodName, cls, primitiveFieldType);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Error finding method: "
					+ methodName + "(" + field.getType().getCanonicalName() + ") or "
					+ methodName + "(" + primitiveFieldType.getCanonicalName() + ")", e);
		}
	}

	/**
	 * Returns the functional interface method if the given class is a functional interface.
	 *
	 * @param <T> type to get the method.
	 *
	 * @param cls functional interface class
	 * @return the functional interface method if the given class is a functional interface
	 * @throws ReflectionException if the class is not a functional interface
	 */
	static <T> Method getFunctionalInterfaceMethod(final Class<T> cls) {
		Method singleAbstractMethod = null;
		for (Method method : cls.getMethods()) {
			if (Modifier.isAbstract(method.getModifiers())) {
				if (null != singleAbstractMethod) {
					throw new ReflectionException(cls + " is not a functional interface because it has more than one abstract method");
				}
				singleAbstractMethod = method;
			}
		}
		if (null == singleAbstractMethod) {
			throw new ReflectionException(cls + " is not a functional interface because it has no abstract method");
		}
		return singleAbstractMethod;
	}

	/**
	 * Interface which groups all methods that ignore access modifiers.
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
			try (MemberAccessor<Method> ignored = new MemberAccessor<>(obj, method)) {
				return JavaObjects.cast(method.invoke(obj, args));
			} catch (InvocationTargetException e) {
				// e is just a wrapper on the real exception, escalate the real one
				Throwable cause = Reflection.unwrapInvocationTargetException(e);
				String className = method.getDeclaringClass().getCanonicalName();
				if (null != obj) {
					className = obj instanceof Class<?> cls ? cls.getCanonicalName() : obj.getClass().getCanonicalName();
				}
				throw new ReflectionException(cause.getMessage() + ". Error invoking " + className + "." + method.getName(), e);
			} catch (Exception e) {
				// escalate any exception invoking the method
				String className = method.getDeclaringClass().getCanonicalName();
				if (null != obj) {
					className = obj instanceof Class<?> cls ? cls.getCanonicalName() : obj.getClass().getCanonicalName();
				}
				throw new ReflectionException(e.getMessage() + ". Error invoking " + className + "." + method.getName(), e);
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
		static <T, A extends Annotation> void invokeWithAnnotation(final T obj, final Class<A> annotationClass) {
			List<Method> methods = Methods.getAllDeclaredInHierarchy(obj.getClass(), withAnnotation(annotationClass));
			for (Method method : methods) {
				invoke(method, obj);
			}
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
			try (MemberAccessor<Method> ignored = new MemberAccessor<>(obj, method)) {
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
	}

	/**
	 * Interface which groups all methods that return null and don't throw exceptions on expected errors. This functions as
	 * a name space so that the method names inside keep the same name pattern.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Safe {

		/**
		 * Returns the method with the given name and given parameter types from the given class. If the method is not present
		 * in the class it returns {@code null}.
		 *
		 * @param <T> type to get the method from
		 *
		 * @param methodName the name of the method
		 * @param cls class containing the method
		 * @param parameterTypes parameter types
		 * @return the method with the given name
		 */
		static <T> Method getOneDeclared(final String methodName, final Class<T> cls, final Class<?>... parameterTypes) {
			if (null == cls || null == methodName) {
				return null;
			}
			try {
				return cls.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				return null;
			}
		}

		/**
		 * Returns the method with the given name and given parameter types from the given class. If the method is not present
		 * in the class it returns {@code null}.
		 *
		 * @param methodName the name of the method
		 * @param obj object containing the method
		 * @param parameterTypes parameter types
		 * @return the method with the given name
		 */
		static Method getOneDeclared(final String methodName, final Object obj, final Class<?>... parameterTypes) {
			if (null == obj) {
				return null;
			}
			Class<?> clazz = obj instanceof Class<?> cls ? cls : obj.getClass();
			return Safe.getOneDeclared(methodName, clazz, parameterTypes);
		}

		/**
		 * Returns a method in the class given as parameter, also searching in all its super classes, and returns {@code null}
		 * if method is not found.
		 *
		 * @param <T> type to get the methods from
		 *
		 * @param methodName name of the method to find
		 * @param cls class from where to start the search
		 * @param parameterTypes types of the method parameters
		 * @return found method, null otherwise
		 */
		static <T> Method getOneDeclaredInHierarchy(final String methodName, final Class<T> cls, final Class<?>... parameterTypes) {
			try {
				return cls.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				if (null == cls.getSuperclass()) {
					return null;
				}
				return Safe.getOneDeclaredInHierarchy(methodName, cls.getSuperclass(), parameterTypes);
			}
		}

		/**
		 * Returns the generic return type for a method or null if the method has no generic return type.
		 * <p>
		 * Note: this method can still throw a {@link ClassCastException} if the generic return type cannot be cast to the
		 * desired.
		 *
		 * @param <T> generic return type
		 *
		 * @param method method for which the generic return type is needed
		 * @param index the zero-based index of the type needed (for a Map, the 2nd generic parameter has index 1)
		 * @return generic return type
		 */
		static <T extends Type> T getGenericReturnType(final Method method, final int index) {
			Type type = method.getGenericReturnType();
			if (!(type instanceof ParameterizedType parameterizedType)) {
				return null;
			}
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (index >= actualTypeArguments.length) {
				return null;
			}
			Type returnType = actualTypeArguments[index];
			return JavaObjects.cast(returnType);
		}
	}

	/**
	 * Namespace interface which groups all methods that handle correctly cyclic dependencies in the class hierarchy
	 * including interfaces.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Complete {

		/**
		 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes and
		 * interfaces.
		 *
		 * @param <T> type to get the methods from
		 *
		 * @param cls class on which the methods are returned
		 * @return list of methods
		 */
		static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls) {
			return getAllDeclaredInHierarchy(cls, Classes.mutableSetOf());
		}

		/**
		 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes and
		 * interfaces.
		 * <p>
		 * Note: the excluded set is also used to avoid cyclic dependencies in the class hierarchy.
		 *
		 * @param <T> type to get the methods from
		 *
		 * @param cls class on which the methods are returned
		 * @param excluded non null mutable set of classes/interfaces/enums/records to be excluded
		 * @return list of methods
		 */
		static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls, final Set<Class<?>> excluded) {
			try {
				if (cls == null || excluded.contains(cls)) {
					return new LinkedList<>();
				}
				excluded.add(cls);
			} catch (UnsupportedOperationException e) {
				throw new ReflectionException("The excluded set is unmodifiable. Please provide a non null modifiable set.", e);
			} catch (NullPointerException e) {
				throw new ReflectionException("The excluded set is null. Please provide a non null modifiable set.", e);
			}

			List<Method> methods = getAllDeclaredInHierarchy(cls.getSuperclass(), excluded);
			for (Class<?> iface : cls.getInterfaces()) {
				methods.addAll(getAllDeclaredInHierarchy(iface, excluded));
			}
			Method[] declared = cls.getDeclaredMethods();
			for (int i = declared.length - 1; i >= 0; --i) {
				methods.addFirst(declared[i]);
			}

			return methods;
		}
	}
}
