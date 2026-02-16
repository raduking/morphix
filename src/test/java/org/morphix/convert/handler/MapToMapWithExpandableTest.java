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
package org.morphix.convert.handler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.morphix.convert.Conversions.convertFrom;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.morphix.convert.annotation.Expandable;

/**
 * Test class for map conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class MapToMapWithExpandableTest {

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
		@Expandable
		Map<Integer, Src> m;
	}

	public static class Destination {
		Map<String, Dst> m;

		public Map<String, Dst> getM() {
			return m;
		}
	}

	@Test
	void shouldExpandField() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		Destination destination = convertFrom(source, Destination::new, singletonList("m"));

		assertNotNull(destination.m);
		assertThat(destination.m.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = destination.m.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

	@Test
	void shouldNotExpandFieldIfNotInExpandableList() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		Destination destination = convertFrom(source, Destination::new, singletonList("x"));

		assertThat(destination.m.entrySet(), hasSize(0));
	}

	@Test
	void shouldNotExpandField() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source source = new Source();
		source.m = srcMap;

		Destination destination = convertFrom(source, Destination::new, emptyList());

		assertThat(destination.m.entrySet(), hasSize(0));
	}

	public static class Source1 {
		Map<Integer, Src> m;
	}

	@Test
	void shouldExpandFieldIfItsNotAnExpandableField() {
		final int size = 10;
		List<Src> list = IntStream.range(0, size).boxed().map(Src::new).toList();
		Map<Integer, Src> srcMap = list.stream().collect(Collectors.toMap(Src::getS, Function.identity()));

		Source1 source = new Source1();
		source.m = srcMap;

		Destination destination = convertFrom(source, Destination::new, singletonList("m"));

		assertNotNull(destination.m);
		assertThat(destination.m.entrySet(), hasSize(size));

		for (Src src : list) {
			String s = String.valueOf(src.getS());
			Dst dst = destination.m.get(s);
			assertThat(dst.s, equalTo(s));
		}
	}

}
