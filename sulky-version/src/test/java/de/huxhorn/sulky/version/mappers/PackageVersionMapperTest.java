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

package de.huxhorn.sulky.version.mappers;

import de.huxhorn.sulky.version.ClassFileVersion;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageVersionMapperTest extends AbstractMapperTest {

	@Override
	protected PackageVersionMapper createInstance() {
		return new PackageVersionMapper();
	}

	@Test
	void packageVersionsAreCollected() {
		PackageVersionMapper instance = createInstance();
		instance.evaluate("source1", "packageName1", "className1", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName1", "className2", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.evaluate("source1", "packageName2", "className1", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName2", "className2", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source1", "", "className1", ClassFileVersion.JAVA_1_4.getMajorVersionCharacter());
		instance.evaluate("source2", "", "className2", ClassFileVersion.JAVA_1_4.getMajorVersionCharacter());

		Map<String, Set<Character>> packageVersions = instance.getPackageVersions();
		assertEquals(3, packageVersions.size());
		assertEquals(Set.of(ClassFileVersion.JAVA_1_5.getMajorVersionCharacter(), ClassFileVersion.JAVA_1_6.getMajorVersionCharacter()), packageVersions.get("packageName1"));
		assertEquals(Set.of(ClassFileVersion.JAVA_1_5.getMajorVersionCharacter()), packageVersions.get("packageName2"));
		assertEquals(Set.of(ClassFileVersion.JAVA_1_4.getMajorVersionCharacter()), packageVersions.get(""));
	}

	@Test
	void resetClearsState() {
		PackageVersionMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.reset();

		assertTrue(instance.getPackageVersions().isEmpty());
	}

	@Test
	void evaluationAfterResetWorks() {
		PackageVersionMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.reset();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());

		assertEquals(1, instance.getPackageVersions().size());
		assertEquals(2, instance.getPackageVersions().get("packageName").size());
	}
}
