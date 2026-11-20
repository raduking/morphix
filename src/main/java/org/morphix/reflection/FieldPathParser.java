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
package org.morphix.reflection;

import java.lang.reflect.Field;
import java.util.Map;

import org.morphix.lang.JavaObjects;

/**
 * Cursor-based field path parser. Each selector is resolved as it is consumed, without allocating tokens or splitting
 * the expression into strings.
 * <p>
 * {@link Syntax} selects a single path or comma-separated alternatives, while {@link Target} selects whether the result
 * is a value or a reflective field with its owner.
 *
 * @author Radu Sebastian LAZIN
 */
public final class FieldPathParser {

	/**
	 * Controls how many paths the expression may contain.
	 */
	enum Syntax {

		/**
		 * Parses the entire expression as one path.
		 */
		SINGLE_PATH,

		/**
		 * Parses comma-separated paths and stops at the first non-null value or matching field.
		 */
		ALTERNATIVE_PATHS
	}

	/**
	 * Selects the kind of result produced by the parser.
	 */
	enum Target {

		/**
		 * Returns the value found at a path.
		 */
		VALUE,

		/**
		 * Returns the final non-static field and its owner, even when the field value is null.
		 */
		FIELD
	}

	/**
	 * Path expression being parsed.
	 */
	private final String paths;

	/**
	 * Length of the path expression.
	 */
	private final int length;

	/**
	 * Whether comma-separated alternative paths are accepted.
	 */
	private final boolean alternatives;

	/**
	 * Whether the result must be a reflective field and its owning object.
	 */
	private final boolean findField;

	/**
	 * Index of the next character to read from {@link #paths}.
	 */
	private int index;

	/**
	 * Whether the current alternative contains invalid syntax.
	 */
	private boolean invalid;

	/**
	 * Creates a parser for the given expression, path syntax, and result target.
	 *
	 * @param paths path expression
	 * @param syntax whether the expression contains one path or comma-separated alternatives
	 * @param target whether to resolve a value or a field with its owner
	 */
	private FieldPathParser(final String paths, final Syntax syntax, final Target target) {
		this.paths = paths;
		this.length = paths.length();
		this.alternatives = Syntax.ALTERNATIVE_PATHS == syntax;
		this.findField = Target.FIELD == target;
	}

	/**
	 * Resolves an expression using the requested syntax and result target.
	 *
	 * @param root root object
	 * @param paths path expression
	 * @param syntax whether the expression contains one path or comma-separated alternatives
	 * @param target whether to resolve a value or a field with its owner
	 * @return resolved value or extended field, or null if the root or expression is null, the expression is empty, or no
	 * path matches
	 */
	static Object resolve(final Object root, final String paths, final Syntax syntax, final Target target) {
		if (null == root || null == paths || paths.isEmpty()) {
			return null;
		}
		return new FieldPathParser(paths, syntax, target).resolve(root);
	}

	/**
	 * Resolves one path to its value.
	 *
	 * @param <U> value type
	 * @param root root object
	 * @param path path to resolve
	 * @return value, or null if the path cannot be resolved
	 */
	public static <U> U resolveValue(final Object root, final String path) {
		return JavaObjects.cast(FieldPathParser.resolve(root, path, Syntax.SINGLE_PATH, Target.VALUE));
	}

	/**
	 * Resolves comma-separated paths to the first non-null value.
	 *
	 * @param <U> value type
	 * @param root root object
	 * @param paths alternative paths
	 * @return first non-null value, or null if no path resolves
	 */
	public static <U> U resolveFirstValue(final Object root, final String paths) {
		return JavaObjects.cast(FieldPathParser.resolve(root, paths, Syntax.ALTERNATIVE_PATHS, Target.VALUE));
	}

	/**
	 * Resolves comma-separated paths to the first non-static field and its owner. A field may be returned even when its
	 * value is null.
	 *
	 * @param root root object
	 * @param paths alternative paths
	 * @return field and its owner, or {@link ExtendedField#EMPTY} if no path resolves to a field
	 */
	public static ExtendedField resolveFirstField(final Object root, final String paths) {
		ExtendedField field = JavaObjects.cast(FieldPathParser.resolve(root, paths, Syntax.ALTERNATIVE_PATHS, Target.FIELD));
		return null == field ? ExtendedField.EMPTY : field;
	}

	/**
	 * Resolves the expression according to the configured syntax and target.
	 *
	 * @param root root object
	 * @return resolved value or extended field, or null
	 */
	Object resolve(final Object root) {
		return alternatives ? resolveAlternatives(root) : resolveSinglePath(root);
	}

	/**
	 * Resolves one path, requiring the entire expression to be valid.
	 *
	 * @param root root object
	 * @return resolved value, or null if the path is invalid or cannot be resolved
	 */
	private Object resolveSinglePath(final Object root) {
		Object result = readPath(root);
		return !invalid && !hasNext() ? result : null;
	}

	/**
	 * Resolves paths in order, stopping at the first match. A malformed alternative can be skipped if its end is
	 * unambiguous. An unterminated bracket or quoted selector consumes the remainder of the expression.
	 *
	 * @param root root object
	 * @return resolved value or extended field, or null if no alternative matches
	 */
	private Object resolveAlternatives(final Object root) {
		while (hasNext()) {
			skipWhitespace();
			if (consume(',')) {
				continue;
			}
			if (!hasNext()) {
				return null;
			}
			invalid = false;
			Object result = readPath(root);
			skipWhitespace();
			if (hasNext() && ',' != peek()) {
				invalid = true;
				skipInvalidPath();
			}
			if (!invalid && null != result) {
				return result;
			}
			consume(',');
		}
		return null;
	}

	/**
	 * Reads one path, resolving each property or bracket selector as it is consumed.
	 *
	 * @param root root object
	 * @return resolved value or extended field, or null if the path is invalid or cannot be resolved
	 */
	private Object readPath(final Object root) {
		Object value = root;
		if ('[' != peek()) {
			value = readProperty(value);
		}
		while (hasNext() && !invalid) {
			if (consume('[')) {
				value = readBracket(value);
			} else if (consume('.')) {
				value = readProperty(value);
			} else {
				break;
			}
		}
		return invalid ? null : value;
	}

	/**
	 * Reads a property name and resolves it against an object or map. In field mode, the final property produces an
	 * extended field without reading its value.
	 *
	 * @param obj object containing the property
	 * @return property value or extended field, or null if it cannot be resolved
	 */
	private Object readProperty(final Object obj) {
		int start = index;
		while (hasNext() && !isPropertyDelimiter(peek())) {
			++index;
		}
		int end = index;
		if (alternatives && (!hasNext() || ',' == peek())) {
			end = trimEnd(start, end);
		}
		if (start == end) {
			return fail();
		}
		if (null == obj) {
			return null;
		}
		String name = paths.substring(start, end);
		if (!findField) {
			if (obj instanceof Map<?, ?> map) {
				return map.get(name);
			}
			return Fields.Safe.get(obj, name);
		}
		if (obj instanceof Map<?, ?> map) {
			return atPathEnd() ? null : map.get(name);
		}
		Class<?> type = obj.getClass();
		Field field = Fields.Unchecked.getOneDeclaredInHierarchy(type, name);
		if (null == field) {
			return null;
		}
		if (JavaModifier.STATIC.isPresentOn(field)) {
			return null;
		}
		if (atPathEnd()) {
			return ExtendedField.of(field, obj);
		}
		return Fields.IgnoreAccess.get(obj, field);
	}

	/**
	 * Reads a bracket selector as an array or list index, or as a map key. The opening bracket has already been consumed.
	 *
	 * @param obj container to read
	 * @return selected value, or null if the selector is invalid or cannot be resolved
	 */
	private Object readBracket(final Object obj) {
		if (!hasNext()) {
			return fail();
		}
		Object value = isQuote(peek()) ? readQuotedKey(obj) : readUnquotedSelector(obj);
		return findField && atPathEnd() ? null : value;
	}

	/**
	 * Reads a quoted map key and its closing bracket.
	 *
	 * @param obj map containing the key
	 * @return map value, or null if the selector is invalid or the key is absent
	 */
	private Object readQuotedKey(final Object obj) {
		char quote = peek();
		++index;
		String key = readQuoted(quote);
		if (invalid) {
			return null;
		}
		if (!consume(']')) {
			skipBracket();
			return fail();
		}
		return readMapKey(obj, key);
	}

	/**
	 * Reads an unquoted array or list index, or a map key, and its closing bracket.
	 *
	 * @param obj container to read
	 * @return selected value, or null if the selector is invalid or cannot be resolved
	 */
	private Object readUnquotedSelector(final Object obj) {
		int start = index;
		boolean numeric = true;
		while (hasNext() && ']' != peek()) {
			char ch = peek();
			if ('[' == ch || isQuote(ch)) {
				skipBracket();
				return fail();
			}
			numeric &= Character.isDigit(ch);
			++index;
		}
		int end = index;
		if (!consume(']') || start == end) {
			return fail();
		}
		if (numeric) {
			try {
				int elementIndex = Integer.parseInt(paths, start, end, 10);
				return JavaObjects.getAt(obj, elementIndex);
			} catch (NumberFormatException e) {
				return fail();
			}
		}
		return readMapKey(obj, paths.substring(start, end));
	}

	/**
	 * Reads a string key from a map. Any other container yields null.
	 *
	 * @param obj container
	 * @param key map key
	 * @return map value, or null if {@code obj} is not a map or the key is absent
	 */
	private static Object readMapKey(final Object obj, final String key) {
		if (obj instanceof Map<?, ?> map) {
			return map.get(key);
		}
		return null;
	}

	/**
	 * Reads a quoted map key, replacing backslash escapes with the escaped characters. The opening quote has already been
	 * consumed.
	 *
	 * @param quote quote character that closes the key
	 * @return unescaped key, or null if the closing quote is missing
	 */
	private String readQuoted(final char quote) {
		int start = index;
		StringBuilder builder = null;
		while (hasNext()) {
			char ch = peek();
			if (quote == ch) {
				String key = null == builder ? paths.substring(start, index) : builder.append(paths, start, index).toString();
				++index;
				return key;
			}
			if ('\\' == ch) {
				if (null == builder) {
					builder = new StringBuilder();
				}
				builder.append(paths, start, index);
				++index;
				if (!hasNext()) {
					invalid = true;
					return null;
				}
				builder.append(peek());
				start = index + 1;
			}
			++index;
		}
		invalid = true;
		return null;
	}

	/**
	 * Recovers at a top-level comma, using the same quote and bracket rules as normal parsing.
	 */
	private void skipInvalidPath() {
		while (hasNext() && ',' != peek()) {
			if (consume('[')) {
				skipBracket();
			} else {
				++index;
			}
		}
	}

	/**
	 * Consumes the remainder of a bracket, including nested malformed brackets and quoted selectors.
	 */
	private void skipBracket() {
		int depth = 1;
		while (hasNext() && 0 < depth) {
			char ch = peek();
			++index;
			if (isQuote(ch)) {
				readQuoted(ch);
			} else if ('[' == ch) {
				++depth;
			} else if (']' == ch) {
				--depth;
			}
		}
	}

	/**
	 * Checks whether the cursor is at the end of the current path, allowing trailing whitespace in alternative mode.
	 *
	 * @return true if the current path ends here
	 */
	private boolean atPathEnd() {
		if (!hasNext()) {
			return true;
		}
		if (!alternatives) {
			return false;
		}
		int end = index;
		while (end < length && paths.charAt(end) <= ' ') {
			++end;
		}
		return end == length || ',' == paths.charAt(end);
	}

	/**
	 * Checks whether a character terminates a property name in the current mode.
	 *
	 * @param ch character to check
	 * @return true if the character terminates a property name
	 */
	private boolean isPropertyDelimiter(final char ch) {
		return '.' == ch || '[' == ch || ']' == ch || (alternatives && ',' == ch);
	}

	/**
	 * Checks whether a character can quote a map key.
	 *
	 * @param ch character to check
	 * @return true for a single or double quote
	 */
	private static boolean isQuote(final char ch) {
		return '\'' == ch || '"' == ch;
	}

	/**
	 * Checks whether the cursor has another character to read.
	 *
	 * @return true if the cursor is before the end of the expression
	 */
	private boolean hasNext() {
		return index < length;
	}

	/**
	 * Returns the character at the cursor without advancing it.
	 *
	 * @return current character
	 */
	private char peek() {
		return paths.charAt(index);
	}

	/**
	 * Consumes the next character if it matches the expected character.
	 *
	 * @param ch expected character
	 * @return true if the character matched and was consumed
	 */
	private boolean consume(final char ch) {
		if (hasNext() && ch == peek()) {
			++index;
			return true;
		}
		return false;
	}

	/**
	 * Skips whitespace at the cursor using the same character range as {@link String#trim()}.
	 */
	private void skipWhitespace() {
		while (hasNext() && peek() <= ' ') {
			++index;
		}
	}

	/**
	 * Finds the end of a property name after removing trailing whitespace from the given range.
	 *
	 * @param start start of the property name, inclusive
	 * @param end end of the property name, exclusive
	 * @return end index after trailing whitespace is removed
	 */
	private int trimEnd(final int start, final int end) {
		int result = end;
		while (start < result && paths.charAt(result - 1) <= ' ') {
			--result;
		}
		return result;
	}

	/**
	 * Marks the current alternative as invalid.
	 *
	 * @return null so callers can return the failed resolution
	 */
	private Object fail() {
		invalid = true;
		return null;
	}
}
