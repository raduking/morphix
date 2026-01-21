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
package org.morphix.convert;

import static org.morphix.reflection.predicates.MethodPredicates.isConverterMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.lang.function.Predicates;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.predicates.MethodPredicates;

/**
 * Base class for field conversion handling.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class FieldHandler {

	/**
	 * Configuration.
	 */
	private final Configuration configuration;

	/**
	 * Constructor with configuration.
	 *
	 * @param configuration configuration
	 */
	protected FieldHandler(final Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Default constructor.
	 */
	protected FieldHandler() {
		// initialize it with null to lazy load default configuration
		this(null);
	}

	/**
	 * Handles the field.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 * @return handler result which can be checked if the handling succeeded or not
	 */
	public abstract FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo);

	/**
	 * Returns true if the handler should try to handle the fields, false otherwise.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 * @return true if the field should be handled, false otherwise
	 */
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		return true;
	}

	/**
	 * Returns the source type constraint.
	 *
	 * @return the source type constraint
	 */
	protected Predicate<Type> sourceTypeConstraint() {
		return Predicates.acceptAll();
	}

	/**
	 * Returns the destination type constraint.
	 *
	 * @return the destination type constraint
	 */
	protected Predicate<Type> destinationTypeConstraint() {
		return Predicates.acceptAll();
	}

	/**
	 * Returns the configuration.
	 *
	 * @return the configuration
	 */
	protected Configuration getConfiguration() {
		return null == configuration ? Configuration.defaultConfiguration() : configuration;
	}

	/**
	 * Returns a list of methods that verify with {@link MethodPredicates#isConverterMethod(Class)}.
	 *
	 * @param cls class in which to search the methods
	 * @param srcClass source class to convert to destination class
	 * @return list of methods
	 */
	protected static List<Method> getConverterMethods(final Class<?> cls, final Class<?> srcClass) {
		return Arrays.stream(cls.getDeclaredMethods())
				.filter(isConverterMethod(srcClass))
				.toList();
	}

	/**
	 * Handle the data exchange between source and destination.
	 *
	 * @param sfo source field object
	 * @param dfo destination field object
	 * @return true if conversion was successful false otherwise
	 */
	protected boolean convert(final ExtendedField sfo, final ExtendedField dfo) {
		boolean typeConstraintsMet = sfo.typeMeets(sourceTypeConstraint()) && dfo.typeMeets(destinationTypeConstraint());
		if (typeConstraintsMet && condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		// we ignore the configuration
		return Objects.equals(obj.getClass(), getClass());
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
