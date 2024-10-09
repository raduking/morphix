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
package org.morphix.convert.handler;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.FieldHandlerResult.BREAK;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ExtendedField.of;

import java.io.Serial;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.ObjectConverterException;

/**
 * Test class for {@link MapToMap}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapToMapTest {

	public static class Src {
		public Integer s;

		public Src(final Integer s) {
			this.s = s;
		}

		public Integer getS() {
			return s;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Src)) {
				return false;
			}
			return Objects.equals(s, ((Src) obj).s);
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	public static class Dst {
		public String s;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Dst)) {
				return false;
			}
			return Objects.equals(s, ((Dst) obj).s);
		}

		@Override
		public int hashCode() {
			return s == null ? super.hashCode() : s.hashCode();
		}
	}

	public static class Source {
		Map<Integer, Src> m;
	}

	public static class Destination {
		Map<String, Dst> m;

		public Map<String, Dst> getM() {
			return m;
		}
	}

	@Test
	void shouldConvertMapToMap() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		Destination destination = convertFrom(source, Destination::new);

		assertNotNull(destination.m);
		assertThat(destination.m.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = destination.m.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

	public static class DestinationNoGetter {
		Map<String, Dst> m;
	}

	@Test
	void shouldNotConvertMapIfDestinationDoesNotHaveGetterMethodOnMap() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		DestinationNoGetter destination = convertFrom(source, DestinationNoGetter::new);

		assertNull(destination.m);
	}

	public static class DestinationNoProperGetter<T> {
		Map<String, T> m;

		public Map<String, T> getM() {
			return m;
		}
	}

	@Test
	void shouldFailConvertMapIfDestinationDoesNotHaveProperGetterMethodOnMap() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		assertThrows(ObjectConverterException.class, () -> convertFrom(source, DestinationNoProperGetter::new));
	}

	public static class DestinationHashMap {
		HashMap<String, Dst> m;

		public HashMap<String, Dst> getM() {
			return m;
		}
	}

	@Test
	void shouldConvertMapToHashMap() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		DestinationHashMap destination = convertFrom(source, DestinationHashMap::new);

		assertNotNull(destination.m);
		assertThat(destination.m.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = destination.m.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

	public static class SourceList {
		List<Src> m;
	}

	@Test
	void shouldNotConvertListToMap() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).collect(Collectors.toList());

		SourceList source = new SourceList();
		source.m = list;

		Destination destination = convertFrom(source, Destination::new);

		assertNull(destination.m);
	}

	public static class DestinationCustomMap {
		AbstractMap<String, Dst> m;

		public AbstractMap<String, Dst> getM() {
			return m;
		}
	}

	@Test
	void shouldNotConvertToAbstractMaps() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		DestinationCustomMap destination = convertFrom(source, DestinationCustomMap::new);

		assertNull(destination.m);
	}

	public static class CustomMap extends HashMap<String, Dst> {
		@Serial
		private static final long serialVersionUID = 9066161785998311006L;

		@SuppressWarnings("unused")
		public CustomMap(final String s) {
			// empty
		}
	}

	public static class DestinationCustomMap2 {
		CustomMap m;
	}

	@Test
	void shouldNotConvertToMapsWithNoConstructor() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		DestinationCustomMap2 destination = convertFrom(source, DestinationCustomMap2::new);

		assertNull(destination.m);
	}

	public static class SrcWithEmptyMap {
		Map<Integer, Integer> a;

		public void setA(final Map<Integer, Integer> a) {
			this.a = a;
		}
	}

	public static class DstWithEmptyMap {
		Map<String, String> a;

		public Map<String, String> getA() {
			return a == null ? emptyMap() : a;
		}
	}

	@Test
	void shouldConvertIfDestinationMapIsEmptyMap() {
		SrcWithEmptyMap src = new SrcWithEmptyMap();
		src.a = singletonMap(10, 15);

		DstWithEmptyMap dst = convertFrom(src, DstWithEmptyMap::new);

		assertThat(dst.getA().keySet(), contains("10"));
		assertThat(dst.getA().values(), contains("15"));
	}

	@Test
	void shouldReturnAsHandledIfConversionWasSuccessful() throws Exception {
		Source src = new Source();
		src.m = new HashMap<>();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("m");
		Field dField = Destination.class.getDeclaredField("m");

		FieldHandlerResult result = new MapToMap().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

	@Test
	void shouldReturnAsHandledIfTheValueIsNull() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("m");
		Field dField = Destination.class.getDeclaredField("m");

		FieldHandlerResult result = new MapToMap().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

	public static class Destination2 {
		Map<String, Dst> m;
	}

	@Test
	void shouldReturnAsHandledIfMapHasNoGetter() throws Exception {
		Source src = new Source();
		src.m = new HashMap<>();
		Destination2 dst = new Destination2();

		Field sField = Source.class.getDeclaredField("m");
		Field dField = Destination2.class.getDeclaredField("m");

		FieldHandlerResult result = new MapToMap().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(BREAK));
	}

}
