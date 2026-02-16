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
package org.morphix.reflection.predicates;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import org.morphix.lang.function.IntPredicates;
import org.morphix.reflection.Constructors;

/**
 * Predicates for members.
 *
 * @author Radu Sebastian LAZIN
 */
public class MemberPredicates {

	/**
	 * Private constructor.
	 */
	private MemberPredicates() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a predicate that checks for presence of the given annotation.
	 *
	 * @param <T> reflection object type
	 *
	 * @param annotation annotation class
	 * @return a predicate that checks for presence of the given annotation
	 */
	public static <T extends AnnotatedElement> Predicate<T> withAnnotation(final Class<? extends Annotation> annotation) {
		return member -> member.isAnnotationPresent(annotation);
	}

	/**
	 * Returns a predicate that tests that the member has any of the given modifiers.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @param modifierPredicates predicates to test
	 * @return a predicate that tests for the given modifiers
	 */
	public static <T extends Member> Predicate<T> withAnyModifiers(final IntPredicate... modifierPredicates) {
		IntPredicate anyModifiers = IntPredicates.anyOf(modifierPredicates);
		return member -> anyModifiers.test(member.getModifiers());
	}

	/**
	 * Returns a predicate that tests that the member has all the given modifiers.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @param modifierPredicates predicates to test
	 * @return a predicate that tests for the given modifiers
	 */
	public static <T extends Member> Predicate<T> withAllModifiers(final IntPredicate... modifierPredicates) {
		IntPredicate allModifiers = IntPredicates.allOf(modifierPredicates);
		return member -> allModifiers.test(member.getModifiers());
	}

	/**
	 * Returns a predicate that tests that the member has all the given modifiers.
	 * <p>
	 * Alias for {@link #withAllModifiers(IntPredicate...)}
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @param modifierPredicates predicates to test
	 * @return a predicate that tests for the given modifiers
	 */
	public static <T extends Member> Predicate<T> withModifiers(final IntPredicate... modifierPredicates) {
		return withAllModifiers(modifierPredicates);
	}

	/**
	 * Returns a predicate that tests for the given member name.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @param name name to test
	 * @return a predicate that tests for the given member name
	 */
	public static <T extends Member> Predicate<T> hasName(final String name) {
		return member -> Objects.equals(name, member.getName());
	}

	/**
	 * Returns a predicate that tests for the given member name in the given list.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @param names names to test
	 * @return a predicate that tests for the given member name
	 */
	public static <T extends Member> Predicate<T> nameIn(final List<String> names) {
		return member -> names.contains(member.getName());
	}

	/**
	 * Returns a predicate that verifies if a member is static.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @return a predicate that verifies if a member is static
	 */
	public static <T extends Member> Predicate<T> isStatic() {
		return member -> Modifier.isStatic(member.getModifiers());
	}

	/**
	 * Returns a predicate that verifies if a member is not static.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @return a predicate that verifies if a member is not static
	 */
	public static <T extends Member> Predicate<T> isNotStatic() {
		return member -> !Modifier.isStatic(member.getModifiers());
	}

	/**
	 * Returns a predicate that verifies if a member is abstract.
	 *
	 * @param <T> member type, can be Field, Method, etc
	 *
	 * @return a predicate that verifies if a member is abstract
	 */
	public static <T extends Member> Predicate<T> isAbstract() {
		return member -> Modifier.isAbstract(member.getModifiers());
	}
}
