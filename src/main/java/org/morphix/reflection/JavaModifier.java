package org.morphix.reflection;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent java modifiers which transfer to actual keywords in the Java language.
 * The only one not represented here is the 'default' package modifier which does
 * not have a keyword associated.
 * <p>
 * The type is named {@link JavaModifier} to not be confused with {@link Modifier}.
 *
 * @author Radu Sebastian LAZIN
 */
public enum JavaModifier {

	/**
	 * <code>public</code> access modifier
	 */
	PUBLIC(true),

	/**
	 * <code>protected</code> access modifier
	 */
	PROTECTED(true),

	/**
	 * <code>private</code> access modifier
	 */
	PRIVATE(true),

	/**
	 * <code>final</code> modifier
	 */
	FINAL,

	/**
	 * <code>static</code> modifier
	 */
	STATIC,

	/**
	 * <code>abstract</code> modifier
	 */
	ABSTRACT,

	/**
	 * <code>transient</code> modifier
	 */
	TRANSIENT,

	/**
	 * <code>synchronized</code> modifier
	 */
	SYNCHRONIZED,

	/**
	 * <code>volatile</code> modifier
	 */
	VOLATILE;

	/**
	 * Name map that maps the java keyword to the corresponding modifier enum value.
	 */
	private static final Map<String, JavaModifier> NAME_MAP =
			Stream.of(values()).collect(Collectors.toUnmodifiableMap(JavaModifier::toString, Function.identity()));

	/**
	 * Method to return the access modifier from a string.
	 * <p>
	 * This method intentionally relaxes and automatically converts the given
	 * value to lower-case string the same way as the internal value is kept.
	 *
	 * @param value value to convert
	 * @return java access modifier
	 */
	public static JavaModifier fromString(final String value) {
		return NAME_MAP.get(value.toLowerCase());
	}

	private final String value;
	private final boolean accessModifier;

	JavaModifier(final boolean accessModifier) {
		this.value = name().toLowerCase();
		this.accessModifier = accessModifier;
	}

	JavaModifier() {
		this(false);
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getValue();
	}

	public boolean isAccessModifier() {
		return accessModifier;
	}

}
