package org.morphix;

import static org.morphix.Conversion.convertEnvelopedFrom;

import java.util.HashMap;
import java.util.Map;

import org.morphix.function.InstanceFunction;
import org.morphix.function.SimpleConverter;

/**
 * Wrapper over the map conversions result.
 *
 * @param <I> source key type
 * @param <S> source value type
 * @param <J> destination key type
 * @param <D> destination value type
 *
 * @author Radu Sebastian LAZIN
 */
public class MapConversionResult<I, S, J, D> {

	private final Map<I, S> sourceMap;

	private InstanceFunction<J> keyInstanceFunction;
	private InstanceFunction<D> valueInstanceFunction;

	private SimpleConverter<I, J> keyConverter;
	private SimpleConverter<S, D> valueConverter;

	MapConversionResult(final Map<I, S> sourceMap, final InstanceFunction<J> keyInstanceFunction, final InstanceFunction<D> valueInstanceFunction) {
		this.sourceMap = sourceMap;
		this.keyInstanceFunction = keyInstanceFunction;
		this.valueInstanceFunction = valueInstanceFunction;
	}

	MapConversionResult(final Map<I, S> sourceMap, final SimpleConverter<I, J> keyConverter, final SimpleConverter<S, D> valueConverter) {
		this.sourceMap = sourceMap;
		this.keyConverter = keyConverter;
		this.valueConverter = valueConverter;
	}

	@SuppressWarnings("unchecked")
	public <T extends Map<J, D>> T to(final Map<J, D> result) {
		for (Map.Entry<I, S> entry : this.sourceMap.entrySet()) {
			J destinationKey = useKeyInstance()
					? convertEnvelopedFrom(entry.getKey(), keyInstanceFunction, Configuration.defaultConfiguration())
					: keyConverter.convert(entry.getKey());
			D destinationValue = useValueInstance()
					? convertEnvelopedFrom(entry.getValue(), valueInstanceFunction, Configuration.defaultConfiguration())
					: valueConverter.convert(entry.getValue());
			result.put(destinationKey, destinationValue);
		}
		return (T) result;
	}

	public <T extends Map<J, D>> T to(final InstanceFunction<Map<J, D>> mapInstanceFunction) {
		return to(mapInstanceFunction.instance());
	}

	public Map<J, D> toMap() {
		return to(HashMap::new);
	}

	private boolean useKeyInstance() {
		return null != keyInstanceFunction;
	}

	private boolean useValueInstance() {
		return null != valueInstanceFunction;
	}
}
