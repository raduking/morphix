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

import java.util.List;

import org.morphix.convert.handler.AnyToAny;
import org.morphix.convert.handler.AnyToAnyFromConstructor;
import org.morphix.convert.handler.AnyToAnyFromStaticMethod;
import org.morphix.convert.handler.AnyToCharArray;
import org.morphix.convert.handler.AnyToIterable;
import org.morphix.convert.handler.AnyToOptional;
import org.morphix.convert.handler.AnyToString;
import org.morphix.convert.handler.ArrayToArray;
import org.morphix.convert.handler.ArrayToIterable;
import org.morphix.convert.handler.CharSequenceToAnyFromStaticMethod;
import org.morphix.convert.handler.CharSequenceToEnum;
import org.morphix.convert.handler.DirectAssignment;
import org.morphix.convert.handler.IterableToArray;
import org.morphix.convert.handler.IterableToIterable;
import org.morphix.convert.handler.MapToAny;
import org.morphix.convert.handler.MapToMap;
import org.morphix.convert.handler.NullSourceSkipper;
import org.morphix.convert.handler.NumberToNumber;
import org.morphix.convert.handler.OptionalToAny;
import org.morphix.convert.handler.PrimitiveAssignment;
import org.morphix.convert.handler.StaticFieldSkipper;
import org.morphix.reflection.Constructors;

/**
 * Scope class for instantiating the default field handlers and the default field handler order. These field handlers do
 * not depend on the current configuration.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DefaultFieldHandlers {

	/**
	 * {@link NullSourceSkipper} default field handler instance.
	 */
	public static final FieldHandler FIELD_HANDLER_NULL_SOURCE_SKIPPER = new NullSourceSkipper();

	/**
	 * {@link StaticFieldSkipper} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_STATIC_FIELD_SKIPPER = new StaticFieldSkipper();

	/**
	 * {@link DirectAssignment} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_DIRECT_ASSIGNMENT = new DirectAssignment();

	/**
	 * {@link PrimitiveAssignment} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_PRIMITIVE_ASSIGNMENT = new PrimitiveAssignment();

	/**
	 * {@link NumberToNumber} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_NUMBER_TO_NUMBER = new NumberToNumber();

	/**
	 * {@link CharSequenceToEnum} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM = new CharSequenceToEnum();

	/**
	 * {@link OptionalToAny} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_OPTIONAL_TO_ANY = new OptionalToAny();

	/**
	 * {@link OptionalToAny} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_OPTIONAL = new AnyToOptional();

	/**
	 * {@link AnyToString} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_STRING = new AnyToString();

	/**
	 * {@link AnyToCharArray} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_CHAR_ARRAY = new AnyToCharArray();

	/**
	 * {@link CharSequenceToAnyFromStaticMethod} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_CHAR_SEQUENCE_TO_ANY = new CharSequenceToAnyFromStaticMethod();

	/**
	 * {@link IterableToIterable} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ITERABLE_TO_ITERABLE = new IterableToIterable();

	/**
	 * {@link ArrayToArray} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ARRAY_TO_ARRAY = new ArrayToArray();

	/**
	 * {@link IterableToArray} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ITERABLE_TO_ARRAY = new IterableToArray();

	/**
	 * {@link ArrayToIterable} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ARRAY_TO_ITERABLE = new ArrayToIterable();

	/**
	 * {@link MapToMap} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_MAP_TO_MAP = new MapToMap();

	/**
	 * {@link AnyToAnyFromStaticMethod} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY_FROM_STATIC_METHOD = new AnyToAnyFromStaticMethod();

	/**
	 * {@link AnyToAnyFromConstructor} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY_FROM_CONSTRUCTOR = new AnyToAnyFromConstructor();

	/**
	 * {@link AnyToIterable} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ITERABLE = new AnyToIterable();

	/**
	 * {@link MapToAny} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_MAP_TO_ANY = new MapToAny();

	/**
	 * {@link AnyToAny} default instance.
	 */
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY = new AnyToAny();

	/**
	 * Default field handlers chain (as array).
	 */
	static final FieldHandler[] FIELD_HANDLERS_CHAIN = {
			FIELD_HANDLER_ANY_TO_OPTIONAL,
			FIELD_HANDLER_NULL_SOURCE_SKIPPER,
			FIELD_HANDLER_STATIC_FIELD_SKIPPER,
			FIELD_HANDLER_DIRECT_ASSIGNMENT,
			FIELD_HANDLER_PRIMITIVE_ASSIGNMENT,
			FIELD_HANDLER_NUMBER_TO_NUMBER,
			FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM,
			FIELD_HANDLER_OPTIONAL_TO_ANY,
			FIELD_HANDLER_ANY_TO_STRING,
			FIELD_HANDLER_ANY_TO_CHAR_ARRAY,
			FIELD_HANDLER_CHAR_SEQUENCE_TO_ANY,
			FIELD_HANDLER_ITERABLE_TO_ITERABLE,
			FIELD_HANDLER_ARRAY_TO_ARRAY,
			FIELD_HANDLER_ITERABLE_TO_ARRAY,
			FIELD_HANDLER_ARRAY_TO_ITERABLE,
			FIELD_HANDLER_MAP_TO_MAP,
			FIELD_HANDLER_ANY_TO_ANY_FROM_STATIC_METHOD,
			FIELD_HANDLER_ANY_TO_ANY_FROM_CONSTRUCTOR,
			FIELD_HANDLER_ANY_TO_ITERABLE,
			FIELD_HANDLER_MAP_TO_ANY
	};

	/**
	 * Default field handlers chain (as list).
	 */
	static final List<FieldHandler> FIELD_HANDLERS_LIST = List.of(FIELD_HANDLERS_CHAIN);

	/**
	 * Returns the default field handlers list.
	 *
	 * @return the default field handlers list
	 */
	static List<FieldHandler> list() {
		return FIELD_HANDLERS_LIST;
	}

	/**
	 * Private constructor.
	 */
	private DefaultFieldHandlers() {
		throw Constructors.unsupportedOperationException();
	}
}
