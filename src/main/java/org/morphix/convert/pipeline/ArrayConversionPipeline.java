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
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.InstanceFunction;

/**
 * Wrapper over the array conversions as a pipeline between the source and destination.
 *
 * @param <S> source type
 * @param <D> destination element type
 *
 * @author Radu Sebastian LAZIN
 */
public class ArrayConversionPipeline<S, D> {

	/**
	 * Source array.
	 */
	private final S[] sourceArray;

	/**
	 * Element simple converter.
	 */
	private final SimpleConverter<S, D> elementConverter;

	/**
	 * Constructs an array conversion pipeline.
	 *
	 * @param sourceArray source array
	 * @param elementConverter element simple converter
	 */
	public ArrayConversionPipeline(final S[] sourceArray, final SimpleConverter<S, D> elementConverter) {
		this.sourceArray = sourceArray;
		this.elementConverter = elementConverter;
	}

	/**
	 * Destination conversion.
	 *
	 * @param array destination array
	 * @return destination array
	 */
	public D[] to(final D[] array) {
		D[] destinationArray = null;
		if (array.length < sourceArray.length) {
			destinationArray = newArrayInstance(JavaObjects.cast(array.getClass().getComponentType()), sourceArray.length);
		} else {
			destinationArray = array;
		}
		for (int i = 0; i < sourceArray.length; ++i) {
			destinationArray[i] = elementConverter.convert(sourceArray[i]);
		}
		return destinationArray;
	}

	/**
	 * Destination conversion.
	 *
	 * @param arrayInstanceFunction array instance function
	 * @return destination array
	 */
	public D[] toArray(final InstanceFunction<D[]> arrayInstanceFunction) {
		return to(arrayInstanceFunction.instance());
	}

	/**
	 * Destination conversion.
	 *
	 * @param arrayInstanceFunction array instance function
	 * @return destination array
	 */
	public D[] toArray(final IntFunction<D[]> arrayInstanceFunction) {
		return to(arrayInstanceFunction.apply(sourceArray.length));
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination collection type
	 *
	 * @param result destination collection
	 * @return destination collection
	 */
	public <T extends Collection<D>> T to(final Collection<D> result) {
		for (S source : sourceArray) {
			result.add(elementConverter.convert(source));
		}
		return JavaObjects.cast(result);
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination collection type
	 *
	 * @param collectionInstanceFunction collection instance function
	 * @return destination collection
	 */
	public <T extends Collection<D>> T to(final InstanceFunction<Collection<D>> collectionInstanceFunction) {
		return to(collectionInstanceFunction.instance());
	}

	/**
	 * Destination conversion.
	 *
	 * @return destination list
	 */
	public List<D> toList() {
		return to(ArrayList::new);
	}

	/**
	 * Destination conversion.
	 *
	 * @return destination set
	 */
	public Set<D> toSet() {
		return to(HashSet::new);
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination type
	 *
	 * @param destinationClass destination class which must be an array or a collection
	 * @return destination object which must be an array or a collection
	 */
	public <T> T toAny(final Class<T> destinationClass) {
		if (isArrayListCompatible().test(destinationClass)) {
			return to(newCollectionInstance(destinationClass));
		}
		if (isArray().test(destinationClass)) {
			Class<D> componentType = JavaObjects.cast(destinationClass.getComponentType());
			return JavaObjects.cast(to(newArrayInstance(componentType, sourceArray.length)));
		}
		return null;
	}

	/**
	 * Destination conversion.
	 *
	 * @param <T> destination type
	 *
	 * @param parameterizedType destination type which must be an array or a collection
	 * @return destination object which must be an array or a collection
	 */
	public <T> T toAny(final ParameterizedType parameterizedType) {
		Class<T> rawType = JavaObjects.cast(parameterizedType.getRawType());
		return toAny(rawType);
	}

}
