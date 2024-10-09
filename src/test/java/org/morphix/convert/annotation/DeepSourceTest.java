/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.convert.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Test class for annotated conversions.
 *
 * @author Radu Sebastian LAZIN
 */
class DeepSourceTest {

	public static final String TEST_STRING = "13";
	public static final Long TEST_LONG = Long.valueOf(TEST_STRING);

	public static class A {
		String s;
		String b;
	}

	public static class B {
		A a;
	}

	public static class Destination {
		@Src("a.s")
		Long l;

		@Src("a.b")
		Long x;
	}

	@Test
	void shouldDeepConvertWithAnnotation() {
		A a = new A();
		a.s = TEST_STRING;
		a.b = TEST_STRING;
		B b = new B();
		b.a = a;

		Destination d = convertFrom(b, Destination::new);

		assertThat(d.l, equalTo(TEST_LONG));
		assertThat(d.x, equalTo(TEST_LONG));
	}

	@Test
	void shouldNotDeepConvertIfTheSourceChainContainsNulls() {
		B b = new B();

		Destination d = convertFrom(b, Destination::new);

		assertThat(d.l, nullValue());
		assertThat(d.x, nullValue());
	}

	public static class AA {
		public Integer x;
	}

	public static class BB {
		public AA aa;
	}

	public static class CC {
		public BB bb;
	}

	public static class DD {
		@Src("bb.aa.x")
		public Long x;
	}

	@Test
	void shouldNotDeepConvertIfTheSourceChainContainsNulls2() {
		CC cc = new CC();

		DD dd = convertFrom(cc, DD::new);

		assertThat(dd.x, nullValue());
	}

	public static class User {
		String name;
	}

	public static class Customer {
		User user;
	}

	public static class CustomerDto {
		String userName;
	}

	@Test
	void shouldDeepConvert() {
		User user = new User();
		user.name = TEST_STRING;
		Customer customer = new Customer();
		customer.user = user;

		CustomerDto userDto = convertFrom(customer, CustomerDto::new);

		assertThat(userDto.userName, equalTo(TEST_STRING));
	}
}
