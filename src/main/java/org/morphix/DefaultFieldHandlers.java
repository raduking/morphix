package org.morphix;

import java.util.List;

import org.morphix.handler.AnyFromMap;
import org.morphix.handler.AnyToAny;
import org.morphix.handler.AnyToAnyFromConstructor;
import org.morphix.handler.AnyToAnyFromStaticMethod;
import org.morphix.handler.AnyToCharArray;
import org.morphix.handler.AnyToIterable;
import org.morphix.handler.AnyToString;
import org.morphix.handler.ArrayToArray;
import org.morphix.handler.ArrayToIterable;
import org.morphix.handler.CharSequenceToAnyFromStaticMethod;
import org.morphix.handler.CharSequenceToEnum;
import org.morphix.handler.DirectAssignment;
import org.morphix.handler.IterableToArray;
import org.morphix.handler.IterableToIterable;
import org.morphix.handler.MapToMap;
import org.morphix.handler.NullSourceSkipper;
import org.morphix.handler.NumberToNumber;
import org.morphix.handler.PrimitiveAssignment;
import org.morphix.handler.StaticFieldSkipper;

/**
 * Scope class for instantiating the default field handlers and the default
 * field handler order. These field handlers do not depend on the current
 * configuration.
 *
 * @author Radu Sebastian LAZIN
 */
public final class DefaultFieldHandlers {

	/**
	 * Field handlers.
	 */
	public static final FieldHandler FIELD_HANDLER_NULL_SOURCE_SKIPPER = new NullSourceSkipper();
	public static final FieldHandler FIELD_HANDLER_STATIC_FIELD_SKIPPER = new StaticFieldSkipper();
	public static final FieldHandler FIELD_HANDLER_DIRECT_ASSIGNMENT = new DirectAssignment();
	public static final FieldHandler FIELD_HANDLER_PRIMITIVE_ASSIGNMENT = new PrimitiveAssignment();
	public static final FieldHandler FIELD_HANDLER_NUMBER_TO_NUMBER = new NumberToNumber();
	public static final FieldHandler FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM = new CharSequenceToEnum();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_STRING = new AnyToString();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_CHAR_ARRAY = new AnyToCharArray();
	public static final FieldHandler FIELD_HANDLER_CHAR_SEQUENCE_TO_ANY = new CharSequenceToAnyFromStaticMethod();
	public static final FieldHandler FIELD_HANDLER_ITERABLE_TO_ITERABLE = new IterableToIterable();
	public static final FieldHandler FIELD_HANDLER_ARRAY_TO_ARRAY = new ArrayToArray();
	public static final FieldHandler FIELD_HANDLER_ITERABLE_TO_ARRAY = new IterableToArray();
	public static final FieldHandler FIELD_HANDLER_ARRAY_TO_ITERABLE = new ArrayToIterable();
	public static final FieldHandler FIELD_HANDLER_MAP_TO_MAP = new MapToMap();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY_FROM_STATIC_METHOD = new AnyToAnyFromStaticMethod();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY_FROM_CONSTRUCTOR = new AnyToAnyFromConstructor();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ITERABLE = new AnyToIterable();
	public static final FieldHandler FIELD_HANDLER_ANY_TO_ANY = new AnyToAny();
	public static final FieldHandler FIELD_HANDLER_ANY_FROM_MAP = new AnyFromMap();

	static final FieldHandler[] FIELD_HANDLERS_CHAIN = {
			FIELD_HANDLER_NULL_SOURCE_SKIPPER,
			FIELD_HANDLER_STATIC_FIELD_SKIPPER,
			FIELD_HANDLER_DIRECT_ASSIGNMENT,
			FIELD_HANDLER_PRIMITIVE_ASSIGNMENT,
			FIELD_HANDLER_NUMBER_TO_NUMBER,
			FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM,
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
			FIELD_HANDLER_ANY_TO_ITERABLE
	};

	static final List<FieldHandler> FIELD_HANDLERS_LIST = List.of(FIELD_HANDLERS_CHAIN);

	static List<FieldHandler> list() {
		return FIELD_HANDLERS_LIST;
	}

	/**
	 * Private constructor.
	 */
	private DefaultFieldHandlers() {
		// empty
	}
}
