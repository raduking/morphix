package org.morphix.reflection.predicates;

import static org.morphix.handler.PrimitiveAssignment.isPrimitiveToClass;
import static org.morphix.reflection.predicates.MemberPredicates.hasName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import org.morphix.reflection.MethodType;
import org.morphix.reflection.MethodType.Prefix;

/**
 * Method predicates utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public final class MethodPredicates {

	/**
	 * Private constructor.
	 */
	private MethodPredicates() {
		throw new UnsupportedOperationException("This class shouldn't be instantiated.");
	}

	/**
	 * Returns a predicate that verifies if the method:
	 * <ul>
	 * <li>is <code>public</code></li>
	 * <li>is <code>static</code></li>
	 * <li>has only one parameter of type srcClass</li>
	 * <li>has the return type the class the method is declared in</li>
	 * </ul>
	 *
	 * @param parameterClass the parameter type of the converter method
	 * @return a predicate
	 */
	public static <T> Predicate<Method> isConverterMethod(final Class<T> parameterClass) {
		return method -> {
			int modifiers = method.getModifiers();
			// method must be public and static
			if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
				return false;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				return false;
			}
			Class<?> methodDeclaringClass = method.getDeclaringClass();
			Class<?> returnType = method.getReturnType();
			if (!methodDeclaringClass.isAssignableFrom(returnType)) {
				return false;
			}
			Class<?> methodParameterClass = parameterTypes[0];
			if (methodParameterClass.isPrimitive()) {
				return isPrimitiveToClass(methodParameterClass, parameterClass);
			}
			return parameterClass.isAssignableFrom(methodParameterClass);
		};
	}

	/**
	 * Returns a predicate for method with given return type and parameters.
	 *
	 * @param returnType return type of the method
	 * @param paramTypes parameter types
	 * @return predicate
	 */
	public static Predicate<Method> isMethodWith(final Class<?> returnType, final Class<?>... paramTypes) {
		return method -> {
			Class<?>[] methodParamTypes = method.getParameterTypes();
			if (methodParamTypes.length != paramTypes.length) {
				return false;
			}
			boolean isMethodWithParams = method.getReturnType().isAssignableFrom(returnType);
			for (int i = 0; i < methodParamTypes.length; ++i) {
				isMethodWithParams &= methodParamTypes[i].isAssignableFrom(paramTypes[i]);
			}
			return isMethodWithParams;
		};
	}

	/**
	 * Returns a predicate that checks if the method is a getter for the given field.
	 *
	 * @param field for which to check getter
	 * @return a predicate that checks if the method is a getter for the given field
	 */
	public static Predicate<Method> isGetter(final Field field) {
		return isMethodWith(field.getType()).and(
				hasName(MethodType.GETTER.getMethodName(field))
				.or(hasName(MethodType.getMethodName(Prefix.GET, field)))
		);
	}

}
