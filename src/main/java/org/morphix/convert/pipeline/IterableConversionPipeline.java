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
package org.morphix.convert.pipeline;

import static org.morphix.convert.extras.ConverterCollections.newArrayInstance;
import static org.morphix.convert.extras.ConverterCollections.newCollectionInstance;
import static org.morphix.reflection.predicates.ClassPredicates.isArray;
import static org.morphix.reflection.predicates.ClassPredicates.isArrayListCompatible;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;

/**
 * Wrapper over the {@link Iterable} conversions as a pipeline between the source and destination.
 *
 * @param <S> source type
 * @param <D> destination element type
 *
 * @author Radu Sebastian LAZIN
 */
public class IterableConversionPipeline<S, D> {

	/**
	 * Source iterable.
	 */
	private final Iterable<S> sourceIterable;

	/**
	 * Element simple converter.
	 */
	private final SimpleConverter<S, D> elementConverter;

	/**
	 * Constructor with the source {@link Iterable} and an element converter.
	 *
	 * @param sourceIterable source iterable
	 * @param elementConverter element converter
	 */
	public IterableConversionPipeline(final Iterable<S> sourceIterable, final SimpleConverter<S, D> elementConverter) {
		this.sourceIterable = sourceIterable;
		this.elementConverter = elementConverter;
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination collection type
	 *
	 * @param result collection to add converted elements to
	 * @return destination collection
	 */
	public <T extends Collection<D>> T to(final Collection<D> result) {
		sourceIterable.forEach(source -> result.add(elementConverter.convert(source)));
		return JavaObjects.cast(result);
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination collection type
	 *
	 * @param collectionInstanceFunction collection instance function to build the destination collection
	 * @return destination collection
	 */
	public <T extends Collection<D>> T to(final InstanceFunction<Collection<D>> collectionInstanceFunction) {
		return to(collectionInstanceFunction.instance());
	}

	/**
	 * Destination conversion to array.
	 *
	 * @param result array to add converted elements to
	 * @return destination array
	 */
	public D[] to(final D[] result) {
		Iterator<S> iterator = sourceIterable.iterator();
		for (int i = 0; i < elementCount(); ++i) {
			result[i] = elementConverter.convert(iterator.next());
		}
		return result;
	}

	/**
	 * Destination conversion to {@link List}.
	 *
	 * @return destination list
	 */
	public List<D> toList() {
		return to(ArrayList::new);
	}

	/**
	 * Destination conversion to {@link Set}.
	 *
	 * @return destination set
	 */
	public Set<D> toSet() {
		return to(HashSet::new);
	}

	/**
	 * Returns the source iterable element count.
	 *
	 * @return the source iterable element count
	 */
	private int elementCount() {
		int count = 0;
		for (Iterator<S> iterator = sourceIterable.iterator(); iterator.hasNext(); ++count) {
			iterator.next();
		}
		return count;
	}

	/**
	 * Destination conversion to any.
	 *
	 * @param <T> destination iterable type
	 *
	 * @param destinationClass iterable destination class
	 * @return destination iterable
	 */
	public <T> T toAny(final Class<T> destinationClass) {
		if (isArrayListCompatible().test(destinationClass)) {
			return to(newCollectionInstance(destinationClass));
		}
		if (isArray().test(destinationClass)) {
			Class<D> componentType = JavaObjects.cast(destinationClass.getComponentType());
			return JavaObjects.cast(to(newArrayInstance(componentType, elementCount())));
		}
		return null;
	}

	/**
	 * Destination conversion to any.
	 *
	 * @param <T> destination iterable type
	 *
	 * @param destinationType iterable destination type
	 * @return destination iterable
	 */
	public <T> T toAny(final ParameterizedType destinationType) {
		Class<T> rawType = JavaObjects.cast(destinationType.getRawType());
		return toAny(rawType);
	}
}
