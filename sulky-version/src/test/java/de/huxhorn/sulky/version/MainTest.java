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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

	private String previousPropertiesName;

	@BeforeEach
	void capturePropertiesName() {
		previousPropertiesName = System.getProperty(Main.PROPERTIES_NAME_PROPERTY);
	}

	@AfterEach
	void restorePropertiesName() {
		if (previousPropertiesName == null) {
			System.clearProperty(Main.PROPERTIES_NAME_PROPERTY);
		} else {
			System.setProperty(Main.PROPERTIES_NAME_PROPERTY, previousPropertiesName);
		}
	}

	@Test
	void defaultMainProperties() {
		System.clearProperty(Main.PROPERTIES_NAME_PROPERTY);
		Main.main(new String[0]);
		assertEquals(Main.NO_ERROR_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void classNotFound() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/classNotFound.properties");
		Main.main(new String[0]);
		assertEquals(Main.FAILED_TO_RESOLVE_CLASS_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void versionMismatch() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/versionMismatch.properties");
		Main.main(new String[0]);
		assertEquals(Main.VERSION_MISMATCH_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void missingVoidMain() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/missingMain.properties");
		Main.main(new String[0]);
		assertEquals(Main.FAILED_TO_RESOLVE_METHOD_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void privateMainMethod() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/nonPublicMain.properties");
		Main.main(new String[0]);
		assertEquals(Main.FAILED_TO_RESOLVE_METHOD_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void exceptionInMain() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/exceptionMain.properties");
		Main.main(new String[0]);
		assertEquals(Main.MAIN_CAUSED_EXCEPTION_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void missingProperties() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/missing.properties");
		Main.main(new String[0]);
		assertEquals(Main.MISSING_PROPERTIES_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void malformedProperties() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/malformed.properties");
		Main.main(new String[0]);
		assertEquals(Main.MALFORMED_PROPERTIES_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void missingJavaVersionProperty() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/missingJavaVersion.properties");
		Main.main(new String[0]);
		assertEquals(Main.MISSING_REQUIRED_JAVA_VERSION_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void invalidJavaVersionProperty() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/invalidJavaVersion.properties");
		Main.main(new String[0]);
		assertEquals(Main.INVALID_REQUIRED_JAVA_VERSION_STATUS_CODE, Main.getStatusCode());
	}

	@Test
	void missingMainClassProperty() {
		System.setProperty(Main.PROPERTIES_NAME_PROPERTY, "/missingMainClass.properties");
		Main.main(new String[0]);
		assertEquals(Main.MISSING_MAIN_CLASS_STATUS_CODE, Main.getStatusCode());
	}
}
