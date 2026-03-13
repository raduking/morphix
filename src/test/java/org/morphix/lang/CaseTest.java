package org.morphix.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link Case} enum.
 *
 * @author Radu Sebastian LAZIN
 */
class CaseTest {

	@Test
	void shouldFormatLowerCamelCase() {
		String[] words = { "hello", "world" };

		assertEquals("helloWorld", Case.LOWER_CAMEL.format(words));
	}

	@Test
	void shouldFormatLowerCamelCaseEmptyArray() {
		String[] words = { };

		assertEquals("", Case.LOWER_CAMEL.format(words));
	}

	@Test
	void shouldConvertLowerCamelCase() {
		String name = "helloWorld";

		assertEquals("helloWorld", Case.LOWER_CAMEL.convert(name));
	}

	@Test
	void shouldConvertLowerCamelCaseNullToNull() {
		assertNull(Case.LOWER_CAMEL.convert(null));
	}

	@Test
	void shouldConvertLowerCamelCaseEmptyToEmpty() {
		assertEquals("", Case.LOWER_CAMEL.convert(""));
	}

	@Test
	void shouldFormatUpperCamelCase() {
		String[] words = { "hello", "world" };

		assertEquals("HelloWorld", Case.UPPER_CAMEL.format(words));
	}

	@Test
	void shouldFormatSnakeCase() {
		String[] words = { "hello", "world" };

		assertEquals("hello_world", Case.SNAKE.format(words));
	}

	@Test
	void shouldFormatUpperSnakeCase() {
		String[] words = { "hello", "world" };

		assertEquals("HELLO_WORLD", Case.UPPER_SNAKE.format(words));
	}

	@Test
	void shouldFormatKebabCase() {
		String[] words = { "hello", "world" };

		assertEquals("hello-world", Case.KEBAB.format(words));
	}

	@Test
	void shouldCapitalizeFirstLetter() {
		String name = "hello";

		assertEquals("Hello", Case.capitalize(name));
	}

	@Test
	void shouldNotCapitalizeNull() {
		assertNull(Case.capitalize(null));
	}

	@Test
	void shouldNotCapitalizeEmpty() {
		assertEquals("", Case.capitalize(""));
	}

	@Test
	void shouldTokenizeCamelCase() {
		String name = "helloWorld";

		String[] expectedTokens = { "hello", "World" };

		assertThat(Case.tokenize(name), equalTo(expectedTokens));
	}

	@Test
	void shouldTokenizeMixedCase() {
		String name = "HTTPServerUserId";

		String[] expectedTokens = { "HTTP", "Server", "User", "Id" };

		assertThat(Case.tokenize(name), equalTo(expectedTokens));
	}

	@Test
	void shouldTokenizeEmpty() {
		String name = "";

		String[] expectedTokens = { };

		assertThat(Case.tokenize(name), equalTo(expectedTokens));
	}

	@Test
	void shouldTokenizeNull() {
		String name = null;

		String[] expectedTokens = { };

		assertThat(Case.tokenize(name), equalTo(expectedTokens));
	}

	@Test
	void shouldTokenizeSingleWord() {
		String name = "hello";

		String[] expectedTokens = { "hello" };

		assertThat(Case.tokenize(name), equalTo(expectedTokens));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForCamelToSnakeCase")
	void shouldTransformCamelToSnakeCaseVariousInputs(final String input, final String expectedOutput) {
		String result = Case.SNAKE.convert(input);

		assertThat(result, equalTo(expectedOutput));
	}

	private static Stream<Arguments> provideValuesForCamelToSnakeCase() {
		return Stream.of(
				Arguments.of("simpleTest", "simple_test"),
				Arguments.of("TestWithUppercaseStart", "test_with_uppercase_start"),
				Arguments.of("already_snake_case", "already_snake_case"),
				Arguments.of("mixedCase_InputString", "mixed_case_input_string"),
				Arguments.of("JSONValue", "json_value"),
				Arguments.of("", ""),
				Arguments.of("a", "a"),
				Arguments.of("FastXMLParser", "fast_xml_parser"));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForCamelToKebabCase")
	void shouldTransformCamelToKebabCaseVariousInputs(final String input, final String expectedOutput) {
		String result = Case.KEBAB.convert(input);

		assertThat(result, equalTo(expectedOutput));
	}

	private static Stream<Arguments> provideValuesForCamelToKebabCase() {
		return Stream.of(
				Arguments.of("simpleTest", "simple-test"),
				Arguments.of("TestWithUppercaseStart", "test-with-uppercase-start"),
				Arguments.of("already_snake_case", "already-snake-case"),
				Arguments.of("mixedCase_InputString", "mixed-case-input-string"),
				Arguments.of("JSONValue", "json-value"),
				Arguments.of("", ""),
				Arguments.of("a", "a"),
				Arguments.of("FastXMLParser", "fast-xml-parser"));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForMixedCaseToCamelCase")
	void shouldTransformMixedCaseToCamelCase(final String input, final String expectedOutput) {
		String result = Case.LOWER_CAMEL.convert(input);

		assertThat(result, equalTo(expectedOutput));
	}

	private static Stream<Arguments> provideValuesForMixedCaseToCamelCase() {
		return Stream.of(
				Arguments.of("simple_test", "simpleTest"),
				Arguments.of("Test_With_Uppercase_Start", "testWithUppercaseStart"),
				Arguments.of("alreadyCamelCase", "alreadyCamelCase"),
				Arguments.of("mixedCase_InputString", "mixedCaseInputString"),
				Arguments.of("_half_snake", "halfSnake"),
				Arguments.of("", ""),
				Arguments.of("a", "a"),
				Arguments.of("__HTTPServer__user-id", "httpServerUserId"),
				Arguments.of("JSONValue", "jsonValue"),
				Arguments.of("ipv6_address", "ipv6Address"),
				Arguments.of("CSS4Version", "css4Version"),
				Arguments.of("user2FA", "user2Fa"),
				Arguments.of("BEAST666", "beast666"),
				Arguments.of("Number-of-the-BEAST-666", "numberOfTheBeast666"),
				Arguments.of("a_b_c_d_e", "aBCDE"),
				Arguments.of("aBaBaBaB", "aBaBaBaB"));
	}

	@Test
	void shouldReturnTheCorrectSeparatorForEachCase() {
		assertEquals("", Case.LOWER_CAMEL.wordSeparator());
		assertEquals("", Case.UPPER_CAMEL.wordSeparator());
		assertEquals("_", Case.SNAKE.wordSeparator());
		assertEquals("_", Case.UPPER_SNAKE.wordSeparator());
		assertEquals("-", Case.KEBAB.wordSeparator());
	}
}
