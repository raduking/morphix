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
package org.morphix.lang.leak;

/**
 * An enumeration representing the levels of leak detection for resources. The levels include:
 * <ul>
 * <li>{@code DISABLED}: Leak detection is turned off.</li>
 * <li>{@code SIMPLE}: Basic leak detection is enabled, which may report leaks but with limited information.</li>
 * <li>{@code ADVANCED}: Enhanced leak detection that provides more detailed information about leaks.</li>
 * <li>{@code PARANOID}: The most aggressive leak detection level, which may report even minor leaks and provide
 * extensive details.</li>
 * </ul>
 *
 * The current leak detection level can be determined by setting the system property
 * {@code morphix.leak.detection.level}.
 *
 * @author Radu Sebastian LAZIN
 */
public enum LeakDetectionLevel {

	/**
	 * Leak detection is disabled. No leak tracking or reporting will occur.
	 */
	DISABLED,

	/**
	 * Basic leak detection is enabled. This level may report leaks but with limited information.
	 */
	SIMPLE,

	/**
	 * Enhanced leak detection that provides more detailed information about leaks.
	 * <p>
	 * This level may have a performance impact due to the additional tracking and reporting.
	 */
	ADVANCED,

	/**
	 * The most aggressive leak detection level.
	 * <p>
	 * This level may report even minor leaks and provide extensive details. It is intended for debugging and development
	 * purposes and may have a significant performance impact.
	 * <p>
	 * TODO: implement resource access tracking for this level
	 */
	PARANOID;

	/**
	 * The system property key used to determine the current leak detection level, it should be set to any of the enum
	 * names. If the property is not set or is invalid, the default level will be {@link #SIMPLE}.
	 */
	public static final String PROPERTY = "morphix.leak.detection.level";

	/**
	 * Retrieves the current leak detection level based on the system property {@code morphix.leak.detection.level}
	 * ({@link #PROPERTY}. If the property is not set, is empty, or contains an invalid value, this method defaults to
	 * returning {@link #SIMPLE}.
	 *
	 * @return the current {@link LeakDetectionLevel} based on the system property
	 */
	public static LeakDetectionLevel current() {
		String level = System.getProperty(PROPERTY);
		if (null == level) {
			return SIMPLE;
		}
		level = level.trim();
		if (level.isEmpty()) {
			return SIMPLE;
		}
		try {
			return valueOf(level.toUpperCase());
		} catch (IllegalArgumentException e) {
			return SIMPLE;
		}
	}

	/**
	 * Sets the global leak detection level by updating the system property {@code morphix.leak.detection.level}
	 * ({@link #PROPERTY}) to the name of the specified level. This allows for dynamically changing the leak detection level
	 * at runtime, which can be useful for testing and debugging purposes.
	 * <p>
	 * Note that changing the global leak detection level will affect all subsequent leak tracking and reporting, but it
	 * will not retroactively change the behavior of already tracked resources. Resources that were tracked before the level
	 * change will continue to be tracked and reported according to the level that was in effect at the time they were
	 * tracked. Therefore, it is recommended to set the desired leak detection level before tracking any resources to ensure
	 * consistent behavior.
	 *
	 * @param level the {@link LeakDetectionLevel} to set as the global leak detection level
	 */
	public static void modifyGlobal(final LeakDetectionLevel level) {
		System.setProperty(PROPERTY, level.name());
	}
}
