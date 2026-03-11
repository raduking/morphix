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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.lang.Ids.UUIDStyle;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Ids}.
 *
 * @author Radu Sebastian LAZIN
 */
class IdsTest {

	private static final String BIG_INTEGER_STRING = "1257028108907581903326148321977";
	private static final BigInteger BIG_INTEGER = new BigInteger(BIG_INTEGER_STRING);
	private static final String UUID_STRING = "0000000f-ddad-47f7-a5b4-0906b77c62b9";

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(Ids.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldGenerateUUID() {
		UUID uuid = Ids.generateUUID();

		assertNotNull(uuid);
	}

	@Test
	void shouldGenerateUUIDString() {
		String uuid = Ids.generateUUIDString();

		assertNotNull(uuid);
		assertTrue(Ids.isUUID(uuid));
	}

	@Test
	void shouldGenerateUUIDStringWithoutDashes() {
		String uuid = Ids.generateUUIDString(UUIDStyle.NO_DASHES, UUIDStyle.UPPERCASE);

		assertNotNull(uuid);
		assertFalse(uuid.contains("-"));
		assertFalse(Ids.isUUID(uuid));
		assertTrue(uuid.chars().noneMatch(Character::isLowerCase));
	}

	@Test
	void shouldGenerateUUIDStringWithLowercase() {
		String uuid = Ids.generateUUIDString(UUIDStyle.NO_DASHES, UUIDStyle.LOWERCASE);

		assertNotNull(uuid);
		assertFalse(uuid.contains("-"));
		assertFalse(Ids.isUUID(uuid));
		assertTrue(uuid.chars().noneMatch(Character::isUpperCase));
	}

	@Test
	void shouldGenerateBigInteger() {
		BigInteger bigInteger = Ids.generateBigInteger();

		assertNotNull(bigInteger);
	}

	@Test
	void shouldGenerateBigIntegerAndConvertItToUUID() {
		BigInteger bigInteger = Ids.generateBigInteger();
		String uuid = Ids.toUUIDString(bigInteger);

		assertNotNull(bigInteger);
		assertNotNull(uuid);
		assertTrue(Ids.isUUID(uuid));
	}

	@Nested
	class IsUUIDTests {

		@ParameterizedTest
		@MethodSource("provideValidStringUUIDs")
		void shouldReturnTrueOnValidUUID(final String uuid) {
			assertTrue(Ids.isUUID(uuid));
		}

		private static Stream<String> provideValidStringUUIDs() {
			return Stream.of(
					"0b00a00f-ddad-47f7-a5b4-0906b77c62b9",
					"0000000f-ddad-47f7-a5b4-0906b77c62b9",
					"0000000F-DDAD-47F7-A5B4-0906B77C62B9");
		}

		@ParameterizedTest
		@MethodSource("provideInvalidStringUUIDs")
		void shouldReturnFalseOnInvalidUUID(final String uuid) {
			assertFalse(Ids.isUUID(uuid));
		}

		private static Stream<String> provideInvalidStringUUIDs() {
			return Stream.of(
					"1b00a00f-ddad-47f7-a5b4-0906b77c62bs",
					"sb00a00f-ddad-47f7-a5b4-0906b77c62b9",
					"1b00a00f0ddad-47f7-a5b4-0906b77c62b9",
					"1b00a00f-ddad047f7-a5b4-0906b77c62b9",
					"1b00a00f-ddad-47f70a5b4-0906b77c62b9",
					"1b00a00f-ddad-47f7-a5b400906b77c62b9",
					"SB00A00F-DDAD-47F7-A5B4-0906B77C62B9",
					"1B00A00F-DDAD-47F7-A5B4-0906B77C62BS",
					"0b00a00f ddad 47f7 a5b4 0906b77c62b9",
					"0b00a00f+ddad+47f7+a5b4+0906b77c62b9",
					"0b00a00f;ddad;47f7;a5b4;0906b77c62b9",
					"+b00a00f-ddad-47f70a5b4-0906b77c62b9",
					";b00a00f-ddad-47f70a5b4-0906b77c62b9",
					"[b00a00f-ddad-47f70a5b4-0906b77c62b9",
					" b00a00f-ddad-47f70a5b4-0906b77c62b9",
					null,
					"");
		}
	}

	@Nested
	class IsBigIntegerTests {

		@ParameterizedTest
		@MethodSource("provideValidStringBigIntegers")
		void shouldTeturnTrueOnValidBigInteger(final String bigInteger) {
			assertTrue(Ids.isBigInteger(bigInteger));
		}

		private static Stream<String> provideValidStringBigIntegers() {
			return Stream.of(
					BIG_INTEGER_STRING,
					"-" + BIG_INTEGER_STRING,
					"0");
		}

		@ParameterizedTest
		@MethodSource("provideInvalidStringBigIntegers")
		void shouldReturnFalseOnInvalidBigInteger(final String bigInteger) {
			assertFalse(Ids.isBigInteger(bigInteger));
		}

		private static Stream<String> provideInvalidStringBigIntegers() {
			return Stream.of(
					"1234a",
					"12-34",
					"-",
					null,
					"");
		}
	}

	@Nested
	class IsUUIDCharacterTests {

		@ParameterizedTest
		@MethodSource("provideValidUUIDCharacters")
		void shouldReturnTrueOnValidUUIDCharacter(final char c) {
			assertTrue(Ids.isUUIDCharacter(c));
		}

		private static Stream<Character> provideValidUUIDCharacters() {
			return Stream.concat(
					Stream.concat(
							Stream.iterate('0', c -> c).limit(10),
							Stream.iterate('A', c -> c).limit(6)),
					Stream.iterate('a', c -> c).limit(6));
		}

		@ParameterizedTest
		@MethodSource("provideInvalidUUIDCharacters")
		void shouldReturnFalseOnInvalidUUIDCharacter(final char c) {
			assertFalse(Ids.isUUIDCharacter(c));
		}

		private static Stream<Character> provideInvalidUUIDCharacters() {
			return Stream.concat(
					Stream.concat(
							Stream.iterate((char) 0, c -> c).limit(48),
							Stream.iterate((char) 58, c -> c).limit(7)),
					Stream.concat(
							Stream.iterate((char) 71, c -> c).limit(25),
							Stream.iterate((char) 103, c -> c).limit(25)));
		}
	}

	@Nested
	class GenerateEntityIdTests {

		@Test
		void shouldGenerateEntityId() {
			BigInteger id = new BigInteger("1257028108907581903326148321977");

			Entity entity = new Entity();
			entity.setUuid(Ids.toUUIDString(id));

			Ids.generateEntityId(entity);

			assertThat(entity.getId(), equalTo(id));
		}

		@Test
		void shouldNotOverwriteExistingId() {
			BigInteger id = new BigInteger("1257028108907581903326148321977");

			Entity entity = new Entity();
			entity.setId(BigInteger.TWO);
			entity.setUuid(Ids.toUUIDString(id));

			Ids.generateEntityId(entity);

			assertThat(entity.getId(), equalTo(BigInteger.TWO));
		}

		@Test
		void shouldNotOverwriteExistingIdWhenUUIDIsNull() {
			Entity entity = new Entity();

			entity.setId(BigInteger.TWO);
			Ids.generateEntityId(entity);

			assertThat(entity.getId(), equalTo(BigInteger.TWO));
		}

		@Test
		void shouldDoNothingWhenIdAndUUIDAreNull() {
			Entity entity = new Entity();

			Ids.generateEntityId(entity);

			assertNull(entity.getId());
			assertNull(entity.getUuid());
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

	@Nested
	class ToBigIntegerTests {

		@Test
		void shouldReturnBigIntegerFromAnyStringNumber() {
			BigInteger value = Ids.toBigInteger("1", Consumers.noConsumer());

			assertThat(value, equalTo(BigInteger.valueOf(1L)));
		}

		@Test
		void shouldReturnNullOnToBigIntegerWhenParameterIsNull() {
			BigInteger bi = Ids.toBigInteger((String) null);

			assertNull(bi);
		}

		@Test
		void shouldReturnBigIntegerFromAnyStringUUID() {
			BigInteger bi = Ids.toBigInteger(UUID_STRING, Consumers.noConsumer());

			assertThat(bi, equalTo(BIG_INTEGER));
		}

		@Test
		void shouldReturnNullOnToBigIntegerWhenStringIsNotANumberNorAUUID() {
			BigInteger bi = Ids.toBigInteger("not a number nor a uuid", Consumers.noConsumer());

			assertNull(bi);
		}

		@Test
		void shouldReturnNullOnToBigIntegerWhenStringIsAnInvalidUUID() {
			BigInteger bi = Ids.toBigInteger("0000000f-ddad-47f7-a5b4-0906b77c62bs", Consumers.noConsumer());

			assertNull(bi);
		}

		@Test
		void shouldCallWrongFormatConsumerWhenStringIsNotANumberNorAUUID() {
			String str = "not a number nor a uuid";
			String[] captured = new String[1];

			BigInteger bi = Ids.toBigInteger(str, s -> captured[0] = s);

			assertNull(bi);
			assertThat(captured[0], equalTo(str));
		}

		@Test
		void shouldSkipNullWrongFormatConsumer() {
			BigInteger bi = Ids.toBigInteger("not a number nor a uuid", null);

			assertNull(bi);
		}
	}

	@Nested
	class ToUUIDTests {

		@Test
		void shouldSwitchIdToUUID() {
			String uuid = Ids.toUUIDString(BIG_INTEGER);

			assertThat(uuid, equalTo(UUID_STRING));
		}

		@Test
		void shouldReturnNullOnToUUIDWhenParameterIsNull() {
			String uuid = Ids.toUUIDString((BigInteger) null);

			assertNull(uuid);
		}

		@Test
		void shouldTransformMax128BitBigIntegerToUUID() {
			BigInteger bigInteger = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
			String uuid = Ids.toUUIDString(bigInteger);

			assertThat(uuid, equalTo("ffffffff-ffff-ffff-ffff-ffffffffffff"));
			assertTrue(Ids.isUUID(uuid));
			assertThat(Ids.toBigInteger(uuid), equalTo(bigInteger));
		}

		@Test
		void shouldTransformBigIntegerWithMoreThan128BitsToUUID() {
			BigInteger bigInteger = BigInteger.ONE.shiftLeft(130).subtract(BigInteger.ONE);
			String uuid = Ids.toUUIDString(bigInteger);

			assertThat(uuid, equalTo("ffffffff-ffff-ffff-ffff-ffffffffffff"));
			assertTrue(Ids.isUUID(uuid));
			assertThat(Ids.toBigInteger(uuid), equalTo(bigInteger.and(Ids.UUID_MASK)));
		}
	}
}
