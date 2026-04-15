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
package org.morphix.utils.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A custom Hamcrest matcher that checks if a string contains a specified substring at least a certain number of times.
 *
 * @author Radu Sebastian LAZIN
 */
public class ContainsAtLeastTimesMatcher extends TypeSafeMatcher<String> {

	private final String substring;
	private final int minOccurrences;
	private int actualOccurrences;

	public ContainsAtLeastTimesMatcher(final String substring, final int minOccurrences) {
		this.substring = substring;
		this.minOccurrences = minOccurrences;
	}

	@Override
	protected boolean matchesSafely(final String text) {
		if (null == text || null == substring || substring.isEmpty()) {
			return false;
		}

		actualOccurrences = 0;
		int index = 0;
		while ((index = text.indexOf(substring, index)) != -1) {
			actualOccurrences++;
			index += substring.length();
		}

		return actualOccurrences >= minOccurrences;
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("string containing '")
				.appendText(substring)
				.appendText("' at least ")
				.appendValue(minOccurrences)
				.appendText(" times");
	}

	@Override
	protected void describeMismatchSafely(final String text, final Description mismatchDescription) {
		mismatchDescription.appendText("was '")
				.appendText(text)
				.appendText("' with ")
				.appendValue(actualOccurrences)
				.appendText(" occurrences");
	}

	public static ContainsAtLeastTimesMatcher containsAtLeastTimes(final String substring, final int minOccurrences) {
		return new ContainsAtLeastTimesMatcher(substring, minOccurrences);
	}
}
