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

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent java modifiers which transfer to actual keywords in the Java language. The only one not represented here is
 * the 'default' package modifier which does not have a keyword associated.
 * <p>
 * The type is named {@link JavaModifier} to not be confused with {@link Modifier}.
 * <p>
 * Example usage:
 *
 * <pre>
 * Arrays.stream(clazz.getDeclaredMethods())
 * 		.filter(JavaModifier.PUBLIC)
 * 		.filter(JavaModifier.STATIC)
 * 		.toList();
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public enum JavaModifier implements Predicate<Member> {

	/**
	 * <code>public</code> access modifier
	 */
	PUBLIC(true, Modifier.PUBLIC),

	/**
	 * <code>protected</code> access modifier
	 */
	PROTECTED(true, Modifier.PROTECTED),

	/**
	 * <code>private</code> access modifier
	 */
	PRIVATE(true, Modifier.PRIVATE),

	/**
	 * <code>final</code> modifier
	 */
	FINAL(Modifier.FINAL),

	/**
	 * <code>static</code> modifier
	 */
	STATIC(Modifier.STATIC),

	/**
	 * <code>abstract</code> modifier
	 */
	ABSTRACT(Modifier.ABSTRACT),

	/**
	 * <code>transient</code> modifier
	 */
	TRANSIENT(Modifier.TRANSIENT),

	/**
	 * <code>synchronized</code> modifier
	 */
	SYNCHRONIZED(Modifier.SYNCHRONIZED),

	/**
	 * <code>volatile</code> modifier
	 */
	VOLATILE(Modifier.VOLATILE),

	/**
	 * <code>native</code> modifier
	 */
	NATIVE(Modifier.NATIVE),

	/**
	 * <code>strictfp</code> modifier
	 */
	STRICTFP(Modifier.STRICT);

	/**
	 * The actual string value of the modifier.
	 */
	private final String value;

	/**
	 * Access modifier flag (some modifiers are not access modifiers).
	 */
	private final boolean accessModifier;

	/**
	 * Mask value to check if the modifier is present on a member.
	 */
	private final int modifierMask;

	/**
	 * Name map that maps the java keyword to the corresponding modifier enum value.
	 */
	private static final Map<String, JavaModifier> NAME_MAP =
			Stream.of(values()).collect(Collectors.toUnmodifiableMap(JavaModifier::toString, Function.identity()));

	/**
	 * Method to return the access modifier from a string.
	 * <p>
	 * This method intentionally relaxes and automatically converts the given value to lower-case string the same way as the
	 * internal value is kept.
	 *
	 * @param value value to convert
	 * @return java access modifier
	 */
	public static JavaModifier fromString(final String value) {
		return NAME_MAP.get(value.toLowerCase());
	}

	/**
	 * Constructor with the access modifier.
	 *
	 * @param accessModifier access modifier
	 * @param modifierMask mask value to check if the modifier is present on a member
	 */
	JavaModifier(final boolean accessModifier, final int modifierMask) {
		this.value = name().toLowerCase();
		this.accessModifier = accessModifier;
		this.modifierMask = modifierMask;
	}

	/**
	 * Constructor with modifier mask only, the access modifier flag is set to false by default.
	 *
	 * @param modifierMask mask value to check if the modifier is present on a member
	 */
	JavaModifier(final int modifierMask) {
		this(false, modifierMask);
	}

	/**
	 * Returns the value of the modifier.
	 *
	 * @return the value of the modifier
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return getValue();
	}

	/**
	 * Returns true if this modifier is an access modifier, false otherwise.
	 *
	 * @return true if this modifier is an access modifier, false otherwise
	 */
	public boolean isAccessModifier() {
		return accessModifier;
	}

	/**
	 * @see Predicate#test(Object)
	 */
	@Override
	public boolean test(final Member member) {
		return (member.getModifiers() & modifierMask) != 0;
	}

	/**
	 * Checks if the given member has this modifier.
	 *
	 * @param member the member to check
	 * @return true if the given member has this modifier, false otherwise
	 */
	public boolean isPresentOn(final Member member) {
		return test(member);
	}

	/**
	 * Checks if the given member does not have this modifier.
	 *
	 * @param member the member to check
	 * @return true if the given member does not have this modifier, false otherwise
	 */
	public boolean isNotPresentOn(final Member member) {
		return !isPresentOn(member);
	}
}
