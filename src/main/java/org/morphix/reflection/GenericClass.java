package org.morphix.reflection;

import java.lang.reflect.Type;

/**
 * The purpose of this class is to enable capturing and passing a generic type. This is achieved by creating an inline
 * subclass of this class. The Java runtime retains in this case the actual generic type.
 *
 * <pre>
 * GenericClass&lt;Map&lt;String, String&gt;&gt; typeRef = new GenericClass&lt;&gt;() {
 * };
 * </pre>
 *
 * and the captured generic type will be: <code>Map&lt;String,String&gt;</code>
 *
 * @param <T> generic type
 *
 * @author Radu Sebastian LAZIN
 */
@SuppressWarnings("unused")
public abstract class GenericClass<T> {

	/**
	 * Captured generic type.
	 */
	private final Type type;

	/**
	 * Default protected constructor.
	 */
	protected GenericClass() {
		Class<?> genericClassSubclass = Reflection.findSubclass(GenericClass.class, getClass());
		this.type = GenericType.getGenericParameterType(genericClassSubclass, 0);
	}

	/**
	 * Private constructor with type.
	 *
	 * @param type captured type
	 */
	private GenericClass(final Type type) {
		this.type = type;
	}

	/**
	 * Build a {@code GenericClass} wrapping the given type.
	 *
	 * @param <T> generic type
	 *
	 * @param type a generic type
	 * @return generic type reference object representing the type
	 */
	public static <T> GenericClass<T> of(final Type type) {
		return new GenericClass<>(type) {
			// empty
		};
	}

	/**
	 * Returns the captured type.
	 *
	 * @return the captured type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Alias for {@link #getType()}.
	 *
	 * @return the captured generic argument type
	 */
	public Type getGenericArgumentType() {
		return getType();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof GenericClass<?> that && type.equals(that.type);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "GenericClass<" + type + ">";
	}

}
