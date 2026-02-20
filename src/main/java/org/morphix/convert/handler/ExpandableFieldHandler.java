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
import org.morphix.convert.FieldHandlerContext;
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
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public ExpandableFieldHandler(final Configuration configuration) {
		super(configuration);
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		// see if we have specific instantiator first
		boolean fieldSet = false;
		for (Map.Entry<Predicate<Type>, Instantiator<?>> entry : InformationHolder.INSTANTIATORS_MAP.entrySet()) {
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

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	protected boolean convert(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		if (condition(sfo, dfo, ctx)) {
			FieldHandlerResult result = handle(sfo, dfo, ctx);
			return result.isHandled();
		}
		return false;
	}

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext context) {
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

	/**
	 * Holder for static data to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InformationHolder {

		/**
		 * Map describing the instantiators for each supported expandable field types.
		 */
		private static final Map<Predicate<Type>, Instantiator<?>> INSTANTIATORS_MAP = new HashMap<>();
		static {
			INSTANTIATORS_MAP.put(isConvertibleIterableType(), ConverterCollections::newCollectionInstance);
			INSTANTIATORS_MAP.put(isConvertibleMapType(), ConverterCollections::newMapInstance);
			INSTANTIATORS_MAP.put(isArray(), ConverterCollections::newEmptyArrayInstance);
		}
	}
}
