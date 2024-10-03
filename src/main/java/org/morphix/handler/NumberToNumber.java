package org.morphix.handler;

import static java.lang.Boolean.TRUE;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.predicates.ClassPredicates.isA;
import static org.morphix.reflection.predicates.Predicates.cast;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.morphix.FieldHandler;
import org.morphix.FieldHandlerResult;
import org.morphix.reflection.ConverterField;
import org.morphix.reflection.Methods;

/**
 * Handles number conversions for number classes covering type coercion for
 * non-primitive number types.
 *
 * @author Radu Sebastian LAZIN
 */
public class NumberToNumber extends FieldHandler {

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

	private static final Map<Class<?>, String> NUMBER_SUPPLIER_MAP = new HashMap<>();
	static {
		NUMBER_SUPPLIER_MAP.put(Short.class, "shortValue");
		NUMBER_SUPPLIER_MAP.put(Integer.class, "intValue");
		NUMBER_SUPPLIER_MAP.put(Long.class, "longValue");
		NUMBER_SUPPLIER_MAP.put(Float.class, "floatValue");
		NUMBER_SUPPLIER_MAP.put(Double.class, "doubleValue");
	}

	@Override
	public FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo) {
		Object sValue = sfo.getFieldValue();
		if (null != sValue) {
			String methodName = NUMBER_SUPPLIER_MAP.get(dfo.toClass());
			Method method = Methods.getSafeDeclaredMethodInHierarchy(methodName, sfo.toClass());
			Object dValue = Methods.invokeIgnoreAccess(method, sValue);
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
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		Class<?> dClass = dfo.toClass();
		Class<?> sClass = sfo.toClass();
		return TYPE_COERTION_MAP.containsKey(pair(sClass, dClass));
	}

	private static <K, V> AbstractMap.SimpleEntry<K, V> pair(final K k, final V v) {
		return new AbstractMap.SimpleEntry<>(k, v);
	}

}
