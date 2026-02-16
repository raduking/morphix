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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.convert.Conversions.convertFromIterable;
import static org.morphix.convert.FieldHandlerResult.CONVERTED;
import static org.morphix.convert.FieldHandlerResult.SKIP;
import static org.morphix.lang.function.InstanceFunction.to;
import static org.morphix.reflection.ExtendedField.of;

import java.io.Serial;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;
import org.morphix.convert.ObjectConverter;

/**
 * Test class for {@link AnyToAny}.
 *
 * @author Radu Sebastian LAZIN
 */
class AnyToAnyTest {

	public static final int TEST_INT = 11;
	public static final String TEST_STRING_1 = "aaa";
	public static final String TEST_STRING_2 = "bbb";

	public static class A {
		int x;
	}

	public static class B {
		int x;
	}

	public static class X {
		A a;
	}

	public static class Y {
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
	void shouldConvertInnerObjects() {
		X x = createX(TEST_INT);

		Y y = convertFrom(x, Y::new);

		assertThat(y.a.x, equalTo(TEST_INT));
	}

	@Test
	void shouldConvertInnerObjectsInIterables() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		List<Y> listY = convertFromIterable(listX, Y::new);

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i));
		}
	}

	@Test
	void shouldConvertInnerObjectsInIterablesWithConverterForExtraConversions() {
		List<X> listX = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			listX.add(createX(TEST_INT + i));
		}

		ObjectConverter<X, Y> converter = new ObjectConverter<>() {
			@Serial
			private static final long serialVersionUID = 4502979412265507678L;

			@Override
			public void convert(final X source, final Y destination) {
				destination.a = convertFrom(source.a, B::new);
				destination.a.x++;
			}
		};

		List<Y> listY;

		listY = convertFromIterable(listX, converter::convert, Y::new);
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertFromIterable(listX, src -> converter.convert(src, Y::new));
		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}

		// equivalent construct
		listY = convertFromIterable(listX, Y::new, (source, destination) -> {
			destination.a = convertFrom(source.a, B::new);
			destination.a.x++;
		});

		for (int i = 0; i < 3; ++i) {
			assertThat(listY.get(i).a.x, equalTo(TEST_INT + i + 1));
		}
	}

	@Test
	void shouldSkipNullSourceValues() {
		X x = new X();
		x.a = null;

		Y y = convertFrom(x, Y::new);

		assertThat(y.a, equalTo(null));
	}

	public static class S1 {
		Iterable<String> a;
	}

	public static class D1 {
		A a;
	}

	@Test
	void shouldSkipIterablesInSource() {
		S1 src = new S1();
		src.a = List.of(TEST_STRING_1, TEST_STRING_2);

		D1 dst = convertFrom(src, D1::new);

		assertThat(dst.a, equalTo(null));
	}

	public static class S2 {
		A a;
	}

	public static class D2 {
		Iterable<String> a;
	}

	@Test
	void shouldSkipIterablesInDestination() {
		S2 src = new S2();
		src.a = new A();

		D2 dst = convertFrom(src, D2::new);

		assertThat(dst.a, equalTo(null));
	}

	@Test
	void shouldConvertToDestinationIfDestinationHasNonNullObjects() {
		X x = new X();
		x.a = new A();
		x.a.x = TEST_INT;

		Y y = new Y();
		y.a = new B();
		y.a.x = 1;

		convertFrom(x, to(y));

		assertThat(y.a.x, equalTo(TEST_INT));
	}

	public static class C {
		String x;
	}

	public static class D {
		Integer x;
	}

	public static class SrcC {
		C a;
	}

	public static class DstD {
		D a;
	}

	@Test
	void shouldReturnAsNotHandledIfSourceIsNull() throws Exception {
		SrcC src = new SrcC();
		src.a = null;
		DstD dst = new DstD();

		Field sField = SrcC.class.getDeclaredField("a");
		Field dField = DstD.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAny().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(SKIP));
	}

	@Test
	void shouldReturnAsHandledIfConversionIsPerformed() throws Exception {
		SrcC src = new SrcC();
		C a = new C();
		a.x = "1";
		src.a = a;
		DstD dst = new DstD();

		Field sField = SrcC.class.getDeclaredField("a");
		Field dField = DstD.class.getDeclaredField("a");

		FieldHandlerResult result = new AnyToAny().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(CONVERTED));
	}

}
