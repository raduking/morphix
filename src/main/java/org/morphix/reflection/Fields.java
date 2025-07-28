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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.lang.JavaObjects;

/**
 * Utility reflection methods for java fields.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Fields {

	/**
	 * Returns a list with all the fields in the class given as parameter. This is different from
	 * {@link Class#getDeclaredFields()} as it returns a {@link List} instead of an array.
	 *
	 * @param <T> type to get the fields from
	 *
	 * @param cls class on which the fields are returned
	 * @return list of fields
	 */
	static <T> List<Field> getDeclaredFields(final Class<T> cls) {
		return List.of(cls.getDeclaredFields());
	}

	/**
	 * Returns a list with all the fields in the class, given as parameter and the field predicate.
	 *
	 * @param <T> type to get the fields from
	 *
	 * @param cls class on which the fields are returned
	 * @param predicate predicate for fields
	 * @return list of fields
	 */
	static <T> List<Field> getDeclaredFields(final Class<T> cls, final Predicate<Field> predicate) {
		return getDeclaredFields(cls).stream().filter(predicate).toList();
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
	 * The returned order of the fields are: class -> super class -> ... -> base class and all fields in each class are
	 * returned in the declared order.
	 *
	 * @param <T> type to get the fields from
	 *
	 * @param cls class on which the fields are returned
	 * @return list of fields
	 */
	static <T> List<Field> getDeclaredFieldsInHierarchy(final Class<T> cls) {
		if (null == cls.getSuperclass()) {
			return new LinkedList<>();
		}
		List<Field> fields = getDeclaredFieldsInHierarchy(cls.getSuperclass());
		fields.addAll(0, getDeclaredFields(cls));
		return fields;
	}

	/**
	 * Returns a list with all the fields in the class, given as parameter and the field predicate, including the ones in
	 * all it's super classes. The returned order of the fields are: class -> super class -> ... -> base class
	 *
	 * @param <T> type to get the fields from
	 *
	 * @param cls class on which the fields are returned
	 * @param predicate predicate for fields
	 * @return list of fields
	 */
	static <T> List<Field> getDeclaredFieldsInHierarchy(final Class<T> cls, final Predicate<Field> predicate) {
		return getDeclaredFieldsInHierarchy(cls).stream().filter(predicate).toList();
	}

	/**
	 * Returns a field in the class and in all super classes of the class given as parameter.
	 *
	 * @param <T> type to get the fields from
	 *
	 * @param cls class on which the fields are returned
	 * @param fieldName the name of the fields to get
	 * @return existing field, null otherwise
	 */
	static <T> Field getDeclaredFieldInHierarchy(final Class<T> cls, final String fieldName) {
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
	 * Variation of {@link #getDeclaredFieldsInHierarchy(Class)}. It will call the method with <code>obj.getClass()</code>.
	 *
	 * @param obj object on which the fields are needed
	 * @return list of fields
	 */
	static List<Field> getDeclaredFieldsInHierarchy(final Object obj) {
		return getDeclaredFieldsInHierarchy(obj.getClass());
	}

	/**
	 * Variation of {@link #getDeclaredFieldInHierarchy(Class, String)}. It will call the method with
	 * <code>obj.getClass()</code>.
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
	 * @param <T> field value type
	 *
	 * @param obj object from which the represented field's value is to be extracted
	 * @param field field on which to extract the value
	 * @return value of the field on the object obj
	 */
	static <T> T get(final Object obj, final Field field) {
		try {
			return JavaObjects.cast(field.get(obj));
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
	static void set(final Object obj, final Field field, final Object value) {
		try {
			field.set(obj, value); // NOSONAR this is a reflection enhancement method
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException("Could not set field " + field.getName(), e);
		}
	}

	/**
	 * Returns the value of the field from the given object. The field is retrieved using its getter if it has one otherwise
	 * it directly accesses the field.
	 * <p>
	 * Note: It ignores access specifiers.
	 *
	 * @param <T> the type of the object
	 * @param <U> the type of the returned field
	 *
	 * @param object the object from which the field should be retrieved
	 * @param fieldName the field name
	 * @return the value of the field requested, retrieved by its getter method, and if not present, using direct access
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
	 * Sets the value of the given field from the given object to the value supplied ignoring field access modifiers. The
	 * field is set using its setter if it has one otherwise sets the field directly.
	 *
	 * @param <T> field value type
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

	/**
	 * Interface which groups all methods that ignore field access modifiers.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface IgnoreAccess {

		/**
		 * Returns the value of the given field from the given object ignoring field access modifiers.
		 *
		 * @param <T> field value type
		 *
		 * @param field field to query
		 * @param obj object containing the field (null for static fields)
		 * @return field value
		 */
		static <T> T get(final Object obj, final Field field) {
			try (MemberAccessor<Field> ignored = new MemberAccessor<>(obj, field)) {
				return Fields.get(obj, field);
			}
		}

		/**
		 * Returns the value of the given field from the given object ignoring field access modifiers.
		 *
		 * @param <T> field value type
		 *
		 * @param obj object containing the field (null for static fields)
		 * @param fieldName field name to query
		 * @return field value
		 */
		static <T> T get(final Object obj, final String fieldName) {
			Field field = Fields.getDeclaredFieldInHierarchy(obj.getClass(), fieldName);
			if (null == field) {
				throw new ReflectionException("Could not find field '" + fieldName + "' on object of type " + obj.getClass());
			}
			return get(obj, field);
		}

		/**
		 * Sets the value of the given field from the given object to the value supplied ignoring field access modifiers.
		 *
		 * @param <T> field value type
		 *
		 * @param field field to query
		 * @param obj object containing the field (null for static fields)
		 * @param value value to set
		 */
		static <T> void set(final Object obj, final Field field, final T value) {
			try (MemberAccessor<Field> ignored = new MemberAccessor<>(obj, field)) {
				Fields.set(obj, field, value);
			} catch (ReflectionException e) {
				if (e.getCause() instanceof IllegalArgumentException) {
					throw e;
				}
				// only final fields will reach this code
				Unsafe.set(obj, field, value);
			}
		}

		/**
		 * Sets the value of the given field from the given object to the value supplied ignoring field access modifiers.
		 *
		 * @param <T> field value type
		 *
		 * @param fieldName field to query
		 * @param obj object containing the field (null for static fields)
		 * @param value value to set
		 */
		static <T> void set(final Object obj, final String fieldName, final T value) {
			Field field = Fields.getDeclaredFieldInHierarchy(obj.getClass(), fieldName);
			if (null == field) {
				throw new ReflectionException("Could not find field '" + fieldName + "' on object of type " + obj.getClass());
			}
			set(obj, field, value);
		}

		/**
		 * Sets the value of the given static field from the given object to the value supplied ignoring field access modifiers.
		 *
		 * @param <T> type containing the static method
		 * @param <U> field value type
		 *
		 * @param cls class containing the static field
		 * @param fieldName field name to query
		 * @param value value to set
		 */
		static <T, U> void setStatic(final Class<T> cls, final String fieldName, final U value) {
			Field field = Fields.getDeclaredFieldInHierarchy(cls, fieldName);
			if (null == field) {
				throw new ReflectionException("Could not find static field with name " + fieldName + " on class " + cls);
			}
			set(null, field, value);
		}

		/**
		 * Returns the value of a static field ignoring access modifiers.
		 *
		 * @param <T> the type of the static field
		 * @param <U> type to get the field from
		 *
		 *
		 * @param cls the class that has the static field
		 * @param fieldName the name of the static field
		 * @return the value of the static field wit the given name
		 */
		static <T, U> T getStatic(final Class<U> cls, final String fieldName) {
			Field field = Fields.getDeclaredFieldInHierarchy(cls, fieldName);
			if (null == field) {
				throw new ReflectionException("Could not find static field with name: " + fieldName + " on class" + cls);
			}
			return get(null, field);
		}

		/**
		 * Returns the field value for the given object. Field is searched with the field path.<br>
		 * TODO: implement full functionality similar to JSON Path
		 *
		 * @param <T> object type
		 * @param <U> field value type
		 *
		 * @param obj object to search the field in
		 * @param paths possible paths to the field
		 * @return field value
		 */
		static <T, U> U getByPaths(final T obj, final String... paths) {
			U result = null;
			try {
				for (String path : paths) {
					result = getByPath(obj, path);
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
		 * Variation of {@link IgnoreAccess#getByPaths(Object, String...)} with paths given as a comma separated list of paths
		 * in a single string.
		 *
		 * @param <T> object type
		 * @param <U> field value type
		 *
		 * @param obj object to search the field in
		 * @param paths comma separated possible paths to the field
		 * @return field value
		 */
		static <T, U> U getByPaths(final T obj, final String paths) {
			return getByPaths(obj, Objects.requireNonNull(paths, "paths").split(","));
		}

		/**
		 * Returns the field value given by its qualified path to the required field in the given object.
		 *
		 * @param <T> object type
		 * @param <U> field value type
		 *
		 * @param obj object to get the field from
		 * @param path qualified path to the field
		 * @return field value
		 */
		static <T, U> U getByPath(final T obj, final String path) {
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
					result = JavaObjects.cast(value);
				}
			}
			return result;
		}

	}

	/**
	 * Unsafe utility methods.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Unsafe {

		/**
		 * Sets the field value using the {@link Unsafe} method.
		 *
		 * @param obj object on which the represented field's value is to be set
		 * @param field field on which to set the value
		 * @param value value to be set on the field
		 */
		static void set(final Object obj, final Field field, final Object value) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			Object instance = obj;
			if (isStatic) {
				instance = TheUnsafe.staticFieldBase(field);
			}
			long offset = isStatic
					? TheUnsafe.staticFieldOffset(field)
					: TheUnsafe.objectFieldOffset(field);
			TheUnsafe.putObject(instance, offset, value);
		}
	}

	/**
	 * Resets the given field to its default value after instantiation on the given object.
	 *
	 * @param <T> type of the object containing the field
	 *
	 * @param field field to reset
	 * @param obj object containing the field
	 */
	static <T> void reset(final Field field, final T obj) {
		Class<?> type = field.getType();
		if (byte.class == type
				|| short.class == type
				|| int.class == type
				|| long.class == type
				|| float.class == type
				|| double.class == type) {
			IgnoreAccess.set(obj, field, (byte) 0);
		} else if (boolean.class == type) {
			IgnoreAccess.set(obj, field, false);
		} else if (char.class == type) {
			IgnoreAccess.set(obj, field, (char) 0);
		} else {
			IgnoreAccess.set(obj, field, null);
		}
	}

}
