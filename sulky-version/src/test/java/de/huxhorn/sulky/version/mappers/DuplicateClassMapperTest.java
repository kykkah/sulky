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

class DuplicateClassMapperTest extends AbstractMapperTest {

	@Override
	protected DuplicateClassMapper createInstance() {
		return new DuplicateClassMapper();
	}

	@Test
	void noDuplicatesDetected() {
		DuplicateClassMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className1", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className2", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.evaluate("source1", "", "className1", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "", "className2", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());

		assertTrue(instance.getDuplicates().isEmpty());
		Map<DuplicateClassMapper.ClassInfo, Set<String>> mapping = instance.getClassSourceMapping();
		assertEquals(4, mapping.size());
		assertEquals(Set.of("source1"), mapping.get(new DuplicateClassMapper.ClassInfo("packageName", "className1")));
		assertEquals(Set.of("source2"), mapping.get(new DuplicateClassMapper.ClassInfo("packageName", "className2")));
		assertEquals(Set.of("source1"), mapping.get(new DuplicateClassMapper.ClassInfo("", "className1")));
		assertEquals(Set.of("source2"), mapping.get(new DuplicateClassMapper.ClassInfo("", "className2")));
	}

	@Test
	void duplicatesDetected() {
		DuplicateClassMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.evaluate("source1", "", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());

		assertEquals(2, instance.getDuplicates().size());
		Map<DuplicateClassMapper.ClassInfo, Set<String>> mapping = instance.getClassSourceMapping();
		assertEquals(2, mapping.size());
		assertEquals(Set.of("source1", "source2"), mapping.get(new DuplicateClassMapper.ClassInfo("packageName", "className")));
		assertEquals(Set.of("source1", "source2"), mapping.get(new DuplicateClassMapper.ClassInfo("", "className")));
	}

	@Test
	void resetClearsState() {
		DuplicateClassMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.reset();

		assertTrue(instance.getDuplicates().isEmpty());
		assertTrue(instance.getClassSourceMapping().isEmpty());
	}

	@Test
	void evaluationAfterResetWorks() {
		DuplicateClassMapper instance = createInstance();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());
		instance.reset();
		instance.evaluate("source1", "packageName", "className", ClassFileVersion.JAVA_1_5.getMajorVersionCharacter());
		instance.evaluate("source2", "packageName", "className", ClassFileVersion.JAVA_1_6.getMajorVersionCharacter());

		assertEquals(1, instance.getDuplicates().size());
		assertEquals(1, instance.getClassSourceMapping().size());
	}
}
