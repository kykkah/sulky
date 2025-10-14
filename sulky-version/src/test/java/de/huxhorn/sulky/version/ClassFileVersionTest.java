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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClassFileVersionTest {

	@ParameterizedTest(name = "getByMajorVersionChar({0}) returns {2}")
	@MethodSource("versionsProvider")
	void getByMajorVersionCharReturnsExpectedValue(int classFileMajorVersion, String sourceName, ClassFileVersion expected) {
		ClassFileVersion version = ClassFileVersion.getByMajorVersionChar((char) classFileMajorVersion);
		assertEquals(expected, version);
		assertEquals(sourceName, version.getSourceName());
		assertEquals((char) classFileMajorVersion, version.getMajorVersionCharacter());
	}

	@ParameterizedTest(name = "getBySourceName(\"{1}\") returns {2}")
	@MethodSource("versionsProvider")
	void getBySourceNameReturnsExpectedValue(int classFileMajorVersion, String sourceName, ClassFileVersion expected) {
		ClassFileVersion version = ClassFileVersion.getBySourceName(sourceName);
		assertEquals(expected, version);
		assertEquals(sourceName, version.getSourceName());
		assertEquals((char) classFileMajorVersion, version.getMajorVersionCharacter());
	}

	@ParameterizedTest
	@ValueSource(strings = {"unknown"})
	@NullSource
	void getBySourceNameReturnsNull(String sourceName) {
		assertNull(ClassFileVersion.getBySourceName(sourceName));
	}

	private static Stream<Arguments> versionsProvider() {
		return Stream.of(
			Arguments.of(0x2D, "1.1", ClassFileVersion.JAVA_1_1),
			Arguments.of(0x2E, "1.2", ClassFileVersion.JAVA_1_2),
			Arguments.of(0x2F, "1.3", ClassFileVersion.JAVA_1_3),
			Arguments.of(0x30, "1.4", ClassFileVersion.JAVA_1_4),
			Arguments.of(0x31, "1.5", ClassFileVersion.JAVA_1_5),
			Arguments.of(0x32, "6", ClassFileVersion.JAVA_1_6),
			Arguments.of(0x33, "7", ClassFileVersion.JAVA_1_7),
			Arguments.of(0x34, "8", ClassFileVersion.JAVA_1_8),
			Arguments.of(0x35, "9", ClassFileVersion.JAVA_9),
			Arguments.of(0x36, "10", ClassFileVersion.JAVA_10),
			Arguments.of(0x37, "11", ClassFileVersion.JAVA_11),
			Arguments.of(0x38, "12", ClassFileVersion.JAVA_12),
			Arguments.of(0x39, "13", ClassFileVersion.JAVA_13),
			Arguments.of(0x40, "14", ClassFileVersion.JAVA_14),
			Arguments.of(0x41, "15", ClassFileVersion.JAVA_15),
			Arguments.of(0x42, "16", ClassFileVersion.JAVA_16),
			Arguments.of(0x43, "17", ClassFileVersion.JAVA_17),
			Arguments.of(0x44, "18", ClassFileVersion.JAVA_18),
			Arguments.of(0x45, "19", ClassFileVersion.JAVA_19),
			Arguments.of(0x46, "20", ClassFileVersion.JAVA_20),
			Arguments.of(0x47, "21", ClassFileVersion.JAVA_21),
			Arguments.of(0x48, "22", ClassFileVersion.JAVA_22),
			Arguments.of(0x49, "23", ClassFileVersion.JAVA_23)
		);
	}
}
