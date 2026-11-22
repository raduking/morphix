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
 * Resolves field paths one selector at a time. Alternative paths are separated before resolving each path, so an
 * invalid alternative does not affect the next one.
 * <p>
 * Value paths can read private and static fields. Treat path expressions as trusted input. If callers supply paths,
 * validate them against an allowlist or provide a root object containing only data those callers may read.
 *
 * @author Radu Sebastian LAZIN
 */
public final class FieldPathParser {

	/**
	 * Result requested from a path.
	 */
	private enum Target {

		/**
		 * The value at the end of the path.
		 */
		VALUE,

		/**
		 * The final non-static field and its owner.
		 */
		FIELD
	}

	/**
	 * Path currently being resolved.
	 */
	private final String path;

	/**
	 * Position of the next character in {@link #path}.
	 */
	private int index;

	/**
	 * Creates a parser for one path.
	 *
	 * @param path path to resolve
	 */
	private FieldPathParser(final String path) {
		this.path = path;
	}

	/**
	 * Resolves one path to its value.
	 *
	 * @param <U> value type
	 * @param root root object
	 * @param path path to resolve
	 * @return value, or null if the path is invalid or cannot be resolved
	 */
	public static <U> U resolveValue(final Object root, final String path) {
		return resolvePath(root, path, Target.VALUE);
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
		return resolveAlternatives(root, paths, Target.VALUE, null);
	}

	/**
	 * Resolves comma-separated paths to the first non-static field and its owner. A field can be returned even when its
	 * value is null.
	 *
	 * @param root root object
	 * @param paths alternative paths
	 * @return field and its owner, or {@link ExtendedField#EMPTY} if none matches
	 */
	public static ExtendedField resolveFirstField(final Object root, final String paths) {
		return resolveAlternatives(root, paths, Target.FIELD, ExtendedField.EMPTY);
	}

	/**
	 * Tries each alternative in order. Commas inside bracket selectors belong to the selector.
	 *
	 * @param root root object
	 * @param paths alternative paths
	 * @param target result type
	 * @param defaultValue result when no path matches
	 * @return first matching result, or {@code defaultValue}
	 */
	private static <U> U resolveAlternatives(final Object root, final String paths, final Target target, final U defaultValue) {
		if (null == root || null == paths) {
			return defaultValue;
		}
		int start = 0;
		while (start < paths.length()) {
			int end = indexOfNextComma(paths, start);
			String path = paths.substring(start, end).trim();
			U result = resolvePath(root, path, target);
			if (null != result) {
				return result;
			}
			start = end + 1;
		}
		return defaultValue;
	}

	/**
	 * Finds the index of the next comma outside brackets and quoted map keys.
	 *
	 * @param paths alternative paths
	 * @param start first character to scan
	 * @return comma position, or the length of the expression
	 */
	private static int indexOfNextComma(final String paths, final int start) {
		int depth = 0;
		char quote = 0;
		int i = start;
		while (i < paths.length()) {
			char ch = paths.charAt(i);
			if (0 != quote) {
				if ('\\' == ch && i + 1 < paths.length()) {
					++i;
				} else if (quote == ch) {
					quote = 0;
				}
			} else if (0 < depth && isQuote(ch)) {
				quote = ch;
			} else if ('[' == ch) {
				++depth;
			} else if (']' == ch && 0 < depth) {
				--depth;
			} else if (',' == ch && 0 == depth) {
				return i;
			}
			++i;
		}
		return paths.length();
	}

	/**
	 * Resolves a single complete path.
	 *
	 * @param root root object
	 * @param path path to resolve
	 * @param target result type
	 * @return resolved value or field, or null
	 */
	private static <U> U resolvePath(final Object root, final String path, final Target target) {
		if (null == root || null == path || path.isEmpty()) {
			return null;
		}
		var parser = new FieldPathParser(path);
		Object result = parser.readPath(root, target);
		return JavaObjects.cast(result);
	}

	/**
	 * Reads each property or bracket selector until the path ends.
	 *
	 * @param root root object
	 * @param target result type
	 * @return resolved value or field, or null
	 */
	private Object readPath(final Object root, final Target target) {
		Object value = root;
		if ('[' != path.charAt(index)) {
			value = readProperty(value, target);
		}
		while (null != value && index < path.length()) {
			char ch = path.charAt(index++);
			if ('[' == ch) {
				value = readBracket(value, target);
			} else if ('.' == ch) {
				value = readProperty(value, target);
			} else {
				return null;
			}
		}
		if (index != path.length()) {
			return null;
		}
		return value;
	}

	/**
	 * Reads a property name and resolves it as a value or field.
	 *
	 * @param obj object containing the property
	 * @param target result type
	 * @return property value or field, or null
	 */
	private Object readProperty(final Object obj, final Target target) {
		int start = index;
		while (index < path.length() && !isPropertyDelimiter(path.charAt(index))) {
			++index;
		}
		if (start == index) {
			return null;
		}
		String name = path.substring(start, index);
		return switch (target) {
			case VALUE -> readValueProperty(obj, name);
			case FIELD -> readFieldProperty(obj, name);
		};
	}

	/**
	 * Reads a property value from a map or object.
	 *
	 * @param obj object containing the property
	 * @param name property name
	 * @return property value, or null
	 */
	private static Object readValueProperty(final Object obj, final String name) {
		if (obj instanceof Map<?, ?>) {
			return readMapKey(obj, name);
		}
		return Fields.Safe.get(obj, name);
	}

	/**
	 * Reads a field or an intermediate property. A terminal map key is not a field.
	 *
	 * @param obj object containing the property
	 * @param name property name
	 * @return field with owner, intermediate value, or null
	 */
	private Object readFieldProperty(final Object obj, final String name) {
		if (obj instanceof Map<?, ?>) {
			if (index == path.length()) {
				return null;
			}
			return readMapKey(obj, name);
		}
		Field field = Fields.Unchecked.getOneDeclaredInHierarchy(obj.getClass(), name);
		if (null == field || JavaModifier.STATIC.isPresentOn(field)) {
			return null;
		}
		if (index == path.length()) {
			return ExtendedField.of(field, obj);
		}
		return Fields.IgnoreAccess.get(obj, field);
	}

	/**
	 * Reads an array or list index, or a map key. The opening bracket has been consumed.
	 *
	 * @param obj container to read
	 * @param target result type
	 * @return selected value, or null
	 */
	private Object readBracket(final Object obj, final Target target) {
		if (index == path.length()) {
			return null;
		}
		Object value;
		if (isQuote(path.charAt(index))) {
			value = readQuotedKey(obj);
		} else {
			value = readUnquotedSelector(obj);
		}
		if (Target.FIELD == target && index == path.length()) {
			return null;
		}
		return value;
	}

	/**
	 * Reads a quoted map key and its closing bracket.
	 *
	 * @param obj map to read
	 * @return map value, or null
	 */
	private Object readQuotedKey(final Object obj) {
		char quote = path.charAt(index++);
		int start = index;

		while (index < path.length()) {
			char ch = path.charAt(index++);
			if (ch == quote) {
				String key = path.substring(start, index - 1);
				if (consume(']')) {
					return readMapKey(obj, key);
				}
				return null;
			}
			if ('\\' == ch) {
				return readQuotedEscapedKey(obj, quote, start);
			}
		}
		return null;
	}

	/**
	 * Finishes a quoted map key after the first escape character, allocating a builder only for this case.
	 *
	 * @param obj map to read
	 * @param quote quote character that closes the key
	 * @param start start of the key, after its opening quote
	 * @return map value, or null if the selector is invalid or the key is absent
	 */
	private Object readQuotedEscapedKey(final Object obj, final char quote, final int start) {
		if (index == path.length()) {
			return null;
		}
		StringBuilder key = new StringBuilder();
		key.append(path, start, index - 1);
		key.append(path.charAt(index++));
		while (index < path.length()) {
			char ch = path.charAt(index++);
			if (ch == quote) {
				if (consume(']')) {
					return readMapKey(obj, key.toString());
				}
				return null;
			}
			if ('\\' == ch && index < path.length()) {
				ch = path.charAt(index++);
			}
			key.append(ch);
		}
		return null;
	}

	/**
	 * Reads an unquoted selector and its closing bracket.
	 *
	 * @param obj container to read
	 * @return indexed or keyed value, or null
	 */
	private Object readUnquotedSelector(final Object obj) {
		int start = index;
		boolean numeric = true;
		while (index < path.length() && ']' != path.charAt(index)) {
			char ch = path.charAt(index++);
			if ('[' == ch || isQuote(ch)) {
				return null;
			}
			numeric &= Character.isDigit(ch);
		}
		if (!consume(']') || start == index - 1) {
			return null;
		}
		int end = index - 1;
		if (!numeric) {
			return readMapKey(obj, path.substring(start, end));
		}
		try {
			int i = Integer.parseInt(path, start, end, 10);
			return JavaObjects.getAt(obj, i);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Reads a key from a map.
	 *
	 * @param obj container to read
	 * @param key map key
	 * @return map value, or null if the container is not a map, the key is absent, or its type is not supported
	 */
	private static Object readMapKey(final Object obj, final String key) {
		if (obj instanceof Map<?, ?> map) {
			try {
				return map.get(key);
			} catch (ClassCastException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Checks whether a character ends a property name.
	 *
	 * @param ch character to check
	 * @return true for a dot or bracket
	 */
	private static boolean isPropertyDelimiter(final char ch) {
		return '.' == ch || '[' == ch || ']' == ch;
	}

	/**
	 * Checks whether a character starts or ends a quoted map key.
	 *
	 * @param ch character to check
	 * @return true for either quote character
	 */
	private static boolean isQuote(final char ch) {
		return '\'' == ch || '"' == ch;
	}

	/**
	 * Consumes a character at the current position if it matches.
	 *
	 * @param ch expected character
	 * @return true if the character was consumed
	 */
	private boolean consume(final char ch) {
		if (index < path.length() && ch == path.charAt(index)) {
			++index;
			return true;
		}
		return false;
	}
}
