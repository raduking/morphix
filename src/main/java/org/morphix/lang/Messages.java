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
package org.morphix.lang;

import java.util.Objects;

/**
 * Lightweight message interpolation for diagnostics and exceptions.
 * <p>
 * Not a general-purpose formatting API.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Messages {

	/**
	 * Replaces each {@code {}} placeholder in the template with the corresponding argument using
	 * {@link Objects#toString(Object)}.
	 * <p>
	 * This method performs <strong>no formatting</strong>. Callers are responsible for formatting arguments before passing
	 * them in.
	 * <p>
	 * Extra arguments are ignored. Unmatched placeholders are left unchanged.
	 * <p>
	 * This method is 2x faster than {@link String#format(String, Object...)}.
	 *
	 * @param template the message template
	 * @param args replacement values
	 * @return the interpolated string
	 */
	static String message(final String template, final Object... args) {
		if (null == template || null == args || args.length == 0) {
			return template;
		}
		int argumentLengthHeuristic = 16;
		final StringBuilder sb = new StringBuilder(template.length() + args.length * argumentLengthHeuristic);

		int argumentIndex = 0;
		int index = 0;
		int templateLength = template.length();

		while (index < templateLength) {
			char currentChar = template.charAt(index);
			if ('{' == currentChar
					&& index + 1 < templateLength
					&& '}' == template.charAt(index + 1)
					&& argumentIndex < args.length) {
				sb.append(Objects.toString(args[argumentIndex]));
				++argumentIndex;
				index += 2;
			} else {
				sb.append(currentChar);
				++index;
			}
		}
		return sb.toString();
	}
}
