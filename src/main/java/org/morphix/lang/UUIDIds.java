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
import java.util.UUID;
import java.util.function.Consumer;

import org.morphix.convert.Conversions.ConvertFields;

/**
 * This class contains helper methods for {@link UUID} and {@link BigInteger} ID generation and conversion.
 *
 * @author Radu Sebastian LAZIN
 */
public class UUIDIds {

	private static final int SIXTY_FOUR = 64;
	private static final BigInteger B = BigInteger.ONE.shiftLeft(SIXTY_FOUR); // 2^64
	private static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);

	/**
	 * Private constructor.
	 */
	private UUIDIds() {
		// empty
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
	 * Generates a UUID String.
	 *
	 * @return UUID String
	 */
	public static String generateUUIDString() {
		return generateUUIDString(false);
	}

	/**
	 * Generates a UUID String.
	 *
	 * @param removeDashes flag to remove the dashes
	 * @return UUID String
	 */
	public static String generateUUIDString(final boolean removeDashes) {
		String uuid = generateUUID().toString();
		if (removeDashes) {
			uuid = uuid.replace("-", "");
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
	 * Transforms a string to a {@link BigInteger}. The string can either be a UUID or a BigInteger.
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
	 * Transforms a UUID to {@link BigInteger}.
	 *
	 * @param uuid UUID to transform
	 * @return big integer representing the UUID
	 */
	public static BigInteger toBigInteger(final UUID uuid) {
		BigInteger lo = BigInteger.valueOf(uuid.getLeastSignificantBits());
		BigInteger hi = BigInteger.valueOf(uuid.getMostSignificantBits());

		// If any of lo/hi parts is negative interpret as unsigned
		if (hi.signum() < 0) {
			hi = hi.add(B);
		}

		if (lo.signum() < 0) {
			lo = lo.add(B);
		}

		return lo.add(hi.shiftLeft(SIXTY_FOUR));
	}

	/**
	 * Transforms a {@link BigInteger} to a UUID string.
	 *
	 * @param value big integer to transform
	 * @return string representing the UUID
	 */
	public static String toUUIDString(final BigInteger value) {
		return Nullables.whenNotNull(value).thenReturn(v -> toUUID(v).toString());
	}

	/**
	 * Transforms a {@link BigInteger} to a UUID.
	 *
	 * @param value big integer to transform
	 * @return the UUID
	 */
	public static UUID toUUID(final BigInteger value) {
		BigInteger[] parts = value.divideAndRemainder(B);
		BigInteger hi = parts[0];
		BigInteger lo = parts[1];

		if (L.compareTo(lo) < 0) {
			lo = lo.subtract(B);
		}

		if (L.compareTo(hi) < 0) {
			hi = hi.subtract(B);
		}

		return new UUID(hi.longValueExact(), lo.longValueExact());
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
		if (idExtractor.id == null) {
			convertFrom(new ConvertFields() {
				@SuppressWarnings("unused")
				BigInteger id = toBigInteger(idExtractor.uuid);
			}, to(entity));
		}
	}

	/**
	 * Fast UUID check without exceptions. Returns true if the string is a UUID false otherwise. It checks a generic UUID
	 * version.
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
				|| uuid.charAt(8) != '-'
				|| uuid.charAt(13) != '-'
				|| uuid.charAt(18) != '-'
				|| uuid.charAt(23) != '-') {
			return false;
		}
		for (int i = 0; i < len; ++i) {
			char c = uuid.charAt(i);
			if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c != '-')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fast BigInteger check without exceptions. Returns true if the string is a BigInteger, false otherwise.
	 *
	 * @param str string to check
	 * @return true if the string is a BigInteger, false otherwise
	 */
	public static boolean isBigInteger(final String str) {
		if (str == null) {
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
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
}
