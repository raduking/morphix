package org.morphix.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link JavaModifier}.
 */
class JavaModifierTest {

	private static final List<JavaModifier> ACCESS_MODIFIERS = List.of(
			JavaModifier.PRIVATE,
			JavaModifier.PROTECTED,
			JavaModifier.PUBLIC
	);

    @ParameterizedTest
    @EnumSource(JavaModifier.class)
    void shouldBuildWithFromStringWithValidValue(final JavaModifier modifier) {
    	String stringRoute = modifier.name().toLowerCase();
    	JavaModifier result = JavaModifier.fromString(stringRoute);
        assertThat(result, equalTo(modifier));
    }

    @Test
    void shouldBuildWithFromStringWithValidValueAnyCase() {
    	String stringRoute = "PrOtEcTeD";
    	JavaModifier result = JavaModifier.fromString(stringRoute);
        assertThat(result, equalTo(JavaModifier.PROTECTED));
    }

    @ParameterizedTest
    @EnumSource(JavaModifier.class)
    void shouldReturnTheSameValueOnToStringAndGetValue(final JavaModifier modifier) {
    	assertThat(modifier.toString(), equalTo(modifier.getValue()));
    }

    @ParameterizedTest
    @EnumSource(JavaModifier.class)
    void shouldSetTheValueToLowerCaseString(final JavaModifier modifier) {
    	assertThat(modifier.getValue(), equalTo(modifier.name().toLowerCase()));
    }

    @ParameterizedTest
    @MethodSource("provideAccessModifiers")
    void shouldSetTrueForAccessModifiers(final JavaModifier modifier) {
    	assertTrue(modifier.isAccessModifier());
    }

    @ParameterizedTest
    @EnumSource(JavaModifier.class)
    void shouldSetTrueAndFalseForAllModifiersDependingOnAccess(final JavaModifier modifier) {
    	if (ACCESS_MODIFIERS.contains(modifier)) {
    		assertTrue(modifier.isAccessModifier());
    	} else {
    		assertFalse(modifier.isAccessModifier());
    	}
    }

    private static Stream<Arguments> provideAccessModifiers() {
    	return ACCESS_MODIFIERS.stream().map(Arguments::of);
    }

}
