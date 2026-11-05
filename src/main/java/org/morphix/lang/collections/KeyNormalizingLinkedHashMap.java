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
package org.morphix.lang.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.morphix.lang.JavaObjects;

/**
 * A {@link LinkedHashMap} variant that stores {@link String} keys using their original form while indexing them by a
 * normalized form for lookup.
 * <p>
 * {@code null} keys are not supported. Subclasses define the normalization strategy.
 *
 * @param <V> the value type
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class KeyNormalizingLinkedHashMap<V> implements Map<String, V>, Serializable, Cloneable {

	/**
	 * Serial version UID, required for {@link Serializable} classes.
	 */
	@Serial
	private static final long serialVersionUID = 8356182637897455059L;

	/**
	 * Default expected number of entries when no capacity hint is supplied.
	 */
	protected static final int DEFAULT_EXPECTED_SIZE = 8;

	/**
	 * Map that stores entries in insertion order using the original key casing.
	 * <p>
	 * {@link LinkedHashMap} is serializable, Java serialization validates each runtime value.
	 */
	private LinkedHashMap<String, V> internalMap; // NOSONAR see JavaDoc for rationale

	/**
	 * Index from normalized key to the actual key string stored in {@link #internalMap}.
	 */
	private HashMap<String, String> normalizedKeys;

	/**
	 * Cached key set view, cleared when the map is cloned.
	 */
	private transient Set<String> keySet;

	/**
	 * Cached entry set view, cleared when the map is cloned.
	 */
	private transient Set<Entry<String, V>> entrySet;

	/**
	 * Cached values view, cleared when the map is cloned.
	 */
	private transient Collection<V> values;

	/**
	 * Creates a map with the given expected size.
	 *
	 * @param expectedSize expected number of entries, used as an initial capacity hint
	 */
	protected KeyNormalizingLinkedHashMap(final int expectedSize) {
		this.internalMap = new InternalLinkedHashMap(expectedSize);
		this.normalizedKeys = HashMap.newHashMap(expectedSize);
	}

	/**
	 * Returns the normalized form of the given key used for lookup.
	 *
	 * @param key the key to normalize
	 * @return the normalized key
	 */
	protected abstract String normalizeKey(String key);

	/**
	 * @see Map#get(Object)
	 */
	@Override
	public V get(final Object key) {
		return getOrDefault(key, null);
	}

	/**
	 * @see Map#getOrDefault(Object, Object)
	 */
	@Override
	public V getOrDefault(final Object key, final V defaultValue) {
		if (key instanceof String string) {
			String normalizedKey = normalizeKey(string);
			String caseInsensitiveKey = normalizedKeys.get(normalizedKey);
			if (null != caseInsensitiveKey) {
				return internalMap.get(caseInsensitiveKey);
			}
		}
		return defaultValue;
	}

	/**
	 * @see Map#put(Object, Object)
	 */
	@Override
	public V put(final String key, final V value) {
		String normalizedKey = normalizeKey(key);
		String oldKey = normalizedKeys.put(normalizedKey, key);
		V oldKeyValue = null;
		if (null != oldKey && !oldKey.equals(key)) {
			oldKeyValue = internalMap.remove(oldKey);
		}
		V oldValue = internalMap.put(key, value);
		return null != oldKeyValue ? oldKeyValue : oldValue;
	}

	/**
	 * @see Map#putAll(Map)
	 */
	@Override
	public void putAll(final Map<? extends String, ? extends V> map) {
		if (map.isEmpty()) {
			return;
		}
		map.forEach(this::put);
	}

	/**
	 * @see Map#putIfAbsent(Object, Object)
	 */
	@Override
	public V putIfAbsent(final String key, final V value) {
		String normalizedKey = normalizeKey(key);
		String newKey = key;
		String oldKey = normalizedKeys.putIfAbsent(normalizedKey, newKey);
		if (null != oldKey) {
			V oldKeyValue = internalMap.get(oldKey);
			if (null != oldKeyValue) {
				return oldKeyValue;
			}
			newKey = oldKey;
		}
		return internalMap.putIfAbsent(newKey, value);
	}

	/**
	 * @see Map#compute(Object, BiFunction)
	 */
	@Override
	public V compute(final String key, final BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		String normalizedKey = normalizeKey(key);
		String actualKey = normalizedKeys.getOrDefault(normalizedKey, key);
		V oldValue = internalMap.get(actualKey);
		V newValue = remappingFunction.apply(actualKey, oldValue);
		if (null == newValue) {
			if (null != oldValue || internalMap.containsKey(actualKey)) {
				remove(actualKey);
			}
			return null;
		}
		put(actualKey, newValue);
		return newValue;
	}

	/**
	 * @see Map#computeIfAbsent(Object, Function)
	 */
	@Override
	public V computeIfAbsent(final String key, final Function<? super String, ? extends V> mappingFunction) {
		Objects.requireNonNull(mappingFunction);
		String normalizedKey = normalizeKey(key);
		String oldKey = normalizedKeys.get(normalizedKey);
		if (null != oldKey) {
			V oldValue = internalMap.get(oldKey);
			if (null != oldValue) {
				return oldValue;
			}
		}
		String actualKey = null != oldKey ? oldKey : key;
		V newValue = mappingFunction.apply(actualKey);
		if (null != newValue) {
			if (null != oldKey) {
				internalMap.put(oldKey, newValue);
			} else {
				put(key, newValue);
			}
		}
		return newValue;
	}

	/**
	 * @see Map#computeIfPresent(Object, BiFunction)
	 */
	@Override
	public V computeIfPresent(final String key, final BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(remappingFunction);
		String normalizedKey = normalizeKey(key);
		String actualKey = normalizedKeys.get(normalizedKey);
		if (null == actualKey) {
			return null;
		}
		V oldValue = internalMap.get(actualKey);
		if (null == oldValue) {
			return null;
		}
		V newValue = remappingFunction.apply(actualKey, oldValue);
		if (null == newValue) {
			remove(actualKey);
		} else {
			internalMap.put(actualKey, newValue);
		}
		return newValue;
	}

	/**
	 * @see Map#merge(Object, Object, BiFunction)
	 */
	@Override
	public V merge(final String key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		Objects.requireNonNull(value);
		Objects.requireNonNull(remappingFunction);
		String normalizedKey = normalizeKey(key);
		String actualKey = normalizedKeys.get(normalizedKey);
		if (null == actualKey) {
			put(key, value);
			return value;
		}
		V oldValue = internalMap.get(actualKey);
		V newValue = null == oldValue ? value : remappingFunction.apply(oldValue, value);
		if (null == newValue) {
			remove(actualKey);
		} else {
			internalMap.put(actualKey, newValue);
		}
		return newValue;
	}

	/**
	 * @see Map#remove(Object)
	 */
	@Override
	public V remove(final Object key) {
		if (key instanceof String string) {
			String caseInsensitiveKey = removeCaseInsensitiveKey(string);
			if (null != caseInsensitiveKey) {
				return internalMap.remove(caseInsensitiveKey);
			}
		}
		return null;
	}

	/**
	 * @see Map#replace(Object, Object, Object)
	 */
	@Override
	public boolean replace(final String key, final V oldValue, final V newValue) {
		String normalizedKey = normalizeKey(key);
		String actualKey = normalizedKeys.get(normalizedKey);
		if (null == actualKey) {
			return false;
		}
		V currentValue = internalMap.get(actualKey);
		if (!Objects.equals(currentValue, oldValue)) {
			return false;
		}
		internalMap.put(actualKey, newValue);
		return true;
	}

	/**
	 * @see Map#replace(Object, Object)
	 */
	@Override
	public V replace(final String key, final V value) {
		String normalizedKey = normalizeKey(key);
		String actualKey = normalizedKeys.get(normalizedKey);
		if (null == actualKey) {
			return null;
		}
		return internalMap.put(actualKey, value);
	}

	/**
	 * @see Map#containsKey(Object)
	 */
	@Override
	public boolean containsKey(final Object key) {
		return switch (key) {
			case null -> false;
			case String string -> {
				String normalizedKey = normalizeKey(string);
				yield normalizedKeys.containsKey(normalizedKey);
			}
			default -> false;
		};
	}

	/**
	 * @see Map#containsValue(Object)
	 */
	@Override
	public boolean containsValue(final Object value) {
		return internalMap.containsValue(value);
	}

	/**
	 * @see Map#size()
	 */
	@Override
	public int size() {
		return internalMap.size();
	}

	/**
	 * @see Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	/**
	 * @see Map#clear()
	 */
	@Override
	public void clear() {
		normalizedKeys.clear();
		internalMap.clear();
	}

	/**
	 * @see Map#keySet()
	 */
	@Override
	public Set<String> keySet() {
		if (null == keySet) {
			keySet = new KeySetWrapper(internalMap.keySet());
		}
		return keySet;
	}

	/**
	 * @see Map#values()
	 */
	@Override
	public Collection<V> values() {
		if (null == values) {
			values = new ValuesWrapper(internalMap.values());
		}
		return values;
	}

	/**
	 * @see Map#entrySet()
	 */
	@Override
	public Set<Entry<String, V>> entrySet() {
		if (null == entrySet) {
			entrySet = new EntrySetWrapper(internalMap.entrySet());
		}
		return entrySet;
	}

	/**
	 * @see Map#forEach(BiConsumer)
	 */
	@Override
	public void forEach(final BiConsumer<? super String, ? super V> action) {
		internalMap.forEach(action);
	}

	/**
	 * Clears cached collection views so they are recreated after structural changes such as {@link #clone()}.
	 */
	private void clearWrappers() {
		this.keySet = null;
		this.entrySet = null;
		this.values = null;
	}

	/**
	 * Returns a shallow copy of this map. The clone has its own {@link #internalMap} and normalized key index but shares
	 * value references with this map.
	 * <p>
	 * A copy constructor cannot preserve the runtime subtype and its overridden {@link #removeEldestEntry(Map.Entry)}
	 * behavior, so cloning is used here instead.
	 *
	 * @return a shallow copy of this map
	 * @throws CloneNotSupportedException if cloning is not supported
	 */
	@Override
	public KeyNormalizingLinkedHashMap<V> clone() throws CloneNotSupportedException { // NOSONAR see JavaDoc for rationale
		KeyNormalizingLinkedHashMap<V> clone = JavaObjects.cast(super.clone());
		clone.normalizedKeys = new HashMap<>(normalizedKeys);
		clone.internalMap = clone.new InternalLinkedHashMap(internalMap.size());
		clone.internalMap.putAll(internalMap);
		clone.clearWrappers();
		return clone;
	}

	/**
	 * {@link LinkedHashMap#removeEldestEntry(Map.Entry)} hook invoked by the internal map. The default implementation
	 * returns {@code false} and never evicts entries. Subclasses may override this method to implement size- or age-based
	 * eviction; when an entry is evicted, the case-insensitive key index is updated automatically.
	 *
	 * @param eldest the eldest entry according to insertion or access order
	 * @return {@code true} if the eldest entry should be removed
	 */
	protected boolean removeEldestEntry(final Map.Entry<String, V> eldest) {
		return false;
	}

	/**
	 * Removes the normalized mapping for the given key and returns the actual key that was stored, if any.
	 *
	 * @param key the key whose normalized form should be removed from the index
	 * @return the actual key previously associated with the normalized form, or {@code null} if none
	 */
	private String removeCaseInsensitiveKey(final String key) {
		String normalizedKey = normalizeKey(key);
		return normalizedKeys.remove(normalizedKey);
	}

	/**
	 * @see Map#equals(Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof KeyNormalizingLinkedHashMap<?> map) {
			return internalMap.equals(map.internalMap);
		}
		if (other instanceof Map<?, ?> map) {
			return internalMap.equals(map);
		}
		return false;
	}

	/**
	 * @see Map#hashCode()
	 */
	@Override
	public int hashCode() {
		return internalMap.hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return internalMap.toString();
	}

	/**
	 * Internal {@link LinkedHashMap} that delegates eldest-entry removal to the enclosing map and keeps the
	 * case-insensitive key index in sync when entries are evicted.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class InternalLinkedHashMap extends LinkedHashMap<String, V> {

		/**
		 * Serial version UID, required for {@link Serializable} classes.
		 */
		@Serial
		private static final long serialVersionUID = 5011870018629132705L;

		/**
		 * Creates an internal map with the given expected size.
		 *
		 * @param expectedSize expected number of entries, used as an initial capacity hint
		 */
		protected InternalLinkedHashMap(final int expectedSize) {
			super(expectedSize);
		}

		/**
		 * @see LinkedHashMap#removeEldestEntry(Map.Entry)
		 */
		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, V> eldest) {
			boolean shouldRemove = KeyNormalizingLinkedHashMap.this.removeEldestEntry(eldest);
			if (shouldRemove) {
				removeCaseInsensitiveKey(eldest.getKey());
			}
			return shouldRemove;
		}
	}

	/**
	 * Key set view whose {@link #remove(Object)} and {@link #clear()} operations update the case-insensitive key index.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class KeySetWrapper extends AbstractSet<String> {

		/**
		 * Backing key set from {@link #internalMap}.
		 */
		private final Set<String> delegate;

		/**
		 * Creates a wrapper around the given delegate set.
		 *
		 * @param delegate backing key set from the internal map
		 */
		protected KeySetWrapper(final Set<String> delegate) {
			this.delegate = delegate;
		}

		/**
		 * @see Set#contains(Object)
		 */
		@Override
		public boolean contains(final Object o) {
			return delegate.contains(o);
		}

		/**
		 * @see Set#remove(Object)
		 */
		@Override
		public boolean remove(final Object o) {
			if (KeyNormalizingLinkedHashMap.this.containsKey(o)) {
				KeyNormalizingLinkedHashMap.this.remove(o);
				return true;
			}
			return false;
		}

		/**
		 * @see Set#size()
		 */
		@Override
		public int size() {
			return delegate.size();
		}

		/**
		 * @see Set#clear()
		 */
		@Override
		public void clear() {
			KeyNormalizingLinkedHashMap.this.clear();
		}

		/**
		 * @see Set#iterator()
		 */
		@Override
		public Iterator<String> iterator() {
			return new KeySetIterator();
		}

		/**
		 * @see Set#spliterator()
		 */
		@Override
		public Spliterator<String> spliterator() {
			return delegate.spliterator();
		}

		/**
		 * @see Iterable#forEach(Consumer)
		 */
		@Override
		public void forEach(final Consumer<? super String> action) {
			delegate.forEach(action);
		}

		/**
		 * @see Set#equals(Object)
		 */
		@Override
		public boolean equals(final Object other) {
			return this == other || delegate.equals(other);
		}

		/**
		 * @see Set#hashCode()
		 */
		@Override
		public int hashCode() {
			return delegate.hashCode();
		}
	}

	/**
	 * Values view whose mutating operations update the enclosing map and its case-insensitive key index.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class ValuesWrapper extends AbstractCollection<V> {

		/**
		 * Backing values collection from {@link #internalMap}.
		 */
		private final Collection<V> delegate;

		/**
		 * Creates a wrapper around the given delegate collection.
		 *
		 * @param delegate backing values collection from the internal map
		 */
		protected ValuesWrapper(final Collection<V> delegate) {
			this.delegate = delegate;
		}

		/**
		 * @see Collection#contains(Object)
		 */
		@Override
		public boolean contains(final Object o) {
			return delegate.contains(o);
		}

		/**
		 * @see Collection#size()
		 */
		@Override
		public int size() {
			return delegate.size();
		}

		/**
		 * @see Collection#clear()
		 */
		@Override
		public void clear() {
			KeyNormalizingLinkedHashMap.this.clear();
		}

		/**
		 * @see Collection#iterator()
		 */
		@Override
		public Iterator<V> iterator() {
			return new ValuesIterator();
		}

		/**
		 * @see Collection#spliterator()
		 */
		@Override
		public Spliterator<V> spliterator() {
			return delegate.spliterator();
		}

		/**
		 * @see Iterable#forEach(Consumer)
		 */
		@Override
		public void forEach(final Consumer<? super V> action) {
			delegate.forEach(action);
		}
	}

	/**
	 * Entry set view whose {@link #contains(Object)} and {@link #remove(Object)} operations match keys case-insensitively
	 * and keep the key index consistent on removal.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class EntrySetWrapper extends AbstractSet<Entry<String, V>> {

		/**
		 * Backing entry set from {@link #internalMap}.
		 */
		private final Set<Entry<String, V>> delegate;

		/**
		 * Creates a wrapper around the given delegate set.
		 *
		 * @param delegate backing entry set from the internal map
		 */
		protected EntrySetWrapper(final Set<Entry<String, V>> delegate) {
			this.delegate = delegate;
		}

		/**
		 * @see Set#contains(Object)
		 */
		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry<?, ?> entry)) {
				return false;
			}
			Object key = entry.getKey();
			if (!KeyNormalizingLinkedHashMap.this.containsKey(key)) {
				return false;
			}
			V value = KeyNormalizingLinkedHashMap.this.get(key);
			return Objects.equals(value, entry.getValue());
		}

		/**
		 * @see Set#remove(Object)
		 */
		@Override
		public boolean remove(final Object o) {
			if (!(o instanceof Map.Entry<?, ?> entry)) {
				return false;
			}
			Object key = entry.getKey();
			if (!KeyNormalizingLinkedHashMap.this.containsKey(key)) {
				return false;
			}
			V value = KeyNormalizingLinkedHashMap.this.get(key);
			if (!Objects.equals(value, entry.getValue())) {
				return false;
			}
			KeyNormalizingLinkedHashMap.this.remove(key);
			return true;
		}

		/**
		 * @see Set#size()
		 */
		@Override
		public int size() {
			return delegate.size();
		}

		/**
		 * @see Set#clear()
		 */
		@Override
		public void clear() {
			KeyNormalizingLinkedHashMap.this.clear();
		}

		/**
		 * @see Set#iterator()
		 */
		@Override
		public Iterator<Entry<String, V>> iterator() {
			return new EntrySetIterator();
		}

		/**
		 * @see Set#spliterator()
		 */
		@Override
		public Spliterator<Entry<String, V>> spliterator() {
			return delegate.spliterator();
		}

		/**
		 * @see Iterable#forEach(Consumer)
		 */
		@Override
		public void forEach(final Consumer<? super Entry<String, V>> action) {
			delegate.forEach(action);
		}

		/**
		 * @see Set#equals(Object)
		 */
		@Override
		public boolean equals(final Object other) {
			return this == other || delegate.equals(other);
		}

		/**
		 * @see Set#hashCode()
		 */
		@Override
		public int hashCode() {
			return delegate.hashCode();
		}
	}

	/**
	 * Iterator base class that removes entries from the case-insensitive key index when {@link #remove()} is called.
	 *
	 * @param <T> the element type returned by the concrete iterator
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected abstract class InternalIterator<T> implements Iterator<T> {

		/**
		 * Iterator over the internal map entries.
		 */
		private final Iterator<Entry<String, V>> delegate;

		/**
		 * Last entry returned by {@link #nextElement()}, used by {@link #remove()}.
		 */
		private Entry<String, V> last;

		/**
		 * Creates an iterator over the internal map entries.
		 */
		protected InternalIterator() {
			this.delegate = internalMap.entrySet().iterator();
		}

		/**
		 * Returns the next internal map entry and remembers it for a subsequent {@link #remove()}.
		 *
		 * @return the next map entry
		 */
		protected Entry<String, V> nextElement() {
			this.last = delegate.next();
			return last;
		}

		/**
		 * @see Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * @see Iterator#remove()
		 */
		@Override
		public void remove() {
			delegate.remove();
			removeCaseInsensitiveKey(last.getKey());
			this.last = null;
		}
	}

	/**
	 * Iterator over map keys that keeps the case-insensitive key index in sync on removal.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class KeySetIterator extends InternalIterator<String> {

		/**
		 * Default constructor, creates an iterator over the internal map keys.
		 */
		protected KeySetIterator() {
			// empty
		}

		/**
		 * @see Iterator#next()
		 */
		@Override
		public String next() {
			return nextElement().getKey();
		}
	}

	/**
	 * Iterator over map values that keeps the case-insensitive key index in sync on removal.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class ValuesIterator extends InternalIterator<V> {

		/**
		 * Default constructor, creates an iterator over the internal map values.
		 */
		protected ValuesIterator() {
			// empty
		}

		/**
		 * @see Iterator#next()
		 */
		@Override
		public V next() {
			return nextElement().getValue();
		}
	}

	/**
	 * Iterator over map entries that keeps the case-insensitive key index in sync on removal.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected class EntrySetIterator extends InternalIterator<Entry<String, V>> {

		/**
		 * Default constructor, creates an iterator over the internal map entries.
		 */
		protected EntrySetIterator() {
			// empty
		}

		/**
		 * @see Iterator#next()
		 */
		@Override
		public Entry<String, V> next() {
			return nextElement();
		}
	}
}
