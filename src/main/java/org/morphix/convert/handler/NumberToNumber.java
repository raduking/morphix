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

import static java.lang.Boolean.TRUE;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.lang.function.Predicates.cast;
import static org.morphix.reflection.predicates.ClassPredicates.isA;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;
import org.morphix.reflection.Methods;

/**
 * Handles number conversions for number classes covering type coercion for non-primitive number types.
 *
 * @author Radu Sebastian LAZIN
 */
public class NumberToNumber extends FieldHandler {

	/**
	 * Type coertion map for all primitive boxed classes.
	 */
	private static final Map<AbstractMap.SimpleEntry<Class<?>, Class<?>>, Boolean> TYPE_COERTION_MAP = new HashMap<>();
	static {
		// Byte -> Short -> Integer -> Float -> Double
		TYPE_COERTION_MAP.put(pair(Byte.class, Short.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Byte.class, Integer.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Byte.class, Long.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Byte.class, Float.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Byte.class, Double.class), TRUE);
		// Short -> Integer -> Float -> Double
		TYPE_COERTION_MAP.put(pair(Short.class, Integer.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Short.class, Long.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Short.class, Float.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Short.class, Double.class), TRUE);
		// Character -> Short -> Integer -> Float -> Double
		TYPE_COERTION_MAP.put(pair(Character.class, Integer.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Character.class, Long.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Character.class, Float.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Character.class, Double.class), TRUE);
		// Integer -> Float -> Double
		TYPE_COERTION_MAP.put(pair(Integer.class, Long.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Integer.class, Float.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Integer.class, Double.class), TRUE);
		// Long -> Float -> Double
		TYPE_COERTION_MAP.put(pair(Long.class, Float.class), TRUE);
		TYPE_COERTION_MAP.put(pair(Long.class, Double.class), TRUE);
		// Float -> Double
		TYPE_COERTION_MAP.put(pair(Float.class, Double.class), TRUE);
	}

	/**
	 * Boxed primitive to primitive value supplier method name.
	 */
	private static final Map<Class<?>, String> NUMBER_SUPPLIER_MAP = new HashMap<>();
	static {
		NUMBER_SUPPLIER_MAP.put(Short.class, "shortValue");
		NUMBER_SUPPLIER_MAP.put(Integer.class, "intValue");
		NUMBER_SUPPLIER_MAP.put(Long.class, "longValue");
		NUMBER_SUPPLIER_MAP.put(Float.class, "floatValue");
		NUMBER_SUPPLIER_MAP.put(Double.class, "doubleValue");
	}

	/**
	 * Default constructor.
	 */
	public NumberToNumber() {
		// empty
	}

	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			String methodName = NUMBER_SUPPLIER_MAP.get(dfo.toClass());
			Method method = Methods.Safe.getOneDeclaredInHierarchy(methodName, sfo.toClass());
			Object dValue = Methods.IgnoreAccess.invoke(method, sValue);
			dfo.setFieldValue(dValue);
		}
		return CONVERTED;
	}

	@Override
	protected Predicate<Type> sourceTypeConstraint() {
		return cast(isA(Number.class));
	}

	@Override
	protected Predicate<Type> destinationTypeConstraint() {
		return cast(isA(Number.class));
	}

	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		return TYPE_COERTION_MAP.containsKey(pair(sClass, dClass));
	}

	/**
	 * Creates a {@link SimpleEntry} given the key and value.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @param k key
	 * @param v value
	 * @return a simple entry with the give key and value
	 */
	private static <K, V> AbstractMap.SimpleEntry<K, V> pair(final K k, final V v) {
		return new AbstractMap.SimpleEntry<>(k, v);
	}
}
