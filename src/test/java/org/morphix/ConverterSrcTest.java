package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.morphix.Converted.convert;

import org.junit.jupiter.api.Test;
import org.morphix.ConverterSrcTest.A2.A3;
import org.morphix.annotation.From;
import org.morphix.annotation.Src;

/**
 * Test class for {@link Src} annotation.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterSrcTest {

	public static class A {

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B {

		@Src("s")
		Integer i;

		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithAnnotation() {
		A a = new A();
		a.s = "13";

		B b = Converted.convert(a).to(B::new);

		assertThat(b.i, equalTo(13));
	}

	public static class A1 {

		String s;

		public String getS() {
			return s;
		}
	}

	public static class B1 {

		Integer i;

		@Src("s")
		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithAnnotationOnGetter() {
		A1 a = new A1();
		a.s = "13";

		B1 b = convert(a).to(B1::new);

		assertThat(b.i, equalTo(13));
	}

	public static class A2 {

		A3 x;

		public A3 getX() {
			return x;
		}

		public static class A3 {
			String x;
		}
	}

	public static class B2 {

		@Src(from = {
				@From(type = A1.class, path = "s"),
				@From(type = A2.class, path = "x.x")
		})
		Integer i;

		public Integer getI() {
			return i;
		}
	}

	@Test
	void shouldFindTheSourceFieldWithFromAnnotation() {
		A2 a2 = new A2();
		A3 a3 = new A3();
		a3.x = "13";
		a2.x = a3;

		B2 b = convert(a2).to(B2::new);

		assertThat(b.i, equalTo(13));

		A1 a1 = new A1();
		a1.s = "17";
		b = convert(a1).to(B2::new);

		assertThat(b.i, equalTo(17));
	}
}
