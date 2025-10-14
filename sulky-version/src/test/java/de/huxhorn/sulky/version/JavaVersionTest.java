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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaVersionTest {

	private static final JavaVersion CURRENT_VERSION = JavaVersion.getSystemJavaVersion();
	private static final String CURRENT_VERSION_STRING = CURRENT_VERSION.toVersionString();
	private static final boolean CURRENT_VERSION_IS_JEP223 = CURRENT_VERSION instanceof Jep223JavaVersion;

	@ParameterizedTest
	@ValueSource(strings = {"1x", "1.2x", "1.2.3x", "1.2.3.4", "1.2.3_4x", "1.2.3_4-", "-1.6"})
	void parseRejectsInvalidVersion(String versionString) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> JavaVersion.parse(versionString));
		assertEquals("versionString '" + versionString + "' is invalid.", exception.getMessage());
	}

	@Test
	void parseRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> JavaVersion.parse(null));
		assertEquals("versionString must not be null!", exception.getMessage());
	}

	@ParameterizedTest(name = "JVM is{1} at least {0}")
	@MethodSource("stringIsAtLeastProvider")
	void isAtLeastString(String versionString, boolean expectedResult, String compareString, String jvmString) {
		boolean result = JavaVersion.isAtLeast(versionString);
		assertEquals(expectedResult, result, () -> formatCompareMessage(jvmString, compareString, versionString));
	}

	@ParameterizedTest(name = "JVM is{1} at least {0}")
	@MethodSource("javaVersionIsAtLeastProvider")
	void isAtLeastJavaVersion(JavaVersion version, boolean expectedResult, String compareString, String jvmString) {
		assertEquals(expectedResult, JavaVersion.isAtLeast(version), () -> formatCompareMessage(jvmString, compareString, version.toVersionString()));
	}

	@ParameterizedTest(name = "JVM is{1} at least {0} for huge old version")
	@MethodSource("hugeOldVersionProvider")
	void isAtLeastJavaVersionHugeOldVersion(JavaVersion version, boolean expectedResult, String compareString, String jvmString) {
		assertEquals(expectedResult, JavaVersion.isAtLeast(version), () -> formatCompareMessage(jvmString, compareString, version.toVersionString()));
	}

	@ParameterizedTest(name = "JVM is{1} at least {0} when ignoring pre-release")
	@MethodSource("javaVersionIsAtLeastIgnoringPreReleaseProvider")
	void isAtLeastJavaVersionIgnoringPreRelease(JavaVersion version, boolean expectedResult, String compareString, String jvmString) {
		assertEquals(expectedResult, JavaVersion.isAtLeast(version, true), () -> formatCompareMessage(jvmString, compareString, version.toVersionString()));
	}

	@ParameterizedTest(name = "JVM is{1} at least {0} for huge old version ignoring pre-release")
	@MethodSource("hugeOldVersionProvider")
	void isAtLeastJavaVersionHugeOldVersionIgnoringPreRelease(JavaVersion version, boolean expectedResult, String compareString, String jvmString) {
		assertEquals(expectedResult, JavaVersion.isAtLeast(version, true), () -> formatCompareMessage(jvmString, compareString, version.toVersionString()));
	}

	@Test
	void isAtLeastJavaVersionSpecial() {
		Assumptions.assumeFalse(CURRENT_VERSION_STRING.contains("-"));
		JavaVersion version = JavaVersion.parse(CURRENT_VERSION_STRING + "-ea");
		boolean result = JavaVersion.isAtLeast(version);
		assertTrue(result);
	}

	@Test
	void isAtLeastJavaVersionIgnoringPreReleaseSpecial() {
		Assumptions.assumeFalse(CURRENT_VERSION_STRING.contains("-"));
		JavaVersion version = JavaVersion.parse(CURRENT_VERSION_STRING + "-ea");
		boolean result = JavaVersion.isAtLeast(version, true);
		assertTrue(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"1x", "1.2x", "1.2.3x", "1.2.3.4", "1.2.3_4x", "1.2.3_4-", "-1.6"})
	void isAtLeastStringRejectsInvalidVersion(String versionString) {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> JavaVersion.isAtLeast(versionString));
		assertEquals("versionString '" + versionString + "' is invalid.", exception.getMessage());
	}

	@Test
	void isAtLeastStringRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> JavaVersion.isAtLeast((String) null));
		assertEquals("versionString must not be null!", exception.getMessage());
	}

	@Test
	void isAtLeastJavaVersionRejectsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> JavaVersion.isAtLeast((JavaVersion) null));
		assertEquals("version must not be null!", exception.getMessage());
	}

	@ParameterizedTest(name = "COMPARATOR compares {0} and {1}")
	@MethodSource("comparatorProvider")
	void comparator( JavaVersion versionA, JavaVersion versionB, int expectedResult) {
		int result1 = JavaVersion.COMPARATOR.compare(versionA, versionB);
		int result2 = JavaVersion.COMPARATOR.compare(versionB, versionA);
		assertEquals(expectedResult, result1);
		assertEquals(expectedResult, result2 * -1);
	}

	@ParameterizedTest
	@MethodSource("comparatorExceptionProvider")
	void comparatorThrowsForUnexpectedTypes(JavaVersion versionA, JavaVersion versionB) {
		ClassCastException exception = assertThrows(ClassCastException.class, () -> JavaVersion.COMPARATOR.compare(versionA, versionB));
		assertEquals("Unexpected JavaVersion of class " + FooVersion.class.getName() + "!", exception.getMessage());
	}

	private static Stream<Arguments> stringIsAtLeastProvider() {
		return Stream.of(
			Arguments.of("1.0.0", true, "", CURRENT_VERSION_STRING),
			Arguments.of(CURRENT_VERSION_STRING, true, "", CURRENT_VERSION_STRING),
			Arguments.of("17.0", CURRENT_VERSION.getMajor() >= 17, CURRENT_VERSION.getMajor() >= 17 ? "" : "n't", CURRENT_VERSION_STRING)
		);
	}

	private static Stream<Arguments> javaVersionIsAtLeastProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(1, 0, 0), true, "", CURRENT_VERSION_STRING),
			Arguments.of(JavaVersion.parse(CURRENT_VERSION_STRING), true, "", CURRENT_VERSION_STRING),
			Arguments.of(new Jep223JavaVersion(new int[]{42, 7, 9}, null, 0, null), false, "n't", CURRENT_VERSION_STRING)
		);
	}

	private static Stream<Arguments> hugeOldVersionProvider() {
		boolean expected = CURRENT_VERSION_IS_JEP223;
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(17, 0), expected, expected ? "" : "n't", CURRENT_VERSION_STRING)
		);
	}

	private static Stream<Arguments> javaVersionIsAtLeastIgnoringPreReleaseProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(1, 0, 0), true, "", CURRENT_VERSION_STRING),
			Arguments.of(JavaVersion.parse(CURRENT_VERSION_STRING), true, "", CURRENT_VERSION_STRING),
			Arguments.of(new Jep223JavaVersion(new int[]{42, 7, 9}, null, 0, null), false, "n't", CURRENT_VERSION_STRING)
		);
	}

	private static Stream<Arguments> comparatorProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(1, 8, 0, 45), new YeOldeJavaVersion(1, 8, 0, 45), 0),
			Arguments.of(new YeOldeJavaVersion(1, 8, 0, 45), new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null), -1),
			Arguments.of(new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null), new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null), 0),
			Arguments.of(null, null, 0),
			Arguments.of(new YeOldeJavaVersion(1, 8, 0, 45), null, 1),
			Arguments.of(new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null), null, 1)
		);
	}

	private static Stream<Arguments> comparatorExceptionProvider() {
		return Stream.of(
			Arguments.of(new YeOldeJavaVersion(1, 8, 0, 45), new FooVersion()),
			Arguments.of(new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null), new FooVersion()),
			Arguments.of(new FooVersion(), new YeOldeJavaVersion(1, 8, 0, 45)),
			Arguments.of(new FooVersion(), new Jep223JavaVersion(new int[]{8, 0, 45}, null, 0, null))
		);
	}

	private static String formatCompareMessage(String jvmString, String compareString, String versionString) {
		return "JVM " + jvmString + " is" + compareString + " at least " + versionString;
	}

	private static final class FooVersion extends JavaVersion {

		@Override
		public int getMajor() {
			return 0;
		}

		@Override
		public int getMinor() {
			return 0;
		}

		@Override
		public int getPatch() {
			return 0;
		}

		@Override
		public int getEmergencyPatch() {
			return 0;
		}

		@Override
		public String getPreReleaseIdentifier() {
			return null;
		}

		@Override
		public String toVersionString() {
			return "foo";
		}

		@Override
		public String toShortVersionString() {
			return "foo";
		}

		@Override
		public JavaVersion withoutPreReleaseIdentifier() {
			return this;
		}
	}
}
