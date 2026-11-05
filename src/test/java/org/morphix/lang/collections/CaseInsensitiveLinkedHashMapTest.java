package org.morphix.lang.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CaseInsensitiveLinkedHashMap}.
 *
 * @author Radu Sebastian LAZIN
 */
class CaseInsensitiveLinkedHashMapTest {

	@Nested
	class ConstructorTests {

		@Test
		void shouldConstructWithExpectedSizeOnly() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>(32);

			map.put("Header", 42);

			assertThat(map.get("HEADER"), is(42));
			assertThat(map.getLocale(), is(Locale.getDefault()));
		}
	}

	@Nested
	class PutTests {

		@Test
		void shouldPreserveInsertionOrderAndReplaceKeyCasing() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);
			map.put("FIRST", 3);

			assertThat(map.size(), is(2));
			assertThat(map.keySet().toArray(), arrayContaining("Second", "FIRST"));
			assertThat(map.get("first"), is(3));
		}

		@Test
		void shouldReturnPreviousValueWhenReplacingKeyCasing() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.put("HEADER", 99), is(42));
			assertThat(map.size(), is(1));
			assertThat(map.keySet().toArray(), arrayContaining("HEADER"));
			assertThat(map.get("header"), is(99));
		}
	}

	@Nested
	class PutAllTests {

		@Test
		void shouldPutAllEntriesFromAnotherMap() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.putAll(Map.of("First", 1, "Second", 2));

			assertThat(map.size(), is(2));
			assertThat(map.get("FIRST"), is(1));
			assertThat(map.get("second"), is(2));
		}

		@Test
		void shouldIgnoreEmptyMapOnPutAll() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);
			map.putAll(Map.of());

			assertThat(map.size(), is(1));
			assertThat(map.get("header"), is(42));
		}
	}

	@Nested
	class GetTests {

		@Test
		void shouldResolveKeysIgnoringCase() {
			CaseInsensitiveLinkedHashMap<String> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Content-Type", "application/json");

			assertThat(map.get("CONTENT-TYPE"), is("application/json"));
			assertThat(map.get("content-type"), is("application/json"));
			assertThat(map.containsKey("CoNtEnT-TyPe"), is(true));
			assertThat(map.getOrDefault("missing", "default"), is("default"));
		}

		@Test
		void shouldDistinguishMissingKeysFromNullValues() {
			CaseInsensitiveLinkedHashMap<String> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.containsKey("HEADER"), is(true));
			assertNull(map.get("header"));
			assertNull(map.getOrDefault("HEADER", "default"));
			assertThat(map.getOrDefault("missing", "default"), is("default"));
		}

		@Test
		void shouldReturnDefaultForNonStringKeyOnGetOrDefault() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.getOrDefault(42, 99), is(99));
		}
	}

	@Nested
	class ContainsKeyTests {

		@Test
		void shouldReturnFalseForNullKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.containsKey(null), is(false));
		}

		@Test
		@SuppressWarnings("unlikely-arg-type")
		void shouldReturnFalseForNonStringKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.containsKey(42), is(false));
		}
	}

	@Nested
	class RemoveTests {

		@Test
		void shouldRemoveKeysIgnoringCase() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.remove("HEADER"), is(42));
			assertThat(map.isEmpty(), is(true));

			map.put("header", 100);

			assertThat(map.size(), is(1));
			assertThat(map.get("HEADER"), is(100));
		}

		@Test
		@SuppressWarnings("unlikely-arg-type")
		void shouldReturnNullWhenRemovingNonStringKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertNull(map.remove(42));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldReturnNullWhenRemovingMissingStringKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertNull(map.remove("OTHER"));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldRemoveEntryConditionallyIgnoringCase() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.remove("HEADER", 99), is(false));
			assertThat(map.containsKey("header"), is(true));

			assertThat(map.remove("HEADER", 42), is(true));
			assertThat(map.containsKey("header"), is(false));
			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldPreserveNullValuesDuringConditionalRemoval() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.remove("HEADER", null), is(true));
			assertThat(map.containsKey("header"), is(false));
		}
	}

	@Nested
	class EntrySetTests {

		@Test
		void shouldRemoveEntriesIgnoringCase() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().contains(Map.entry("HEADER", 42)), is(true));
			assertThat(map.entrySet().remove(Map.entry("header", 42)), is(true));
			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldNotRemoveEntryWhenValueDoesNotMatch() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().remove(Map.entry("HEADER", 99)), is(false));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldSupportNullValueRemovalThroughEntrySet() {
			CaseInsensitiveLinkedHashMap<String> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.entrySet().remove(
					new java.util.AbstractMap.SimpleEntry<>("HEADER", null)), is(true));

			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldSupportNullValuesThroughEntrySet() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("HEADER", null)), is(true));
			assertThat(map.entrySet().remove(new AbstractMap.SimpleEntry<>("header", null)), is(true));
			assertThat(map.containsKey("HEADER"), is(false));
		}

		@Test
		@SuppressWarnings("unlikely-arg-type")
		void shouldRejectNonEntryOnContains() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().contains("not-an-entry"), is(false));
		}

		@Test
		void shouldReturnFalseWhenEntryKeyIsAbsentOnContains() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().contains(Map.entry("OTHER", 42)), is(false));
		}

		@Test
		@SuppressWarnings("unlikely-arg-type")
		void shouldRejectNonEntryOnRemove() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().remove("not-an-entry"), is(false));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldReturnFalseWhenRemovingAbsentEntry() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.entrySet().remove(Map.entry("OTHER", 42)), is(false));
		}

		@Test
		void shouldExposeEntrySetSize() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			assertThat(map.entrySet().size(), is(2));
		}

		@Test
		void shouldClearThroughEntrySet() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.entrySet().clear();

			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldTraverseEntrySetWithSpliteratorAndForEach() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			long spliteratorCount = StreamSupport.stream(map.entrySet().spliterator(), false).count();
			assertThat(spliteratorCount, is(2L));

			List<String> keys = new ArrayList<>();
			map.entrySet().forEach(entry -> keys.add(entry.getKey()));

			assertThat(keys, contains("First", "Second"));
		}
	}

	@Nested
	class CollectionViewTests {

		@Test
		void shouldRemoveNullValuedKeyThroughKeySet() {
			CaseInsensitiveLinkedHashMap<String> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.keySet().remove("HEADER"), is(true));
			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("header"), is(false));
		}

		@Test
		void shouldContainActualKeyInKeySet() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.keySet().contains("Header"), is(true));
			assertThat(map.keySet().contains("HEADER"), is(false));
		}

		@Test
		void shouldReturnFalseWhenRemovingUnknownKeyFromKeySet() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.keySet().remove("OTHER"), is(false));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldClearThroughKeySet() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.keySet().clear();

			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldTraverseKeySetWithSpliteratorAndForEach() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			long spliteratorCount = StreamSupport.stream(map.keySet().spliterator(), false).count();
			assertThat(spliteratorCount, is(2L));

			List<String> keys = new ArrayList<>();
			map.keySet().forEach(keys::add);

			assertThat(keys, contains("First", "Second"));
		}

		@Test
		void shouldContainValueInValuesCollection() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.values().contains(42), is(true));
			assertThat(map.values().contains(99), is(false));
		}

		@Test
		void shouldExposeValuesCollectionSize() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			assertThat(map.values().size(), is(2));
		}

		@Test
		void shouldTraverseValuesWithSpliteratorAndForEach() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			long spliteratorCount = StreamSupport.stream(map.values().spliterator(), false).count();
			assertThat(spliteratorCount, is(2L));

			List<Integer> values = new ArrayList<>();
			map.values().forEach(values::add);

			assertThat(values, contains(1, 2));
		}

		@Test
		void shouldReuseCachedCollectionViews() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.keySet(), sameInstance(map.keySet()));
			assertThat(map.values(), sameInstance(map.values()));
			assertThat(map.entrySet(), sameInstance(map.entrySet()));
		}

		@Test
		void shouldCompareKeySetWithItsDelegateSemantics() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();
			map.put("Header", 42);

			Set<String> keySet = map.keySet();

			assertThat(keySet.equals(keySet), is(true));
			assertThat(keySet.equals(Set.of("Header")), is(true));
			assertThat(keySet.equals(Set.of("Other")), is(false));
			assertThat(keySet.hashCode(), is(Set.of("Header").hashCode()));
		}

		@Test
		void shouldCompareEntrySetWithItsDelegateSemantics() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();
			map.put("Header", 42);

			Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
			Set<Map.Entry<String, Integer>> expected = Set.of(Map.entry("Header", 42));

			assertThat(entrySet.equals(entrySet), is(true));
			assertThat(entrySet.equals(expected), is(true));
			assertThat(entrySet.equals(Set.of(Map.entry("Other", 42))), is(false));
			assertThat(entrySet.hashCode(), is(expected.hashCode()));
		}

		@Test
		void shouldClearBothMapsThroughCollectionViews() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			map.values().clear();

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("FIRST"), is(false));

			map.put("FIRST", 3);

			assertThat(map.get("first"), is(3));
		}

		@Test
		void shouldKeepIndexConsistentAfterKeySetRetainAll() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);
			map.put("Third", 3);

			map.keySet().retainAll(Set.of("Second"));

			assertThat(map.keySet().toArray(), arrayContaining("Second"));
			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.containsKey("THIRD"), is(false));

			map.put("FIRST", 4);

			assertThat(map.keySet().toArray(), arrayContaining("Second", "FIRST"));
		}

		@Test
		void shouldKeepIndexConsistentAfterValuesRemoveAll() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);
			map.put("Third", 3);

			map.values().removeAll(List.of(1, 3));

			assertThat(map.keySet().toArray(), arrayContaining("Second"));
			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.containsKey("THIRD"), is(false));
		}

		@Test
		void shouldKeepIndexConsistentAfterEntrySetRemoveIf() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);
			map.put("Third", 3);

			map.entrySet().removeIf(entry -> entry.getValue() > 1);

			assertThat(map.keySet().toArray(), arrayContaining("First"));
			assertThat(map.containsKey("SECOND"), is(false));
			assertThat(map.containsKey("THIRD"), is(false));
		}
	}

	@Nested
	class IteratorTests {

		@Test
		void shouldKeepIndexConsistentAfterIteratorRemoval() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			Iterator<String> iterator = map.keySet().iterator();

			assertThat(iterator.next(), is("First"));
			iterator.remove();

			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.size(), is(1));

			map.put("FIRST", 3);

			assertThat(map.keySet().toArray(), arrayContaining("Second", "FIRST"));
		}

		@Test
		void shouldKeepIndexConsistentAfterValuesIteratorRemoval() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			Iterator<Integer> iterator = map.values().iterator();

			assertThat(iterator.next(), is(1));
			iterator.remove();

			assertThat(map.containsKey("first"), is(false));
			assertThat(map.size(), is(1));
		}

		@Test
		void shouldKeepIndexConsistentAfterEntryIteratorRemoval() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();

			assertThat(iterator.next().getKey(), is("First"));
			iterator.remove();

			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.size(), is(1));
		}

		@Test
		void shouldKeepIndexConsistentAfterRemoveIf() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);
			map.put("Third", 3);

			map.values().removeIf(value -> value % 2 != 0);

			assertThat(map.keySet().toArray(), arrayContaining("Second"));
			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.containsKey("THIRD"), is(false));
		}

		@Test
		void shouldRejectRepeatedIteratorRemoval() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			Iterator<String> iterator = map.keySet().iterator();

			assertThrows(IllegalStateException.class, iterator::remove);

			iterator.next();
			iterator.remove();

			assertThrows(IllegalStateException.class, iterator::remove);

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("HEADER"), is(false));
		}
	}

	@Nested
	class PutIfAbsentTests {

		@Test
		void shouldPreserveExistingCasingWithPutIfAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.putIfAbsent("HEADER", 99), is(42));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldPreserveKeyCasingWhenReplacingNullWithPutIfAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);
			map.putIfAbsent("HEADER", 42);

			assertThat(map.keySet().toArray(), arrayContaining("Header"));
			assertThat(map.get("header"), is(42));
			assertThat(map.size(), is(1));
		}

		@Test
		void shouldInsertAbsentKeyWithPutIfAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertNull(map.putIfAbsent("Header", 42));

			assertThat(map.get("HEADER"), is(42));
		}
	}

	@Nested
	class MapApiTests {

		@Test
		void shouldReportContainedValues() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.containsValue(42), is(true));
			assertThat(map.containsValue(99), is(false));
		}

		@Test
		void shouldForEachEntries() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			List<String> keys = new ArrayList<>();
			map.forEach((key, value) -> keys.add(key));

			assertThat(keys, contains("First", "Second"));
		}

		@Test
		void shouldDelegateToStringToInternalMap() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.toString(), is("{Header=42}"));
		}
	}

	@Nested
	class ComputeIfAbsentTests {

		@Test
		void shouldComputeValueOnlyWhenAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.computeIfAbsent("HEADER", key -> 99), is(42));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));

			assertThat(map.computeIfAbsent("New", key -> 100), is(100));
			assertThat(map.get("NEW"), is(100));
		}

		@Test
		void shouldNotInsertWhenMappingFunctionReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertNull(map.computeIfAbsent("Header", key -> null));

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("HEADER"), is(false));
		}

		@Test
		void shouldPreserveCasingWhenComputingAbsentNullValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.computeIfAbsent("HEADER", key -> 42), is(42));

			assertThat(map.keySet().toArray(), arrayContaining("Header"));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldNotInvokeMappingFunctionForExistingValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();
			AtomicInteger invocations = new AtomicInteger();

			map.put("Header", 42);

			assertThat(map.computeIfAbsent("HEADER", key -> {
				invocations.incrementAndGet();
				return 99;
			}), is(42));

			assertThat(invocations.get(), is(0));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldNotChangeMapWhenComputeIfAbsentThrows() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertThrows(IllegalStateException.class, () -> map.computeIfAbsent("Header", key -> {
				throw new IllegalStateException("Mapping failed");
			}));

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("HEADER"), is(false));

			map.put("HEADER", 42);

			assertThat(map.keySet().toArray(), arrayContaining("HEADER"));
		}
	}

	@Nested
	class ComputeTests {

		@Test
		void shouldPreserveExistingCasingWhenComputingValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.compute("HEADER", (key, value) -> value + 1), is(43));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
			assertThat(map.get("header"), is(43));
		}

		@Test
		void shouldRemoveExistingEntryWhenComputeReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertNull(map.compute("HEADER", (key, value) -> null));

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("header"), is(false));

			map.put("HEADER", 99);

			assertThat(map.keySet().toArray(), arrayContaining("HEADER"));
		}

		@Test
		void shouldRemoveNullValuedEntryWhenComputeReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertNull(map.compute("HEADER", (key, value) -> null));

			assertThat(map.containsKey("header"), is(false));
			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldInsertMissingEntryWithCompute() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertThat(map.compute("Header", (key, value) -> 42), is(42));

			assertThat(map.get("HEADER"), is(42));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldNotInsertWhenComputeReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertNull(map.compute("Header", (key, value) -> null));

			assertThat(map.isEmpty(), is(true));
			assertThat(map.containsKey("HEADER"), is(false));
		}

		@Test
		void shouldNotChangeMapWhenComputeThrows() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThrows(IllegalStateException.class, () -> map.compute("HEADER", (key, value) -> {
				throw new IllegalStateException("Computation failed");
			}));

			assertThat(map.get("header"), is(42));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}
	}

	@Nested
	class ComputeIfPresentTests {

		@Test
		void shouldPreserveCasingWhenComputingPresentValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.computeIfPresent("HEADER", (key, value) -> value + 1), is(43));

			assertThat(map.get("header"), is(43));
			assertThat(map.size(), is(1));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldReturnNullWhenKeyIsAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertNull(map.computeIfPresent("Header", (key, value) -> value + 1));
			assertThat(map.isEmpty(), is(true));
		}

		@Test
		void shouldReturnNullWhenStoredValueIsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertNull(map.computeIfPresent("HEADER", (key, value) -> value));
			assertThat(map.containsKey("header"), is(true));
		}

		@Test
		void shouldRemoveEntryWhenRemappingReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertNull(map.computeIfPresent("HEADER", (key, value) -> null));

			assertThat(map.isEmpty(), is(true));
		}
	}

	@Nested
	class MergeTests {

		@Test
		void shouldPreserveCasingWhenMergingValues() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.merge("HEADER", 8, Integer::sum), is(50));

			assertThat(map.get("header"), is(50));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldInsertValueWhenKeyIsAbsent() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertThat(map.merge("Header", 42, Integer::sum), is(42));
			assertThat(map.get("HEADER"), is(42));
		}

		@Test
		void shouldUseIncomingValueWhenExistingValueIsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", null);

			assertThat(map.merge("HEADER", 42, Integer::sum), is(42));
			assertThat(map.get("header"), is(42));
		}

		@Test
		void shouldRemoveEntryWhenMergeRemappingReturnsNull() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertNull(map.merge("HEADER", 8, (left, right) -> null));

			assertThat(map.isEmpty(), is(true));
		}
	}

	@Nested
	class ReplaceTests {

		@Test
		void shouldPreserveCasingWhenReplacingValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.replace("HEADER", 99), is(42));

			assertThat(map.get("header"), is(99));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldPreserveCasingWhenConditionallyReplacingValue() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.replace("HEADER", 42, 99), is(true));

			assertThat(map.get("header"), is(99));
			assertThat(map.keySet().toArray(), arrayContaining("Header"));
		}

		@Test
		void shouldPreserveKeysWhenReplacingAllValues() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("First", 1);
			map.put("Second", 2);

			map.replaceAll((key, value) -> value * 10);

			assertThat(map.keySet().toArray(), arrayContaining("First", "Second"));
			assertThat(map.get("FIRST"), is(10));
			assertThat(map.get("SECOND"), is(20));
		}

		@Test
		void shouldReturnNullWhenReplacingMissingKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertNull(map.replace("Header", 42));
		}

		@Test
		void shouldReturnFalseWhenConditionallyReplacingMissingKey() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			assertThat(map.replace("Header", 42, 99), is(false));
		}

		@Test
		void shouldReturnFalseWhenOldValueDoesNotMatchOnConditionalReplace() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.replace("HEADER", 99, 100), is(false));
			assertThat(map.get("header"), is(42));
		}
	}

	@Nested
	class LocaleTests {

		@Test
		void shouldPreserveLocaleDuringNormalization() {
			CaseInsensitiveLinkedHashMap<Integer> map =
					new CaseInsensitiveLinkedHashMap<>(Locale.forLanguageTag("tr-TR"));

			map.put("I", 42);

			assertThat(map.get("ı"), is(42));
			assertThat(map.containsKey("i"), is(false));
		}
	}

	@Nested
	class RemoveEldestEntryTests {

		@Test
		void shouldEvictEldestEntryAndUpdateIndex() {
			CaseInsensitiveLinkedHashMap<Integer> map =
					new CaseInsensitiveLinkedHashMap<>() {
						@Override
						protected boolean removeEldestEntry(final Map.Entry<String, Integer> eldest) {
							return size() > 2;
						}
					};

			map.put("First", 1);
			map.put("Second", 2);
			map.put("Third", 3);

			assertThat(map.size(), is(2));
			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.keySet().toArray(), arrayContaining("Second", "Third"));

			map.put("FIRST", 4);

			assertThat(map.keySet().toArray(), arrayContaining("Third", "FIRST"));
		}

		@Test
		void shouldKeepIndexConsistentWhenComputeTriggersEviction() {
			CaseInsensitiveLinkedHashMap<Integer> map =
					new CaseInsensitiveLinkedHashMap<>() {
						@Override
						protected boolean removeEldestEntry(final Map.Entry<String, Integer> eldest) {
							return size() > 1;
						}
					};

			map.computeIfAbsent("First", key -> 1);
			map.computeIfAbsent("Second", key -> 2);

			assertThat(map.containsKey("FIRST"), is(false));
			assertThat(map.containsKey("SECOND"), is(true));
			assertThat(map.size(), is(1));
		}
	}

	@Nested
	class CloneTests {

		@Test
		void shouldCloneIndependently() throws CloneNotSupportedException {
			CaseInsensitiveLinkedHashMap<Integer> original = new CaseInsensitiveLinkedHashMap<>();

			original.put("Header", 42);

			CaseInsensitiveLinkedHashMap<Integer> clone = original.clone();

			clone.put("HEADER", 99);

			assertThat(original.get("header"), is(42));
			assertThat(clone.get("header"), is(99));
			assertThat(original.keySet().toArray(), arrayContaining("Header"));
			assertThat(clone.keySet().toArray(), arrayContaining("HEADER"));
		}

		@Test
		void shouldKeepOriginalMapIndexIndependentAfterCloneEviction() throws CloneNotSupportedException {
			CaseInsensitiveLinkedHashMap<Integer> original =
					new CaseInsensitiveLinkedHashMap<>() {
						@Override
						protected boolean removeEldestEntry(final Map.Entry<String, Integer> eldest) {
							return size() > 2;
						}
					};

			original.put("First", 1);
			original.put("Second", 2);

			CaseInsensitiveLinkedHashMap<Integer> clone = original.clone();

			clone.put("Third", 3);

			assertThat(original.size(), is(2));
			assertThat(original.containsKey("FIRST"), is(true));
			assertThat(original.get("first"), is(1));
			assertThat(original.keySet().toArray(), arrayContaining("First", "Second"));

			assertThat(clone.size(), is(2));
			assertThat(clone.containsKey("FIRST"), is(false));
			assertThat(clone.keySet().toArray(), arrayContaining("Second", "Third"));
		}
	}

	@Nested
	class EqualsAndHashCodeTests {

		@Test
		void shouldCompareActualKeysWhenCheckingEquality() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();
			Map<String, Integer> ordinary = new HashMap<>();

			map.put("Header", 42);
			ordinary.put("header", 42);

			assertThat(map.equals(ordinary), is(false));
		}

		@Test
		void shouldHaveConsistentEqualityAndHashCode() {
			CaseInsensitiveLinkedHashMap<Integer> first = new CaseInsensitiveLinkedHashMap<>();
			CaseInsensitiveLinkedHashMap<Integer> second = new CaseInsensitiveLinkedHashMap<>();

			first.put("Header", 42);
			second.put("Header", 42);

			assertThat(first.equals(second), is(true));
			assertThat(second.equals(first), is(true));
			assertThat(first.hashCode(), is(second.hashCode()));
		}

		@Test
		void shouldPreserveActualKeyCasingForEquality() {
			CaseInsensitiveLinkedHashMap<Integer> first = new CaseInsensitiveLinkedHashMap<>();
			CaseInsensitiveLinkedHashMap<Integer> second = new CaseInsensitiveLinkedHashMap<>();

			first.put("Header", 42);
			second.put("header", 42);

			assertThat(first.equals(second), is(false));
			assertThat(second.equals(first), is(false));
		}

		@Test
		void shouldHaveSameHashCodeAsEquivalentOrdinaryMap() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();
			Map<String, Integer> ordinary = new HashMap<>();

			map.put("Header", 42);
			ordinary.put("Header", 42);

			assertThat(map.equals(ordinary), is(true));
			assertThat(map.hashCode(), is(ordinary.hashCode()));
		}

		@Test
		void shouldBeEqualToItself() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.equals(map), is(true));
		}

		@Test
		@SuppressWarnings("unlikely-arg-type")
		void shouldNotEqualUnrelatedObject() {
			CaseInsensitiveLinkedHashMap<Integer> map = new CaseInsensitiveLinkedHashMap<>();

			map.put("Header", 42);

			assertThat(map.equals("not-a-map"), is(false));
		}
	}
}
