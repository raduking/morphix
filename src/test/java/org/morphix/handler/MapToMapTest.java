package org.morphix.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.FieldHandlerResult.BREAK;
import static org.morphix.FieldHandlerResult.CONVERTED;
import static org.morphix.reflection.ConverterField.of;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.FieldHandlerResult;

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
