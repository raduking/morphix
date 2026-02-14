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
package org.morphix.convert.context;

import java.util.function.Supplier;

import org.morphix.lang.function.Suppliers;

/**
 * Context for tracking visited objects during conversion.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConversionContext {

	/**
	 * Called when an object is being visited. Returns {@code true} if the object has already been visited, {@code false}
	 * otherwise. The default implementation always returns {@code false}, meaning that by default, no object is considered
	 * as visited. Implementations can override this method to provide custom logic for tracking visited objects, such as
	 * using a set to store visited object references.
	 *
	 * @param obj the object being visited
	 * @return {@code true} if the object has already been visited, {@code false} otherwise
	 */
	default boolean enter(final Object obj) {
		return false;
	}

	/**
	 * Called when an object has been fully visited. By default, this method does nothing.
	 *
	 * @param obj the object that has been fully visited
	 */
	default void exit(final Object obj) {
		// empty
	}

	/**
	 * Visits an object, returning a result from the provided suppliers. If the object has already been visited, the
	 * {@code visitedResultSupplier} is used to provide the result. Otherwise, the {@code resultSupplier} is used to provide
	 * the result, and the object is marked as visited during the execution of the supplier.
	 *
	 * @param <T> the type of the result
	 *
	 * @param obj the object being visited
	 * @param resultSupplier the supplier to provide the result if the object has not been visited
	 * @param visitedResultSupplier the supplier to provide the result if the object has already been visited
	 * @return the result of visiting the object
	 */
	default <T> T visit(final Object obj, final Supplier<T> resultSupplier, final Supplier<T> visitedResultSupplier) {
		if (enter(obj)) {
			return visitedResultSupplier.get();
		}
		try {
			return resultSupplier.get();
		} finally {
			exit(obj);
		}
	}

	/**
	 * Visits an object, returning a result from the provided supplier. If the object has already been visited, {@code null}
	 * is returned. Otherwise, the {@code resultSupplier} is used to provide the result, and the object is marked as visited
	 * during the execution of the supplier.
	 *
	 * @param <T> the type of the result
	 *
	 * @param obj the object being visited
	 * @param resultSupplier the supplier to provide the result if the object has not been visited
	 * @return the result of visiting the object, or {@code null} if the object has already been visited
	 */
	default <T> T visit(final Object obj, final Supplier<T> resultSupplier) {
		return visit(obj, resultSupplier, Suppliers.supplyNull());
	}
}
