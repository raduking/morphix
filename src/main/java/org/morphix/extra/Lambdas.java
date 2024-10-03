package org.morphix.extra;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.reflection.ConstantPool;
import org.morphix.reflection.Methods;
import org.morphix.reflection.Reflection;
import org.morphix.reflection.predicates.MethodPredicates;

/**
 * Methods for handling lambdas.
 *
 * @author Radu Sebastian LAZIN
 */
public class Lambdas {

	/**
	 * Name of the method that returns the serialized lambda.
	 */
	private static final String SERIALIZATION_METHOD_NAME = "writeReplace";

	/**
	 * Private constructor.
	 */
	private Lambdas() {
		throw new UnsupportedOperationException("This class shouldn't be instantiated.");
	}

	/**
	 * Returns true if the lambda has the given parameter paramType and return
	 * type, false otherwise.
	 *
	 * @param lambda lambda function object
	 * @param returnType return type
	 * @param paramTypes parameter types
	 * @return true if the lambda has the given parameter paramTypes and return
	 *         type
	 */
	public static <T> boolean isLambdaWithParams(final T lambda,
			final Class<?> returnType, final Class<?>... paramTypes) {
		Predicate<Method> lambdaPredicate = MethodPredicates.isMethodWith(returnType, paramTypes);

		Method lambdaMethod = getLambdaMethod(lambda, lambdaPredicate);
		return null != lambdaMethod;
	}

	/**
	 * Returns the lambda method found in the constant pool. If the constant
	 * pool doesn't have the lambda then the returned value is null.
	 *
	 * @param constantPool the constant pool
	 * @param predicate lambda method predicate
	 *
	 * @return the parameter types of the lambda expression
	 */
	public static <T> Method getLambdaMethod(final ConstantPool<T> constantPool, final Predicate<Method> predicate) {
		for (Member member : constantPool) {
			if (null != member
					&& !member.getDeclaringClass().isAssignableFrom(constantPool.getTargetClass())
					&& member instanceof Method method
					&& (predicate.test(method))) {
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link SerializedLambda} for a lambda function.
	 *
	 * @param function function
	 * @return serialized lambda
	 */
	public static <T> SerializedLambda getSerializedLambda(final T function) {
		if (!(function instanceof Serializable)) {
			return null;
		}
		return getSerializedLambda(function, function.getClass());
	}

	/**
	 * Returns the serialized lambda from the given lambda and class.
	 *
	 * @param function lambda function
	 * @param cls class
	 * @return serialized lambda object
	 */
	static <T> SerializedLambda getSerializedLambda(final T function, final Class<?> cls) {
		if (null != cls) {
			Method replaceMethod;
			try {
				replaceMethod = cls.getDeclaredMethod(SERIALIZATION_METHOD_NAME);
				Object serializedForm = Methods.invokeIgnoreAccess(replaceMethod, function);
				if (serializedForm instanceof SerializedLambda serializedLambda) {
					return serializedLambda;
				}
			} catch (Exception e) {
				return getSerializedLambda(function, cls.getSuperclass());
			}
		}
		return null;
	}

	/**
	 * Returns the lambda method, or null if the given parameter is not a
	 * lambda.
	 *
	 * @param function function object
	 * @return the lambda method
	 */
	private static <T> Method getLambdaMethod(final T function, final Predicate<Method> predicate) {
		SerializedLambda serializedLambda = getSerializedLambda(function);
		if (null == serializedLambda) {
			return null;
		}
		return getLambdaMethod(serializedLambda, predicate);
	}

	/**
	 * Returns the lambda method, or null if the given parameter is not a
	 * {@link SerializedLambda}.
	 *
	 * @param serializedLambda serialized lambda
	 * @return the lambda method
	 */
	public static Method getLambdaMethod(final SerializedLambda serializedLambda, final Predicate<Method> predicate) {
		String lambdaMethodName = serializedLambda.getImplMethodName();

		for (Method method : getLambdaDeclaredMethods(serializedLambda)) {
			if (Objects.equals(method.getName(), lambdaMethodName)
					&& predicate.test(method)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns the lambda class methods from a {@link SerializedLambda}.
	 *
	 * @param serializedLambda serialized lambda
	 * @return lambda class
	 */
	public static List<Method> getLambdaDeclaredMethods(final SerializedLambda serializedLambda) {
		String implClassName = serializedLambda.getImplClass().replace('/', '.');
		Class<?> implClass = Reflection.getClass(implClassName);
		if (null == implClass) {
			return Collections.emptyList();
		}
		return List.of(implClass.getDeclaredMethods());
	}

}
