package org.morphix.reflection;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility reflection methods for java fields.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Fields {

	/**
	 * Returns a list with all the fields in the class given as parameter.
	 * This is different from {@link Class#getDeclaredFields()} as it returns
	 * a {@link List} instead of an array.
	 *
	 * @param cls class on which the fields are returned
	 * @return list of fields
	 */
	static List<Field> getDeclaredFields(final Class<?> cls) {
		return List.of(cls.getDeclaredFields());
	}

	/**
	 * Returns a list with all the fields in the class, given as parameter and
	 * the field predicate.
	 *
	 * @param cls class on which the fields are returned
	 * @param predicate predicate for fields
	 * @return list of fields
	 */
	static List<Field> getDeclaredFields(final Class<?> cls, final Predicate<? super Field> predicate) {
		return getDeclaredFields(cls).stream().filter(predicate).toList();
	}

	/**
	 * Returns a list with all the fields in the class given as parameter
	 * including the ones in all it's super classes.
	 * <p>
	 * {@link LinkedList} is used because:
	 * <ul>
	 *   <li>it is more efficient in terms of memory consumption</li>
	 *   <li>accessing the first and last has O(1) complexity</li>
	 *   <li>more often than not no random access is needed</li>
	 *   <li>profiling: ~2 times faster than using {@link java.util.ArrayList}</li>
	 * </ul>
	 * The returned order of the fields are: class -> super class -> ... -> base class
	 * and all fields in each class are returned in the declared order.
	 *
	 * @param cls class on which the fields are returned
	 * @return list of fields
	 */
	static List<Field> getDeclaredFieldsInHierarchy(final Class<?> cls) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Field> fields = getDeclaredFieldsInHierarchy(cls.getSuperclass());
		fields.addAll(0, getDeclaredFields(cls));
		return fields;
	}

	/**
	 * Returns a list with all the fields in the class, given as parameter and
	 * the field predicate, including the ones in all it's super classes.
	 * The returned order of the fields are: class -> super class -> ... -> base class
	 *
	 * @param cls class on which the fields are returned
	 * @param predicate predicate for fields
	 * @return list of fields
	 */
	static List<Field> getDeclaredFieldsInHierarchy(final Class<?> cls, final Predicate<? super Field> predicate) {
		return getDeclaredFieldsInHierarchy(cls).stream().filter(predicate).toList();
	}

	/**
	 * Returns a field in the class and in all super classes of the class given
	 * as parameter.
	 *
	 * @param cls class on which the fields are returned
	 * @param fieldName the name of the fields to get
	 * @return existing field, null otherwise
	 */
	static Field getDeclaredFieldInHierarchy(final Class<?> cls, final String fieldName) {
		if (null == cls) {
			return null;
		}
		List<Field> fields = getDeclaredFieldsInHierarchy(cls);
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Variation of {@link #getDeclaredFieldsInHierarchy(Class)}. It will call
	 * the method with <code>obj.getClass()</code>.
	 *
	 * @param obj object on which the fields are needed
	 * @return list of fields
	 */
	static List<Field> getDeclaredFieldsInHierarchy(final Object obj) {
		return getDeclaredFieldsInHierarchy(obj.getClass());
	}

	/**
	 * Variation of {@link #getDeclaredFieldInHierarchy(Class, String)}. It will
	 * call the method with <code>obj.getClass()</code>.
	 *
	 * @param obj object on which the fields are needed
	 * @param fieldName the name of the field to be retrieved
	 * @return existing field, null otherwise
	 */
	static Field getDeclaredFieldInHierarchy(final Object obj, final String fieldName) {
		return getDeclaredFieldInHierarchy(obj.getClass(), fieldName);
	}

	/**
	 * Calls field.get(obj). See {@link Field#get(Object)}.
	 *
	 * @param obj object from which the represented field's value is to be
	 *            extracted
	 * @param field field on which to extract the value
	 * @return value of the field on the object obj
	 */
	@SuppressWarnings("unchecked")
	static <T> T getFieldValue(final Object obj, final Field field) {
		try {
			return (T) field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException("Could not get field " + field.getName(), e);
		}
	}

	/**
	 * Calls field.set(obj, value). See {@link Field#set(Object, Object)}.
	 *
	 * @param obj object on which the represented field's value is to be set
	 * @param field field on which to set the value
	 * @param value value to be set on the field
	 */
	static void setFieldValue(final Object obj, final Field field, final Object value) {
		try {
			field.set(obj, value); // NOSONAR this is a reflection enhancement method
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException("Could not set field " + field.getName(), e);
		}
	}

	/**
	 * Returns the value of the given field from the given object ignoring field
	 * access modifiers.
	 *
	 * @param field field to query
	 * @param obj object containing the field (null for static fields)
	 * @return field value
	 */
	static <T> T getIgnoreAccess(final Object obj, final Field field) {
		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(obj, field)) {
			return getFieldValue(obj, field);
		}
	}

	/**
	 * Returns the value of the given field from the given object ignoring field
	 * access modifiers.
	 *
	 * @param obj object containing the field (null for static fields)
	 * @param fieldName field name to query
	 * @return field value
	 */
	static <T> T getIgnoreAccess(final Object obj, final String fieldName) {
		Field field;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Could not find field '" + fieldName + "' on object of type " + obj.getClass(), e);
		}
		return getIgnoreAccess(obj, field);
	}

	/**
	 * Sets the value of the given field from the given object to the value
	 * supplied ignoring field access modifiers.
	 *
	 * @param field field to query
	 * @param obj object containing the field (null for static fields)
	 * @param value value to set
	 */
	static <T> void setIgnoreAccess(final Object obj, final Field field, final T value) {
		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(obj, field)) {
			setFieldValue(obj, field, value);
		}
	}

	/**
	 * Sets the value of the given field from the given object to the value
	 * supplied ignoring field access modifiers.
	 *
	 * @param fieldName field to query
	 * @param obj object containing the field (null for static fields)
	 * @param value value to set
	 */
	static <T> void setIgnoreAccess(final Object obj, final String fieldName, final T value) {
		Field field;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Could not find field '" + fieldName + "' on object of type " + obj.getClass(), e);
		}
		setIgnoreAccess(obj, field, value);
	}

	/**
	 * Sets the value of the given static field from the given object to the
	 * value supplied ignoring field access modifiers.
	 *
	 * @param cls class containing the static field
	 * @param fieldName field name to query
	 * @param value value to set
	 */
	static <T, U> void setStaticIgnoreAccess(final Class<T> cls, final String fieldName, final U value) {
		Field field;
		try {
			field = cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Could not find static field with name " + fieldName + " on class " + cls, e);
		}
		setIgnoreAccess(null, field, value);
	}

	/**
	 * Returns the value of a static field ignoring access modifiers.
	 *
	 * @param cls the class that has the static field
	 * @param fieldName the name of the static field
	 * @param <T> the type of the static field
	 * @return the value of the static field wit the given name
	 */
	static <T> T getStaticIgnoreAccess(final Class<?> cls, final String fieldName) {
		try {
			Field field = cls.getDeclaredField(fieldName);
			return getIgnoreAccess(null, field);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Could not find static field: " + fieldName, e);
		}
	}

	/**
	 * Resets the given field to its default value after instantiation on the
	 * given object.
	 *
	 * @param obj object containing the field
	 * @param field field to reset
	 */
	static <T> void resetField(final T obj, final Field field) {
		Class<?> type = field.getType();
		if (byte.class.equals(type)
				|| short.class.equals(type)
				|| int.class.equals(type)
				|| long.class.equals(type)
				|| float.class.equals(type)
				|| double.class.equals(type)) {
			setIgnoreAccess(obj, field, (byte) 0);
		} else if (boolean.class.equals(type)) {
			setIgnoreAccess(obj, field, false);
		} else if (char.class.equals(type)) {
			setIgnoreAccess(obj, field, (char) 0);
		} else {
			setIgnoreAccess(obj, field, null);
		}
	}

	/**
	 * Returns the field value for the given object. Field is searched with the
	 * field path.<br>
	 * TODO: implement full functionality similar to JSON Path
	 *
	 * @param obj object to search the field in
	 * @param paths possible paths to the field
	 * @return field value
	 */
	static <T, U> U getIgnoreAccessByPaths(final T obj, final String... paths) {
		U result = null;
		try {
			for (String path : paths) {
				result = getIgnoreAccessByPath(obj, path);
				if (null != result) {
					break;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	/**
	 * Variation of {@link Fields#getIgnoreAccessByPaths(Object, String...)}
	 * with paths given as a comma separated list of paths in a single string.
	 *
	 * @param obj object to search the field in
	 * @param paths comma separated possible paths to the field
	 * @return field value
	 */
	static <T, U> U getIgnoreAccessByPaths(final T obj, final String paths) {
		return getIgnoreAccessByPaths(obj, requireNonNull(paths).split(","));
	}

	/**
	 * Returns the field value given by its qualified path to the required field
	 * in the given object.
	 *
	 * @param obj object to get the field from
	 * @param path qualified path to the field
	 * @return field value
	 */
	@SuppressWarnings("unchecked")
	static <T, U> U getIgnoreAccessByPath(final T obj, final String path) {
		U result = null;
		String[] fieldNames = path.split("\\.");
		if (fieldNames.length == 1) {
			result = Fields.get(obj, fieldNames[0]);
		} else {
			Object value = obj;
			for (String fieldName : fieldNames) {
				try {
					value = Fields.get(value, fieldName);
				} catch (ReflectionException e) {
					value = null;
				}
				if (value == null) {
					break;
				}
			}
			if (null != value) {
				result = (U) value;
			}
		}
		return result;
	}

	/**
	 * Returns the value of the field from the given object. The field is
	 * retrieved using its getter if it has one otherwise it directly accesses
	 * the field.
	 * <p>
	 * Note: It ignores access specifiers.
	 *
	 * @param object the object from which the field should be retrieved
	 * @param fieldName the field name
	 * @param <T> the type of the object
	 * @param <U> the type of the returned field
	 * @return the value of the field requested, retrieved by its getter method,
	 *         and if not present, using direct access
	 */
	static <T, U> U get(final T object, final String fieldName) {
		Class<?> cls = object.getClass();
		Field field = Fields.getDeclaredFieldInHierarchy(cls, fieldName);
		if (null == field) {
			throw new ReflectionException("Object does not contain a field named: " + fieldName);
		}
		return Reflection.getFieldValue(object, field);
	}

	/**
	 * Sets the value of the given field from the given object to the value
	 * supplied ignoring field access modifiers. The field is set using its
	 * setter if it has one otherwise sets the field directly.
	 *
	 * @param fieldName field to query
	 * @param obj object containing the field (null for static fields)
	 * @param value value to set
	 */
	static <T> void set(final Object obj, final String fieldName, final T value) {
		Class<?> cls = obj.getClass();
		Field field = Fields.getDeclaredFieldInHierarchy(cls, fieldName);
		if (null == field) {
			throw new ReflectionException("Object does not contain a field named: " + fieldName);
		}
		Reflection.setFieldValue(obj, field, value);
	}

}
