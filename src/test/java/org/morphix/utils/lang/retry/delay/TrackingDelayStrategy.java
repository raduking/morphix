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
package org.morphix.utils.lang.retry.delay;

import java.util.ArrayList;
import java.util.List;

import org.morphix.lang.retry.DelayStrategy;

/**
 * A {@link DelayStrategy} that tracks all attempt values passed to {@link #delay(int)}.
 *
 * @author Radu Sebastian LAZIN
 */
public class TrackingDelayStrategy implements DelayStrategy {

	/**
	 * Holds all attempts.
	 */
	private final List<Integer> attempts = new ArrayList<>();

	/**
	 * @see #delay(int)
	 */
	@Override
	public long delay(final int attempt) {
		attempts.add(attempt);
		return 0;
	}

	/**
	 * @see #copy()
	 */
	@Override
	public DelayStrategy copy() {
		return this;
	}

	/**
	 * Returns the list of attempt values that were passed to {@link #delay(int)}.
	 *
	 * @return the list of attempt values
	 */
	public List<Integer> getAttempts() {
		return attempts;
	}
}
