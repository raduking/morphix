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
package org.morphix.lang.function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Predicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class PredicatesTest {

	@Test
	void shouldAlwaysReturnFalseOnAlwaysFalseAndBeSingleton() {
		Predicate<Object> p1 = Predicates.alwaysFalse();
		Predicate<Object> p2 = Predicates.alwaysFalse();

		assertSame(p1, p2);

		assertFalse(p1.test(new Object()));
		assertFalse(p1.test("test"));
		assertFalse(p1.test(42));
		assertFalse(p1.test(null));
	}

	@Test
	void shouldAlwaysReturnTrueOnAlwaysTrueAndBeSingleton() {
		Predicate<Object> p1 = Predicates.alwaysTrue();
		Predicate<Object> p2 = Predicates.alwaysTrue();

		assertSame(p1, p2);

		assertTrue(p1.test(new Object()));
		assertTrue(p1.test("test"));
		assertTrue(p1.test(42));
		assertTrue(p1.test(null));
	}
}
