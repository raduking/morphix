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
package org.morphix.lang;

import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.reflection.Constructors;

/**
 * Utility class for {@link Throwable} related helper methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Throwables {

	/**
	 * Private constructor.
	 */
	private Throwables() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns true when any throwable in the cause chain matches the given predicate.
	 *
	 * @param throwable throwable to inspect
	 * @param predicate predicate to determine if a throwable matches
	 * @return true when a throwable in the cause chain matches, false otherwise
	 */
	public static boolean anyMatch(final Throwable throwable, final Predicate<? super Throwable> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		if (null == throwable) {
			return false;
		}
		// use 2 pointers to detect cycles in the cause chain.
		Throwable slow = throwable;
		Throwable fast = throwable;
		int fastSteps = 2;
		do {
			for (int i = 0; i < fastSteps; ++i) {
				if (predicate.test(fast)) {
					return true;
				}
				fast = fast.getCause();
				if (null == fast) {
					return false;
				}
			}
			slow = slow.getCause();
		} while (slow != fast);
		return false;
	}

	/**
	 * Returns true when any throwable in the cause chain matches the given type.
	 *
	 * @param throwable throwable to inspect
	 * @param type throwable type to match
	 * @return true when a throwable in the cause chain matches the given type, false otherwise
	 */
	public static boolean anyMatch(final Throwable throwable, final Class<? extends Throwable> type) {
		Objects.requireNonNull(type, "type cannot be null");
		return anyMatch(throwable, type::isInstance);
	}

	/**
	 * Returns true when any throwable in the cause chain matches the given predicate.
	 * <p>
	 * This method starts from {@link Throwable#getCause()} and does not check the given throwable itself.
	 *
	 * @param throwable throwable to inspect
	 * @param predicate predicate to determine if a cause matches
	 * @return true when a cause matches the given predicate, false otherwise
	 */
	public static boolean hasCause(final Throwable throwable, final Predicate<? super Throwable> predicate) {
		Objects.requireNonNull(predicate, "predicate cannot be null");
		return anyMatch(null != throwable ? throwable.getCause() : null, predicate);
	}

	/**
	 * Returns true when any throwable in the cause chain matches the given cause type.
	 * <p>
	 * This method starts from {@link Throwable#getCause()} and does not check the given throwable itself.
	 *
	 * @param throwable throwable to inspect
	 * @param causeType cause type to match against
	 * @return true when a cause matches the given type, false otherwise
	 */
	public static boolean hasCause(final Throwable throwable, final Class<? extends Throwable> causeType) {
		Objects.requireNonNull(causeType, "causeType cannot be null");
		return hasCause(throwable, causeType::isInstance);
	}

	/**
	 * Returns true when the direct cause of the given throwable matches the given cause type.
	 * <p>
	 * This method only checks {@link Throwable#getCause()} and does not traverse deeper.
	 *
	 * @param throwable throwable to inspect
	 * @param causeType cause type to match against
	 * @return true when the direct cause matches the given type, false otherwise
	 */
	public static boolean hasDirectCause(final Throwable throwable, final Class<? extends Throwable> causeType) {
		Objects.requireNonNull(causeType, "causeType cannot be null");
		if (null == throwable) {
			return false;
		}
		Throwable cause = throwable.getCause();
		return null != cause && causeType.isInstance(cause);
	}

	/**
	 * Returns true when the given throwable or any throwable in its cause chain matches the given predicate.
	 *
	 * @param throwable throwable to inspect
	 * @param predicate predicate to determine if a throwable matches
	 * @return true when the throwable or one of its causes matches the given predicate, false otherwise
	 */
	public static boolean isOrHasCause(final Throwable throwable, final Predicate<? super Throwable> predicate) {
		return anyMatch(throwable, predicate);
	}

	/**
	 * Returns true when the given throwable is of the given type or any throwable in its cause chain matches the given
	 * type.
	 *
	 * @param throwable throwable to inspect
	 * @param type cause type to match against
	 * @return true when the throwable or one of its causes matches the given type, false otherwise
	 */
	public static boolean isOrHasCause(final Throwable throwable, final Class<? extends Throwable> type) {
		Objects.requireNonNull(type, "type cannot be null");
		return isOrHasCause(throwable, type::isInstance);
	}

	/**
	 * Peels the given throwable type off the cause chain, returning the first cause that is not an instance of the given
	 * type (with any deeper causes intact) or the last matching throwable when the chain is exhausted. If the chain is
	 * cyclic, the throwable at the point where the cycle repeats is returned to avoid an infinite loop.
	 *
	 * @param throwable throwable to unwrap
	 * @param peelType throwable type to peel off
	 * @return the throwable with the given type peeled off, or {@code null} when the throwable is {@code null}
	 */
	public static Throwable unwrap(final Throwable throwable, final Class<? extends Throwable> peelType) {
		Objects.requireNonNull(peelType, "peelType cannot be null");
		if (null == throwable) {
			return null;
		}
		// use 2 pointers to detect cycles in the cause chain.
		Throwable slow = throwable;
		Throwable fast = throwable;
		do {
			for (int i = 0; i < 2; ++i) {
				if (!peelType.isInstance(fast)) {
					return fast;
				}
				Throwable cause = fast.getCause();
				if (null == cause) {
					return fast;
				}
				fast = cause;
			}
			slow = slow.getCause();
		} while (fast != slow);
		return fast;
	}
}
