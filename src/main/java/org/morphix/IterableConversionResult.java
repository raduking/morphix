package org.morphix;

import static org.morphix.extra.ConverterCollections.newArrayInstance;
import static org.morphix.extra.ConverterCollections.newCollectionInstance;
import static org.morphix.reflection.predicates.ClassPredicates.isArray;
import static org.morphix.reflection.predicates.ClassPredicates.isArrayListCompatible;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Wrapper over the iterable conversions result.
 *
 * @param <S> source type
 * @param <D> destination element type
 *
 * @author Radu Sebastian LAZIN
 */
public class IterableConversionResult<S, D> {

	private final Iterable<S> sourceIterable;
	private final SimpleConverter<S, D> elementConverter;

	IterableConversionResult(final Iterable<S> sourceIterable, final SimpleConverter<S, D> elementConverter) {
		this.sourceIterable = sourceIterable;
		this.elementConverter = elementConverter;
	}

	@SuppressWarnings("unchecked")
	public <T extends Collection<D>> T to(final Collection<D> result) {
		sourceIterable.forEach(source -> result.add(elementConverter.convert(source)));
		return (T) result;
	}

	public <T extends Collection<D>> T to(final InstanceFunction<Collection<D>> collectionInstanceFunction) {
		return to(collectionInstanceFunction.instance());
	}

	public D[] to(final D[] result) {
		Iterator<S> iterator = sourceIterable.iterator();
		for (int i = 0; i < elementCount(); ++i) {
			result[i] = elementConverter.convert(iterator.next());
		}
		return result;
	}

	public List<D> toList() {
		return to(ArrayList::new);
	}

	public Set<D> toSet() {
		return to(HashSet::new);
	}

	private int elementCount() {
		int count = 0;
		for (Iterator<S> iterator = sourceIterable.iterator(); iterator.hasNext(); ++count) {
			iterator.next();
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public <T> T toAny(final Class<T> destinationClass) {
		if (isArrayListCompatible().test(destinationClass)) {
			return to(newCollectionInstance(destinationClass));
		}
		if (isArray().test(destinationClass)) {
			return (T) to(newArrayInstance((Class<D>) destinationClass.getComponentType(), elementCount()));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T toAny(final ParameterizedType destinationType) {
		return toAny((Class<T>) destinationType.getRawType());
	}

}
