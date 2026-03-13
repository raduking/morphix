package org.morphix.lang;

import java.util.Arrays;
import java.util.Locale;

/**
 * Enum representing different naming cases for strings. Each enum constant provides a method to format an array of
 * words into the corresponding case style and methods to convert from one case to another and to convert an arbitrary
 * {@link String} input into the specified case.
 * <p>
 * It uses a tokenization approach to split the input string into words based on common delimiters (hyphens,
 * underscores) and camel case boundaries without relying on regular expressions. The tokenization method processes the
 * input string character by character to identify word boundaries and extract the individual words.
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
		/**
		 * @see Case#format(String[], Locale)
		 */
		@Override
		public String format(final String[] words, final Locale locale) {
			if (words.length == 0) {
				return "";
			}
			StringBuilder sb = new StringBuilder(words[0].toLowerCase(locale));
			for (int i = 1; i < words.length; ++i) {
				sb.append(capitalize(words[i], locale));
			}
			return sb.toString();
		}
	},

	/**
	 * Upper camel case style, where all words are capitalized (e.g. "UpperCamelCase").
	 */
	UPPER_CAMEL {
		/**
		 * @see Case#format(String[], Locale)
		 */
		@Override
		public String format(final String[] words, final Locale locale) {
			StringBuilder sb = new StringBuilder();
			for (String word : words) {
				sb.append(capitalize(word, locale));
			}
			return sb.toString();
		}
	},

	/**
	 * Snake case style, where all words are in lowercase and separated by underscores (e.g. "snake_case").
	 */
	SNAKE {
		/**
		 * The separator used for joining words in snake case style, which is an underscore ("_").
		 */
		public static final String SEPARATOR = "_";

		/**
		 * @see Case#format(String[], Locale)
		 */
		@Override
		public String format(final String[] words, final Locale locale) {
			return join(words, SEPARATOR, locale, Letter.LOWER);
		}

		/**
		 * @see Case#wordSeparator()
		 */
		@Override
		public String wordSeparator() {
			return SEPARATOR;
		}
	},

	/**
	 * Upper snake case style, where all words are in uppercase and separated by underscores (e.g. "UPPER_SNAKE_CASE").
	 */
	UPPER_SNAKE {
		/**
		 * The separator used for joining words in upper snake case style, which is an underscore ("_").
		 */
		public static final String SEPARATOR = "_";

		/**
		 * @see Case#format(String[], Locale)
		 */
		@Override
		public String format(final String[] words, final Locale locale) {
			return join(words, SEPARATOR, locale, Letter.UPPER);
		}

		/**
		 * @see Case#wordSeparator()
		 */
		@Override
		public String wordSeparator() {
			return SEPARATOR;
		}
	},

	/**
	 * Kebab case style, where all words are in lowercase and separated by hyphens (e.g. "kebab-case").
	 */
	KEBAB {
		/**
		 * The separator used for joining words in kebab case style, which is a hyphen ("-").
		 */
		public static final String SEPARATOR = "-";

		/**
		 * @see Case#format(String[], Locale)
		 */
		@Override
		public String format(final String[] words, final Locale locale) {
			return join(words, SEPARATOR, locale, Letter.LOWER);
		}

		/**
		 * @see Case#wordSeparator()
		 */
		@Override
		public String wordSeparator() {
			return SEPARATOR;
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
	 * Convert an arbitrary input name into this case.
	 *
	 * @param name the name to convert
	 * @return the converted name in this case
	 */
	public String convert(final String name) {
		return convert(name, Locale.ROOT);
	}

	/**
	 * Convert an arbitrary input name into this case.
	 *
	 * @param name the name to convert
	 * @param locale the locale to use for any locale-specific formatting
	 * @return the converted name in this case
	 */
	public String convert(final String name, final Locale locale) {
		if (null == name || name.isEmpty()) {
			return name;
		}
		return format(tokenize(name), locale);
	}

	/**
	 * Format an array of words into the specific case style defined by the enum constant. The implementation of this method
	 * is provided by each enum constant to define how the words should be formatted according to the case style.
	 *
	 * @param words the array of words to format
	 * @return the formatted string in the specific case style
	 */
	public String format(final String[] words) {
		return format(words, Locale.ROOT);
	}

	/**
	 * Format an array of words into the specific case style defined by the enum constant, using the provided locale for any
	 * locale-specific formatting. The implementation of this method is provided by each enum constant to define how the
	 * words should be formatted according to the case style and locale.
	 *
	 * @param words the array of words to format
	 * @param locale the locale to use for formatting
	 *
	 * @return the formatted string in the specific case style and locale
	 */
	public abstract String format(String[] words, Locale locale);

	/**
	 * Returns the separator used for joining words in this case style. By default, it returns an empty string, but it can
	 * be overridden by specific case styles that use a separator (e.g. SNAKE, UPPER_SNAKE, KEBAB) to return the appropriate
	 * separator (e.g. "_" for snake case and "-" for kebab case).
	 *
	 * @return the separator used for joining words in this case style
	 */
	public String wordSeparator() {
		return "";
	}

	/**
	 * Tokenizes the input name into an array of words by splitting on hyphens, underscores, and camel case boundaries. The
	 * method first normalizes the input by replacing hyphens with underscores, then splits the normalized string on
	 * underscores, and finally further splits each part on camel case boundaries. The resulting tokens are filtered to
	 * remove any empty strings and returned as an array.
	 *
	 * @param name the input name to tokenize
	 * @return an array of words extracted from the input name
	 */
	public static String[] tokenize(final String name) { // NOSONAR sometimes tokenization is harder to read, deal with it
		if (null == name || name.isEmpty()) {
			return new String[0];
		}
		int length = name.length();
		// the worst cases would be "a_b_c_d_e_f_g_h" or "aBcdEfgH", which would result in length / 2 + 1 words
		// so we allocate an array of that size plus one extra for safety
		String[] words = new String[(length / 2 + 1) + 1];
		int wordCount = 0;

		int wordStartIndex = -1;
		for (int i = 0; i < length; ++i) {
			char c = name.charAt(i);
			boolean isWordSeparator = c == '_' || c == '-';
			if (isWordSeparator) {
				if (wordStartIndex >= 0) {
					words[wordCount] = name.substring(wordStartIndex, i);
					++wordCount;
					wordStartIndex = -1;
				}
			} else if (wordStartIndex < 0) {
				wordStartIndex = i;
				isWordSeparator = true;
			}
			if (isWordSeparator) {
				continue;
			}
			// this part is an optimization to avoid checking for camel case boundaries on every character, we only check when the
			// previous character is a letter or a digit and the current character is a letter, or when the previous character is an
			// uppercase letter and the current character is an uppercase letter followed by a lowercase letter (e.g. "JSONValue"
			// should be split into "JSON" and "Value")
			char prev = name.charAt(i - 1);

			boolean wordEnded = (Character.isLowerCase(prev) && Character.isUpperCase(c))
					|| (Character.isDigit(prev) && Character.isLetter(c))
					|| (Character.isUpperCase(prev) && Character.isUpperCase(c)
							&& i + 1 < length && Character.isLowerCase(name.charAt(i + 1)));

			if (wordEnded) {
				words[wordCount] = name.substring(wordStartIndex, i);
				++wordCount;
				wordStartIndex = i;
			}
		}
		words[wordCount] = name.substring(wordStartIndex);
		++wordCount;
		return Arrays.copyOf(words, wordCount);
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
	private static String join(final String[] words, final String sep, final Locale locale, final Letter letter) {
		String joined = String.join(sep, words);
		return switch (letter) {
			case LOWER -> joined.toLowerCase(locale);
			case UPPER -> joined.toUpperCase(locale);
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
		return capitalize(word, Locale.ROOT);
	}

	/**
	 * Capitalizes the first letter of the given word and converts the rest of the letters to lowercase. If the input word
	 * is empty, it returns the empty string. This method is used to convert words to a format suitable for camel case
	 * styles.
	 *
	 * @param word the input word to capitalize
	 * @param locale the locale to use for any locale-specific formatting
	 * @return the capitalized word with the first letter in uppercase and the rest in lowercase
	 */
	public static String capitalize(final String word, final Locale locale) {
		if (null == word || word.isEmpty()) {
			return word;
		}
		if (word.length() == 1) {
			return word.toUpperCase(locale);
		}
		return word.substring(0, 1).toUpperCase(locale) +
				word.substring(1).toLowerCase(locale);
	}
}
