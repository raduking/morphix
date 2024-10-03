package org.morphix.reflection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class to transform primitives to their respective boxed class
 * back and forth.
 *
 * @author Radu Sebastian LAZIN
 */
public class Primitives {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_CLASS_MAP = new HashMap<>();
	static {
		PRIMITIVE_TO_CLASS_MAP.put(int.class, Integer.class);
		PRIMITIVE_TO_CLASS_MAP.put(long.class, Long.class);
		PRIMITIVE_TO_CLASS_MAP.put(char.class, Character.class);
		PRIMITIVE_TO_CLASS_MAP.put(short.class, Short.class);
		PRIMITIVE_TO_CLASS_MAP.put(byte.class, Byte.class);
		PRIMITIVE_TO_CLASS_MAP.put(boolean.class, Boolean.class);
		PRIMITIVE_TO_CLASS_MAP.put(float.class, Float.class);
		PRIMITIVE_TO_CLASS_MAP.put(double.class, Double.class);
	}

	private static final Map<Class<?>, Class<?>> CLASS_TO_PRIMITIVE_MAP = PRIMITIVE_TO_CLASS_MAP.keySet().stream()
			.collect(Collectors.toMap(PRIMITIVE_TO_CLASS_MAP::get, Function.identity()));

	/**
	 * Returns the primitive type from the boxed class.
	 *
	 * @param <T> primitive type
	 *
	 * @param cls source class
	 * @return primitive type
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> toPrimitive(final Class<?> cls) {
		if (!isUnboxable(cls)) {
			throw new ReflectionException("Class " + cls.getCanonicalName() + " cannot be transformed to primitive type");
		}
		return (Class<T>) CLASS_TO_PRIMITIVE_MAP.get(cls);
	}

	/**
	 * Returns the boxed type from a primitive.
	 *
	 * @param <T> boxed type
	 *
	 * @param cls primitive class
	 * @return the boxed class
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> fromPrimitive(final Class<?> cls) {
		if (!PRIMITIVE_TO_CLASS_MAP.containsKey(cls)) {
			throw new ReflectionException("Class " + cls.getCanonicalName() + " cannot be transformed to boxed type");
		}
		return (Class<T>) PRIMITIVE_TO_CLASS_MAP.get(cls);
	}

	/**
	 * Returns the boxed class for a given primitive class.
	 *
	 * @param clsPrimitive primitive class
	 * @return the boxed class for a given primitive class
	 */
	public static Class<?> getBoxedClass(final Class<?> clsPrimitive) {
		return PRIMITIVE_TO_CLASS_MAP.get(clsPrimitive);
	}

	/**
	 * Returns true if the given class can be un-boxed to its primitive counterpart.
	 *
	 * @param clsBoxed boxed class
	 * @return true if the given class can be un-boxed to its primitive counterpart
	 */
	public static boolean isUnboxable(final Class<?> clsBoxed) {
		return CLASS_TO_PRIMITIVE_MAP.containsKey(clsBoxed);
	}

	/**
	 * Private constructor that throws exception if called.
	 */
	private Primitives() {
		throw new UnsupportedOperationException("This class should not be instantiated!");
	}

}
