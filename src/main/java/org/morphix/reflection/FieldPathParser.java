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

import static org.morphix.reflection.JavaModifier.STATIC;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Cursor-based field path parser. Each selector is resolved as it is consumed, without allocating tokens or splitting
 * the expression into strings. The same parser handles single paths and comma-separated alternatives.
 *
 * @author Radu Sebastian LAZIN
 */
final class FieldPathParser {

	private final String paths;

	private final int length;

	private final boolean alternatives;

	private final boolean findField;

	private int index;

	private boolean invalid;

	FieldPathParser(final String paths, final boolean alternatives, final boolean findField) {
		this.paths = paths;
		this.length = paths.length();
		this.alternatives = alternatives;
		this.findField = findField;
	}

	/**
	 * Resolves paths in order, stopping at the first match. A malformed alternative can be skipped if its end is
	 * unambiguous. An unterminated bracket or quoted selector consumes the remainder of the expression.
	 *
	 * @param root root object
	 * @return resolved value or extended field, or null
	 */
	Object resolve(final Object root) {
		while (hasNext()) {
			if (alternatives) {
				skipWhitespace();
				if (consume(',')) {
					continue;
				}
			}
			if (!hasNext()) {
				break;
			}
			invalid = false;
			Object result = readPath(root);
			if (!alternatives) {
				return !invalid && !hasNext() ? result : null;
			}
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
			return FieldPaths.readProperty(obj, name);
		}
		if (obj instanceof Map<?, ?> map) {
			return atPathEnd() ? null : map.get(name);
		}
		Field field = Fields.Unchecked.getOneDeclaredInHierarchy(Classes.getFrom(obj), name);
		if (null == field) {
			return null;
		}
		if (STATIC.isPresentOn(field)) {
			return null;
		}
		if (atPathEnd()) {
			return ExtendedField.of(field, obj);
		}
		return Fields.IgnoreAccess.get(obj, field);
	}

	private Object readBracket(final Object obj) {
		if (!hasNext()) {
			return fail();
		}
		char ch = peek();
		Object value;
		if (isQuote(ch)) {
			++index;
			String key = readQuoted(ch);
			if (invalid) {
				return null;
			}
			if (!consume(']')) {
				skipBracket();
				return fail();
			}
			value = FieldPaths.readMapKey(obj, key);
		} else {
			int start = index;
			boolean numeric = true;
			while (hasNext() && ']' != peek()) {
				ch = peek();
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
					value = FieldPaths.readIndex(obj, elementIndex);
				} catch (NumberFormatException e) {
					return fail();
				}
			} else {
				value = FieldPaths.readMapKey(obj, paths.substring(start, end));
			}
		}
		return findField && atPathEnd() ? null : value;
	}

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

	private boolean isPropertyDelimiter(final char ch) {
		return '.' == ch || '[' == ch || ']' == ch || (alternatives && ',' == ch);
	}

	private static boolean isQuote(final char ch) {
		return '\'' == ch || '"' == ch;
	}

	private boolean hasNext() {
		return index < length;
	}

	private char peek() {
		return paths.charAt(index);
	}

	private boolean consume(final char ch) {
		if (hasNext() && ch == peek()) {
			++index;
			return true;
		}
		return false;
	}

	private void skipWhitespace() {
		while (hasNext() && peek() <= ' ') {
			++index;
		}
	}

	private int trimEnd(final int start, final int end) {
		int result = end;
		while (start < result && paths.charAt(result - 1) <= ' ') {
			--result;
		}
		return result;
	}

	private Object fail() {
		invalid = true;
		return null;
	}
}
