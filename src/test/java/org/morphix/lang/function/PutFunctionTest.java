package org.morphix.lang.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link PutFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class PutFunctionTest {

	private static final String KEY1 = "key1";
	private static final String KEY2 = "key2";

	@Test
	void shouldPutValueInMap() {
		PutFunction<String, Integer> putFunction = PutFunction.of();

		Map<String, Integer> map = new HashMap<>();
		putFunction.put(map, KEY1, 100);

		assertThat(map.get(KEY1), equalTo(100));
	}

	@Test
	void shouldPutValueInMapWithCustomMap() {
		PutFunction<String, Integer> putFunction = PutFunction.of();

		Map<String, Integer> map = new ConcurrentHashMap<>();
		putFunction.put(map, KEY1, 200);

		assertThat(map.get(KEY1), equalTo(200));
	}

	@Test
	void shouldComposePutFunctions() {
		PutFunction<String, Integer> putFunction1 = (map, key, value) -> map.put(key, value * 2);
		PutFunction<String, Integer> putFunction2 = (map, key, value) -> map.put(key, value + 100);

		PutFunction<String, Integer> composedPutFunction = PutFunction.compose(putFunction1, putFunction2);

		Map<String, Integer> map = new HashMap<>();
		composedPutFunction.put(map, KEY1, 300);

		assertThat(map.get(KEY1), equalTo(400));
	}

	@Test
	void shouldComposeMultiplePutFunctions() {
		PutFunction<String, Integer> putFunction1 = (map, key, value) -> map.put(key, value * 2);
		PutFunction<String, Integer> putFunction2 = (map, key, value) -> map.put(key, value + 100);
		PutFunction<String, Integer> putFunction3 = (map, key, value) -> map.put(key, value + 50);

		PutFunction<String, Integer> composedPutFunction = putFunction1
				.andThen(putFunction2)
				.andThen(putFunction3);

		Map<String, Integer> map = new HashMap<>();
		composedPutFunction.put(map, KEY1, 300);

		assertThat(map.get(KEY1), equalTo(350));
	}

	@Test
	void shouldPutIfNotNullValueInMap() {
		PutFunction<String, Integer> putFunction = PutFunction.ifNotNullValue();

		Map<String, Integer> map = new HashMap<>();
		putFunction.put(map, KEY1, null);
		putFunction.put(map, KEY2, 400);

		assertThat(map.containsKey(KEY1), equalTo(false));
		assertThat(map.get(KEY2), equalTo(400));
	}

	@Test
	void shouldPutIfNotNullKeyInMap() {
		PutFunction<String, Integer> putFunction = PutFunction.ifNotNullKey();

		Map<String, Integer> map = new HashMap<>();
		putFunction.put(map, null, 500);
		putFunction.put(map, KEY2, 600);

		assertThat(map.containsKey(null), equalTo(false));
		assertThat(map.get(KEY2), equalTo(600));
	}

	@Test
	void shouldPutIfNotNullKeyAndValueInMap() {
		PutFunction<String, Integer> putFunction = PutFunction.ifNotNullKeyAndValue();

		Map<String, Integer> map = new HashMap<>();
		putFunction.put(map, null, 700);
		putFunction.put(map, KEY1, null);
		putFunction.put(map, KEY2, 800);

		assertThat(map.containsKey(null), equalTo(false));
		assertThat(map.containsKey(KEY1), equalTo(false));
		assertThat(map.get(KEY2), equalTo(800));
	}
}
