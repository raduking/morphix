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
import static org.morphix.convert.FieldHandlerResult.SKIPPED;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerContext;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.MapConversions;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.InstanceCreator;

/**
 * Any from {@link Map} field handler.
 *
 * @author Radu Sebastian LAZIN
 */
public final class MapToAny extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public MapToAny() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField, FieldHandlerContext)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo, final FieldHandlerContext ctx) {
		if (!sfo.hasObject()) {
			return SKIPPED;
		}
		InstanceFunction<Object> instanceFunction =
				() -> Constructors.IgnoreAccess.newInstance(ctx.getDClass(dfo), InstanceCreator.getInstance());
		Map<String, Object> map = JavaObjects.cast(sfo.getObject());

		Object value = MapConversions.convertFromMap(map, instanceFunction, getConfiguration());
		dfo.setFieldValue(value);

		return CONVERTED;
	}

	/**
	 * @see FieldHandler#sourceTypeConstraint()
	 */
	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return PredicateHolder.SOURCE_TYPE_CONSTRAINT;
	}

	/**
	 * @see FieldHandler#destinationTypeConstraint()
	 */
	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return PredicateHolder.DESTINATION_TYPE_CONSTRAINT;
	}

	/**
	 * Holder for predicates to avoid unnecessary class loading of the predicates when the handler is not used.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class PredicateHolder {

		/**
		 * Source type constraint for map to map handler.
		 */
		private static final Predicate<Type> SOURCE_TYPE_CONSTRAINT = isMap();

		/**
		 * Destination type constraint for map to map handler.
		 */
		private static final Predicate<Type> DESTINATION_TYPE_CONSTRAINT = not(isMap());
	}
}
