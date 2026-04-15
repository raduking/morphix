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

import static org.morphix.convert.Conversions.convertFrom;
import static org.morphix.lang.function.InstanceFunction.to;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

import org.morphix.convert.Conversions.ConvertFields;
import org.morphix.reflection.Constructors;

/**
 * This class contains helper methods for ID generation and conversion. This may be used for generating UUIDs,
 * converting UUIDs to BigIntegers and vice versa, and generating database IDs for entities based on their UUIDs.
 *
 * @author Radu Sebastian LAZIN
 */
public class Ids {

	/**
	 * The character used to separate the parts of a UUID string. This is a constant that can be used when manipulating UUID
	 * strings.
	 */
	public static final char DASH = '-';

	/**
	 * The mask used to ensure that a BigInteger value fits within the 128 bits of a UUID. This is calculated as 2^128 - 1,
	 * which is the maximum value that can be represented in 128 bits. This mask can be used to normalize BigInteger values
	 * when converting them to UUIDs, ensuring that any excess bits are discarded.
	 */
	protected static final BigInteger UUID_MASK = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);

	/**
	 * Enum for UUID string styles. Multiple styles can be applied to the same UUID string. The styles are applied in the
	 * order they are provided.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum UUIDStyle {

		/**
		 * UUID string style without dashes.
		 * <p>
		 * Example: "123e4567e89b12d3a456426614174000"
		 */
		NO_DASHES,

		/**
		 * UUID string style with uppercase letters.
		 * <p>
		 * Example: "123E4567-E89B-12D3-A456-426614174000"
		 */
		UPPERCASE,

		/**
		 * UUID string style with lowercase letters.
		 * <p>
		 * Example: "123e4567-e89b-12d3-a456-426614174000"
		 */
		LOWERCASE
	}

	/**
	 * Private constructor.
	 */
	private Ids() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Generates a UUID.
	 *
	 * @return UUID
	 */
	public static UUID generateUUID() {
		return UUID.randomUUID();
	}

	/**
	 * Generates a {@link BigInteger} that represents a UUID. The generated UUID is converted to a BigInteger using the
	 * {@link #toBigInteger(UUID)} method.
	 *
	 * @return big integer representing the generated UUID
	 */
	public static BigInteger generateBigInteger() {
		return toBigInteger(generateUUID());
	}

	/**
	 * Generates a UUID String.
	 *
	 * @param styles optional styles to apply to the generated UUID string
	 * @return UUID String
	 */
	public static String generateUUIDString(final UUIDStyle... styles) {
		String uuid = generateUUID().toString();
		if (JavaArrays.isEmpty(styles)) {
			return uuid;
		}
		for (UUIDStyle style : styles) {
			uuid = switch (style) {
				case NO_DASHES -> uuid.replace("-", "");
				case UPPERCASE -> uuid.toUpperCase();
				case LOWERCASE -> uuid.toLowerCase();
			};
		}
		return uuid;
	}

	/**
	 * Transforms a UUID string to {@link BigInteger}.
	 *
	 * @param uuid UUID to transform
	 * @return big integer representing the UUID
	 */
	public static BigInteger toBigInteger(final String uuid) {
		return Nullables.apply(uuid, v -> toBigInteger(UUID.fromString(v)), Nullables.supplyNull());
	}

	/**
	 * Transforms a string to a {@link BigInteger}. The string can either be a {@link UUID} or a {@link BigInteger}.
	 *
	 * @param str string to transform
	 * @param wrongFormatConsumer consumer to call if the string is not a valid UUID or BigInteger
	 * @return big integer representing the UUID
	 */
	public static BigInteger toBigInteger(final String str, final Consumer<String> wrongFormatConsumer) {
		if (isUUID(str)) {
			return toBigInteger(UUID.fromString(str));
		}
		if (isBigInteger(str)) {
			return new BigInteger(str);
		}
		if (null != wrongFormatConsumer) {
			wrongFormatConsumer.accept(str);
		}
		return null;
	}

	/**
	 * Transforms a {@link UUID} to {@link BigInteger}.
	 *
	 * @param uuid UUID to transform
	 * @return big integer representing the UUID
	 */
	public static BigInteger toBigInteger(final UUID uuid) {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return new BigInteger(1, buffer.array());
	}

	/**
	 * Transforms a {@link BigInteger} to a {@link UUID} string.
	 *
	 * @param value big integer to transform
	 * @return string representing the UUID
	 */
	public static String toUUIDString(final BigInteger value) {
		return Nullables.whenNotNull(value)
				.andNotNull(Ids::toUUID)
				.thenReturn(UUID::toString);
	}

	/**
	 * Transforms a {@link BigInteger} to a {@link UUID}.
	 *
	 * @param value big integer to transform
	 * @return the UUID
	 */
	public static UUID toUUID(final BigInteger value) {
		BigInteger normalizedValue = value;
		if (value.bitLength() > 128) {
			normalizedValue = normalizedValue.and(UUID_MASK);
		}
		byte[] bytes = normalizedValue.toByteArray();
		byte[] uuidBytes = new byte[16];

		int start = Math.max(0, bytes.length - 16);
		int length = Math.min(bytes.length, 16);

		System.arraycopy(bytes, start, uuidBytes, 16 - length, length);

		long hi = 0;
		long lo = 0;
		for (int i = 0; i < 8; ++i) {
			hi = (hi << 8) | (uuidBytes[i] & 0xFF);
			lo = (lo << 8) | (uuidBytes[8 + i] & 0xFF);
		}
		return new UUID(hi, lo);
	}

	/**
	 * Generates the database ID for a given entity from its UUID, provided that the entity doesn't have one already.
	 *
	 * @param entity the entity for which and id must be generated
	 * @param <T> any type of entity that has the following fields: id, uuid.
	 */
	public static <T> void generateEntityId(final T entity) {
		class IdExtractor {
			private BigInteger id;
			private String uuid;
		}
		IdExtractor idExtractor = convertFrom(entity, IdExtractor::new);
		if (null == idExtractor.id && null != idExtractor.uuid) {
			convertFrom(new ConvertFields() {
				@SuppressWarnings("unused")
				BigInteger id = toBigInteger(idExtractor.uuid);
			}, to(entity));
		}
	}

	/**
	 * Fast UUID check without exceptions. Returns true if the string is a UUID false otherwise. It checks a generic UUID
	 * version. The UUID can be uppercase or lowercase but must contain dashes in the correct positions. The method does not
	 * check for the version or variant of the UUID.
	 *
	 * @param uuid UUID as String
	 * @return true if the string is a UUID false otherwise
	 */
	public static boolean isUUID(final String uuid) {
		if (null == uuid) {
			return false;
		}
		int len = uuid.length();
		if (len != 36
				|| uuid.charAt(8) != DASH
				|| uuid.charAt(13) != DASH
				|| uuid.charAt(18) != DASH
				|| uuid.charAt(23) != DASH) {
			return false;
		}
		for (int i = 0; i < len; ++i) {
			if (i == 8 || i == 13 || i == 18 || i == 23) {
				continue;
			}
			char c = uuid.charAt(i);
			if (!isUUIDCharacter(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the given character is a valid UUID (hexadecimal) character (0-9, A-F, a-f), false otherwise.
	 *
	 * @param c character to check
	 * @return true if the character is a valid UUID character, false otherwise
	 */
	public static boolean isUUIDCharacter(final char c) {
		return (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'F')
				|| (c >= 'a' && c <= 'f');
	}

	/**
	 * Fast {@link BigInteger} check without exceptions. Returns true if the string is a {@link BigInteger}, false
	 * otherwise.
	 *
	 * @param str string to check
	 * @return true if the string is a BigInteger, false otherwise
	 */
	public static boolean isBigInteger(final String str) {
		if (null == str) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; ++i) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
}
