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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.collections.Lists;

/**
 * Parses and resolves JSON Path-style selectors for field and container access (properties, array or list indices, and
 * map keys).
 *
 * @author Radu Sebastian LAZIN
 */
public interface FieldPaths {

	/**
	 * Kind of path step.
	 */
	enum StepKind {

		/**
		 * A property or map key accessed with dot notation.
		 */
		PROPERTY,

		/**
		 * A zero-based index into an array or list.
		 */
		INDEX,

		/**
		 * A map key from bracket notation.
		 */
		KEY
	}

	/**
	 * One step in a path expression.
	 *
	 * @param kind step kind
	 * @param selector property name, index text, or map key
	 */
	record Step(StepKind kind, String selector) {
		// empty
	}

	/**
	 * Resolves the value at the given path starting from {@code root}, using the same rules as
	 * {@link Fields.Safe#getByPath(Object, String)}.
	 *
	 * @param <U> value type
	 *
	 * @param root start object
	 * @param path path expression
	 * @return value at the path, or {@code null} if the path is empty, invalid, or not found
	 */
	static <U> U resolveValue(final Object root, final String path) {
		List<Step> steps = parse(path);
		if (Lists.isEmpty(steps)) {
			return null;
		}
		Object current = root;
		for (Step step : steps) {
			current = navigate(current, step);
			if (null == current) {
				break;
			}
		}
		return JavaObjects.cast(current);
	}

	/**
	 * Parses a path into steps. Returns an empty list for an empty path, or {@code null} when the expression is invalid.
	 *
	 * @param path path expression
	 * @return parsed steps, empty list for {@code ""}, or {@code null} if invalid
	 */
	static List<Step> parse(final String path) {
		if (path.isEmpty()) {
			return List.of();
		}
		List<Step> steps = new ArrayList<>();
		int index = 0;
		int length = path.length();
		while (index < length) {
			if (path.charAt(index) == '.') {
				return null;
			}
			int propertyStart = index;
			while (index < length) {
				char ch = path.charAt(index);
				if (ch == '.' || ch == '[') {
					break;
				}
				index++;
			}
			if (propertyStart < index) {
				steps.add(new Step(StepKind.PROPERTY, path.substring(propertyStart, index)));
			}
			index = parseBracketSteps(path, index, length, steps);
			if (index < 0) {
				return null;
			}
			if (index < length) {
				if (path.charAt(index) != '.') {
					return null;
				}
				index++;
				if (index >= length) {
					steps.add(new Step(StepKind.PROPERTY, ""));
					return steps;
				}
			}
		}
		return steps;
	}

	/**
	 * Splits a comma-separated list of paths, ignoring commas inside {@code [...]} segments.
	 *
	 * @param paths comma-separated path expressions
	 * @return trimmed non-empty paths, or an empty list when nothing remains after trimming
	 */
	static List<String> split(final String paths) {
		if (paths.isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int bracketDepth = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for (int i = 0; i < paths.length(); ++i) {
			char ch = paths.charAt(i);
			if (inSingleQuote) {
				current.append(ch);
				if (ch == '\'' && (i == 0 || paths.charAt(i - 1) != '\\')) {
					inSingleQuote = false;
				}
				continue;
			}
			if (inDoubleQuote) {
				current.append(ch);
				if (ch == '"' && (i == 0 || paths.charAt(i - 1) != '\\')) {
					inDoubleQuote = false;
				}
				continue;
			}
			if (ch == '\'') {
				inSingleQuote = true;
				current.append(ch);
				continue;
			}
			if (ch == '"') {
				inDoubleQuote = true;
				current.append(ch);
				continue;
			}
			if (ch == '[') {
				bracketDepth++;
				current.append(ch);
				continue;
			}
			if (ch == ']') {
				bracketDepth--;
				current.append(ch);
				continue;
			}
			if (ch == ',' && bracketDepth == 0) {
				String path = current.toString().trim();
				if (!path.isEmpty()) {
					result.add(path);
				}
				current.setLength(0);
				continue;
			}
			current.append(ch);
		}
		String path = current.toString().trim();
		if (!path.isEmpty()) {
			result.add(path);
		}
		return result;
	}

	/**
	 * Applies a single step to the current value.
	 *
	 * @param current current value
	 * @param step step to apply
	 * @return next value, or {@code null} if navigation fails
	 */
	static Object navigate(final Object current, final Step step) {
		return switch (step.kind()) {
			case PROPERTY -> readProperty(current, step.selector());
			case INDEX -> readIndex(current, step.selector());
			case KEY -> readMapKey(current, step.selector());
		};
	}

	/**
	 * @see StepKind#PROPERTY
	 */
	static Object readProperty(final Object current, final String propertyName) {
		if (null == current || null == propertyName || propertyName.isEmpty()) {
			return null;
		}
		Class<?> cls = Classes.getFrom(current);
		Field field = Fields.Unchecked.getOneDeclaredInHierarchy(cls, propertyName);
		if (null != field) {
			return Fields.Safe.get(current, field);
		}
		if (current instanceof Map<?, ?> map) {
			return map.get(propertyName);
		}
		return null;
	}

	/**
	 * @see StepKind#INDEX
	 */
	static Object readIndex(final Object current, final String indexText) {
		if (null == current || null == indexText) {
			return null;
		}
		int index;
		try {
			index = Integer.parseInt(indexText);
		} catch (NumberFormatException e) {
			return null;
		}
		if (index < 0) {
			return null;
		}
		if (current instanceof List<?> list) {
			return index < list.size() ? list.get(index) : null;
		}
		if (current.getClass().isArray()) {
			int length = Array.getLength(current);
			return index < length ? Array.get(current, index) : null;
		}
		return null;
	}

	/**
	 * @see StepKind#KEY
	 */
	static Object readMapKey(final Object current, final String key) {
		if (null == current || null == key) {
			return null;
		}
		if (current instanceof Map<?, ?> map) {
			return map.get(key);
		}
		return null;
	}

	private static int parseBracketSteps(final String path, final int index, final int length, final List<Step> steps) {
		int currentIndex = index;
		while (currentIndex < length && path.charAt(currentIndex) == '[') {
			++currentIndex;
			if (currentIndex >= length) {
				return -1;
			}
			char ch = path.charAt(currentIndex);
			if (ch == '\'' || ch == '"') {
				int[] nextIndex = new int[1];
				String key = readQuoted(path, currentIndex, length, ch, nextIndex);
				if (null == key) {
					return -1;
				}
				steps.add(new Step(StepKind.KEY, key));
				currentIndex = nextIndex[0];
			} else {
				int selectorStart = index;
				while (currentIndex < length && path.charAt(currentIndex) != ']') {
					++currentIndex;
				}
				if (currentIndex >= length) {
					return -1;
				}
				String selector = path.substring(selectorStart, currentIndex);
				if (selector.isEmpty()) {
					return -1;
				}
				if (isInteger(selector)) {
					steps.add(new Step(StepKind.INDEX, selector));
				} else {
					steps.add(new Step(StepKind.KEY, selector));
				}
			}
			if (currentIndex >= length || path.charAt(currentIndex) != ']') {
				return -1;
			}
			++currentIndex;
		}
		return currentIndex;
	}

	private static boolean isInteger(final String text) {
		if (text.isEmpty()) {
			return false;
		}
		for (int i = 0; i < text.length(); ++i) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static String readQuoted(final String path, final int start, final int length, final char quote, final int[] nextIndexOut) {
		StringBuilder builder = new StringBuilder();
		int index = start + 1;
		while (index < length) {
			char ch = path.charAt(index);
			if (ch == '\\') {
				++index;
				if (index >= length) {
					return null;
				}
				builder.append(path.charAt(index));
				index++;
				continue;
			}
			if (ch == quote) {
				++index;
				nextIndexOut[0] = index;
				return builder.toString();
			}
			builder.append(ch);
			++index;
		}
		return null;
	}
}
