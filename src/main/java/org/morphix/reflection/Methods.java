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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.morphix.reflection.predicates.MemberPredicates;

/**
 * Utility reflection static methods for java methods. The philosophy of this class is to provide methods that cover
 * most of the use cases when dealing with methods through reflection and only throwing exceptions when something
 * unexpected happens (like invoking a method). For methods that return {@code null} instead of throwing exceptions, see
 * {@link Methods.Safe}.
 * <p>
 * Methods like {@link #getOneDeclaredInHierarchy(String, Class, Class[])} return {@code null} if the method is not
 * found because most of the time the caller just needs to check for {@code null} and not to handle exceptions (the
 * implementation actually delegates to {@link Methods.Safe}).
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
		if (null == cls) {
			return List.of();
		}
		return List.of(cls.getDeclaredMethods());
	}

	/**
	 * Returns a list with all the methods declared in the class given as parameter that verify the given method predicate.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param cls class on which the methods are returned
	 * @param predicate filter predicate for methods
	 * @return list of methods
	 */
	static <T> List<Method> getAllDeclared(final Class<T> cls, final Predicate<? super Method> predicate) {
		if (null == cls || null == predicate) {
			return List.of();
		}
		Method[] declared = cls.getDeclaredMethods();
		if (declared.length == 0) {
			return List.of();
		}
		List<Method> methods = new LinkedList<>();
		for (int i = declared.length - 1; i >= 0; --i) {
			if (predicate.test(declared[i])) {
				methods.addFirst(declared[i]);
			}
		}
		return methods;
	}

	/**
	 * Returns the method with the given name and given parameter types from the given class.
	 *
	 * @param <T> type to get the method from
	 *
	 * @param methodName the name of the method
	 * @param cls class containing the method
	 * @param parameterTypes parameter types
	 * @return the method with the given name, {@code null} if it is not found or if the class or the method name is
	 * {@code null}
	 */
	static <T> Method getOneDeclared(final String methodName, final Class<T> cls, final Class<?>... parameterTypes) {
		return Safe.getOneDeclared(methodName, cls, parameterTypes);
	}

	/**
	 * Returns a method in the class given as parameter, also searching in all its super classes, including {@link Object}.
	 * This method does not return methods from super interfaces. Use {@link Complete#getAllDeclaredInHierarchy(Class)} for
	 * the full interface hierarchy.
	 * <p>
	 * The method returns {@code null} if no method is found. The philosophy of this method is to return the method or
	 * {@code null} and not to throw exceptions because most of the time the method may not be present and the caller just
	 * needs to check for {@code null}.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param methodName name of the method to find
	 * @param cls class from where to start the search
	 * @param parameterTypes types of the method parameters
	 * @return found method
	 */
	static <T> Method getOneDeclaredInHierarchy(final String methodName, final Class<T> cls, final Class<?>... parameterTypes) {
		return Safe.getOneDeclaredInHierarchy(methodName, cls, parameterTypes);
	}

	/**
	 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes,
	 * including {@link Object}. This method does not return methods from super interfaces. This is a simpler version of
	 * {@link Complete#getAllDeclaredInHierarchy(Class)}, which also walks interfaces and can exclude specific classes.
	 * <p>
	 * Note: if the class given as parameter is an interface then the methods declared by that interface are returned, but
	 * not the ones declared by its super interfaces, since those are interfaces as well. Use
	 * {@link Complete#getAllDeclaredInHierarchy(Class)} for the full interface hierarchy.
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
		if (null == cls) {
			return List.of();
		}
		// an interface has no super class, but it does declare methods of its own
		Class<?> superClass = cls.getSuperclass();
		List<Method> methods = null == superClass ? new LinkedList<>() : getAllDeclaredInHierarchy(superClass);
		Method[] declared = cls.getDeclaredMethods();
		for (int i = declared.length - 1; i >= 0; --i) {
			methods.addFirst(declared[i]);
		}
		return methods;
	}

	/**
	 * Returns a list with all the methods in the class given as parameter including the ones in all its super classes,
	 * including {@link Object}, that verify the given method predicate. This method does not return methods from super
	 * interfaces.
	 *
	 * @param <T> type to get the methods from
	 *
	 * @param cls class on which the methods are returned
	 * @param predicate filter predicate for methods
	 * @return list of methods
	 */
	static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls, final Predicate<? super Method> predicate) {
		if (null == cls || null == predicate) {
			return List.of();
		}
		// an interface has no super class, but it does declare methods of its own
		Class<?> superClass = cls.getSuperclass();
		List<Method> methods = null == superClass ? new LinkedList<>() : getAllDeclaredInHierarchy(superClass, predicate);
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
	 * @throws ReflectionException if the method is null, if the method has a raw return type or if the generic return type
	 *     cannot be found at the given index
	 */
	static <T extends Type> T getGenericReturnType(final Method method, final int index) {
		Reflection.requireNotNull(method, "Cannot get generic return type: method descriptor is null");
		Type type = method.getGenericReturnType();
		if (!(type instanceof ParameterizedType parameterizedType)) {
			throw new ReflectionException(
					"Return type {} is not parameterized for method {}.{}",
					type.getTypeName(), method.getDeclaringClass().getCanonicalName(), method.getName());
		}
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (index < 0 || index >= actualTypeArguments.length) {
			throw new ReflectionException(
					"Cannot get generic argument at index {} for return type {} ({} arguments) of method {}.{}",
					index, parameterizedType.getTypeName(), actualTypeArguments.length,
					method.getDeclaringClass().getCanonicalName(), method.getName());
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
	 * @throws ReflectionException if the method is {@code null} or if the generic return type cannot be cast to a Class
	 */
	static <T> Class<T> getGenericReturnClass(final Method method, final int index) {
		Reflection.requireNotNull(method, "Cannot get generic return class: method descriptor is null");
		try {
			return getGenericReturnType(method, index);
		} catch (ClassCastException e) {
			throw new ReflectionException(e,
					"Cannot get generic return class for method {}.{}: type argument at index {} is not a Class",
					method.getDeclaringClass().getCanonicalName(), method.getName(), index);
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
	 * fetched lazily, so you don't construct the full stack trace before checking the first method. If the current method
	 * name is not found, {@code null} is returned.
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
				.filter(frame -> packageName.isEmpty()
						? !frame.getClassName().contains(".")
						: frame.getClassName().startsWith(packageName + '.'))
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
	 * @throws ReflectionException if no setter method is found
	 */
	static <T> Method getSetterMethod(final Class<T> cls, final Field field) {
		Reflection.requireNotNull(cls, "Cannot find setter method: class is null");
		Reflection.requireNotNull(field, "Cannot find setter method in class {}: field descriptor is null", cls);
		String methodName = MethodType.SETTER.getMethodName(field);
		Class<?> fieldType = field.getType();
		Method method = Methods.getOneDeclaredInHierarchy(methodName, cls, fieldType);
		if (null != method) {
			return method;
		}
		Class<?> alternateType;
		if (fieldType.isPrimitive()) {
			alternateType = Primitives.fromPrimitive(fieldType);
		} else if (Primitives.isUnboxable(fieldType)) {
			alternateType = Primitives.toPrimitive(fieldType);
		} else {
			throw new ReflectionException("Cannot find setter method {}({}) in class {}", methodName, fieldType.getTypeName(), cls);
		}
		method = Methods.getOneDeclaredInHierarchy(methodName, cls, alternateType);
		if (null == method) {
			throw new ReflectionException(
					"Cannot find setter method {}({}) or {}({}) in class {}",
					methodName, fieldType.getTypeName(), methodName, alternateType.getTypeName(), cls);
		}
		return method;
	}

	/**
	 * Returns the functional interface method if the given class is a functional interface.
	 *
	 * @param <T> type to get the method.
	 *
	 * @param cls functional interface class
	 * @return the functional interface method if the given class is a functional interface
	 * @throws ReflectionException if the class is {@code null} or if it is not a functional interface
	 */
	static <T> Method getFunctionalInterfaceMethod(final Class<T> cls) {
		Reflection.requireNotNull(cls, "Cannot find functional interface method: class is null");
		Method singleAbstractMethod = null;
		for (Method method : cls.getMethods()) {
			if (JavaModifier.ABSTRACT.isPresentOn(method)) {
				if (null != singleAbstractMethod) {
					throw new ReflectionException("Cannot find functional interface method in {}: multiple abstract methods found", cls);
				}
				singleAbstractMethod = method;
			}
		}
		return Reflection.requireNotNull(singleAbstractMethod, "Cannot find functional interface method in {}: no abstract method found", cls);
	}

	/**
	 * Invokes the given method on the given object with parameters.
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
		try {
			return JavaObjects.cast(method.invoke(obj, args));
		} catch (InvocationTargetException e) {
			// e is just a wrapper on the real exception, escalate the real one
			Throwable cause = Reflection.unwrapInvocationTargetException(e);
			String className = Classes.getCanonicalName(method, obj);
			throw new ReflectionException(e, "Cannot invoke method {}.{}: {}", className, method.getName(), cause.getMessage());
		} catch (Exception e) {
			// escalate any exception invoking the method
			String className = Classes.getCanonicalName(method, obj);
			throw new ReflectionException(e, "Cannot invoke method {}.{}: {}", className, method.getName(), e.getMessage());
		}
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
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <T, R> R invoke(final Method method, final T obj, final Object... args) {
			Reflection.requireNotNull(method, "Cannot invoke method: method descriptor is null");
			return Unchecked.invoke(method, obj, args);
		}

		/**
		 * Invokes all methods that are annotated with the given annotation. The annotated method should have no parameters and
		 * should return <code>void</code>
		 * <p>
		 * Note: if the object supplied is a {@link Class} then there is no instance to invoke on, so only the annotated
		 * {@code static} methods are invoked and the annotated instance methods are skipped. This mirrors
		 * {@link Fields.Safe#get(Object, String)}, where a {@link Class} argument also selects the static members.
		 * <p>
		 * Note: this method does not reach the methods declared by the interfaces of the class, use
		 * {@link Complete#getAllDeclaredInHierarchy(Class, Set)} based variants if the interface hierarchy is needed.
		 *
		 * @param <T> object type
		 * @param <A> annotation type
		 *
		 * @param obj object on which to invoke the methods, or the {@link Class} declaring them
		 * @param annotationClass annotation class
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <T, A extends Annotation> void invokeWithAnnotation(final T obj, final Class<A> annotationClass) {
			Class<?> cls = Classes.getFrom(obj);
			List<Method> methods = Methods.getAllDeclaredInHierarchy(cls, MemberPredicates.withAnnotation(annotationClass));
			// when obj is the class itself there is no instance, so only its static methods can be invoked
			boolean instanceAvailable = obj != cls;
			for (Method method : methods) {
				if (instanceAvailable || JavaModifier.STATIC.isPresentOn(method)) {
					Unchecked.invoke(method, obj);
				}
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
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <T, R> R invokeWithOriginalException(final Method method, final T obj, final Object... args) {
			Reflection.requireNotNull(method, "Cannot invoke method with original exception: method descriptor is null");
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

		/**
		 * Invokes the given static method on the given class with parameters, ignoring the access modifiers.
		 *
		 * @param <T> class type on which the method is invoked
		 * @param <R> method return type
		 *
		 * @param method method to be invoked
		 * @param cls class on which the method is invoked
		 * @param args method arguments
		 * @return result of the method invocation
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <T, R> R invokeStatic(final Method method, final Class<T> cls, final Object... args) {
			Reflection.requireNotNull(method, "Cannot invoke static method on class {}: method descriptor is null", cls);
			if (JavaModifier.STATIC.isNotPresentOn(method)) {
				throw new ReflectionException("Cannot invoke static method '{}' on class {}: method is not static", method.getName(), cls);
			}
			return Unchecked.invokeStatic(method, args);
		}
	}

	/**
	 * Interface which groups all methods that return {@code null} and don't throw exceptions on expected errors. This
	 * functions as a name space so that the method names inside keep the same name pattern.
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
			Class<?> cls = Classes.getFrom(obj);
			return Safe.getOneDeclared(methodName, cls, parameterTypes);
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
		 * @return found method, {@code null} otherwise
		 */
		static <T> Method getOneDeclaredInHierarchy(final String methodName, final Class<T> cls, final Class<?>... parameterTypes) {
			if (null == cls || null == methodName) {
				return null;
			}
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
		 * Returns the generic return type for a method or {@code null} if the method has no generic return type.
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
			if (null == method) {
				return null;
			}
			Type type = method.getGenericReturnType();
			if (!(type instanceof ParameterizedType parameterizedType)) {
				return null;
			}
			Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (index < 0 || index >= actualTypeArguments.length) {
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
		 * interfaces. Pass classes in the excluded set of {@link #getAllDeclaredInHierarchy(Class, Set)} to leave a specific
		 * class out of the walk.
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
		 * Note: the excluded set is mutated to track visited types and avoid duplicate traversal. A class in that set,
		 * including {@link Object}, is left out of the walk together with everything above it.
		 * <p>
		 * Note: a {@code null} class short-circuits the walk and returns an empty list, so in that case the excluded set is
		 * never validated and no exception is thrown for it.
		 *
		 * @param <T> type to get the methods from
		 *
		 * @param cls class on which the methods are returned
		 * @param excluded non-{@code null} mutable set of classes/interfaces/enums/records to be excluded
		 * @return list of methods
		 * @throws ReflectionException if the excluded set is {@code null} or unmodifiable
		 */
		static <T> List<Method> getAllDeclaredInHierarchy(final Class<T> cls, final Set<Class<?>> excluded) {
			if (null == cls) {
				return new LinkedList<>();
			}
			Reflection.requireNotNull(excluded, "Cannot traverse method hierarchy for class {}: excluded set is null", cls);
			if (excluded.contains(cls)) {
				return new LinkedList<>();
			}
			try {
				excluded.add(cls);
			} catch (UnsupportedOperationException e) {
				throw new ReflectionException(e, "Cannot traverse method hierarchy for class {}: excluded set is unmodifiable", cls);
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

	/**
	 * Name space interface for methods that ignore access modifiers and do not do validations and emphasize speed. The
	 * caller is responsible to provide valid input and to handle exceptions.
	 * <p>
	 * For example, when invoking a method on a class that is known to have that method, or when invoking a method in a loop
	 * and the overhead of handling exceptions is too high.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Unchecked {

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
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <T, R> R invoke(final Method method, final T obj, final Object... args) {
			return MemberAccessor.on(obj, method, () -> Methods.invoke(method, obj, args));
		}

		/**
		 * Invokes the given static method on the given class with parameters, ignoring the access modifiers.
		 *
		 * @param <R> method return type
		 *
		 * @param method method to be invoked
		 * @param args method arguments
		 * @return result of the method invocation
		 * @throws ReflectionException if any error occurs during method invocation
		 */
		static <R> R invokeStatic(final Method method, final Object... args) {
			return Unchecked.invoke(method, null, args);
		}
	}
}
