/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.lang;

/**
 * Utility class re-throws unchecked exceptions for lambda expressions without the need for the caller to surround his
 * lambda expression with a try/catch.
 * <p>
 * Example:
 * <p>
 * Having a code snippet like this:
 *
 * <pre>
 * import java.io.IOException;
 *
 * public Integer getId() throws IOException {
 * 	// ...
 * }
 *
 * public void foo(Supplier&lt;Integer&gt; integerSupplier) {
 * 	// ...
 * }
 * </pre>
 *
 * when calling this method with:
 *
 * <pre>
 * foo(() -> getId());
 * </pre>
 *
 * a compiler error is generated because checked exceptions must be surrounded with a try/catch block and looks
 * something like this:
 *
 * <pre>
 * foo(() -> {
 * 	try {
 * 		return getId();
 * 	} catch (IOException e) {
 * 		// usually
 * 		throw new MyAwesomeException("something bad happened", e);
 * 	}
 * });
 * </pre>
 *
 * which effectively makes the caller of the method handle the exception anyway. This class helps simplify this by
 * automatically re-throwing the exception to the caller:
 *
 * <pre>
 * import static org.morphix.lang.function.ThrowingSupplier.unchecked;
 *
 * foo(() -> unchecked(() -> getId()));
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public interface Unchecked {

	/**
	 * Re-throws the exception.
	 *
	 * @param <T> exception type
	 * @param <R> return type to accommodate to any method that returns
	 *
	 * @param t exception to re-throw
	 * @return nothing since the method only throws
	 * @throws T automatically throws the exception
	 */
	@SuppressWarnings("unchecked")
	static <T extends Throwable, R> R reThrow(final Throwable t) throws T {
		throw (T) t;
	}

	/**
	 * Interface that creates a name-space for the other version of the {@link Unchecked#reThrow(Throwable)} function that
	 * does not have a <code>throws</code> declaration.
	 */
	interface Undeclared {

		/**
		 * Variation of the {@link Unchecked#reThrow(Throwable)} without a <code>throws</code> declaration to accommodate
		 * functional interfaces and method references.
		 *
		 * @param <R> return type to accommodate to any method that returns
		 *
		 * @param t exception to re-throw
		 * @return nothing since the method only throws
		 */
		static <R> R reThrow(final Throwable t) {
			return Unchecked.reThrow(t);
		}

	}

}
