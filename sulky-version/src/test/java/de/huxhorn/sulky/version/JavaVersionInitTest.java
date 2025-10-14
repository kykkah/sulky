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

import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaVersionInitTest {

	private static final String JAVA_VERSION = "java.version";
	private static final String JAVA_SPEC_VERSION = "java.specification.version";

	private final TestSecurityManager manager = new TestSecurityManager();

	private SecurityManager originalSecurityManager;
	private String originalJavaVersion;
	private String originalJavaSpecificationVersion;

	@BeforeAll
	void installSecurityManager() {
		originalSecurityManager = System.getSecurityManager();
		originalJavaVersion = System.getProperty(JAVA_VERSION);
		originalJavaSpecificationVersion = System.getProperty(JAVA_SPEC_VERSION);
		System.setSecurityManager(manager);
	}

	@AfterAll
	void restoreSecurityManager() {
		System.setSecurityManager(originalSecurityManager);
		restoreProperty(JAVA_VERSION, originalJavaVersion);
		restoreProperty(JAVA_SPEC_VERSION, originalJavaSpecificationVersion);
	}

	@AfterEach
	void resetManagerAndProperties() {
		manager.setUnreadableProperties(null);
		manager.setUnwritableProperties(null);
		manager.setDeniedProperties(null);
		restoreProperty(JAVA_VERSION, originalJavaVersion);
		restoreProperty(JAVA_SPEC_VERSION, originalJavaSpecificationVersion);
	}

	@Test
	void noPropertyAccessFallsBackToMinimumVersion() {
		manager.setUnreadableProperties(Set.of(JAVA_VERSION, JAVA_SPEC_VERSION));
		System.setProperty(JAVA_VERSION, "1.6.1_25");
		System.setProperty(JAVA_SPEC_VERSION, "1.6");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertEquals(YeOldeJavaVersion.MIN_VALUE, version);
	}

	@Test
	void fallbackToSpecificationVersion() {
		manager.setUnreadableProperties(Set.of(JAVA_VERSION));
		System.setProperty(JAVA_VERSION, "1.6.1_25");
		System.setProperty(JAVA_SPEC_VERSION, "1.6");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertNotNull(version);
		assertEquals(new YeOldeJavaVersion(1, 6), version);
	}

	@Test
	void fullPropertyAccessUsesFullVersion() {
		manager.setUnreadableProperties(null);
		System.setProperty(JAVA_VERSION, "1.6.1_25");
		System.setProperty(JAVA_SPEC_VERSION, "1.6");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertNotNull(version);
		assertEquals(new YeOldeJavaVersion(1, 6, 1, 25), version);
	}

	@Test
	void brokenJavaVersionFallsBackToSpecification() {
		manager.setUnreadableProperties(null);
		System.setProperty(JAVA_VERSION, "1.6.x_25");
		System.setProperty(JAVA_SPEC_VERSION, "1.6");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertNotNull(version);
		assertEquals(new YeOldeJavaVersion(1, 6), version);
	}

	@Test
	void brokenJavaVersionAndSpecificationFallBackToMinimum() {
		manager.setUnreadableProperties(null);
		System.setProperty(JAVA_VERSION, "1.6.x_25");
		System.setProperty(JAVA_SPEC_VERSION, "1.x");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertEquals(YeOldeJavaVersion.MIN_VALUE, version);
	}

	@Test
	void missingJavaVersionFallsBackToSpecification() {
		manager.setUnreadableProperties(null);
		System.clearProperty(JAVA_VERSION);
		System.setProperty(JAVA_SPEC_VERSION, "1.6");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertNotNull(version);
		assertEquals(new YeOldeJavaVersion(1, 6), version);
	}

	@Test
	void missingJavaVersionAndSpecificationFallBackToMinimum() {
		manager.setUnreadableProperties(null);
		System.clearProperty(JAVA_VERSION);
		System.clearProperty(JAVA_SPEC_VERSION);

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertEquals(YeOldeJavaVersion.MIN_VALUE, version);
	}

	@Test
	void minimalJep223VersionIsSupported() {
		manager.setUnreadableProperties(null);
		System.setProperty(JAVA_VERSION, "9");
		System.setProperty(JAVA_SPEC_VERSION, "9");

		JavaVersion version = JavaVersion.getSystemJavaVersion();
		assertNotNull(version);
		assertEquals(new Jep223JavaVersion(new int[]{9}, null, 0, null), version);
	}

	@Test
	void ignoringPreReleaseIdentifier() {
		manager.setUnreadableProperties(null);
		System.setProperty(JAVA_VERSION, "1.8.0_66-internal");
		System.setProperty(JAVA_SPEC_VERSION, "1.8");

		assertTrue(JavaVersion.isAtLeast("1.8.0_66", true));
		assertFalse(JavaVersion.isAtLeast("1.8.0_66", false));
	}

	private static void restoreProperty(String name, String value) {
		if (value == null) {
			System.clearProperty(name);
		} else {
			System.setProperty(name, value);
		}
	}
}
