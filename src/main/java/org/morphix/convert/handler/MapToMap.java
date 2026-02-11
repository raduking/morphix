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

import static org.morphix.convert.Conversions.convertEnvelopedFrom;
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.MapConversions.convertMap;
import static org.morphix.convert.extras.ConverterCollections.isConvertibleMapType;
import static org.morphix.convert.extras.ConverterCollections.newMapInstance;
import static org.morphix.reflection.predicates.TypePredicates.isMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.Configuration;
import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Handles {@link Map} to {@link Map} conversions. This handler requires that the destination is a {@link Map} and it
 * has a getter method.
 * <p>
 * The getter method is used to find the element type within the map so that it can instantiate each element of the
 * destination list.
 * <p>
 * In java the return type of a method must keep it's actual generic arguments at runtime (see
 * {@link Method#getGenericReturnType()}).
 *
 * @author Radu Sebastian LAZIN
 */
public final class MapToMap extends FieldHandler {

	/**
	 * Default configuration.
	 */
	public MapToMap() {
		// empty
	}

	/**
	 * Constructor for custom configuration.
	 *
	 * @param configuration configuration object
	 */
	public MapToMap(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null == sValue) {
			return BREAK;
		}
		Type keyType = getKeyType(dfo);
		if (null == keyType) {
			return BREAK;
		}

		Type valueType = getValueType(dfo);

		Map<?, ?> dValue = convertMap((Map<?, ?>) sValue,
				srcKey -> convertEnvelopedFrom(srcKey, keyType, getConfiguration()),
				srcValue -> convertEnvelopedFrom(srcValue, valueType, getConfiguration()))
						.to(newMapInstance(dfo));
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return isMap();
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return isConvertibleMapType();
	}

	/**
	 * Returns the value class from the destination Map field.
	 * <p>
	 * Example: for <code>Map&lt;String, Integer&gt;</code> the method will return <code>Class&lt;Integer&gt;</code>
	 *
	 * @param fop field object pair
	 * @return the value type from the destination map field
	 */
	private static <T extends Type> T getValueType(final ExtendedField fop) {
		return fop.getGenericReturnType(1);
	}

	/**
	 * Returns the key class from the destination Map field.
	 * <p>
	 * Example: for <code>Map&lt;String, Integer&gt;</code> the method will return <code>Class&lt;String&gt;</code>
	 *
	 * @param fop field object pair
	 * @return the key type from the destination map field
	 */
	private static <T extends Type> T getKeyType(final ExtendedField fop) {
		return fop.getGenericReturnType(0);
	}
}
