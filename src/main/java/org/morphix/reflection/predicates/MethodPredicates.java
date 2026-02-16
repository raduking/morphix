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
package org.morphix.reflection.predicates;

import static org.morphix.convert.handler.PrimitiveAssignment.isPrimitiveToClass;
import static org.morphix.reflection.predicates.MemberPredicates.hasName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.MethodType;
import org.morphix.reflection.MethodType.Prefix;

/**
 * Method predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class MethodPredicates {

	/**
	 * Private constructor.
	 */
	private MethodPredicates() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a predicate that verifies if the method:
	 * <ul>
	 * <li>is <code>public</code></li>
	 * <li>is <code>static</code></li>
	 * <li>has only one parameter of type srcClass</li>
	 * <li>has the return type the class the method is declared in</li>
	 * </ul>
	 *
	 * @param <T> parameter type
	 *
	 * @param parameterClass the parameter type of the converter method
	 * @return a predicate
	 */
	public static <T> Predicate<Method> isConverterMethod(final Class<T> parameterClass) {
		return method -> {
			int modifiers = method.getModifiers();
			// method must be public and static
			if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
				return false;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				return false;
			}
			Class<?> methodDeclaringClass = method.getDeclaringClass();
			Class<?> returnType = method.getReturnType();
			if (!methodDeclaringClass.isAssignableFrom(returnType)) {
				return false;
			}
			Class<?> methodParameterClass = parameterTypes[0];
			if (methodParameterClass.isPrimitive()) {
				return isPrimitiveToClass(methodParameterClass, parameterClass);
			}
			return parameterClass.isAssignableFrom(methodParameterClass);
		};
	}

	/**
	 * Returns a predicate for method with given return type and parameters.
	 *
	 * @param returnType return type of the method
	 * @param paramTypes parameter types
	 * @return predicate
	 */
	public static Predicate<Method> hasSignature(final Class<?> returnType, final Class<?>... paramTypes) {
		return method -> {
			Class<?>[] methodParamTypes = method.getParameterTypes();
			if (methodParamTypes.length != paramTypes.length) {
				return false;
			}
			boolean isMethodWithParams = method.getReturnType().isAssignableFrom(returnType);
			for (int i = 0; i < methodParamTypes.length; ++i) {
				isMethodWithParams &= methodParamTypes[i].isAssignableFrom(paramTypes[i]);
			}
			return isMethodWithParams;
		};
	}

	/**
	 * Returns a predicate for method with given return type and parameters.
	 *
	 * @param returnType return type of the method
	 * @param paramTypes parameter types
	 * @return predicate
	 */
	public static Predicate<Method> isMethodWith(final Class<?> returnType, final Class<?>... paramTypes) {
		return hasSignature(returnType, paramTypes);
	}

	/**
	 * Returns a predicate that checks if the method is a getter for the given field.
	 *
	 * @param field for which to check getter
	 * @return a predicate that checks if the method is a getter for the given field
	 */
	public static Predicate<Method> isGetter(final Field field) {
		return isMethodWith(field.getType()).and(
				hasName(MethodType.GETTER.getMethodName(field))
						.or(hasName(MethodType.getMethodName(Prefix.GET, field))));
	}
}
