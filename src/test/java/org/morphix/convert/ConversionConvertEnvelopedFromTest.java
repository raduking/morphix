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

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericType;

/**
 * Test class for:
 *
 * <ul>
 * <li>{@link Conversions#convertEnvelopedFrom(Object, Type, Configuration)}</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
class ConversionConvertEnvelopedFromTest {

	private static final String STRING_THIRTEEN = "13";
	private static final String STRING_ELEVEN = "11";
	private static final Long LONG_THIRTEEN = 13L;

	public static class SrcUser {

		private String id;
		private String y;

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getY() {
			return y;
		}

		public void setY(final String y) {
			this.y = y;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (null == o) {
				return false;
			}
			if (getClass() != o.getClass()) {
				return false;
			}
			SrcUser that = (SrcUser) o;
			return Objects.equals(this.id, that.id)
					&& Objects.equals(this.y, that.y);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, y);
		}

		@Override
		public String toString() {
			return "{\n" +
					"id:" + id + "\n" +
					"y:" + y + "\n" +
					"}";
		}
	}

	public static class DstUser {

		private String id;
		private Long y;

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public Long getY() {
			return y;
		}

		public void setY(final Long y) {
			this.y = y;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (null == o) {
				return false;
			}
			if (getClass() != o.getClass()) {
				return false;
			}
			DstUser dstUser = (DstUser) o;
			return Objects.equals(this.id, dstUser.id)
					&& Objects.equals(this.y, dstUser.y);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, y);
		}

		@Override
		public String toString() {
			return "{\n" +
					"id:" + id + "\n" +
					"y:" + y + "\n" +
					"}";
		}
	}

	@Test
	void shouldConvertWithObjectConversionMethod() {
		SrcUser srcUser = new SrcUser();
		srcUser.id = STRING_ELEVEN;
		srcUser.y = STRING_THIRTEEN;

		GenericType listType = GenericType.of(List.class, GenericType.Arguments.of(DstUser.class));
		List<DstUser> dstUser = Conversions.convertEnvelopedFrom(srcUser, listType, Configuration.defaults());

		assertThat(dstUser.getFirst().id, equalTo(STRING_ELEVEN));
		assertThat(dstUser.getFirst().y, equalTo(LONG_THIRTEEN));
	}

	@Test
	void shouldConvertWithObjectConversionMethodWithNullInnerUser() {
		SrcUser srcUser = new SrcUser();
		srcUser.id = STRING_ELEVEN;
		srcUser.y = STRING_THIRTEEN;

		GenericType listType = GenericType.of(List.class,
				GenericType.Arguments.of(GenericType.of(List.class,
						GenericType.Arguments.of(DstUser.class))));
		List<List<DstUser>> dstUser = Conversions.convertEnvelopedFrom(srcUser, listType, Configuration.defaults());

		assertThat(dstUser.getFirst().getFirst().id, equalTo(STRING_ELEVEN));
		assertThat(dstUser.getFirst().getFirst().y, equalTo(LONG_THIRTEEN));
	}

}
