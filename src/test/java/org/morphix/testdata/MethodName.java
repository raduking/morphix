package org.morphix.testdata;

import java.util.Optional;
import java.util.function.Supplier;

import org.morphix.reflection.Methods;

public class MethodName {

	public static final String SEPARATOR = "-";

	public static Optional<String> get(final Supplier<String> supplier) {
		return Methods.getCallerMethodName(supplier, (className, methodName) -> className + SEPARATOR + methodName);
	}

}
