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
import static org.morphix.lang.function.InstanceFunction.to;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.convert.function.Mappers;

/**
 * Test class for conversion methods inside classes.
 *
 * @author Radu Sebastian LAZIN
 */
class ConversionMethodsTest {

	public static class SrcUser {

		private String someId;
		private String y;

		private SrcUser user;

		public String getSomeId() {
			return someId;
		}

		public void setSomeId(final String id) {
			this.someId = id;
		}

		public String getY() {
			return y;
		}

		public void setY(final String siteId) {
			this.y = siteId;
		}

		public SrcUser getUser() {
			return user;
		}

		public void setUser(final SrcUser user) {
			this.user = user;
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
			return Objects.equals(this.someId, that.someId)
					&& Objects.equals(this.y, that.y)
					&& Objects.equals(this.user, that.user);
		}

		@Override
		public int hashCode() {
			return Objects.hash(someId, y, user);
		}

		@Override
		public String toString() {
			return "{\n" +
					"someId:" + someId + "\n" +
					"y:" + y + "\n" +
					user + "\n" +
					"}";
		}
	}

	public static class DstUser {

		private String id;
		private String y;
		private DstUser user;

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

		public DstUser getUser() {
			return user;
		}

		public void setUser(final DstUser user) {
			this.user = user;
		}

		public void set(final SrcUser srcUser) {
			if (null == srcUser) {
				return;
			}
			DstUser dstUser = convertFromSrcUser(srcUser);
			Conversions.copyFrom(dstUser, to(this));
		}

		public DstUser convertFromSrcUser(final SrcUser srcUser) {
			return convertFrom(srcUser, DstUser::new, (src, dst) -> {
				Mappers.mapNonNull(src::getSomeId, dst::setId);
			}, this::convertFromSrcUser);
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
			DstUser that = (DstUser) o;
			return Objects.equals(this.id, that.id)
					&& Objects.equals(this.y, that.y)
					&& Objects.equals(this.user, that.user);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, y, user);
		}

		@Override
		public String toString() {
			return "{\n" +
					"id:" + id + "\n" +
					"y:" + y + "\n" +
					user + "\n" +
					"}";
		}
	}

	@Test
	void shouldConvertWithObjectConversionMethod() {
		SrcUser srcUser = new SrcUser();
		srcUser.someId = "11";
		srcUser.y = "13";

		SrcUser user = new SrcUser();
		user.someId = "17";
		user.y = "19";

		srcUser.setUser(user);

		DstUser dstUser = new DstUser();
		dstUser.set(srcUser);

		assertThat(dstUser.id, equalTo("11"));
		assertThat(dstUser.y, equalTo("13"));
		assertThat(dstUser.user.id, equalTo("17"));
		assertThat(dstUser.user.y, equalTo("19"));
	}

	@Test
	void shouldConvertWithObjectConversionMethodWithNullInnerUser() {
		SrcUser srcUser = new SrcUser();
		srcUser.someId = "11";
		srcUser.y = "13";

		DstUser dstUser = new DstUser();
		dstUser.set(srcUser);

		assertThat(dstUser.id, equalTo("11"));
		assertThat(dstUser.y, equalTo("13"));
		assertThat(dstUser.user, equalTo(null));
	}
}
