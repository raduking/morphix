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
package org.morphix.convert.handler;

import static org.morphix.convert.Conversions.convertEnvelopedFrom;
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.TypePredicates.isArray;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Array to Array conversions.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ArrayToArray extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public ArrayToArray() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public ArrayToArray(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object[] sValue = (Object[]) sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}

		Class<?> elementClass = dfo.toClass().getComponentType();
		Object[] dValue = (Object[]) Array.newInstance(elementClass, sValue.length);

		for (int i = 0; i < sValue.length; ++i) {
			dValue[i] = convertEnvelopedFrom(sValue[i], elementClass, getConfiguration());
		}
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isArray();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isArray();
	}

}
