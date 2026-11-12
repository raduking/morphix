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
package org.morphix.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SerializableHolder}.
 *
 * @author Radu Sebastian LAZIN
 */
class SerializableHolderTest {

	private static final String VALUE = "value";

	@Test
	void shouldSerializeAndDeserializeValue() throws Exception {
		SerializableHolder<String> holder = new SerializableHolder<>(VALUE);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
			output.writeObject(holder);
		}

		SerializableHolder<?> copy;
		try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			copy = (SerializableHolder<?>) input.readObject();
		}

		assertThat(copy.getValue(), equalTo(VALUE));
	}

	@Test
	void shouldHaveHolderParity() {
		SerializableHolder<String> holder = SerializableHolder.empty();
		holder.set(VALUE);

		assertThat(holder.get(), equalTo(VALUE));
		assertThat(holder, equalTo(SerializableHolder.of(VALUE)));
		assertThat(holder.hashCode(), equalTo(SerializableHolder.of(VALUE).hashCode()));
	}

	@Test
	void shouldConstructWithoutAValue() {
		SerializableHolder<String> holder = new SerializableHolder<>();

		assertThat(holder.getValue(), nullValue());
		assertThat(holder.get(), nullValue());
	}

	@Test
	void shouldEqualSameInstance() {
		SerializableHolder<String> holder = SerializableHolder.of(VALUE);

		assertThat(holder, equalTo(holder));
	}

	@Test
	void shouldEqualWhenBothValuesAreNull() {
		assertThat(SerializableHolder.empty(), equalTo(new SerializableHolder<>()));
	}

	@Test
	void shouldNotEqualWhenOnlyOneValueIsNull() {
		assertThat(SerializableHolder.empty(), not(equalTo(SerializableHolder.of(VALUE))));
	}

	@Test
	void shouldNotEqualWhenValuesDiffer() {
		assertThat(SerializableHolder.of(VALUE), not(equalTo(SerializableHolder.of("other"))));
	}

	@Test
	void shouldNotEqualWhenOtherIsNotASerializableHolder() {
		SerializableHolder<String> holder = SerializableHolder.of(VALUE);

		assertThat(holder, not(equalTo(VALUE)));
	}

}
