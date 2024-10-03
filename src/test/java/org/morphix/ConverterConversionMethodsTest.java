package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.function.ExtraConvertFunction.mapNonNull;
import static org.morphix.function.InstanceFunction.to;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for conversion methods inside classes.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterConversionMethodsTest {

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

		public void setUser(final SrcUser drTenant) {
			this.user = drTenant;
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
			SrcUser srcUser = (SrcUser) o;
			return Objects.equals(this.someId, srcUser.someId)
					&& Objects.equals(this.y, srcUser.someId)
					&& Objects.equals(this.user, srcUser.user);
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

		public void setY(final String siteId) {
			this.y = siteId;
		}

		public DstUser getUser() {
			return user;
		}

		public void setUser(final DstUser drTenant) {
			this.user = drTenant;
		}

		public void set(final SrcUser vccTenant) {
			if (null == vccTenant) {
				return;
			}
			DstUser dstUser = convertFromSrcUser(vccTenant);
			Conversion.copyFrom(dstUser, to(this));
		}

		public DstUser convertFromSrcUser(final SrcUser vccGpsTenant) {
			return convertFrom(vccGpsTenant, DstUser::new, (src, dst) -> {
				mapNonNull(src::getSomeId, dst::setId);
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
			DstUser dstUser = (DstUser) o;
			return Objects.equals(this.id, dstUser.id)
					&& Objects.equals(this.y, dstUser.id)
					&& Objects.equals(this.user, dstUser.user);
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
