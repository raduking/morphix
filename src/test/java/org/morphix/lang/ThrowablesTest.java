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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Throwables}.
 *
 * @author Radu Sebastian LAZIN
 */
class ThrowablesTest {

	private static final String MATCH = "match";

	@Nested
	class ConstructorTest {

		@Test
		void shouldThrowExceptionOnCallingConstructor() {
			UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Throwables.class);

			assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
	}

	@Nested
	class AnyMatchTest {

		@Test
		void shouldThrowWhenPredicateIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.anyMatch(t, (Predicate<? super Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("predicate cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.anyMatch(null, throwable -> true);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenRootThrowableMatches() {
			Throwable throwable = new IllegalStateException(MATCH);

			boolean result = Throwables.anyMatch(throwable, t -> MATCH.equals(t.getMessage()));

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnTrueWhenCauseThrowableMatches() {
			Throwable cause = new IllegalArgumentException(MATCH);
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.anyMatch(throwable, t -> MATCH.equals(t.getMessage()));

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenNoThrowableMatches() {
			Throwable leaf = new IllegalArgumentException("leaf");
			Throwable middle = new IllegalStateException("middle", leaf);
			Throwable throwable = new RuntimeException("root", middle);

			boolean result = Throwables.anyMatch(throwable, t -> MATCH.equals(t.getMessage()));

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnFalseWhenCauseChainContainsCycleAndNoMatch() {
			NodeThrowable first = new NodeThrowable("first");
			NodeThrowable second = new NodeThrowable("second");
			first.cause = second;
			second.cause = first;

			boolean result = Throwables.anyMatch(first, t -> MATCH.equals(t.getMessage()));

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenCauseChainContainsCycleAndMatchExists() {
			NodeThrowable first = new NodeThrowable("first");
			NodeThrowable second = new NodeThrowable(MATCH);
			first.cause = second;
			second.cause = first;

			boolean result = Throwables.anyMatch(first, t -> MATCH.equals(t.getMessage()));

			assertThat(result, equalTo(true));
		}
	}

	@Nested
	class AnyMatchClassTest {

		@Test
		void shouldThrowWhenTypeIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.anyMatch(t, (Class<? extends Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("type cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.anyMatch(null, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenRootThrowableMatches() {
			Throwable throwable = new IllegalArgumentException(MATCH);

			boolean result = Throwables.anyMatch(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnTrueWhenCauseThrowableMatches() {
			Throwable throwable = new RuntimeException("root", new IllegalArgumentException(MATCH));

			boolean result = Throwables.anyMatch(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenNoThrowableMatches() {
			Throwable throwable = new RuntimeException("root", new IllegalStateException("state"));

			boolean result = Throwables.anyMatch(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}
	}

	@Nested
	class HasCausePredicateTest {

		@Test
		void shouldThrowWhenPredicateIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> Throwables.hasCause(t, (Predicate<? super Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("predicate cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.hasCause(null, throwable -> true);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnFalseWhenRootMatchesButHasCauseStartsFromCause() {
			Throwable throwable = new IllegalArgumentException(MATCH);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class::isInstance);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenCauseMatches() {
			Throwable cause = new IllegalArgumentException(MATCH);
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class::isInstance);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenNoCauseMatches() {
			Throwable cause = new IllegalStateException("state");
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class::isInstance);

			assertThat(result, equalTo(false));
		}
	}

	@Nested
	class HasCauseClassTest {

		@Test
		void shouldThrowWhenCauseTypeIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.hasCause(t, (Class<? extends Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("causeType cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.hasCause(null, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnFalseWhenRootMatchesButHasCauseStartsFromCause() {
			Throwable throwable = new IllegalArgumentException(MATCH);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenCauseMatchesForHasCause() {
			Throwable cause = new IllegalArgumentException(MATCH);
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenNoCauseMatchesForHasCause() {
			Throwable cause = new IllegalStateException("state");
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.hasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenCycleContainsMatchingCauseForHasCause() {
			NodeThrowable first = new NodeThrowable("first");
			NodeThrowable second = new NodeThrowable(MATCH);
			first.cause = second;
			second.cause = first;

			boolean result = Throwables.hasCause(first, NodeThrowable.class);

			assertThat(result, equalTo(true));
		}
	}

	@Nested
	class IsOrHasCausePredicateTest {

		@Test
		void shouldThrowWhenPredicateIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException =
					assertThrows(NullPointerException.class, () -> Throwables.isOrHasCause(t, (Predicate<? super Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("predicate cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.isOrHasCause(null, throwable -> true);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenRootMatches() {
			Throwable throwable = new IllegalArgumentException(MATCH);

			boolean result = Throwables.isOrHasCause(throwable, IllegalArgumentException.class::isInstance);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnTrueWhenCauseMatches() {
			Throwable cause = new IllegalArgumentException(MATCH);
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.isOrHasCause(throwable, IllegalArgumentException.class::isInstance);

			assertThat(result, equalTo(true));
		}
	}

	@Nested
	class IsOrHasCauseClassTest {

		@Test
		void shouldThrowWhenCauseTypeIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.isOrHasCause(t, (Class<? extends Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("type cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.isOrHasCause(null, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenRootMatches() {
			Throwable throwable = new IllegalArgumentException(MATCH);

			boolean result = Throwables.isOrHasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnTrueWhenCauseMatches() {
			Throwable cause = new IllegalArgumentException(MATCH);
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.isOrHasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenNoCauseMatches() {
			Throwable cause = new IllegalStateException("state");
			Throwable throwable = new RuntimeException("root", cause);

			boolean result = Throwables.isOrHasCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}
	}

	@Nested
	class HasDirectCauseTest {

		@Test
		void shouldThrowWhenCauseTypeIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.hasDirectCause(t, (Class<? extends Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("causeType cannot be null"));
		}

		@Test
		void shouldReturnFalseWhenThrowableIsNull() {
			boolean result = Throwables.hasDirectCause(null, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnFalseWhenThrowableHasNoCause() {
			Throwable throwable = new RuntimeException("root");

			boolean result = Throwables.hasDirectCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}

		@Test
		void shouldReturnTrueWhenDirectCauseMatches() {
			Throwable throwable = new RuntimeException("root", new IllegalArgumentException(MATCH));

			boolean result = Throwables.hasDirectCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(true));
		}

		@Test
		void shouldReturnFalseWhenOnlyNestedCauseMatches() {
			Throwable nestedCause = new IllegalArgumentException(MATCH);
			Throwable directCause = new IllegalStateException("state", nestedCause);
			Throwable throwable = new RuntimeException("root", directCause);

			boolean result = Throwables.hasDirectCause(throwable, IllegalArgumentException.class);

			assertThat(result, equalTo(false));
		}
	}

	@Nested
	class UnwrapTest {

		@Test
		void shouldThrowWhenTypeIsNull() {
			Throwable t = new Throwable();
			NullPointerException nullPointerException = assertThrows(
					NullPointerException.class,
					() -> Throwables.unwrap(t, (Class<? extends Throwable>) null));

			assertThat(nullPointerException.getMessage(), equalTo("peelType cannot be null"));
		}

		@Test
		void shouldReturnNullWhenThrowableIsNull() {
			Throwable result = Throwables.unwrap(null, CompletionException.class);

			assertThat(result, equalTo(null));
		}

		@Test
		void shouldReturnThrowableWhenItDoesNotMatchType() {
			Throwable throwable = new IllegalStateException(MATCH);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(throwable));
		}

		@Test
		void shouldReturnThrowableWhenItMatchesButHasNoCause() {
			CompletionException throwable = new CompletionException((Throwable) null);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(throwable));
		}

		@Test
		void shouldReturnCauseWhenSingleMatch() {
			RuntimeException cause = new RuntimeException(MATCH);
			CompletionException throwable = new CompletionException(cause);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(cause));
		}

		@Test
		void shouldReturnFirstNonMatchingWhenMultipleMatches() {
			RuntimeException cause = new RuntimeException(MATCH);
			CompletionException nested = new CompletionException(cause);
			CompletionException throwable = new CompletionException(nested);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(cause));
		}

		@Test
		void shouldKeepDeeperCausesOfReturnedThrowableIntact() {
			RuntimeException deeper = new RuntimeException("deeper");
			RuntimeException cause = new RuntimeException(MATCH, deeper);
			CompletionException throwable = new CompletionException(cause);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(cause));
			assertThat(result.getCause(), equalTo(deeper));
		}

		@Test
		void shouldStopAtFirstNonMatchingEvenWhenDeeperMatchesAgain() {
			CompletionException deeper = new CompletionException(new RuntimeException(MATCH));
			RuntimeException cause = new RuntimeException(MATCH, deeper);
			CompletionException throwable = new CompletionException(cause);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(cause));
		}

		@Test
		void shouldReturnOnlyMatchingRootLayerWhenCauseChainIsLong() {
			RuntimeException leaf = new RuntimeException("leaf");
			IllegalStateException middle = new IllegalStateException("middle", leaf);
			RuntimeException cause = new RuntimeException("root", middle);
			CompletionException throwable = new CompletionException(cause);

			Throwable result = Throwables.unwrap(throwable, CompletionException.class);

			assertThat(result, equalTo(cause));
		}

		@Test
		void shouldTerminateWhenCauseChainContainsCycle() {
			NodeThrowable first = new NodeThrowable("first");
			NodeThrowable second = new NodeThrowable("second");
			first.cause = second;
			second.cause = first;

			Throwable result = Throwables.unwrap(first, NodeThrowable.class);

			assertThat(result, instanceOf(NodeThrowable.class));
		}
	}

	private static final class NodeThrowable extends Throwable {

		private static final long serialVersionUID = 1L;

		private Throwable cause;

		private NodeThrowable(final String message) {
			super(message);
		}

		@Override
		public synchronized Throwable getCause() {
			return cause;
		}

	}
}
