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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.morphix.convert.Conversions.convertFrom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.annotation.Expandable;
import org.morphix.convert.extras.SimpleConverters;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.function.InstanceFunction;

/**
 * Test class for {@link Conversions#convertFrom(Object, InstanceFunction, List, SimpleConverter)}.
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithSimpleConvertMethodAndExpandableFieldsTest {

	private static final String PREFIX = "Converted ";
	private static final int SIZE = 5;

	static class A {
		int x;

		A(final int x) {
			this.x = x;
		}
	}

	static class B {
		String x;
	}

	static class Src {

		@Expandable
		List<A> expandAs;

		@Expandable
		List<A> notExpandAs;

		List<A> getExpandAs() {
			return expandAs;
		}

		List<A> getNotExpandAs() {
			return notExpandAs;
		}
	}

	static class Dst {

		List<B> expandAs;

		List<B> notExpandAs;

		List<B> getExpandAs() {
			return expandAs;
		}

		List<B> getNotExpandAs() {
			return notExpandAs;
		}
	}

	private static B convertAtoB(final A a) {
		B b = new B();
		b.x = PREFIX + a.x;
		return b;
	}

	@Test
	void shouldConvertExpandableFieldsWithSimpleConvertFunction() {
		Src src = new Src();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < SIZE; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i));
		}

		List<String> expandedFields = Collections.singletonList("expandAs");
		Dst dst = convertFrom(src, Dst::new, expandedFields,
				FromIterableWithSimpleConvertMethodAndExpandableFieldsTest::convertAtoB);

		for (int i = 0; i < SIZE; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
		}
		assertThat(dst.notExpandAs, hasSize(0));
	}

	@Test
	void shouldConvertWithNoExtraConvertFunctionWithExpandableFieldsAndSimpleConverter() {
		Src src = new Src();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < SIZE; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i));
		}

		List<String> expandedFields = Collections.singletonList("expandAs");
		Dst dst = convertFrom(src, Dst::new, ConvertFunction.empty(), expandedFields,
				SimpleConverters.of(FromIterableWithSimpleConvertMethodAndExpandableFieldsTest::convertAtoB));

		for (int i = 0; i < SIZE; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
		}
		assertThat(dst.notExpandAs, hasSize(0));
	}

	@Test
	void shouldConvertAllExpandableFieldsWithSimpleConvertFunction() {
		Src src = new Src();
		src.expandAs = new ArrayList<>();
		src.notExpandAs = new ArrayList<>();
		for (int i = 0; i < SIZE; ++i) {
			src.expandAs.add(new A(i));
			src.notExpandAs.add(new A(i + SIZE));
		}

		List<String> expandedFields = List.of("expandAs", "notExpandAs");
		Dst dst = convertFrom(src, Dst::new, expandedFields,
				FromIterableWithSimpleConvertMethodAndExpandableFieldsTest::convertAtoB);

		for (int i = 0; i < SIZE; ++i) {
			assertThat(dst.expandAs.get(i).x, equalTo(PREFIX + src.expandAs.get(i).x));
			assertThat(dst.notExpandAs.get(i).x, equalTo(PREFIX + (src.notExpandAs.get(i).x)));
		}
	}

}
