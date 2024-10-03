package org.morphix;

import static org.morphix.extra.ConverterCollections.newArrayInstance;
import static org.morphix.extra.ConverterCollections.newCollectionInstance;
import static org.morphix.reflection.predicates.ClassPredicates.isArray;
import static org.morphix.reflection.predicates.ClassPredicates.isArrayListCompatible;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Wrapper over the array conversions result.
 *
 * @param <S> source type
 * @param <D> destination element type
 *
 * @author Radu Sebastian LAZIN
 */
public class ArrayConversionResult<S, D> {

	private final S[] sourceArray;
	private final SimpleConverter<S, D> elementConverter;

	ArrayConversionResult(final S[] sourceArray, final SimpleConverter<S, D> elementConverter) {
		this.sourceArray = sourceArray;
		this.elementConverter = elementConverter;
	}

	public D[] to(final D[] result) {
		for (int i = 0; i < sourceArray.length; ++i) {
			result[i] = elementConverter.convert(sourceArray[i]);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends Collection<D>> T to(final Collection<D> result) {
		for (S source : sourceArray) {
			result.add(elementConverter.convert(source));
		}
		return (T) result;
	}

	public <T extends Collection<D>> T to(final InstanceFunction<Collection<D>> collectionInstanceFunction) {
		return to(collectionInstanceFunction.instance());
	}

	public List<D> toList() {
		return to(ArrayList::new);
	}

	public Set<D> toSet() {
		return to(HashSet::new);
	}

	@SuppressWarnings("unchecked")
	public <T> T toAny(final Class<T> destinationClass) {
		if (isArrayListCompatible().test(destinationClass)) {
			return to(newCollectionInstance(destinationClass));
		}
		if (isArray().test(destinationClass)) {
			return (T) to(newArrayInstance((Class<D>) destinationClass.getComponentType(), sourceArray.length));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T toAny(final ParameterizedType parameterizedType) {
		return toAny((Class<T>) parameterizedType.getRawType());
	}

}
