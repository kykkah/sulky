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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionTest {

	@ParameterizedTest(name = "'{0}' is a valid semantic version.")
	@MethodSource("validVersionProvider")
	void validVersions(String versionString, SemanticVersion expectedValue) {
		SemanticVersion version = SemanticVersion.parse(versionString);
		String generatedVersionString = version.toString();
		String regeneratedVersionString = version.toString();

		assertTrue(version.equals(expectedValue));
		assertEquals(expectedValue.hashCode(), version.hashCode());
		assertEquals(versionString, generatedVersionString);
		assertEquals(expectedValue.toString(), generatedVersionString);
		assertEquals(generatedVersionString, regeneratedVersionString);
	}

	@ParameterizedTest(name = "{0} {2} {1}.")
	@MethodSource("compareToProvider")
	void compareUsingCompareTo(String versionAString, String versionBString, int expectedResult, String compareString) {
		SemanticVersion versionA = SemanticVersion.parse(versionAString);
		SemanticVersion versionB = SemanticVersion.parse(versionBString);
		int compareToAB = versionA.compareTo(versionB);
		int compareToBA = versionB.compareTo(versionA);

		assertEquals(expectedResult, compareToAB, () -> formatCompareMessage(versionAString, compareString, versionBString));
		assertEquals(-expectedResult, compareToBA);
	}

	@ParameterizedTest(name = "{0} equals {1}? {2}")
	@MethodSource("equalsAndHashCodeProvider")
	void compareUsingEqualsAndHashCode(String versionAString, String versionBString, boolean expectedResult) {
		SemanticVersion versionA = SemanticVersion.parse(versionAString);
		SemanticVersion versionB = SemanticVersion.parse(versionBString);
		if (expectedResult) {
			assertTrue(versionA.equals(versionB));
			assertEquals(versionA.hashCode(), versionB.hashCode());
		} else {
			assertFalse(versionA.equals(versionB));
		}
	}

	@Test
	void preReleaseMustNotContainNull() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new SemanticVersion(1, 0, 0, new String[]{"foo", null, "bar"})
		);
		assertEquals("preRelease must not contain null!", exception.getMessage());
	}

	@ParameterizedTest(name = "'{0}' is an illegal preRelease identifier.")
	@ValueSource(strings = {"", "$foo"})
	void illegalPreReleaseValue(String illegalValue) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new SemanticVersion(1, 0, 0, new String[]{"foo", illegalValue, "bar"})
		);
		assertEquals("preRelease identifier '" + illegalValue + "' is invalid!", exception.getMessage());
	}

	@Test
	void buildMetadataMustNotContainNull() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new SemanticVersion(1, 0, 0, null, new String[]{"foo", null, "bar"})
		);
		assertEquals("buildMetadata must not contain null!", exception.getMessage());
	}

	@ParameterizedTest(name = "'{0}' is an illegal buildMetadata identifier.")
	@ValueSource(strings = {"", "$foo"})
	void illegalBuildMetadataValue(String illegalValue) {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new SemanticVersion(1, 0, 0, null, new String[]{"foo", illegalValue, "bar"})
		);
		assertEquals("buildMetadata identifier '" + illegalValue + "' is invalid!", exception.getMessage());
	}

	@Test
	void preReleaseListIsImmutable() {
		SemanticVersion version = new SemanticVersion(1, 0, 0, new String[]{"foo", "bar"});
		assertThrows(UnsupportedOperationException.class, () -> version.getPreRelease().remove(0));
	}

	@Test
	void buildMetadataListIsImmutable() {
		SemanticVersion version = new SemanticVersion(1, 0, 0, null, new String[]{"foo", "bar"});
		assertThrows(UnsupportedOperationException.class, () -> version.getBuildMetadata().remove(0));
	}

	@Test
	void preReleaseArrayIsCopied() {
		String[] identifiers = {"foo", "bar"};
		SemanticVersion version = new SemanticVersion(1, 0, 0, identifiers);
		identifiers[0] = "fooBar";
		assertEquals("foo", version.getPreRelease().get(0));
	}

	@Test
	void buildMetadataArrayIsCopied() {
		String[] identifiers = {"foo", "bar"};
		SemanticVersion version = new SemanticVersion(1, 0, 0, null, identifiers);
		identifiers[0] = "fooBar";
		assertEquals("foo", version.getBuildMetadata().get(0));
	}

	@Test
	void equalsNullReturnsFalse() {
		assertFalse(new SemanticVersion(1, 0, 0).equals(null));
	}

	@Test
	void equalsDifferentTypeReturnsFalse() {
		assertFalse(new SemanticVersion(1, 0, 0).equals(new Object()));
	}

	@Test
	void equalsThisReturnsTrue() {
		SemanticVersion version = new SemanticVersion(1, 0, 0);
		assertTrue(version.equals(version));
	}

	@Test
	void parseRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> SemanticVersion.parse(null));
		assertEquals("versionString must not be null!", exception.getMessage());
	}

	@Test
	void compareToRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> new SemanticVersion(1, 0, 0).compareTo(null));
		assertEquals("other must not be null!", exception.getMessage());
	}

	@ParameterizedTest(name = "parse('{0}') throws exception.")
	@ValueSource(strings = {"1.0"})
	void parseInvalidStringThrows(String versionString) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse(versionString));
		assertEquals("'" + versionString + "' is not a valid semantic version!", exception.getMessage());
	}

	@Test
	void invalidMajorNumber() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(-1, 0, 0));
		assertEquals("major must not be negative!", exception.getMessage());
	}

	@Test
	void invalidMinorNumber() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(0, -1, 0));
		assertEquals("minor must not be negative!", exception.getMessage());
	}

	@Test
	void invalidPatchNumber() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(0, 0, -1));
		assertEquals("patch must not be negative!", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getterVerificationProvider")
	void gettersReturnExpectedValues(int major, int minor, int patch, String[] preRelease, String[] buildMetadata) {
		SemanticVersion version = new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
		assertEquals(major, version.getMajor());
		assertEquals(minor, version.getMinor());
		assertEquals(patch, version.getPatch());
		assertEquals(preRelease == null ? List.of() : List.of(preRelease), version.getPreRelease());
		assertEquals(buildMetadata == null ? List.of() : List.of(buildMetadata), version.getBuildMetadata());
	}

	@ParameterizedTest(name = "serializable for {0}")
	@MethodSource("serializableProvider")
	void serializable(SemanticVersion object) {
		assertDoesNotThrow(() -> JUnitTools.testSerialization(object));
	}

	private static String formatCompareMessage(String versionA, String compareString, String versionB) {
		return versionA + " " + compareString + " " + versionB + ".";
	}

	private static Stream<Arguments> validVersionProvider() {
		return Stream.of(
			Arguments.of("0.1.2", new SemanticVersion(0, 1, 2)),
			Arguments.of("1.0.0", new SemanticVersion(1, 0, 0)),
			Arguments.of("1.0.0", new SemanticVersion(1, 0, 0, null, null)),
			Arguments.of("1.0.0+20130313144700", new SemanticVersion(1, 0, 0, null, new String[]{"20130313144700"})),
			Arguments.of("1.0.0-0.3.7", new SemanticVersion(1, 0, 0, new String[]{"0", "3", "7"})),
			Arguments.of("1.0.0-alpha", new SemanticVersion(1, 0, 0, new String[]{"alpha"})),
			Arguments.of("1.0.0-alpha+001", new SemanticVersion(1, 0, 0, new String[]{"alpha"}, new String[]{"001"})),
			Arguments.of("1.0.0-alpha.1", new SemanticVersion(1, 0, 0, new String[]{"alpha", "1"})),
			Arguments.of("1.0.0-alpha.beta", new SemanticVersion(1, 0, 0, new String[]{"alpha", "beta"})),
			Arguments.of("1.0.0-beta", new SemanticVersion(1, 0, 0, new String[]{"beta"})),
			Arguments.of("1.0.0-beta+exp.sha.5114f85", new SemanticVersion(1, 0, 0, new String[]{"beta"}, new String[]{"exp", "sha", "5114f85"})),
			Arguments.of("1.0.0-beta.2", new SemanticVersion(1, 0, 0, new String[]{"beta", "2"})),
			Arguments.of("1.0.0-beta.11", new SemanticVersion(1, 0, 0, new String[]{"beta", "11"})),
			Arguments.of("1.0.0-rc.1", new SemanticVersion(1, 0, 0, new String[]{"rc", "1"})),
			Arguments.of("1.0.0-x.7.z.92", new SemanticVersion(1, 0, 0, new String[]{"x", "7", "z", "92"})),
			Arguments.of("1.9.0", new SemanticVersion(1, 9, 0)),
			Arguments.of("1.10.0", new SemanticVersion(1, 10, 0)),
			Arguments.of("1.11.0", new SemanticVersion(1, 11, 0)),
			Arguments.of("2.0.0", new SemanticVersion(2, 0, 0)),
			Arguments.of("2.1.0", new SemanticVersion(2, 1, 0)),
			Arguments.of("2.1.1", new SemanticVersion(2, 1, 1))
		);
	}

	private static Stream<Arguments> compareToProvider() {
		return Stream.of(
			Arguments.of("0.9.0", "1.0.0", -1, compareString(-1)),
			Arguments.of("1.9.0", "1.10.0", -1, compareString(-1)),
			Arguments.of("1.9.0", "1.11.0", -1, compareString(-1)),
			Arguments.of("1.10.0", "1.11.0", -1, compareString(-1)),
			Arguments.of("1.11.0", "1.11.1", -1, compareString(-1)),
			Arguments.of("1.0.0", "2.0.0", -1, compareString(-1)),
			Arguments.of("1.0.0", "2.1.0", -1, compareString(-1)),
			Arguments.of("1.0.0", "2.1.1", -1, compareString(-1)),
			Arguments.of("2.0.0", "2.1.0", -1, compareString(-1)),
			Arguments.of("2.0.0", "2.1.1", -1, compareString(-1)),
			Arguments.of("2.1.0", "2.1.1", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha.1", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha.beta", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-beta", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-beta.2", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-beta.11", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.beta", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta.2", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta.11", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta.2", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta.11", -1, compareString(-1)),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.0.0-beta", "1.0.0-beta.2", -1, compareString(-1)),
			Arguments.of("1.0.0-beta", "1.0.0-beta.11", -1, compareString(-1)),
			Arguments.of("1.0.0-beta", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.0.0-beta.2", "1.0.0-beta.11", -1, compareString(-1)),
			Arguments.of("1.0.0-beta.2", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.0.0-beta.11", "1.0.0-rc.1", -1, compareString(-1)),
			Arguments.of("1.9.0", "1.9.0", 0, compareString(0)),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha+001", 0, compareString(0)),
			Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.1", 0, compareString(0))
		);
	}

	private static Stream<Arguments> equalsAndHashCodeProvider() {
		return Stream.of(
			Arguments.of("0.9.0", "1.0.0", false),
			Arguments.of("1.9.0", "1.10.0", false),
			Arguments.of("1.9.0", "1.11.0", false),
			Arguments.of("1.10.0", "1.11.0", false),
			Arguments.of("1.11.0", "1.11.1", false),
			Arguments.of("1.0.0", "2.0.0", false),
			Arguments.of("1.0.0", "2.1.0", false),
			Arguments.of("1.0.0", "2.1.1", false),
			Arguments.of("2.0.0", "2.1.0", false),
			Arguments.of("2.0.0", "2.1.1", false),
			Arguments.of("2.1.0", "2.1.1", false),
			Arguments.of("1.0.0-alpha", "1.0.0", false),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha.1", false),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha.beta", false),
			Arguments.of("1.0.0-alpha", "1.0.0-beta", false),
			Arguments.of("1.0.0-alpha", "1.0.0-beta.2", false),
			Arguments.of("1.0.0-alpha", "1.0.0-beta.11", false),
			Arguments.of("1.0.0-alpha", "1.0.0-rc.1", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.beta", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta.2", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-beta.11", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-rc.1", false),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta", false),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta.2", false),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-beta.11", false),
			Arguments.of("1.0.0-alpha.beta", "1.0.0-rc.1", false),
			Arguments.of("1.0.0-beta", "1.0.0-beta.2", false),
			Arguments.of("1.0.0-beta", "1.0.0-beta.11", false),
			Arguments.of("1.0.0-beta", "1.0.0-rc.1", false),
			Arguments.of("1.0.0-beta.2", "1.0.0-beta.11", false),
			Arguments.of("1.0.0-beta.2", "1.0.0-rc.1", false),
			Arguments.of("1.0.0-beta.11", "1.0.0-rc.1", false),
			Arguments.of("1.9.0", "1.9.0", true),
			Arguments.of("1.0.0-alpha", "1.0.0-alpha+001", false),
			Arguments.of("1.0.0-alpha.1", "1.0.0-alpha.1", true),
			Arguments.of("1.0.0-alpha+001", "2.0.0-alpha+001", false),
			Arguments.of("1.0.0-alpha+001", "1.1.0-alpha+001", false),
			Arguments.of("1.0.0-alpha+001", "1.0.1-alpha+001", false),
			Arguments.of("1.0.0-alpha+001", "1.0.1-alpha.1+001", false),
			Arguments.of("1.0.0-alpha+001", "1.0.1-alpha1+001", false),
			Arguments.of("1.0.0-alpha+001", "1.0.1-alpha+001.1", false),
			Arguments.of("1.0.0-alpha+001", "1.0.1-alpha+002", false)
		);
	}

	private static Stream<Arguments> getterVerificationProvider() {
		return Stream.of(
			Arguments.of(1, 2, 3, new String[0], new String[0]),
			Arguments.of(1, 2, 3, new String[]{"alpha"}, new String[]{"foo"})
		);
	}

	private static Stream<Arguments> serializableProvider() {
		return Stream.of(
			Arguments.of(new SemanticVersion(1, 2, 3)),
			Arguments.of(new SemanticVersion(1, 2, 3, new String[]{"foo", "bar"})),
			Arguments.of(new SemanticVersion(1, 2, 3, new String[]{"foo", "bar"}, new String[]{"17", "foobar", "1"}))
		);
	}

	private static String compareString(int value) {
		if (value < 0) {
			return "is less than";
		}
		if (value > 0) {
			return "is greater than";
		}
		return "is equal to";
	}
}
