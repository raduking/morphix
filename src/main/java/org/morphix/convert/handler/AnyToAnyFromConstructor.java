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
import static org.morphix.convert.FieldHandlerResult.SKIP;

import java.lang.reflect.Constructor;

import org.morphix.convert.FieldHandler;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.reflection.ExtendedField;

/**
 * Converts an object based on the other objects' constructor.
 * <p>
 * Example:
 *
 * <pre>
 * public class A {
 * 	// ...
 * }
 *
 * public class B {
 *
 * 	public B(A a) {
 * 		// ...
 * 	}
 *
 * }
 *
 * A a = new A();
 * B b = new B(a);
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 *
 */
public final class AnyToAnyFromConstructor extends FieldHandler {

	/**
	 * Default constructor.
	 */
	public AnyToAnyFromConstructor() {
		// empty
	}

	/**
	 * @see FieldHandler#handle(ExtendedField, ExtendedField)
	 */
	@Override
	public FieldHandlerResult handle(final ExtendedField sfo, final ExtendedField dfo) {
		Object dValue;
		try {
			Constructor<?> constructor = dfo.toClass().getDeclaredConstructor(sfo.toClass());
			dValue = constructor.newInstance(sfo.getFieldValue());
		} catch (Exception e) {
			return SKIP;
		}
		dfo.setFieldValue(dValue);
		return CONVERTED;
	}

	/**
	 * @see FieldHandler#condition(ExtendedField, ExtendedField)
	 */
	@Override
	public boolean condition(final ExtendedField sfo, final ExtendedField dfo) {
		try {
			dfo.toClass().getDeclaredConstructor(sfo.toClass());
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}
}
