package org.morphix.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Enum declaring standard method types.
 * <p>
 * For the getter methods the Generic Converter uses the 'is' convention
 * for both <code>boolean</code> and {@link Boolean} types.
 *
 * @author Radu Sebastian LAZIN
 */
public enum MethodType {

	GETTER(Prefix.GET, 0,
			TypePrefixConvention.of(Boolean.class, Prefix.IS),
			TypePrefixConvention.of(boolean.class, Prefix.IS)),
	SETTER(Prefix.SET, 1);

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
		String prefix = getDefaultPrefix();
		if (methodName.startsWith(prefix)) {
			return getFieldName(prefix, methodName);
		}
		for (Map.Entry<Class<?>, String> typeConventionEntry : typeConventions.entrySet()) {
			String fieldName = getFieldName(typeConventionEntry.getValue(), methodName);
			if (null != fieldName) {
				return fieldName;
			}
		}
		return null;
	}

	/**
	 * Returns a field name based on a prefix and a method name.
	 * <p>
	 * The method will return <code>null</code> if the following conditions are
	 * met:
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
		if (prefix == null
				|| methodName == null
				|| Objects.equals(prefix, methodName)
				|| !methodName.startsWith(prefix)) {
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
			String methodName = method.getName();
			boolean hasPrefix = methodName.startsWith(getDefaultPrefix());
			for (Map.Entry<Class<?>, String> typeConventionPrefix : typeConventions.entrySet()) {
				hasPrefix |= methodName.startsWith(typeConventionPrefix.getValue());
			}
			return hasPrefix && method.getParameterCount() == getParameterCount();
		};
	}

	/**
	 * Prefix name for type convention.
	 *
	 * @param <T> type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class TypePrefixConvention<T> {

		private final Class<T> type;
		private final String prefix;

		public TypePrefixConvention(final Class<T> type, final String prefix) {
			this.type = type;
			this.prefix = prefix;
		}

		public String getPrefix() {
			return prefix;
		}

		public Class<T> getType() {
			return type;
		}

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

		public static final String GET = "get";
		public static final String SET = "set";
		public static final String IS = "is";

		private Prefix() {
			// empty
		}

	}

}
