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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.function.Consumers;

/**
 * Test class for {@link UUIDIds}.
 *
 * @author Radu Sebastian LAZIN
 */
class UUIDIdsTest {

	private static final String BIG_INTEGER_STRING = "1257028108907581903326148321977";
	private static final BigInteger BIG_INTEGER = new BigInteger(BIG_INTEGER_STRING);
	private static final String UUID_STRING = "0000000f-ddad-47f7-a5b4-0906b77c62b9";

	@Test
	void shouldSwitchIdToUUID() {
		String uuid = UUIDIds.toUUIDString(BIG_INTEGER);

		assertThat(uuid, equalTo(UUID_STRING));
	}

	@ParameterizedTest
	@MethodSource("provideValidStringUUIDs")
	void shouldReturnTrueOnValidUUID(final String uuid) {
		assertTrue(UUIDIds.isUUID(uuid));
	}

	private static Stream<String> provideValidStringUUIDs() {
		return Stream.of(
				"0b00a00f-ddad-47f7-a5b4-0906b77c62b9",
				"0000000f-ddad-47f7-a5b4-0906b77c62b9");
	}

	@ParameterizedTest
	@MethodSource("provideInvalidStringUUIDs")
	void shouldReturnFalseOnInvalidUUID(final String uuid) {
		assertFalse(UUIDIds.isUUID(uuid));
	}

	private static Stream<String> provideInvalidStringUUIDs() {
		return Stream.of(
				"sb00a00f-ddad-47f7-a5b4-0906b77c62b9",
				"sb00a00f0ddad-47f7-a5b4-0906b77c62b9",
				"sb00a00f-ddad047f7-a5b4-0906b77c62b9",
				"sb00a00f-ddad-47f70a5b4-0906b77c62b9",
				"sb00a00f-ddad-47f7-a5b400906b77c62b9",
				null,
				"");
	}

	@Test
	void shouldTeturnTrueOnValidBigInteger() {
		assertTrue(UUIDIds.isBigInteger(BIG_INTEGER_STRING));
		assertTrue(UUIDIds.isBigInteger("-" + BIG_INTEGER_STRING));
		assertTrue(UUIDIds.isBigInteger("0"));
	}

	@Test
	void shouldReturnFalseOnInvalidBigInteger() {
		assertFalse(UUIDIds.isBigInteger("1234a"));
		assertFalse(UUIDIds.isBigInteger("12-34"));
		assertFalse(UUIDIds.isBigInteger("-"));
		assertFalse(UUIDIds.isBigInteger(null));
		assertFalse(UUIDIds.isBigInteger(""));
	}

	@Test
	void shouldGenerateEntityId() {
		BigInteger id = new BigInteger("1257028108907581903326148321977");

		Entity entity = new Entity();
		entity.setUuid(UUIDIds.toUUIDString(id));

		UUIDIds.generateEntityId(entity);

		assertThat(entity.getId(), equalTo(id));
	}

	@Test
	void shouldNotOverwriteExistingId() {
		BigInteger id = new BigInteger("1257028108907581903326148321977");

		Entity entity = new Entity();
		entity.setId(BigInteger.TWO);
		entity.setUuid(UUIDIds.toUUIDString(id));

		UUIDIds.generateEntityId(entity);

		assertThat(entity.getId(), equalTo(BigInteger.TWO));
	}

	@Test
	void shouldGenerateUUID() {
		UUID uuid = UUIDIds.generateUUID();

		assertNotNull(uuid);
	}

	@Test
	void shouldGenerateUUIDString() {
		String uuid = UUIDIds.generateUUIDString();

		assertNotNull(uuid);
		assertTrue(UUIDIds.isUUID(uuid));
	}

	@Test
	void shouldGenerateUUIDStringWithoutDashes() {
		String uuid = UUIDIds.generateUUIDString(true);

		assertNotNull(uuid);
		assertFalse(uuid.contains("-"));
	}

	@Test
	void shouldReturnBigIntegerFromAnyStringNumber() {
		BigInteger value = UUIDIds.toBigInteger("1", Consumers.noConsumer());

		assertThat(value, equalTo(BigInteger.valueOf(1L)));
	}

	@Test
	void shouldReturnBigIntegerFromAnyStringUUID() {
		BigInteger bi = UUIDIds.toBigInteger(UUID_STRING, Consumers.noConsumer());

		assertThat(bi, equalTo(BIG_INTEGER));
	}

	@Test
	void shouldReturnNullOnToBigIntegerWhenParameterIsNull() {
		BigInteger bi = UUIDIds.toBigInteger((String) null);

		assertNull(bi);
	}

	static class Entity {

		private BigInteger id;

		private String uuid;

		public BigInteger getId() {
			return id;
		}

		public void setId(final BigInteger id) {
			this.id = id;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}

	}
}
