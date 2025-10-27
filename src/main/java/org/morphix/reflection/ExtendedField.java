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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Predicate;

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
 *
 * @author Radu Sebastian LAZIN
 */
public class ExtendedField {

	/**
	 * Default class for a field.
	 */
	private static final Class<?> DEFAULT_CLASS = Object.class;

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
	private Object fieldValue;

	/**
	 * Field modifiers.
	 */
	private int modifiers;

	/**
	 * Field name;
	 */
	private String name;

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
		this.name = MethodType.GETTER.getFieldName(getterMethod);
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
		if (null == fieldValue && null != getterMethod) {
			fieldValue = Methods.IgnoreAccess.invoke(getterMethod, object);
		}
		if (null == fieldValue && null != field) {
			fieldValue = Reflection.getFieldValue(object, field);
		}
		return fieldValue;
	}

	/**
	 * Sets the field value.
	 *
	 * @param value value to set
	 */
	public void setFieldValue(final Object value) {
		if (null != field) {
			Reflection.setFieldValue(object, field, value);
			fieldValue = value;
		} else if (null != getterMethod) {
			Reflection.setFieldValue(object, name, getterMethod.getReturnType(), value);
			fieldValue = value;
		}
	}

	/**
	 * Returns the type of the field. If the object is present, it returns its class; otherwise the type of the field is
	 * returned. If there is no field then {@link Object#getClass()} is returned.
	 *
	 * @return type of the field
	 */
	public Type getType() {
		if (!hasField()) {
			return DEFAULT_CLASS;
		}
		if (hasObject()) {
			Object value = getFieldValue();
			if (null != value) {
				return value.getClass();
			}
		}
		if (null != field) {
			return field.getGenericType();
		}
		return getterMethod.getGenericReturnType();
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
		if (null == getterMethod) {
			return GenericType.getGenericArgumentType(field, object.getClass(), index);
		}
		return Methods.Safe.getGenericReturnType(getterMethod, index);
	}

	/**
	 * Returns the class of the field. If the object is present, it returns its class; otherwise the type of the field is
	 * returned. If there is no field then {@link Object#getClass()} is returned.
	 *
	 * @return type of the field
	 */
	public Class<?> toClass() {
		if (!hasField()) {
			return DEFAULT_CLASS;
		}
		if (hasObject()) {
			Object value = getFieldValue();
			if (null != value) {
				return value.getClass();
			}
		}
		if (null != field) {
			return field.getType();
		}
		return getterMethod.getReturnType();
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
			sb.append("Object: ").append(object.toString());
		}
		return sb.toString();
	}
}
