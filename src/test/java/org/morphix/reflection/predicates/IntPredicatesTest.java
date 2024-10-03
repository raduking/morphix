package org.morphix.reflection.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;

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
