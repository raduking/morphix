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
package org.morphix.utils;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Properties for concurrency tests. This record encapsulates the configuration parameters for running concurrency
 * tests, such as the number of threads, iterations per thread and such used for testing the thread-safety and
 * performance of concurrent data structures or algorithms.
 *
 * @author Radu Sebastian LAZIN
 */
public record ConcurrencyTestProperties(
		int threadCount,
		int iterationsPerThread,
		int keySpace,
		Duration timeout,
		Logger logger) {

	public static class Default {

		public static final int THREAD_COUNT = 100;
		public static final int ITERATIONS_PER_THREAD = 1000;
		public static final int KEY_SPACE = 7;
		public static final Duration TIMEOUT = Duration.ofSeconds(1);

		private Default() {
			// hide constructor
		}
	}

	public ConcurrencyTestProperties {
		if (null == logger) {
			logger = Logger.getLogger("concurrency-test");
		}
	}

	public static class Builder {

		private int threadCount = Default.THREAD_COUNT;
		private int iterationsPerThread = Default.ITERATIONS_PER_THREAD;
		private int keySpace = Default.KEY_SPACE;
		private Duration timeout = Default.TIMEOUT;
		private Logger logger;

		public Builder threadCount(final int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public Builder iterationsPerThread(final int iterationsPerThread) {
			this.iterationsPerThread = iterationsPerThread;
			return this;
		}

		public Builder keySpace(final int keySpace) {
			this.keySpace = keySpace;
			return this;
		}

		public Builder timeout(final Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder logger(final Logger logger) {
			this.logger = logger;
			return this;
		}

		public ConcurrencyTestProperties build() {
			return new ConcurrencyTestProperties(threadCount, iterationsPerThread, keySpace, timeout, logger);
		}
	}
}
