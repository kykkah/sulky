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

package de.huxhorn.sulky.version;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YeOldeJavaVersionTest {

	@ParameterizedTest(name = "parse({0}) returns {1}")
	@MethodSource("validVersionProvider")
	void parseReturnsExpectedInstance(String versionString, YeOldeJavaVersion expectedVersion) {
		YeOldeJavaVersion version = YeOldeJavaVersion.parse(versionString);
		assertEquals(expectedVersion, version);
	}

	@ParameterizedTest(name = "parse({0}) throws IllegalArgumentException")
	@ValueSource(strings = {"1", "1x", "1.2x", "1.2.3x", "1.2.3.4a", "1.2.3_4x", "1.2.3_4-", "-1.6"})
	void parseRejectsInvalidVersions(String versionString) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> YeOldeJavaVersion.parse(versionString));
		assertEquals("versionString '" + versionString + "' is invalid.", exception.getMessage());
	}

	@Test
	void parseRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> YeOldeJavaVersion.parse(null));
		assertEquals("versionString must not be null!", exception.getMessage());
	}

	@Test
	void constructorRejectsEmptyIdentifier() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new YeOldeJavaVersion(1, 2, 3, 4, ""));
		assertEquals("preReleaseIdentifier must not be empty string!", exception.getMessage());
	}

	@Test
	void constructorRejectsStarInIdentifier() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new YeOldeJavaVersion(1, 2, 3, 4, "f*o"));
		assertEquals("preReleaseIdentifier must not contain the '*' character!", exception.getMessage());
	}

	@Test
	void constructorRejectsPlusInIdentifier() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new YeOldeJavaVersion(1, 2, 3, 4, "f+o"));
		assertEquals("preReleaseIdentifier must not contain the '+' character!", exception.getMessage());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}, {3}, {4}) throws because {5} is negative")
	@MethodSource("negativeConstructorArgumentsWithIdentifier")
	void constructorRejectsNegativeValuesWithIdentifier(int huge, int major, int minor, int patch, String identifier, String failingPart) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new YeOldeJavaVersion(huge, major, minor, patch, identifier)
		);
		assertEquals(failingPart + " must not be negative!", exception.getMessage());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}, {3}) throws because {4} is negative")
	@MethodSource("negativeConstructorArgumentsWithoutIdentifier")
	void constructorRejectsNegativeValues(int huge, int major, int minor, int patch, String failingPart) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new YeOldeJavaVersion(huge, major, minor, patch)
		);
		assertEquals(failingPart + " must not be negative!", exception.getMessage());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}) throws because {3} is negative")
	@MethodSource("negativeConstructorArgumentsThreeParameters")
	void constructorRejectsNegativeValues(int huge, int major, int minor, String failingPart) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new YeOldeJavaVersion(huge, major, minor)
		);
		assertEquals(failingPart + " must not be negative!", exception.getMessage());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}) throws because {2} is negative")
	@MethodSource("negativeConstructorArgumentsTwoParameters")
	void constructorRejectsNegativeValues(int huge, int major, String failingPart) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new YeOldeJavaVersion(huge, major)
		);
		assertEquals(failingPart + " must not be negative!", exception.getMessage());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}, {3}, {4}) initialises fields")
	@MethodSource("constructorInitializationWithIdentifier")
	void constructorInitialisesFieldsWithIdentifier(int huge, int major, int minor, int patch, String identifier) {
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor, patch, identifier);
		assertEquals(huge, result.getHuge());
		assertEquals(major, result.getMajor());
		assertEquals(minor, result.getMinor());
		assertEquals(patch, result.getPatch());
		assertEquals(identifier, result.getPreReleaseIdentifier());
		assertEquals(major, result.getFeature());
		assertEquals(minor, result.getInterim());
		assertEquals(patch, result.getUpdate());
		assertEquals(0, result.getEmergencyPatch());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}, {3}) initialises fields")
	@MethodSource("constructorInitializationWithoutIdentifier")
	void constructorInitialisesFields(int huge, int major, int minor, int patch) {
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor, patch);
		assertEquals(huge, result.getHuge());
		assertEquals(major, result.getMajor());
		assertEquals(minor, result.getMinor());
		assertEquals(patch, result.getPatch());
		assertEquals(null, result.getPreReleaseIdentifier());
		assertEquals(major, result.getFeature());
		assertEquals(minor, result.getInterim());
		assertEquals(patch, result.getUpdate());
		assertEquals(0, result.getEmergencyPatch());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}, {2}) initialises fields")
	@MethodSource("constructorInitializationThreeParameters")
	void constructorInitialisesFields(int huge, int major, int minor) {
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor);
		assertEquals(huge, result.getHuge());
		assertEquals(major, result.getMajor());
		assertEquals(minor, result.getMinor());
		assertEquals(0, result.getPatch());
		assertEquals(null, result.getPreReleaseIdentifier());
		assertEquals(major, result.getFeature());
		assertEquals(minor, result.getInterim());
		assertEquals(0, result.getUpdate());
		assertEquals(0, result.getEmergencyPatch());
	}

	@ParameterizedTest(name = "new YeOldeJavaVersion({0}, {1}) initialises fields")
	@MethodSource("constructorInitializationTwoParameters")
	void constructorInitialisesFields(int huge, int major) {
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major);
		assertEquals(huge, result.getHuge());
		assertEquals(major, result.getMajor());
		assertEquals(0, result.getMinor());
		assertEquals(0, result.getPatch());
		assertEquals(null, result.getPreReleaseIdentifier());
		assertEquals(major, result.getFeature());
		assertEquals(0, result.getInterim());
		assertEquals(0, result.getUpdate());
		assertEquals(0, result.getEmergencyPatch());
	}

	@ParameterizedTest(name = "{0} compared to {1} yields {2}")
	@MethodSource("compareToProvider")
	void compareToBehavesAsExpected(YeOldeJavaVersion object, YeOldeJavaVersion other, int expectedResult) {
		int result = object.compareTo(other);
		assertEquals(expectedResult, result);
		assertEquals(-expectedResult, other.compareTo(object));
		if (expectedResult == 0) {
			assertEquals(object, other);
			assertEquals(object.hashCode(), other.hashCode());
		} else {
			assertNotEquals(object, other);
		}
	}

	@ParameterizedTest(name = "toVersionString({0}) -> {1}")
	@MethodSource("toVersionStringProvider")
	void toVersionStringProducesExpectedValue(YeOldeJavaVersion object, String expected) {
		assertEquals(expected, object.toVersionString());
	}

	@ParameterizedTest(name = "toShortVersionString({0}) -> {1}")
	@MethodSource("toShortVersionStringProvider")
	void toShortVersionStringProducesExpectedValue(YeOldeJavaVersion object, String expected) {
		assertEquals(expected, object.toShortVersionString());
	}

	@ParameterizedTest(name = "toString({0}) -> {1}")
	@MethodSource("toStringProvider")
	void toStringProducesExpectedValue(YeOldeJavaVersion object, String expected) {
		assertEquals(expected, object.toString());
	}

	@Test
	void compareToRejectsNull() {
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6);
		NullPointerException exception = assertThrows(NullPointerException.class, () -> object.compareTo(null));
		assertEquals("other must not be null!", exception.getMessage());
	}

	@Test
	void equalsNullReturnsFalse() {
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6);
		assertNotEquals(null, object);
	}

	@Test
	void equalsDifferentTypeReturnsFalse() {
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6);
		assertNotEquals(object, 1);
	}

	@ParameterizedTest(name = "serialization works for {0}")
	@MethodSource("serializableProvider")
	void serializable(YeOldeJavaVersion object) {
		assertDoesNotThrow(() -> JUnitTools.testSerialization(object));
	}

	@ParameterizedTest(name = "withoutPreReleaseIdentifier({0}) -> {1}")
	@MethodSource("withoutPreReleaseProvider")
	void withoutPreReleaseIdentifierBehavesAsExpected(YeOldeJavaVersion input, YeOldeJavaVersion expectedResult) {
		YeOldeJavaVersion result = input.withoutPreReleaseIdentifier();
		assertEquals(expectedResult, result);
		if (input.getPreReleaseIdentifier() == null) {
			assertSame(input, result);
		} else {
			assertNotSame(input, result);
		}
	}

	private static Stream<Arguments> validVersionProvider() {
		return Stream.of(
			Arguments.of("1.6", new YeOldeJavaVersion(1, 6)),
			Arguments.of("1.6.17", new YeOldeJavaVersion(1, 6, 17)),
			Arguments.of("1.6.17_42", new YeOldeJavaVersion(1, 6, 17, 42)),
			Arguments.of("1.4.2-02", new YeOldeJavaVersion(1, 4, 2, 0, "02")),
			Arguments.of("1.3.0", new YeOldeJavaVersion(1, 3, 0)),
			Arguments.of("1.3.1-beta", new YeOldeJavaVersion(1, 3, 1, 0, "beta")),
			Arguments.of("1.3.1_05-ea", new YeOldeJavaVersion(1, 3, 1, 5, "ea")),
			Arguments.of("1.3.1_05", new YeOldeJavaVersion(1, 3, 1, 5)),
			Arguments.of("1.4.0_03-ea", new YeOldeJavaVersion(1, 4, 0, 3, "ea")),
			Arguments.of("1.4.0_03", new YeOldeJavaVersion(1, 4, 0, 3))
		);
	}

	private static Stream<Arguments> negativeConstructorArgumentsWithIdentifier() {
		return Stream.of(
			Arguments.of(-1, 2, 3, 4, null, "huge"),
			Arguments.of(1, -2, 3, 4, null, "major"),
			Arguments.of(1, 2, -3, 4, null, "minor"),
			Arguments.of(1, 2, 3, -4, null, "patch")
		);
	}

	private static Stream<Arguments> negativeConstructorArgumentsWithoutIdentifier() {
		return Stream.of(
			Arguments.of(-1, 2, 3, 4, "huge"),
			Arguments.of(1, -2, 3, 4, "major"),
			Arguments.of(1, 2, -3, 4, "minor"),
			Arguments.of(1, 2, 3, -4, "patch")
		);
	}

	private static Stream<Arguments> negativeConstructorArgumentsThreeParameters() {
		return Stream.of(
			Arguments.of(-1, 2, 3, "huge"),
			Arguments.of(1, -2, 3, "major"),
			Arguments.of(1, 2, -3, "minor")
		);
	}

	private static Stream<Arguments> negativeConstructorArgumentsTwoParameters() {
		return Stream.of(
			Arguments.of(-1, 2, "huge"),
			Arguments.of(1, -2, "major")
		);
	}

	private static Stream<Arguments> constructorInitializationWithIdentifier() {
		return Stream.of(
			Arguments.of(0, 0, 0, 0, null),
			Arguments.of(1, 0, 0, 0, null),
			Arguments.of(1, 6, 0, 0, null),
			Arguments.of(1, 6, 17, 0, null),
			Arguments.of(1, 6, 17, 4, null),
			Arguments.of(0, 0, 0, 0, "foo"),
			Arguments.of(1, 0, 0, 0, "foo"),
			Arguments.of(1, 6, 0, 0, "foo"),
			Arguments.of(1, 6, 17, 0, "foo"),
			Arguments.of(1, 6, 17, 4, "foo")
		);
	}

	private static Stream<Arguments> constructorInitializationWithoutIdentifier() {
		return Stream.of(
			Arguments.of(0, 0, 0, 0),
			Arguments.of(1, 0, 0, 0),
			Arguments.of(1, 6, 0, 0),
			Arguments.of(1, 6, 17, 0),
			Arguments.of(1, 6, 17, 4)
		);
	}

	private static Stream<Arguments> constructorInitializationThreeParameters() {
		return Stream.of(
			Arguments.of(0, 0, 0),
			Arguments.of(1, 0, 0),
			Arguments.of(1, 6, 0),
			Arguments.of(1, 6, 17)
		);
	}

	private static Stream<Arguments> constructorInitializationTwoParameters() {
		return Stream.of(
			Arguments.of(0, 0),
			Arguments.of(1, 0),
			Arguments.of(1, 6)
		);
	}

	private static Stream<Arguments> compareToProvider() {
		return Stream.of(
			Arguments.of(YeOldeJavaVersion.MIN_VALUE, YeOldeJavaVersion.MIN_VALUE, 0),
			Arguments.of(YeOldeJavaVersion.MIN_VALUE, new YeOldeJavaVersion(0, 0, 0, 0, "!"), 0),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0), YeOldeJavaVersion.MIN_VALUE, 1),
			Arguments.of(YeOldeJavaVersion.MIN_VALUE, new YeOldeJavaVersion(0, 0, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), YeOldeJavaVersion.MIN_VALUE, 1),
			Arguments.of(YeOldeJavaVersion.MIN_VALUE, new YeOldeJavaVersion(0, 0, 0, 1), -1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), new YeOldeJavaVersion(0, 0, 0, 1), 0),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 2), new YeOldeJavaVersion(0, 0, 0, 1), 1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), new YeOldeJavaVersion(0, 0, 0, 2), -1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 1, 0), new YeOldeJavaVersion(0, 0, 1, 0), 0),
			Arguments.of(new YeOldeJavaVersion(0, 0, 1, 0), new YeOldeJavaVersion(0, 0, 0, 1), 1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), new YeOldeJavaVersion(0, 0, 1, 0), -1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 2, 0), new YeOldeJavaVersion(0, 0, 1, 0), 1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 1, 0), new YeOldeJavaVersion(0, 0, 2, 0), -1),
			Arguments.of(new YeOldeJavaVersion(0, 1, 0, 0), new YeOldeJavaVersion(0, 1, 0, 0), 0),
			Arguments.of(new YeOldeJavaVersion(0, 1, 0, 0), new YeOldeJavaVersion(0, 0, 0, 1), 1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), new YeOldeJavaVersion(0, 1, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(0, 2, 0, 0), new YeOldeJavaVersion(0, 1, 0, 0), 1),
			Arguments.of(new YeOldeJavaVersion(0, 1, 0, 0), new YeOldeJavaVersion(0, 2, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(1, 0, 0, 0), new YeOldeJavaVersion(1, 0, 0, 0), 0),
			Arguments.of(new YeOldeJavaVersion(1, 0, 0, 0), new YeOldeJavaVersion(0, 0, 0, 1), 1),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 1), new YeOldeJavaVersion(1, 0, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(2, 0, 0, 0), new YeOldeJavaVersion(1, 0, 0, 0), 1),
			Arguments.of(new YeOldeJavaVersion(1, 0, 0, 0), new YeOldeJavaVersion(2, 0, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0), new YeOldeJavaVersion(1, 3, 0, 1), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 1), new YeOldeJavaVersion(1, 3, 1, 0), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 1, 0), new YeOldeJavaVersion(1, 3, 1, 1), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 1, 1), new YeOldeJavaVersion(1, 3, 1, 0), 1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 1, 0), new YeOldeJavaVersion(1, 3, 0, 1), 1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 1), new YeOldeJavaVersion(1, 3, 0, 0), 1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0, "x"), new YeOldeJavaVersion(1, 3, 0, 0, "x"), 0),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0), new YeOldeJavaVersion(1, 3, 0, 0, "x"), 1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0, "x"), new YeOldeJavaVersion(1, 3, 0, 0), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 1, 0, "x"), new YeOldeJavaVersion(1, 3, 0, 0, "x"), 1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0, "x"), new YeOldeJavaVersion(1, 3, 1, 0, "x"), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0, "a"), new YeOldeJavaVersion(1, 3, 0, 0, "b"), -1),
			Arguments.of(new YeOldeJavaVersion(1, 3, 0, 0, "b"), new YeOldeJavaVersion(1, 3, 0, 0, "a"), 1)
		);
	}

	private static Stream<Arguments> toVersionStringProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0), "0.0.0"),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0, "x"), "0.0.0-x"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4, "x"), "1.2.3_04-x"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 14, "x"), "1.2.3_14-x"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 114, "x"), "1.2.3_114-x")
		);
	}

	private static Stream<Arguments> toShortVersionStringProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0), "0.0"),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0, "x"), "0.0"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4, "x"), "1.2"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 14, "x"), "1.2"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 114, "x"), "1.2")
		);
	}

	private static Stream<Arguments> toStringProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0), "YeOldeJavaVersion{huge=0, major=0, minor=0, patch=0, preReleaseIdentifier=null}"),
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0, "x"), "YeOldeJavaVersion{huge=0, major=0, minor=0, patch=0, preReleaseIdentifier=\"x\"}"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4, "x"), "YeOldeJavaVersion{huge=1, major=2, minor=3, patch=4, preReleaseIdentifier=\"x\"}"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 14, "x"), "YeOldeJavaVersion{huge=1, major=2, minor=3, patch=14, preReleaseIdentifier=\"x\"}"),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 114, "x"), "YeOldeJavaVersion{huge=1, major=2, minor=3, patch=114, preReleaseIdentifier=\"x\"}")
		);
	}

	private static Stream<Arguments> serializableProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0, "foo")),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4, "bar")),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4))
		);
	}

	private static Stream<Arguments> withoutPreReleaseProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(0, 0, 0, 0, "foo"), new YeOldeJavaVersion(0, 0, 0, 0)),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4, "foo"), new YeOldeJavaVersion(1, 2, 3, 4)),
			Arguments.of(new YeOldeJavaVersion(1, 2, 3, 4), new YeOldeJavaVersion(1, 2, 3, 4))
		);
	}
}
