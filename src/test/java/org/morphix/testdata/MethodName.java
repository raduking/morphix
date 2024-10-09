/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.testdata;

import java.util.Optional;
import java.util.function.Supplier;

import org.morphix.reflection.Methods;

/**
 * Class used in testing.
 *
 * @author Radu Sebastian LAZIN
 */
public class MethodName {

	public static final String SEPARATOR = "-";

	public static Optional<String> get(final Supplier<String> supplier) {
		return Methods.getCallerMethodName(supplier, (className, methodName) -> className + SEPARATOR + methodName);
	}

}
