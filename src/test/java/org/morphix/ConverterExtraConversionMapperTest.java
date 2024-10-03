package org.morphix;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.function.ExtraConvertFunction.map;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;

/**
 * Tests extra conversion lambdas with commutative mapper.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterExtraConversionMapperTest {

	private static final String USER_NAME = "userName";
	private static final String NICK_NAME = "nickname";
	private static final String FULL_NAME = "fullName";

	public static class Source {

		@Expandable
		String nickname;

		@Expandable
		String fullName;

		String userName;

		void setNickname() {
			this.nickname = ConverterExtraConversionMapperTest.NICK_NAME;
		}

		void setUserName() {
			this.userName = ConverterExtraConversionMapperTest.USER_NAME;
		}

		public void setFullName() {
			this.fullName = ConverterExtraConversionMapperTest.FULL_NAME;
		}

		String getUserName() {
			return userName;
		}
	}

	public static class Destination {

		String nickname;

		String fullName;

		String name;

		public void setName(final String name) {
			this.name = name;
		}

		String getNickname() {
			return nickname;
		}

		public String getName() {
			return name;
		}

		public String getFullName() {
			return fullName;
		}
	}

	@Test
	void shouldConvertExtraFieldsWithCommutativeMapper() {
		Source s = new Source();
		s.setUserName();

		Destination d = convertFrom(s, Destination::new, (src, dst) -> map(src::getUserName, dst::setName));

		assertThat(d.getName(), equalTo(USER_NAME));

		d = convertFrom(s, Destination::new, (src, dst) -> map(dst::setName, src::getUserName));

		assertThat(d.getName(), equalTo(USER_NAME));
	}

	@Test
	void shouldConvertExtraFieldsWithCommutativeMapperAndNoExpandableFields() {
		Source s = new Source();
		s.setUserName();
		s.setNickname();

		Destination d = convertFrom(s, Destination::new, (src, dst) -> {
			map(src::getUserName, dst::setName);
		}, Collections.emptyList());

		assertThat(d.getName(), equalTo(USER_NAME));
		assertNull(d.getNickname());

		d = convertFrom(s, Destination::new, (src, dst) -> {
			map(dst::setName, src::getUserName);
		}, (List<String>) null);

		assertThat(d.getName(), equalTo(USER_NAME));
		assertThat(d.getNickname(), equalTo(NICK_NAME));
	}

	@Test
	void shouldConvertExtraFieldsWithCommutativeMapperAndExpandableFields() {
		Source s = new Source();
		s.setUserName();
		s.setNickname();
		s.setFullName();

		Destination d = convertFrom(s, Destination::new, (src, dst) -> map(src::getUserName, dst::setName), singletonList("nickname"));

		assertThat(d.getName(), equalTo(USER_NAME));
		assertThat(d.getNickname(), equalTo(NICK_NAME));
		assertNull(d.getFullName());
	}
}
