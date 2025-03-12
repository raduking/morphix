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
package org.morphix.reflection.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.IntPredicates;

/**
 * Test class for {@link IntPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class IntPredicatesTest {

	@Test
	void shouldNegatePredicate() {
		IntPredicate greaterThan10 = x -> x > 10;
		IntPredicate lessOrEqualThan10 = IntPredicates.not(greaterThan10);

		assertTrue(lessOrEqualThan10.test(10));
		assertTrue(lessOrEqualThan10.test(-10));
		assertFalse(lessOrEqualThan10.test(11));
	}

}
