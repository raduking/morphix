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
package org.morphix.convert.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.morphix.convert.Conversions.convertFrom;

import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.ExtendedField;

/**
 * Test class deep name path strategy.
 *
 * @author Radu Sebastian LAZIN
 */
class FinderDeepNamePathStrategyTest {

	private static final String TEST_STREET = "Str. Cimitirului";
	private static final String TEST_CITY = "Smallville";

	private static final String TEST_LAST_NAME = "Acula";
	private static final String TEST_FIRST_NAME = "Doctor";

	public static class Order {
		public Customer customer;
		public Address billingAddress;
	}

	public static class Customer {
		public Name name;
	}

	public static class Name {
		public String firstName;
		public String lastName;
	}

	public static class Address {
		public String street;
		public String city;
	}

	public static class OrderDto {
		public String customerFirstName;
		public String customerLastName;
		public String billingStreet;
		public String billingCity;
	}

	@Disabled("Deep conversion is not yet implemented")
	@Test
	void shouldDeepConvert() {
		Address address = new Address();
		address.street = TEST_STREET;
		address.city = TEST_CITY;

		Name name = new Name();
		name.firstName = TEST_FIRST_NAME;
		name.lastName = TEST_LAST_NAME;

		Customer customer = new Customer();
		customer.name = name;

		Order order = new Order();
		order.customer = customer;
		order.billingAddress = address;

		OrderDto orderDto = convertFrom(order, OrderDto::new);

		assertThat(orderDto.billingCity, equalTo(order.billingAddress.city));
		assertThat(orderDto.billingStreet, equalTo(order.billingAddress.street));
		assertThat(orderDto.customerFirstName, equalTo(order.customer.name.firstName));
		assertThat(orderDto.customerLastName, equalTo(order.customer.name.lastName));
	}

	@Test
	void shouldDeepConvertWithDeepNamePath() {
		FinderDeepNamePathStrategy strategy = new FinderDeepNamePathStrategy();

		ExtendedField fop = strategy.find(strategy, Collections.emptyList(), "x");
		assertThat(fop.getObject(), nullValue());
	}

}
