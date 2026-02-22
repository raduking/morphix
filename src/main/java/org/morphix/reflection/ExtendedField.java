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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import org.morphix.lang.Nullables;

/**
 * Class that holds a {@link Field} and the object for which this field corresponds. It represents a link between the
 * {@link Field}, getter/setter methods on it and the object the field represents.
 * <p>
 * The {@link ExtendedField} object will use the following priority when doing operations on it:
 * <ol>
 * <li>the object it represents</li>
 * <li>getter method</li>
 * <li>reflection field object</li>
 * </ol>
 * Operations are thread safe.
 * <p>
 * The field value, type and class are cached when they are calculated for the first time.
 * <p>
 * The field value is cached only if it is not null, otherwise it is calculated every time. The field type and class are
 * cached even if they are null, because they are calculated based on the field or getter method and they will not
 * change during the lifetime of the object.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExtendedField {

	/**
	 * Empty extended field.
	 */
	public static final ExtendedField EMPTY = ExtendedField.of((Field) null, null);

	/**
	 * {@link Field} object.
	 */
	private final Field field;

	/**
	 * Getter {@link Method} object.
	 */
	private Method getterMethod;

	/**
	 * Actual object for which this field associates to.
	 */
	private final Object object;

	/**
	 * Actual field value.
	 */
	private AtomicReference<Object> fieldValue = new AtomicReference<>();

	/**
	 * Field modifiers.
	 */
	private int modifiers;

	/**
	 * Field name;
	 */
	private String name;

	/**
	 * Class of the field. It is used for caching the class of the field when it is calculated for the first time.
	 */
	private Class<?> cls;

	/**
	 * Type of the field. It is used for caching the type of the field when it is calculated for the first time.
	 */
	private Type type;

	/**
	 * Constructor.
	 *
	 * @param field field
	 * @param object object for given field
	 */
	private ExtendedField(final Field field, final Object object) {
		this.field = field;
		if (null != field) {
			this.modifiers = field.getModifiers();
			this.name = field.getName();
		}
		this.object = object;
	}

	/**
	 * Builds a new {@link ExtendedField} object.
	 *
	 * @param field field
	 * @param object object
	 * @return a new {@link ExtendedField} object
	 */
	public static ExtendedField of(final Field field, final Object object) {
		return new ExtendedField(field, object);
	}

	/**
	 * Builds a new {@link ExtendedField} object.
	 *
	 * @param getterMethod getter method
	 * @param object object
	 * @return a new {@link ExtendedField} object
	 */
	public static ExtendedField of(final Method getterMethod, final Object object) {
		ExtendedField extendedField = of((Field) null, object);
		extendedField.setGetterMethod(getterMethod);
		extendedField.setModifiers(getterMethod.getModifiers());
		return extendedField;
	}

	/**
	 * Builds a new {@link ExtendedField} object.
	 *
	 * @param field field
	 * @return a new {@link ExtendedField} object
	 */
	public static ExtendedField of(final Field field) {
		return of(field, null);
	}

	/**
	 * Returns the associated java reflection field.
	 *
	 * @return the associated java reflection field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Returns the object for which this field refers to.
	 *
	 * @return the object for which this field refers to
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Returns the field modifiers.
	 *
	 * @return the field modifiers
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets the field modifiers.
	 *
	 * @param modifiers modifiers
	 */
	public void setModifiers(final int modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Getter method for the field.
	 *
	 * @param getterMethod getter method
	 */
	public void setGetterMethod(final Method getterMethod) {
		this.getterMethod = getterMethod;
		if (null != getterMethod) {
			this.name = MethodType.GETTER.getFieldName(getterMethod);
		}
	}

	/**
	 * Returns the getter {@link Method}.
	 *
	 * @return the getter method
	 */
	public Method getGetterMethod() {
		return getterMethod;
	}

	/**
	 * Returns the field name.
	 *
	 * @return the field name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the field value.
	 *
	 * @return the field value
	 */
	public Object getFieldValue() {
		Object value = fieldValue.get();
		if (null != value) {
			return value;
		}
		if (null != getterMethod) {
			value = Methods.IgnoreAccess.invoke(getterMethod, object);
		}
		if (null == value && null != field) {
			value = Fields.IgnoreAccess.get(object, field);
		}
		if (null == value && null == field && null == getterMethod && hasObject()) {
			value = object;
		}
		if (null != value) {
			// only cache non null values
			fieldValue.compareAndSet(null, value);
		}
		return fieldValue.get();
	}

	/**
	 * Sets the field value.
	 *
	 * @param value value to set
	 */
	public void setFieldValue(final Object value) {
		if (null != field) {
			Reflection.setFieldValue(object, field, value);
			fieldValue.set(value);
		} else if (null != getterMethod) {
			Reflection.setFieldValue(object, name, getterMethod.getReturnType(), value);
			fieldValue.set(value);
		}
	}

	/**
	 * Returns the type of the field. If the object is present, it returns its class; otherwise the type of the field is
	 * returned. If there is no field then {@link Object#getClass()} is returned.
	 *
	 * @return type of the field
	 */
	public Type getType() {
		if (null != type) {
			return type;
		}
		if (hasObject()) {
			Object value = getFieldValue();
			if (null != value) {
				return setType(value.getClass());
			}
		}
		if (null != field) {
			return setType(field.getGenericType());
		}
		if (null == type && null != getterMethod) {
			return setType(getterMethod.getGenericReturnType());
		}
		return setType(Default.CLASS);
	}

	/**
	 * Sets the type of the field. It is used for caching the type of the field when it is calculated for the first time.
	 *
	 * @param type the type to set.
	 * @return the cached type;
	 */
	protected Type setType(final Type type) {
		this.type = type;
		return type;
	}

	/**
	 * Returns the generic return type parameter of the field.
	 *
	 * @param <T> generic type
	 *
	 * @param index index
	 * @return the generic return type parameter of the field
	 */
	public <T extends Type> T getGenericReturnType(final int index) {
		if (null != getterMethod) {
			return Methods.Safe.getGenericReturnType(getterMethod, index);
		}
		if (null == field) {
			return null;
		}
		if (hasObject()) {
			return GenericType.getGenericArgumentType(field, object.getClass(), index);
		}
		return null;
	}

	/**
	 * Returns the class of the field. If the object is present, it returns its class; otherwise the type of the field is
	 * returned. If there is no field then {@link Object#getClass()} is returned.
	 *
	 * @return type of the field
	 */
	public Class<?> toClass() {
		if (null != cls) {
			return cls;
		}
		if (type instanceof Class<?> classType) {
			return setClass(classType);
		}
		if (hasObject()) {
			Object value = getFieldValue();
			if (null != value) {
				return setClass(value.getClass());
			}
		}
		if (null != field) {
			return setClass(field.getType());
		}
		if (null == cls && null != getterMethod) {
			return setClass(getterMethod.getReturnType());
		}
		return setClass(Default.CLASS);
	}

	/**
	 * Sets the class of the field. It is used for caching the class of the field when it is calculated for the first time.
	 *
	 * @param cls the class to set.
	 * @return the cached class;
	 */
	protected Class<?> setClass(final Class<?> cls) {
		this.cls = cls;
		return cls;
	}

	/**
	 * Returns true if it has an object associated, false otherwise. The field object pair object can have no object or
	 * field.
	 *
	 * @return true if it has an object, false otherwise
	 */
	public boolean hasObject() {
		return null != object;
	}

	/**
	 * Returns true if it has a field associated, false otherwise. The field object pair object can have no object or field.
	 * Whenever the field or the getter method is set, the name of the field is also set.
	 *
	 * @return true if it has a field, false otherwise
	 */
	public boolean hasField() {
		return null != name;
	}

	/**
	 * Returns true if the type meets the predicate conditions.
	 *
	 * @param predicate predicate to test
	 * @return true if the type meets the predicate conditions
	 */
	public boolean typeMeets(final Predicate<Type> predicate) {
		return predicate.test(getType());
	}

	/**
	 * Returns true if the annotation is present on the extended field.
	 *
	 * @param <T> annotation type
	 *
	 * @param annotation annotation to check
	 * @return true if the annotation is present on the extended field
	 */
	public <T extends Annotation> boolean isAnnotationPresent(final Class<T> annotation) {
		boolean isPresent = false;
		if (getterMethod != null) {
			isPresent = getterMethod.isAnnotationPresent(annotation);
		}
		if (null != field) {
			isPresent |= field.isAnnotationPresent(annotation);
		}
		return isPresent;
	}

	/**
	 * Returns the annotation if it is present on the extended field, null otherwise.
	 *
	 * @param <T> annotation type
	 *
	 * @param annotationClass annotation class
	 * @return the annotation if it is present on the extended field, null otherwise
	 */
	public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
		Function<AnnotatedElement, T> getAnnotation = annotated -> annotated.getAnnotation(annotationClass);
		T annotation = Nullables.apply(getField(), getAnnotation);
		if (null != annotation) {
			return annotation;
		}
		return Nullables.apply(getGetterMethod(), getAnnotation);
	}

	/**
	 * {@link Object#toString()} implementation for debugging purposes.
	 */
	@Override
	public String toString() {
		String eol = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		if (hasField()) {
			sb.append("Field: ").append(getName()).append(eol);
			sb.append("Type: ").append(getType());
		}
		if (hasObject()) {
			if (hasField()) {
				sb.append(eol).append("Value: ").append(getFieldValue()).append(eol);
			}
			sb.append("Object: ").append(object);
		}
		return sb.toString();
	}

	/**
	 * Namespace class for defaults.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Default class for a field.
		 */
		private static final Class<?> CLASS = Object.class;

		/**
		 * Hide constructor.
		 */
		private Default() {
			throw Constructors.unsupportedOperationException();
		}
	}
}
