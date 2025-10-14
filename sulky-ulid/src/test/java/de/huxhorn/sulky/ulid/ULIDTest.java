/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2024 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2024 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.ulid;

import de.huxhorn.sulky.junit.JUnitTools;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ULIDTest {

    private static final long PAST_TIMESTAMP = 1_481_195_424_879L;
    private static final String PAST_TIMESTAMP_PART = "01B3F2133F";

    private static final long MAX_TIMESTAMP = 0xFFFF_FFFF_FFFFL;
    private static final String MAX_TIMESTAMP_PART = "7ZZZZZZZZZ";

    private static final String MIN_RANDOM_PART = "0000000000000000";
    private static final String MAX_RANDOM_PART = "ZZZZZZZZZZZZZZZZ";

    private static final char[] CROCKFORD_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private static final byte[] ZERO_BYTES = new byte[16];
    private static final byte[] FULL_BYTES = repeat((byte) 0xFF, 16);
    private static final byte[] PATTERN_BYTES = {
        (byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33,
        (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77,
        (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
        (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF,
    };

    private static final long PATTERN_MOST_SIGNIFICANT_BITS = 0x0011_2233_4455_6677L;
    private static final long PATTERN_LEAST_SIGNIFICANT_BITS = 0x8899_AABB_CCDD_EEFFL;

    private static final long ALL_BITS_SET = 0xFFFF_FFFF_FFFF_FFFFL;

    @ParameterizedTest
    @MethodSource("singleDigitArguments")
    void internalAppendCrockfordSingleDigit(long value, char expected) {
        StringBuilder builder = new StringBuilder();
        ULID.internalAppendCrockford(builder, value, 1);
        assertEquals(String.valueOf(expected), builder.toString());
    }

    private static Stream<Arguments> singleDigitArguments() {
        return LongStream.range(0, CROCKFORD_ALPHABET.length)
            .mapToObj(i -> Arguments.of(i, CROCKFORD_ALPHABET[(int) i]));
    }

    @ParameterizedTest
    @MethodSource("internalAppendCrockfordSamples")
    void internalAppendCrockfordSamples(long value, int length, String expected) {
        StringBuilder builder = new StringBuilder();
        ULID.internalAppendCrockford(builder, value, length);
        assertEquals(expected, builder.toString());
    }

    private static Stream<Arguments> internalAppendCrockfordSamples() {
        return Stream.of(
            Arguments.of(32L, 1, "0"),
            Arguments.of(32L, 2, "10"),
            Arguments.of(0L, 0, ""),
            Arguments.of(0L, 13, "0000000000000"),
            Arguments.of(194L, 2, "62"),
            Arguments.of(45_678L, 4, "1CKE"),
            Arguments.of(393_619L, 4, "C0CK"),
            Arguments.of(398_373L, 4, "C515"),
            Arguments.of(421_562L, 4, "CVNT"),
            Arguments.of(456_789L, 4, "DY2N"),
            Arguments.of(519_571L, 4, "FVCK"),
            Arguments.of(3_838_385_658_376_483L, 11, "3D2ZQ6TVC93"),
            Arguments.of(0x1FL, 1, "Z"),
            Arguments.of(0x1FL << 5, 1, "0"),
            Arguments.of(0x1FL << 5, 2, "Z0"),
            Arguments.of(0x1FL << 10, 2, "00"),
            Arguments.of(0x1FL << 10, 3, "Z00"),
            Arguments.of(0x1FL << 15, 3, "000"),
            Arguments.of(0x1FL << 15, 4, "Z000"),
            Arguments.of(0x1FL << 55, 13, "0Z00000000000"),
            Arguments.of(0x1FL << 60, 13, "F000000000000"),
            Arguments.of(ALL_BITS_SET, 13, "FZZZZZZZZZZZZ"),
            Arguments.of(PAST_TIMESTAMP, 10, PAST_TIMESTAMP_PART),
            Arguments.of(MAX_TIMESTAMP, 10, MAX_TIMESTAMP_PART)
        );
    }

    @ParameterizedTest
    @MethodSource("singleDigitArguments")
    void internalWriteCrockfordSingleDigit(long value, char expected) {
        char[] buffer = initBuffer(1, '#');
        ULID.internalWriteCrockford(buffer, value, 1, 0);
        assertEquals(String.valueOf(expected), new String(buffer));
    }

    @ParameterizedTest
    @MethodSource("internalWriteCrockfordSamples")
    void internalWriteCrockfordSamples(long value, int bufferSize, int length, int offset, String expected) {
        char[] buffer = initBuffer(bufferSize, '#');
        ULID.internalWriteCrockford(buffer, value, length, offset);
        assertEquals(expected, new String(buffer));
    }

    private static Stream<Arguments> internalWriteCrockfordSamples() {
        return Stream.of(
            Arguments.of(0L, 0, 0, 0, ""),
            Arguments.of(0L, 2, 0, 0, "##"),
            Arguments.of(0L, 13, 13, 0, "0000000000000"),
            Arguments.of(194L, 2, 2, 0, "62"),
            Arguments.of(45_678L, 4, 4, 0, "1CKE"),
            Arguments.of(393_619L, 4, 4, 0, "C0CK"),
            Arguments.of(398_373L, 4, 4, 0, "C515"),
            Arguments.of(421_562L, 4, 4, 0, "CVNT"),
            Arguments.of(456_789L, 4, 4, 0, "DY2N"),
            Arguments.of(519_571L, 4, 4, 0, "FVCK"),
            Arguments.of(3_838_385_658_376_483L, 11, 11, 0, "3D2ZQ6TVC93"),
            Arguments.of(0x1FL, 1, 1, 0, "Z"),
            Arguments.of(0x1FL << 5, 1, 1, 0, "0"),
            Arguments.of(0x1FL << 5, 2, 2, 0, "Z0"),
            Arguments.of(0x1FL << 10, 1, 1, 0, "0"),
            Arguments.of(0x1FL << 10, 2, 2, 0, "00"),
            Arguments.of(0x1FL << 10, 3, 3, 0, "Z00"),
            Arguments.of(0x1FL << 15, 3, 3, 0, "000"),
            Arguments.of(0x1FL << 15, 4, 4, 0, "Z000"),
            Arguments.of(0x1FL << 55, 13, 13, 0, "0Z00000000000"),
            Arguments.of(0x1FL << 60, 13, 13, 0, "F000000000000"),
            Arguments.of(ALL_BITS_SET, 13, 13, 0, "FZZZZZZZZZZZZ"),
            Arguments.of(PAST_TIMESTAMP, 10, 10, 0, PAST_TIMESTAMP_PART),
            Arguments.of(MAX_TIMESTAMP, 10, 10, 0, MAX_TIMESTAMP_PART),
            Arguments.of(45_678L, 8, 4, 3, "###1CKE#"),
            Arguments.of(45_678L, 8, 4, 4, "####1CKE")
        );
    }

    @Test
    void internalAppendULIDWithZeroRandom() {
        StubRandom random = StubRandom.fromValues(0L, 0L);
        StringBuilder builder = new StringBuilder();
        ULID.internalAppendULID(builder, PAST_TIMESTAMP, random);
        random.assertExhausted();
        String result = builder.toString();
        assertEquals(26, result.length());
        assertEquals(PAST_TIMESTAMP_PART, result.substring(0, 10));
        assertEquals("0000000000000000", result.substring(10));
    }

    @Test
    void internalAppendULIDWithMinusOneRandom() {
        StubRandom random = StubRandom.fromValues(-1L, -1L);
        StringBuilder builder = new StringBuilder();
        ULID.internalAppendULID(builder, PAST_TIMESTAMP, random);
        random.assertExhausted();
        String result = builder.toString();
        assertEquals(26, result.length());
        assertEquals(PAST_TIMESTAMP_PART, result.substring(0, 10));
        assertEquals("ZZZZZZZZZZZZZZZZ", result.substring(10));
    }

    @Test
    void internalUIDStringWithZeroRandom() {
        StubRandom random = StubRandom.fromValues(0L, 0L);
        String result = ULID.internalUIDString(PAST_TIMESTAMP, random);
        random.assertExhausted();
        assertEquals(26, result.length());
        assertEquals(PAST_TIMESTAMP_PART, result.substring(0, 10));
        assertEquals("0000000000000000", result.substring(10));
    }

    @Test
    void internalUIDStringWithMinusOneRandom() {
        StubRandom random = StubRandom.fromValues(-1L, -1L);
        String result = ULID.internalUIDString(PAST_TIMESTAMP, random);
        random.assertExhausted();
        assertEquals(26, result.length());
        assertEquals(PAST_TIMESTAMP_PART, result.substring(0, 10));
        assertEquals("ZZZZZZZZZZZZZZZZ", result.substring(10));
    }

    @Test
    void nextULIDWithZeroRandom() {
        StubRandom random = StubRandom.fromValues(0L, 0L);
        ULID ulid = new ULID(random);
        String result = ulid.nextULID();
        random.assertExhausted();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("0000000000000000", result.substring(10));
    }

    @Test
    void nextULIDWithMinusOneRandom() {
        StubRandom random = StubRandom.fromValues(-1L, -1L);
        ULID ulid = new ULID(random);
        String result = ulid.nextULID();
        random.assertExhausted();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("ZZZZZZZZZZZZZZZZ", result.substring(10));
    }

    @Test
    void nextULIDWithRealRandomLooksSane() {
        ULID ulid = new ULID();
        String result = ulid.nextULID();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        String randomPart = result.substring(10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertTrue(MIN_RANDOM_PART.compareTo(randomPart) <= 0);
        assertTrue(MAX_RANDOM_PART.compareTo(randomPart) >= 0);
    }

    @Test
    void nextValueWithZeroRandom() {
        StubRandom random = StubRandom.fromValues(0L, 0L);
        ULID ulid = new ULID(random);
        String result = ulid.nextValue().toString();
        random.assertExhausted();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("0000000000000000", result.substring(10));
    }

    @Test
    void nextValueWithMinusOneRandom() {
        StubRandom random = StubRandom.fromValues(-1L, -1L);
        ULID ulid = new ULID(random);
        String result = ulid.nextValue().toString();
        random.assertExhausted();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("ZZZZZZZZZZZZZZZZ", result.substring(10));
    }

    @Test
    void nextValueWithRealRandomLooksSane() {
        ULID ulid = new ULID();
        String result = ulid.nextValue().toString();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        String randomPart = result.substring(10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertTrue(MIN_RANDOM_PART.compareTo(randomPart) <= 0);
        assertTrue(MAX_RANDOM_PART.compareTo(randomPart) >= 0);
    }

    @Test
    void appendULIDWithZeroRandom() {
        StubRandom random = StubRandom.fromValues(0L, 0L);
        ULID ulid = new ULID(random);
        StringBuilder builder = new StringBuilder();
        ulid.appendULID(builder);
        random.assertExhausted();
        String result = builder.toString();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("0000000000000000", result.substring(10));
    }

    @Test
    void appendULIDWithMinusOneRandom() {
        StubRandom random = StubRandom.fromValues(-1L, -1L);
        ULID ulid = new ULID(random);
        StringBuilder builder = new StringBuilder();
        ulid.appendULID(builder);
        random.assertExhausted();
        String result = builder.toString();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertEquals("ZZZZZZZZZZZZZZZZ", result.substring(10));
    }

    @Test
    void appendULIDWithRealRandomLooksSane() {
        ULID ulid = new ULID();
        StringBuilder builder = new StringBuilder();
        ulid.appendULID(builder);
        String result = builder.toString();
        assertEquals(26, result.length());
        String timePart = result.substring(0, 10);
        String randomPart = result.substring(10);
        assertTrue(PAST_TIMESTAMP_PART.compareTo(timePart) < 0);
        assertTrue(MAX_TIMESTAMP_PART.compareTo(timePart) >= 0);
        assertTrue(MIN_RANDOM_PART.compareTo(randomPart) <= 0);
        assertTrue(MAX_RANDOM_PART.compareTo(randomPart) >= 0);
    }

    @Test
    void constructorRejectsNullRandom() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new ULID(null));
        assertEquals("random must not be null!", exception.getMessage());
    }

    @Test
    void appendULIDRejectsNullBuilder() {
        ULID ulid = new ULID();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ulid.appendULID(null));
        assertEquals("stringBuilder must not be null!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("valueToStringArguments")
    void valueToString(long most, long least, String expected) {
        ULID.Value value = new ULID.Value(most, least);
        assertEquals(expected, value.toString());
    }

    @ParameterizedTest
    @MethodSource("internalParseCrockfordArguments")
    void internalParseCrockford(String input, long expected) {
        assertEquals(expected, ULID.internalParseCrockford(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "!", ":", "[", "`"})
    void internalParseCrockfordRejectsInvalidCharacters(String input) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ULID.internalParseCrockford(input));
        assertEquals("Illegal character '" + input + "'!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("parseULIDArguments")
    void parseULID(String ulidString, long most, long least) {
        ULID.Value value = ULID.parseULID(ulidString);
        assertEquals(most, value.getMostSignificantBits());
        assertEquals(least, value.getLeastSignificantBits());
    }

    @ParameterizedTest
    @MethodSource("parseULIDFailsArguments")
    void parseULIDRejectsInvalidValues(String input, String expectedMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ULID.parseULID(input));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void parseULIDRejectsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ULID.parseULID(null));
        assertEquals("ulidString must not be null!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("fromBytesArguments")
    void fromBytes(byte[] data, long most, long least) {
        ULID.Value value = ULID.fromBytes(data);
        assertEquals(most, value.getMostSignificantBits());
        assertEquals(least, value.getLeastSignificantBits());
    }

    @ParameterizedTest
    @MethodSource("fromBytesFailsArguments")
    void fromBytesRejectsInvalidLength(byte[] data) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ULID.fromBytes(data));
        assertEquals("data must be 16 bytes in length!", exception.getMessage());
    }

    @Test
    void fromBytesRejectsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ULID.fromBytes(null));
        assertEquals("data must not be null!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("valueToBytesArguments")
    void valueToBytes(long most, long least, byte[] expected) {
        ULID.Value value = new ULID.Value(most, least);
        assertArrayEquals(expected, value.toBytes());
    }

    private static Stream<Arguments> valueToStringArguments() {
        return Stream.of(
            Arguments.of(0x0L, 0x0L, "00000000000000000000000000"),
            Arguments.of(ALL_BITS_SET, ALL_BITS_SET, "7ZZZZZZZZZZZZZZZZZZZZZZZZZ"),
            Arguments.of(0xFFFF_FFFF_FFFF_0000L, 0x0L, "7ZZZZZZZZZ0000000000000000"),
            Arguments.of(0xFFFFL, ALL_BITS_SET, "0000000000ZZZZZZZZZZZZZZZZ"),
            Arguments.of(0x1L, ALL_BITS_SET, "0000000000000ZZZZZZZZZZZZZ"),
            Arguments.of(0x3L, ALL_BITS_SET, "0000000000001ZZZZZZZZZZZZZ"),
            Arguments.of(0xFFFFL, 0x0L, "0000000000ZZZG000000000000"),
            Arguments.of(0x0886L, 0x4298_E84A_96C6_B9F0L, "0000000000123456789ABCDEFG")
        );
    }

    private static Stream<Arguments> internalParseCrockfordArguments() {
        return Stream.of(
            Arguments.of("0", 0L),
            Arguments.of("O", 0L),
            Arguments.of("o", 0L),
            Arguments.of("1", 1L),
            Arguments.of("i", 1L),
            Arguments.of("I", 1L),
            Arguments.of("l", 1L),
            Arguments.of("L", 1L),
            Arguments.of("Z", 31L),
            Arguments.of("z", 31L)
        );
    }

    @ParameterizedTest
    @MethodSource("compareArguments")
    void compareEqualsAndHashCode(long most1, long least1, long most2, long least2, int expected) {
        ULID.Value value1 = new ULID.Value(most1, least1);
        ULID.Value value2 = new ULID.Value(most2, least2);
        boolean equals12 = value1.equals(value2);
        boolean equals21 = value2.equals(value1);
        int compare12 = value1.compareTo(value2);
        int compare21 = value2.compareTo(value1);
        assertEquals(equals12, equals21);
        assertEquals(compare12, -compare21);
        if (expected == 0) {
            assertEquals(0, compare12);
            assertEquals(value1.hashCode(), value2.hashCode());
            assertTrue(equals12);
        } else {
            assertEquals(expected, compare12);
            assertFalse(equals12);
        }
    }

    @Test
    void valueEqualsItself() {
        ULID.Value value = new ULID.Value(0, 0);
        assertEquals(value, value);
    }

    @Test
    void valueNotEqualToNullOrDifferentType() {
        ULID.Value value = new ULID.Value(0, 0);
        assertNotEquals(null, value);
        assertNotEquals("", value);
    }

    @ParameterizedTest
    @MethodSource("serializableValues")
    void valueSerialization(long most, long least) {
        ULID.Value value = new ULID.Value(most, least);
        assertDoesNotThrow(() -> JUnitTools.testSerialization(value));
    }

    @Test
    void internalUIDStringRejectsOverflowTimestamp() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ULID.internalUIDString(0x0001_0000_0000_0000L, new SecureRandom()));
        assertEquals("ULID does not support timestamps after +10889-08-02T05:31:50.655Z!", exception.getMessage());
    }

    @Test
    void internalAppendULIDRejectsOverflowTimestamp() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ULID.internalAppendULID(new StringBuilder(), 0x0001_0000_0000_0000L, new SecureRandom()));
        assertEquals("ULID does not support timestamps after +10889-08-02T05:31:50.655Z!", exception.getMessage());
    }

    @Test
    void internalNextValueRejectsOverflowTimestamp() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ULID.internalNextValue(0x0001_0000_0000_0000L, new SecureRandom()));
        assertEquals("ULID does not support timestamps after +10889-08-02T05:31:50.655Z!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("incrementArguments")
    void incrementProducesExpectedValue(ULID.Value value, ULID.Value expected) {
        assertEquals(expected, value.increment());
    }

    @ParameterizedTest
    @MethodSource("nextMonotonicArguments")
    void nextMonotonicValue(ULID.Value previous, ULID.Value expected) {
        ULID ulid = new ULID();
        assertEquals(expected, ulid.nextMonotonicValue(previous, 0));
    }

    @Test
    void nextMonotonicValueProducesNewValueForTimestampMismatch() {
        ULID ulid = new ULID();
        ULID.Value previous = new ULID.Value(0, 0);
        ULID.Value next = ulid.nextMonotonicValue(previous, 1);
        assertEquals(1, next.timestamp());
        next = ulid.nextMonotonicValue(previous);
        assertTrue(next.timestamp() > 0);
    }

    @ParameterizedTest
    @MethodSource("nextStrictlyMonotonicArguments")
    void nextStrictlyMonotonicValue(ULID.Value previous, Optional<ULID.Value> expected) {
        ULID ulid = new ULID();
        assertEquals(expected, ulid.nextStrictlyMonotonicValue(previous, 0));
    }

    @Test
    void nextStrictlyMonotonicValueProducesNewValueForTimestampMismatch() {
        ULID ulid = new ULID();
        ULID.Value previous = new ULID.Value(0, 0);
        Optional<ULID.Value> next = ulid.nextStrictlyMonotonicValue(previous, 1);
        assertTrue(next.isPresent());
        assertEquals(1, next.get().timestamp());
        next = ulid.nextStrictlyMonotonicValue(previous);
        assertTrue(next.isPresent());
        assertTrue(next.get().timestamp() > 0);
    }

    @Test
    void nextMonotonicValueRejectsNull() {
        ULID ulid = new ULID();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ulid.nextMonotonicValue(null));
        assertEquals("previousUlid must not be null!", exception.getMessage());
        exception = assertThrows(NullPointerException.class, () -> ulid.nextMonotonicValue(null, 0));
        assertEquals("previousUlid must not be null!", exception.getMessage());
    }

    @Test
    void nextStrictlyMonotonicValueRejectsNull() {
        ULID ulid = new ULID();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> ulid.nextStrictlyMonotonicValue(null));
        assertEquals("previousUlid must not be null!", exception.getMessage());
        exception = assertThrows(NullPointerException.class, () -> ulid.nextStrictlyMonotonicValue(null, 0));
        assertEquals("previousUlid must not be null!", exception.getMessage());
    }

    private static Stream<byte[]> fromBytesFailsArguments() {
        return Stream.of(new byte[15], new byte[17]);
    }

    private static Stream<Arguments> parseULIDArguments() {
        return Stream.of(
            Arguments.of("00000000000000000000000000", 0L, 0L),
            Arguments.of("7ZZZZZZZZZZZZZZZZZZZZZZZZZ", ALL_BITS_SET, ALL_BITS_SET),
            Arguments.of("01B3F2133F0000000000000000", PAST_TIMESTAMP << 16, 0L),
            Arguments.of("01B3F2133FZZZZZZZZZZZZZZZZ", (PAST_TIMESTAMP << 16) | 0xFFFFL, ALL_BITS_SET)
        );
    }

    private static Stream<Arguments> parseULIDFailsArguments() {
        return Stream.of(
            Arguments.of("0000000000000000000000000", "ulidString must be exactly 26 chars long."),
            Arguments.of("000000000000000000000000000", "ulidString must be exactly 26 chars long."),
            Arguments.of("80000000000000000000000000", "ulidString must not exceed '7ZZZZZZZZZZZZZZZZZZZZZZZZZ'!")
        );
    }

    private static Stream<Arguments> fromBytesArguments() {
        return Stream.of(
            Arguments.of(ZERO_BYTES, 0L, 0L),
            Arguments.of(FULL_BYTES, ALL_BITS_SET, ALL_BITS_SET),
            Arguments.of(PATTERN_BYTES, PATTERN_MOST_SIGNIFICANT_BITS, PATTERN_LEAST_SIGNIFICANT_BITS)
        );
    }

    private static Stream<Arguments> valueToBytesArguments() {
        return Stream.of(
            Arguments.of(0L, 0L, ZERO_BYTES),
            Arguments.of(ALL_BITS_SET, ALL_BITS_SET, FULL_BYTES),
            Arguments.of(PATTERN_MOST_SIGNIFICANT_BITS, PATTERN_LEAST_SIGNIFICANT_BITS, PATTERN_BYTES)
        );
    }

    private static Stream<Arguments> compareArguments() {
        return Stream.of(
            Arguments.of(0L, 0L, 0L, 0L, 0),
            Arguments.of(ALL_BITS_SET, ALL_BITS_SET, ALL_BITS_SET, ALL_BITS_SET, 0),
            Arguments.of(PATTERN_MOST_SIGNIFICANT_BITS, PATTERN_LEAST_SIGNIFICANT_BITS,
                PATTERN_MOST_SIGNIFICANT_BITS, PATTERN_LEAST_SIGNIFICANT_BITS, 0),
            Arguments.of(0L, 1L, 0L, 0L, 1),
            Arguments.of(1L << 16, 0L, 0L, 0L, 1)
        );
    }

    private static Stream<Arguments> serializableValues() {
        return Stream.of(
            Arguments.of(0L, 0L),
            Arguments.of(ALL_BITS_SET, ALL_BITS_SET),
            Arguments.of(PATTERN_MOST_SIGNIFICANT_BITS, PATTERN_LEAST_SIGNIFICANT_BITS)
        );
    }

    private static Stream<Arguments> incrementArguments() {
        return Stream.of(
            Arguments.of(new ULID.Value(0, 0), new ULID.Value(0, 1)),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFEL), new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL)),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL), new ULID.Value(1, 0)),
            Arguments.of(new ULID.Value(0xFFFFL, 0xFFFF_FFFF_FFFF_FFFFL), new ULID.Value(0, 0)),
            Arguments.of(new ULID.Value(0x1_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL), new ULID.Value(0x1_0000L, 0))
        );
    }

    private static Stream<Arguments> nextMonotonicArguments() {
        return Stream.of(
            Arguments.of(new ULID.Value(0, 0), new ULID.Value(0, 1)),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFEL), new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL)),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL), new ULID.Value(1, 0)),
            Arguments.of(new ULID.Value(0xFFFFL, 0xFFFF_FFFF_FFFF_FFFFL), new ULID.Value(0, 0))
        );
    }

    private static Stream<Arguments> nextStrictlyMonotonicArguments() {
        return Stream.of(
            Arguments.of(new ULID.Value(0, 0), Optional.of(new ULID.Value(0, 1))),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFEL), Optional.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL))),
            Arguments.of(new ULID.Value(0, 0xFFFF_FFFF_FFFF_FFFFL), Optional.of(new ULID.Value(1, 0))),
            Arguments.of(new ULID.Value(0xFFFFL, 0xFFFF_FFFF_FFFF_FFFFL), Optional.empty())
        );
    }

    private static byte[] repeat(byte value, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = value;
        }
        return result;
    }

    private static char[] initBuffer(int size, char fill) {
        char[] buffer = new char[size];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = fill;
        }
        return buffer;
    }

    private static final class StubRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final long[] values;
        private int index;

        private StubRandom(long... values) {
            this.values = values;
        }

        static StubRandom fromValues(long... values) {
            return new StubRandom(values);
        }

        @Override
        public long nextLong() {
            if (index >= values.length) {
                throw new AssertionError("Unexpected nextLong call");
            }
            return values[index++];
        }

        void assertExhausted() {
            assertEquals(values.length, index, "Unexpected number of nextLong invocations");
        }
    }
}
