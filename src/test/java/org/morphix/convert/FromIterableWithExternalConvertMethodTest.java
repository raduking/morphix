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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.IterableConversions.convertIterable;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.function.ConvertFunction;
import org.morphix.convert.function.InstanceConvertFunction;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.function.InstanceFunction;

/**
 * Test class for {@link IterableConversions#convertIterable(Iterable, InstanceConvertFunction, InstanceFunction)}
 * {@link IterableConversions#convertIterable(Iterable, SimpleConverter)}
 * {@link IterableConversions#convertIterable(Iterable, InstanceFunction, ConvertFunction)}
 *
 * @author Radu Sebastian LAZIN
 */
class FromIterableWithExternalConvertMethodTest {

	public static final int TEST_INT = 11;

	static class A {
		int x;
	}

	static class B {
		int x;
	}

	static class X {
		A a;
	}

	static class Y {
		B a;
	}

	private static X createX(final int i) {
		A a = new A();
		a.x = i;
		X x = new X();
		x.a = a;
		return x;
	}

	@Test
	void shouldConvertInnerObjectsInIterablesWithConverterForExtraConversions() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		ObjectConverter<X, Y> converter = new ObjectConverter<>() {
			@Serial
			private static final long serialVersionUID = 134724497607973054L;

			@Override
			public void convert(final X source, final Y destination) {
				destination.a = convertFrom(source.a, B::new);
				destination.a.x++;
			}
		};

		List<Y> listY;

		listY = convertIterable(listX, converter::convert, Y::new).toList();
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertIterable(listX, src -> converter.convert(src, Y::new)).toList();
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertIterable(listX, Y::new, (source, destination) -> {
			destination.a = convertFrom(source.a, B::new);
			destination.a.x++;
		}).toList();

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}
	}

}
