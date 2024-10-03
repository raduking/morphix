package org.morphix.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.annotation.From;
import org.morphix.annotation.Src;

/**
 * Class that holds a {@link Field} and the object for which this field
 * corresponds. It represents a link between the {@link Field}, getter/setter
 * methods on it and the object the field represents.
 * <p>
 * The {@link ConverterField} object will use the following priority when doing
 * operations on it:
 * <ol>
 * <li>the object it represents</li>
 * <li>getter method</li>
 * <li>reflection field object</li>
 * </ol>
 *
 * @author Radu Sebastian LAZIN
 */
public class ConverterField {

	private static final Class<?> DEFAULT_CLASS = Object.class;

	public static final ConverterField EMPTY = ConverterField.of((Field) null, null);

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
	private ConverterField(final Field field, final Object object) {
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
		if (null == fieldValue) {
			if (null == getterMethod) {
				fieldValue = Reflection.getFieldValue(object, field);
			} else {
				fieldValue = Methods.invokeIgnoreAccess(getterMethod, object);
			}
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
		} else {
			Reflection.setFieldValue(object, this.name, getterMethod.getReturnType(), value);
		}
		fieldValue = value;
	}

	/**
	 * Returns the type of the field. If the object is present it returns it's
	 * class, otherwise the type of the field is returned. If there is no field
	 * then {@link Object#getClass()} is returned.
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
	 * @param index index
	 * @return the generic return type parameter of the field
	 */
	public <T extends Type> T getGenericReturnType(final int index) {
		if (null == getterMethod) {
			return Reflection.getGenericArgumentType(field, object.getClass(), index);
		}
		return Methods.getSafeGenericReturnType(getterMethod, index);
	}

	/**
	 * Returns the class of the field. If the object is present it returns it's
	 * class, otherwise the type of the field is returned. If there is no field
	 * then {@link Object#getClass()} is returned.
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
	 * Returns true if it has an object associated, false otherwise. The field
	 * object pair object can have no object or field.
	 *
	 * @return true if it has an object, false otherwise
	 */
	public boolean hasObject() {
		return null != object;
	}

	/**
	 * Returns true if it has a field associated, false otherwise. The field
	 * object pair object can have no object or field. Whenever the field or the
	 * getter method is set the name of the field is also set.
	 *
	 * @return true if it has a field, false otherwise
	 */
	public boolean hasField() {
		return null != name;
	}

	/**
	 * Finds the source field name if the destination field is annotated.
	 *
	 * @return source field name
	 */
	public <T> String sourceFieldName(final T source) {
		Src srcAnnotation = null;
		if (null != getterMethod) {
			srcAnnotation = getterMethod.getAnnotation(Src.class);
		}
		if (null == srcAnnotation && null != field) {
			srcAnnotation = field.getAnnotation(Src.class);
		}
		if (null == srcAnnotation) {
			return name;
		}
		for (From from : srcAnnotation.from()) {
			if (Objects.equals(source.getClass(), from.type())) {
				return from.path();
			}
		}
		if (srcAnnotation.name().isEmpty() && srcAnnotation.value().isEmpty()) {
			return name;
		}
		return srcAnnotation.name().isEmpty() ? srcAnnotation.value() : srcAnnotation.name();
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
	 * Builds a new {@link ConverterField} object.
	 *
	 * @param field field
	 * @param object object
	 * @return a new {@link ConverterField} object
	 */
	public static ConverterField of(final Field field, final Object object) {
		return new ConverterField(field, object);
	}

	/**
	 * Builds a new {@link ConverterField} object.
	 *
	 * @param getterMethod getter method
	 * @param object object
	 * @return a new {@link ConverterField} object
	 */
	public static ConverterField of(final Method getterMethod, final Object object) {
		ConverterField converterField = of((Field) null, object);
		converterField.setGetterMethod(getterMethod);
		converterField.setModifiers(getterMethod.getModifiers());
		return converterField;
	}

	/**
	 * Builds a new {@link ConverterField} object.
	 *
	 * @param field field
	 * @return a new {@link ConverterField} object
	 */
	public static ConverterField of(final Field field) {
		return of(field, null);
	}

	/**
	 * Returns true if the annotation is present on the converter field.
	 *
	 * @param annotation annotation to check
	 * @return true if the annotation is present on the converter field
	 */
	public <T extends Annotation> boolean isAnnotationPresent(final Class<T> annotation) {
		boolean isExpandableField = false;
		if (getterMethod != null) {
			isExpandableField = getterMethod.isAnnotationPresent(annotation);
		}
		if (null != field) {
			isExpandableField |= field.isAnnotationPresent(annotation);
		}
		return isExpandableField;
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
