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
package org.morphix.convert.function;

import static org.morphix.lang.function.SetterFunction.nonNullSetter;

import org.morphix.lang.function.GetterFunction;
import org.morphix.lang.function.SetterFunction;

/**
 * Mapper functions.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Mappers {

	/**
	 * Maps a setter to a getter.
	 *
	 * @param <T> value type
	 *
	 * @param setter setter function
	 * @param getter getter function
	 */
	static <T> void map(final SetterFunction<T> setter, final GetterFunction<T> getter) {
		setter.set(getter.get());
	}

	/**
	 * Maps a getter to a setter.
	 *
	 * @param <T> value type
	 *
	 * @param getter getter function
	 * @param setter setter function
	 */
	static <T> void map(final GetterFunction<T> getter, final SetterFunction<T> setter) {
		map(setter, getter);
	}

	/**
	 * Maps a setter to a getter, the setter will only be called if the value returned by the getter in non-null.
	 *
	 * @param <T> value type
	 *
	 * @param setter setter function
	 * @param getter getter function
	 */
	static <T> void mapNonNull(final SetterFunction<T> setter, final GetterFunction<T> getter) {
		map(nonNullSetter(setter), getter);
	}

	/**
	 * Maps a getter to a setter, the setter will only be called if the value returned by the getter in non-null.
	 *
	 * @param <T> value type
	 *
	 * @param getter getter function
	 * @param setter setter function
	 */
	static <T> void mapNonNull(final GetterFunction<T> getter, final SetterFunction<T> setter) {
		mapNonNull(setter, getter);
	}

}
