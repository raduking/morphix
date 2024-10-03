package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.Converted.convert;
import static org.morphix.function.ExtraConvertFunction.map;
import static org.morphix.function.ExtraConvertFunction.mapNonNull;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Src;

/**
 * Test class for {@link Src} annotation.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterMapperTest {

	public static class A {

		String s;
		String t;

		public String getS() {
			return s;
		}

		public String getT() {
			return t;
		}
	}

	public static class B {

		String i;
		String j;

		public void setI(final String i) {
			this.i = i;
		}

		public void setJ(final String j) {
			this.j = j;
		}
	}

	@Test
	void shouldFindTheSourceFieldMultipleExtraConvertFunctions() {
		A a = new A();
		a.s = "13";
		a.t = "17";

		B b = convert(a)
				.with((final A s, final B d) -> {
					map(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					map(s::getT, d::setJ);
				})
				.to(B::new);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}

	@Test
	void shouldMapNonNullFieldsExtraConvertFunctions() {
		A a = new A();
		a.s = "13";
		a.t = "17";

		B b = new B();

		b = convert(a)
				.with((final A s, final B d) -> {
					mapNonNull(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					mapNonNull(d::setJ, s::getT);
				})
				.to(b);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}

	@Test
	void shouldNotMapNullFieldsExtraConvertFunctions() {
		A a = new A();

		B b = new B();
		b.i = "13";
		b.j = "17";

		b = convert(a)
				.with((final A s, final B d) -> {
					mapNonNull(s::getS, d::setI);
				})
				.with((final A s, final B d) -> {
					mapNonNull(d::setJ, s::getT);
				})
				.to(b);

		assertThat(b.i, equalTo("13"));
		assertThat(b.j, equalTo("17"));
	}
}
