package org.morphix.lang;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Enum representing different naming cases for strings. Each enum constant provides a method to format an array of
 * words into the corresponding case style and methods to convert from one case to another and to convert an arbitrary
 * {@link String} input into the specified case.
 * <p>
 * The case styles supported by this enum include:
 * <ul>
 * <li>{@code LOWER_CAMEL} (e.g. "lowerCamelCase")</li>
 * <li>{@code UPPER_CAMEL} (e.g. "UpperCamelCase")</li>
 * <li>{@code SNAKE} (e.g. "snake_case")</li>
 * <li>{@code UPPER_SNAKE} (e.g. "UPPER_SNAKE_CASE")</li>
 * <li>{@code KEBAB} (e.g. "kebab-case")</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public enum Case {

	/**
	 * Lower camel case style, where the first word is in lowercase and subsequent words are capitalized (e.g.
	 * "lowerCamelCase").
	 */
	LOWER_CAMEL {
		@Override
		public String format(final String[] words) {
			if (words.length == 0) {
				return "";
			}
			StringBuilder sb = new StringBuilder(words[0].toLowerCase(Locale.ROOT));
			for (int i = 1; i < words.length; i++) {
				sb.append(capitalize(words[i]));
			}
			return sb.toString();
		}
	},

	/**
	 * Upper camel case style, where all words are capitalized (e.g. "UpperCamelCase").
	 */
	UPPER_CAMEL {
		@Override
		public String format(final String[] words) {
			StringBuilder sb = new StringBuilder();
			for (String word : words) {
				sb.append(capitalize(word));
			}
			return sb.toString();
		}
	},

	/**
	 * Snake case style, where all words are in lowercase and separated by underscores (e.g. "snake_case").
	 */
	SNAKE {
		@Override
		public String format(final String[] words) {
			return join(words, "_", Letter.LOWER);
		}
	},

	/**
	 * Upper snake case style, where all words are in uppercase and separated by underscores (e.g. "UPPER_SNAKE_CASE").
	 */
	UPPER_SNAKE {
		@Override
		public String format(final String[] words) {
			return join(words, "_", Letter.UPPER);
		}
	},

	/**
	 * Kebab case style, where all words are in lowercase and separated by hyphens (e.g. "kebab-case").
	 */
	KEBAB {
		@Override
		public String format(final String[] words) {
			return join(words, "-", Letter.LOWER);
		}
	};

	/**
	 * Enum representing the letter case to apply when joining words in a case style. It can be either {@code LOWER} or
	 * {@code UPPER}.
	 */
	public enum Letter {

		/**
		 * Lowercase letter case, where all letters are converted to lowercase.
		 */
		LOWER,

		/**
		 * Uppercase letter case, where all letters are converted to uppercase.
		 */
		UPPER
	}

	/**
	 * Format an array of words into the specific case style defined by the enum constant. The implementation of this method
	 * is provided by each enum constant to define how the words should be formatted according to the case style.
	 *
	 * @param words the array of words to format
	 * @return the formatted string in the specific case style
	 */
	public abstract String format(String[] words);

	/**
	 * Convert an arbitrary input name into this case.
	 *
	 * @param name the name to convert
	 * @return the converted name in this case
	 */
	public String convert(final String name) {
		if (null == name || name.isEmpty()) {
			return name;
		}
		return format(tokenize(name));
	}

	/**
	 * Regular expression pattern to split camel case words. It matches the boundaries between lowercase letters or digits
	 * followed by uppercase letters, and between uppercase letters followed by uppercase letters and then lowercase
	 * letters. This allows for proper tokenization of camel case strings into individual words.
	 */
	private static final Pattern CAMEL_BOUNDARY =
			Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

	/**
	 * Tokenizes the input name into an array of words by splitting on hyphens, underscores, and camel case boundaries. The
	 * method first normalizes the input by replacing hyphens with underscores, then splits the normalized string on
	 * underscores, and finally further splits each part on camel case boundaries. The resulting tokens are filtered to
	 * remove any empty strings and returned as an array.
	 *
	 * @param name the input name to tokenize
	 * @return an array of words extracted from the input name
	 */
	private static String[] tokenize(final String name) {
		String normalized = name.replace('-', '_');
		return Arrays.stream(normalized.split("_"))
				.flatMap(part -> Arrays.stream(CAMEL_BOUNDARY.split(part)))
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	/**
	 * Joins the given array of words into a single string using the specified separator and letter case. The method first
	 * joins the words using the provided separator, then applies the specified letter case (either lowercase or uppercase)
	 * to the joined string before returning it.
	 *
	 * @param words the array of words to join
	 * @param sep the separator to use between words
	 * @param letter the letter case to apply to the joined string (either LOWER or UPPER)
	 * @return the joined string with the specified separator and letter case
	 */
	private static String join(final String[] words, final String sep, final Letter letter) {
		String joined = String.join(sep, words);
		return switch (letter) {
			case LOWER -> joined.toLowerCase(Locale.ROOT);
			case UPPER -> joined.toUpperCase(Locale.ROOT);
		};
	}

	/**
	 * Capitalizes the first letter of the given word and converts the rest of the letters to lowercase. If the input word
	 * is empty, it returns the empty string. This method is used to convert words to a format suitable for camel case
	 * styles.
	 *
	 * @param word the input word to capitalize
	 * @return the capitalized word with the first letter in uppercase and the rest in lowercase
	 */
	public static String capitalize(final String word) {
		if (null == word || word.isEmpty()) {
			return word;
		}
		return Character.toUpperCase(word.charAt(0)) +
				word.substring(1).toLowerCase(Locale.ROOT);
	}
}
