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
package org.morphix.convert.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ExtendedField;

/**
 * Test class for {@link ConversionStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConversionStrategyTest {

	public static class A {
		String x;

		public String getY() {
			return x;
		}
	}

	public static class B extends A {
		int x;

		public int getX() {
			return x;
		}
	}

	public static class C {
		// empty
	}

	public static class TestStrategy implements ConversionStrategy {
		@Override
		public <T> ExtendedField find(final T source, final List<ExtendedField> fields, final String sourceFieldName) {
			return null;
		}
	}

	@Test
	void shouldFindAllFields() {
		TestStrategy strategy = new TestStrategy();
		A a = new A();

		List<ExtendedField> fields = strategy.findAll(a);

		List<String> names = fields.stream().map(ExtendedField::getName).toList();
		assertThat(names, hasSize(2));
		assertThat(names, containsInAnyOrder("x", "y"));
	}

	@Test
	void shouldFindAllFieldsInHierarchy() {
		TestStrategy strategy = new TestStrategy();
		A a = new B();

		List<ExtendedField> fields = strategy.findAll(a);

		List<String> names = fields.stream().map(ExtendedField::getName).toList();
		assertThat(names, hasSize(2));
		assertThat(names, containsInAnyOrder("x", "y"));
	}

	@Test
	void shouldNotFindAnyFieldsInEmptyClass() {
		TestStrategy strategy = new TestStrategy();
		C c = new C();

		List<ExtendedField> fields = strategy.findAll(c);

		List<String> names = fields.stream().map(ExtendedField::getName).toList();
		assertThat(names, hasSize(0));
	}

}
