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
package org.morphix.invoke;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.ReflectionException;

/**
 * Utility reflection static methods for finding and invoking Java {@link MethodHandle}s.
 * <p>
 * This class provides static methods for efficient lookup, caching, and invocation of method handles. It also supports
 * wrapping method handles into functional interfaces using {@link LambdaMetafactory}.
 * </p>
 * <p>
 * All lookup and handle creation operations are cached per-class to minimize reflective overhead.
 * </p>
 *
 * Example usage:
 *
 * <pre>{@code
 * MethodHandle handle = HandleMethods.getStaticMethod(MyClass.class, "staticMethod", void.class);
 * HandleMethods.invoke(handle);
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public class HandleMethods {

	/**
	 * Cache for Lookup instances per class.
	 */
	private static final ConcurrentMap<Class<?>, MethodHandles.Lookup> LOOKUP_CACHE = new ConcurrentHashMap<>();

	/**
	 * Cache for resolved {@link MethodHandle}s per class and method signature.
	 */
	private static final ConcurrentMap<Class<?>, ConcurrentMap<MethodSignature, MethodHandle>> METHOD_CACHE = new ConcurrentHashMap<>();

	/**
	 * Retrieves a {@link MethodHandle} for an instance method of the given class. The result is cached for subsequent
	 * calls.
	 *
	 * @param <T> the type of the class.
	 *
	 * @param name the name of the method.
	 * @param cls the class where the method is declared.
	 * @param returnType the return type of the method.
	 * @param parameterTypes the parameter types of the method.
	 * @return a {@link MethodHandle} pointing to the specified method.
	 * @throws ReflectionException If the method cannot be found or accessed.
	 */
	public static <T> MethodHandle getMethod(final String name, final Class<T> cls, final Class<?> returnType, final Class<?>... parameterTypes) {
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		return getMethod(name, cls, methodType, false);
	}

	/**
	 * Retrieves a {@link MethodHandle} for a static method of the given class. The result is cached for subsequent calls.
	 *
	 * @param <T> the type of the class.
	 *
	 * @param name the name of the method.
	 * @param cls the class where the static method is declared.
	 * @param returnType the return type of the method.
	 * @param parameterTypes the parameter types of the method.
	 * @return A {@link MethodHandle} pointing to the specified static method.
	 * @throws ReflectionException If the method cannot be found or accessed.
	 */
	public static <T> MethodHandle getStaticMethod(final String name, final Class<T> cls, final Class<?> returnType,
			final Class<?>... parameterTypes) {
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		return getMethod(name, cls, methodType, true);
	}

	/**
	 * Invokes the given {@link MethodHandle} with the provided arguments. This method automatically handles exceptions and
	 * type casting.
	 *
	 * @param <T> the expected return type.
	 *
	 * @param handle the method handle to invoke.
	 * @param args the arguments to pass to the method.
	 * @return the result of the method invocation, cast to the expected type.
	 * @throws ReflectionException If invocation fails.
	 */
	public static <T> T invoke(final MethodHandle handle, final Object... args) {
		try {
			Object result = handle.invokeWithArguments(args);
			return JavaObjects.cast(result);
		} catch (Throwable t) {
			throw new ReflectionException("Error invoking method " + handle, t);
		}
	}

	/**
	 * Retrieves a {@link MethodHandle} for a static method of the given class. The result is cached for subsequent calls.
	 *
	 * @param <T> the type of the class.
	 *
	 * @param methodName the name of the method.
	 * @param cls the class where the static method is declared.
	 * @param methodType the type of the method.
	 * @param isStatic flag to specify if the method is static or not
	 * @return A {@link MethodHandle} pointing to the specified static method.
	 * @throws ReflectionException If the method cannot be found or accessed.
	 */
	private static MethodHandle getMethod(final String methodName, final Class<?> cls, final MethodType methodType, final boolean isStatic) {
		ConcurrentMap<MethodSignature, MethodHandle> classMethods = METHOD_CACHE
				.computeIfAbsent(cls, k -> new ConcurrentHashMap<>());

		MethodSignature signature = MethodSignature.of(cls, methodName, methodType, isStatic);
		return classMethods.computeIfAbsent(signature, k -> createHandle(cls, signature));
	}

	/**
	 * Creates a {@link MethodHandle} for the given method signature using the appropriate lookup.
	 *
	 * @param cls the class where the static method is declared.
	 * @param signature the method signature
	 * @return a method handle for the given class and signature
	 */
	private static MethodHandle createHandle(final Class<?> cls, final MethodSignature signature) {
		try {
			Lookup lookup = getLookup(cls);
			return signature.isStatic()
					? lookup.findStatic(cls, signature.name(), signature.type())
					: lookup.findVirtual(cls, signature.name(), signature.type());
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new ReflectionException("Method handle creation failed for " + cls.getName() + "#" + signature.name(), e);
		}
	}

	/**
	 * Retrieves a {@link Lookup} instance for the given class from cache, or creates it if not present.
	 *
	 * @param cls class to retrieve the lookup for
	 * @return a lookup instance for the given class from cache
	 */
	private static <T> Lookup getLookup(final Class<T> cls) {
		return LOOKUP_CACHE.computeIfAbsent(cls, HandleMethods::getPrivateLookupIn);
	}

	/**
	 * Retrieves a private {@link Lookup} object for the given class. This is required for accessing private or
	 * package-private members. The result is cached for subsequent calls.
	 *
	 * @param cls The target class.
	 * @param <T> The type of the class.
	 * @return A private lookup for the given class.
	 * @throws ReflectionException If the lookup cannot be obtained.
	 */
	public static <T> Lookup getPrivateLookupIn(final Class<T> cls) {
		try {
			return MethodHandles.privateLookupIn(cls, MethodHandles.lookup());
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Failed to get lookup for " + cls + " because "
					+ cls.getModule() + " does not open " + cls.getPackage(), e);
		}
	}

	/**
	 * Hide constructor.
	 */
	private HandleMethods() {
		// empty
	}
}
