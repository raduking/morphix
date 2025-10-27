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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Enumeration declaring standard method types.
 *
 * @author Radu Sebastian LAZIN
 */
public enum MethodType {

	/**
	 * Getter method type.
	 */
	GETTER(Prefix.GET, ParameterCount.GETTER,
			TypePrefixConvention.of(Boolean.class, Prefix.IS),
			TypePrefixConvention.of(boolean.class, Prefix.IS)),

	/**
	 * Setter method type.
	 */
	SETTER(Prefix.SET, ParameterCount.SETTER);

	/**
	 * Name space class for parameter counts.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class ParameterCount {

		/**
		 * Getter parameter count.
		 */
		public static final int GETTER = 0;

		/**
		 * Setter parameter count.
		 */
		public static final int SETTER = 1;

		/**
		 * Hide constructor.
		 */
		private ParameterCount() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Default method prefix when no other type convention is met.
	 */
	private final String defaultPrefix;

	/**
	 * Map of type conventions specifying prefix for any specific type.
	 */
	private final Map<Class<?>, String> typeConventions;

	/**
	 * Number of parameters the type has.
	 */
	private final int parameterCount;

	/**
	 * Constructor with default prefix and type conventions.
	 *
	 * @param defaultPrefix default prefix
	 */
	MethodType(final String defaultPrefix, final int parameterCount, final TypePrefixConvention<?>... conventions) {
		this.defaultPrefix = defaultPrefix;
		this.typeConventions = new HashMap<>();
		this.parameterCount = parameterCount;
		for (TypePrefixConvention<?> convention : conventions) {
			this.typeConventions.put(convention.getType(), convention.getPrefix());
		}
	}

	/**
	 * Returns the default prefix.
	 *
	 * @return the default prefix
	 */
	public String getDefaultPrefix() {
		return this.defaultPrefix;
	}

	/**
	 * Returns the prefix for a given field.
	 *
	 * @param field field to get prefix for
	 * @return the prefix for a given field
	 */
	public String getPrefix(final Field field) {
		if (null == field) {
			return getDefaultPrefix();
		}
		return getPrefix(field.getType());
	}

	/**
	 * Returns the prefix for a given field type.
	 *
	 * @param fieldType field to get prefix for
	 * @return the prefix for a given field type
	 */
	public String getPrefix(final Class<?> fieldType) {
		if (null == fieldType) {
			return getDefaultPrefix();
		}
		return this.typeConventions.getOrDefault(fieldType, getDefaultPrefix());
	}

	/**
	 * Returns the parameter count of this method type.
	 *
	 * @return the parameter count of this method type
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * Returns the default prefix.
	 *
	 * @return the default prefix
	 */
	@Override
	public String toString() {
		return getDefaultPrefix();
	}

	/**
	 * Builds a method name based on a field.
	 *
	 * @param field field from which the method name will be built
	 * @return method name
	 */
	public String getMethodName(final Field field) {
		return getMethodName(null != field ? getPrefix(field) : null, field);
	}

	/**
	 * Builds a method name based on a field name.
	 *
	 * @param fieldName field name from which the method name will be built
	 * @return method name
	 */
	public String getMethodName(final String fieldName) {
		return getMethodName(getDefaultPrefix(), fieldName);
	}

	/**
	 * Builds a method name based on a field.
	 *
	 * @param prefix prefix to prepend
	 * @param field field from which the method name will be built
	 * @return method name
	 */
	public static String getMethodName(final String prefix, final Field field) {
		return getMethodName(prefix, null != field ? field.getName() : null);
	}

	/**
	 * Builds a method name based on a field.
	 *
	 * @param prefix prefix to prepend
	 * @param fieldName field name from which the method name will be built
	 * @return method name
	 */
	public static String getMethodName(final String prefix, final String fieldName) {
		if (null == prefix || null == fieldName) {
			return null;
		}
		return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

	/**
	 * Returns a field name based on a method.
	 *
	 * @param method method
	 * @return a field name based on a method
	 */
	public String getFieldName(final Method method) {
		String methodName = method.getName();
		String fieldName = getFieldName(getDefaultPrefix(), methodName);
		if (null != fieldName) {
			return fieldName;
		}
		for (Map.Entry<Class<?>, String> typeConventionEntry : typeConventions.entrySet()) {
			fieldName = getFieldName(typeConventionEntry.getValue(), methodName);
			if (null != fieldName) {
				return fieldName;
			}
		}
		if (GETTER == this && method.getParameterCount() == getParameterCount()) {
			return whenRecordAccessor(method, () -> methodName, () -> null);
		}
		return null;
	}

	/**
	 * Returns a field name based on a prefix and a method name.
	 * <p>
	 * The method will return <code>null</code> if the following conditions are met:
	 * <ul>
	 * <li>any of the parameters are <code>null</code></li>
	 * <li>prefix is equal to methodName</li>
	 * <li>methodName does not start with prefix</li>
	 * </ul>
	 *
	 * @param prefix prefix for the method name
	 * @param methodName method name from which to extract the field name
	 * @return a field name based on a prefix and a method name
	 */
	public static String getFieldName(final String prefix, final String methodName) {
		if (prefix == null || methodName == null || !startsWithStrict(prefix, methodName)) {
			return null;
		}
		int index = prefix.length();
		return methodName.substring(index, index + 1).toLowerCase() + methodName.substring(index + 1);
	}

	/**
	 * Returns a method type predicate.
	 *
	 * @return a method type predicate
	 */
	public Predicate<Method> getPredicate() {
		return method -> {
			if (method.getParameterCount() != getParameterCount()) {
				return false;
			}
			String methodName = method.getName();
			if (startsWithStrict(getDefaultPrefix(), methodName)) {
				return true;
			}
			for (Map.Entry<Class<?>, String> typeConventionPrefix : typeConventions.entrySet()) {
				if (startsWithStrict(typeConventionPrefix.getValue(), methodName)) {
					return true;
				}
			}
			if (GETTER == this) {
				return whenRecordAccessor(method, () -> true, () -> false);
			}
			return false;
		};
	}

	/**
	 * Returns true if the method has the specified prefix and has more characters than the prefix.
	 *
	 * @param prefix the prefix to check
	 * @param methodName the method name
	 * @return true if the method has the specified prefix, false otherwise
	 */
	private static boolean startsWithStrict(final String prefix, final String methodName) {
		return !Objects.equals(prefix, methodName) && methodName.startsWith(prefix);
	}

	/**
	 * Evaluates the given suppliers based on whether the method is a record accessor.
	 *
	 * @param <T> the result type
	 *
	 * @param method the method to evaluate
	 * @param onAccessor the supplier to invoke if the method is a record accessor
	 * @param onNonAccessor the supplier to invoke otherwise
	 * @return the result of {@code onAccessor} for a record accessor, otherwise the result of {@code onNonAccessor}
	 */
	private static <T> T whenRecordAccessor(final Method method, final Supplier<T> onAccessor, final Supplier<T> onNonAccessor) {
		Class<?> declaringClass = method.getDeclaringClass();
		if (declaringClass.isRecord()) {
			// for records consider only generated methods as getters
			for (var component : declaringClass.getRecordComponents()) {
				if (component.getAccessor().equals(method)) {
					return onAccessor.get();
				}
			}
		}
		return onNonAccessor.get();
	}

	/**
	 * Prefix name for type convention.
	 *
	 * @param <T> type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class TypePrefixConvention<T> {

		/**
		 * The type.
		 */
		private final Class<T> type;

		/**
		 * The prefix associated with the type.
		 */
		private final String prefix;

		/**
		 * Constructor with all fields.
		 *
		 * @param type type to add convention to
		 * @param prefix method prefix
		 */
		protected TypePrefixConvention(final Class<T> type, final String prefix) {
			this.type = type;
			this.prefix = prefix;
		}

		/**
		 * Returns the method prefix.
		 *
		 * @return the method prefix
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * Returns the type for this convention.
		 *
		 * @return the type for this convention
		 */
		public Class<T> getType() {
			return type;
		}

		/**
		 * Builds a new type prefix convention.
		 *
		 * @param <T> type for the resulting convention
		 *
		 * @param type type to add convention to
		 * @param prefix method prefix
		 * @return a new type prefix convention
		 */
		public static <T> TypePrefixConvention<T> of(final Class<T> type, final String prefix) {
			return new TypePrefixConvention<>(type, prefix);
		}
	}

	/**
	 * Holds the prefix constants.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Prefix {

		/**
		 * Get prefix.
		 */
		public static final String GET = "get";

		/**
		 * Set prefix.
		 */
		public static final String SET = "set";

		/**
		 * Is prefix.
		 */
		public static final String IS = "is";

		/**
		 * Hide constructor.
		 */
		private Prefix() {
			throw Constructors.unsupportedOperationException();
		}

	}

}
