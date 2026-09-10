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

import java.util.List;
import java.util.Objects;

/**
 * Describes a set of typed arguments for a reflective invocation.
 *
 * @param arguments the typed arguments
 *
 * @author Radu Sebastian LAZIN
 */
public record TypedArguments(List<TypedArgument> arguments) {

	/**
	 * Compact constructor that ensures the arguments list is not null and creates an unmodifiable copy of it.
	 *
	 * @param arguments the typed arguments
	 */
	public TypedArguments {
		Objects.requireNonNull(arguments, "arguments must not be null");
		arguments = List.copyOf(arguments);
	}

	/**
	 * Creates typed arguments from the given arguments.
	 *
	 * @param arguments the arguments
	 * @return the typed arguments
	 */
	public static TypedArguments of(final TypedArgument... arguments) {
		Objects.requireNonNull(arguments, "arguments must not be null");
		return new TypedArguments(List.of(arguments));
	}

	/**
	 * Creates typed arguments containing a single argument.
	 *
	 * @param type the argument type
	 * @param value the argument value
	 * @return the typed arguments
	 */
	public static TypedArguments of(final Class<?> type, final Object value) {
		return of(TypedArgument.of(type, value));
	}

	/**
	 * Returns the argument types.
	 *
	 * @return the argument types
	 */
	public Class<?>[] types() {
		Class<?>[] types = new Class<?>[arguments.size()];
		for (int i = 0; i < arguments.size(); ++i) {
			types[i] = arguments.get(i).type();
		}
		return types;
	}

	/**
	 * Returns the argument values.
	 *
	 * @return the argument values
	 */
	public Object[] values() {
		Object[] values = new Object[arguments.size()];
		for (int i = 0; i < arguments.size(); ++i) {
			values[i] = arguments.get(i).value();
		}
		return values;
	}

	/**
	 * Returns whether there are no arguments.
	 *
	 * @return true if there are no arguments
	 */
	public boolean isEmpty() {
		return arguments.isEmpty();
	}

	/**
	 * Returns the number of arguments.
	 *
	 * @return the number of arguments
	 */
	public int size() {
		return arguments.size();
	}
}
