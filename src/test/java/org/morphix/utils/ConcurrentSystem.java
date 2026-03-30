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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class for concurrent access to system properties. This class provides thread-safe methods for setting and
 * getting system properties by using a lock for each property key. This ensures that concurrent threads do not
 * interfere with each other when accessing the same property key, while still allowing concurrent access to different
 * keys.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConcurrentSystem {

	private static final Map<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

	public static String getAndSetProperty(final String key, final String value) {
		if (null == value) {
			return getAndClearProperty(key);
		}
		ReentrantLock keyLock = getLockForKey(key);
		keyLock.lock();
		try {
			return System.setProperty(key, value);
		} finally {
			keyLock.unlock();
		}
	}

	public static String getAndClearProperty(final String key) {
		ReentrantLock keyLock = getLockForKey(key);
		keyLock.lock();
		try {
			return System.clearProperty(key);
		} finally {
			keyLock.unlock();
		}
	}

	public static void withProperty(final String key, final String value, final AutoCloseable action) throws Exception {
		ReentrantLock keyLock = getLockForKey(key);
		keyLock.lock();
		try {
			String oldValue = System.setProperty(key, value);
			try {
				action.close();
			} finally {
				if (null == oldValue) {
					System.clearProperty(key);
				} else {
					System.setProperty(key, oldValue);
				}
			}
		} finally {
			keyLock.unlock();
		}
	}

	public static ReentrantLock getLockForKey(final String key) {
		return LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
	}
}
