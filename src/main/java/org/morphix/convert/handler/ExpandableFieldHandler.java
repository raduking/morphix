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
package org.morphix.convert.handler;

import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.extras.ConverterCollections.isConvertibleIterableType;
import static org.morphix.convert.extras.ConverterCollections.isConvertibleMapType;
import static org.morphix.reflection.predicates.TypePredicates.isArray;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.annotation.Expandable;
import org.morphix.convert.extras.ConverterCollections;
import org.morphix.reflection.ExtendedField;

/**
 * Handles fields annotated with {@link Expandable} in the source. It creates empty Iterables, Arrays and Maps in the
 * destination, for any other fields the destination will be <code>null</code>.
 *
 * @author Radu Sebastian LAZIN
 */
public final class ExpandableFieldHandler extends FieldHandler {

	/**
	 * Map describing the instantiators for each supported expandable field types.
	 */
	private static final Map<Predicate<Type>, Instantiator<?>> INSTANTIATORS_MAP = new HashMap<>();
	static {
		INSTANTIATORS_MAP.put(isConvertibleIterableType(), ConverterCollections::newCollectionInstance);
		INSTANTIATORS_MAP.put(isConvertibleMapType(), ConverterCollections::newMapInstance);
		INSTANTIATORS_MAP.put(isArray(), ConverterCollections::newEmptyArrayInstance);
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public ExpandableFieldHandler(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		// see if we have specific instantiator first
		boolean fieldSet = false;
		for (Map.Entry<Predicate<Type>, Instantiator<?>> entry : INSTANTIATORS_MAP.entrySet()) {
			if (entry.getKey().test(dfo.getType())) {
				dfo.setFieldValue(entry.getValue().instance(dfo));
				fieldSet = true;
				break;
			}
		}
		if (!fieldSet) {
			dfo.setFieldValue(null);
		}
		return CONVERTED;
	}

	@Override
	protected boolean convert(final ExtendedField sfo, final ExtendedField dfo) {
		if (condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}

	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		return getConfiguration().getExpandableFields().shouldNotExpandField(sfo);
	}

	/**
	 * Instance function based on a {@link ExtendedField} object.
	 *
	 * @param <T> instance type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface Instantiator<T> {

		/**
		 * Creates an instance from the given extended field.
		 *
		 * @param fop extended field
		 * @return instance of the object declared by the given field
		 */
		T instance(ExtendedField fop);
	}

}
