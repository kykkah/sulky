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

import de.huxhorn.sulky.version.mappers.DuplicateClassMapper;
import de.huxhorn.sulky.version.mappers.HighestVersionMapper;
import de.huxhorn.sulky.version.mappers.PackageVersionMapper;
import de.huxhorn.sulky.version.mappers.SourceVersionMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFileScannerTest {

	private static final String SLF4J_API_179_JAR_NAME = "slf4j-api-1.7.9.jar";
	private static final String SLF4J_API_1710_JAR_NAME = "slf4j-api-1.7.10.jar";
	private static final String FOO_JAR_NAME = "foo.jar";
	private static final String FOO_DIRECTORY_NAME = "fooDirectory";
	private static final String SLF4J_DIRECTORY_NAME = "slf4j-api";

	@TempDir
	Path temporaryFolder;

	private Path slf4jApi179File;
	private Path slf4jApi1710File;
	private Path fooJarFile;
	private Path unzippedFooDirectory;
	private Path unzippedSlf4jDirectory;

	@BeforeEach
	void setUp() throws IOException {
		slf4jApi179File = copyResource(SLF4J_API_179_JAR_NAME);
		slf4jApi1710File = copyResource(SLF4J_API_1710_JAR_NAME);
		fooJarFile = copyResource(FOO_JAR_NAME);

		unzippedFooDirectory = temporaryFolder.resolve(FOO_DIRECTORY_NAME);
		Files.createDirectories(unzippedFooDirectory);
		unzip(fooJarFile, unzippedFooDirectory);

		unzippedSlf4jDirectory = temporaryFolder.resolve(SLF4J_DIRECTORY_NAME);
		Files.createDirectories(unzippedSlf4jDirectory);
		unzip(slf4jApi1710File, unzippedSlf4jDirectory);
	}

	@Test
	void scanSlf4jApiJar() throws IOException {
		ClassFileScanner scanner = new ClassFileScanner();
		HighestVersionMapper highestVersionMapper = new HighestVersionMapper();
		scanner.getClassStatisticMappers().add(highestVersionMapper);
		PackageVersionMapper packageVersionMapper = new PackageVersionMapper();
		scanner.getClassStatisticMappers().add(packageVersionMapper);

		scanner.scanJar(slf4jApi1710File.toFile());

		assertEquals((char) 0x31, highestVersionMapper.getHighestVersionChar());
		assertEquals(ClassFileVersion.JAVA_1_5, highestVersionMapper.getHighestVersion());

		Map<String, Set<Character>> packageVersions = packageVersionMapper.getPackageVersions();
		assertEquals(3, packageVersions.size());
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.spi"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.helpers"));
	}

	@Test
	void scanFooJar() throws IOException {
		ClassFileScanner scanner = new ClassFileScanner();
		HighestVersionMapper highestVersionMapper = new HighestVersionMapper();
		scanner.getClassStatisticMappers().add(highestVersionMapper);
		PackageVersionMapper packageVersionMapper = new PackageVersionMapper();
		scanner.getClassStatisticMappers().add(packageVersionMapper);

		scanner.scanJar(fooJarFile.toFile());

		assertEquals((char) 0x35, highestVersionMapper.getHighestVersionChar());
		assertEquals(ClassFileVersion.JAVA_9, highestVersionMapper.getHighestVersion());

		Map<String, Set<Character>> packageVersions = packageVersionMapper.getPackageVersions();
		assertEquals(2, packageVersions.size());
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34, (char) 0x35), toList(packageVersions, ""));
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34), toList(packageVersions, "some.pkg"));
	}

	@Test
	void scanMultipleFiles() throws IOException {
		ClassFileScanner scanner = new ClassFileScanner();
		HighestVersionMapper highestVersionMapper = new HighestVersionMapper();
		scanner.getClassStatisticMappers().add(highestVersionMapper);
		PackageVersionMapper packageVersionMapper = new PackageVersionMapper();
		scanner.getClassStatisticMappers().add(packageVersionMapper);
		SourceVersionMapper sourceVersionMapper = new SourceVersionMapper();
		scanner.getClassStatisticMappers().add(sourceVersionMapper);
		DuplicateClassMapper duplicateClassMapper = new DuplicateClassMapper();
		scanner.getClassStatisticMappers().add(duplicateClassMapper);

		scanner.scanJar(slf4jApi179File.toFile());
		scanner.scanJar(slf4jApi1710File.toFile());
		scanner.scanJar(fooJarFile.toFile());

		assertEquals((char) 0x35, highestVersionMapper.getHighestVersionChar());
		assertEquals(ClassFileVersion.JAVA_9, highestVersionMapper.getHighestVersion());

		Map<String, Set<Character>> packageVersions = packageVersionMapper.getPackageVersions();
		assertEquals(5, packageVersions.size());
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34, (char) 0x35), toList(packageVersions, ""));
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34), toList(packageVersions, "some.pkg"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.spi"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.helpers"));

		Map<String, Set<Character>> sourceVersions = sourceVersionMapper.getSourceVersions();
		assertEquals(List.of((char) 0x31), toList(sourceVersions, SLF4J_API_179_JAR_NAME));
		assertEquals(List.of((char) 0x31), toList(sourceVersions, SLF4J_API_1710_JAR_NAME));
		List<Character> fooVersions = toList(sourceVersions, FOO_JAR_NAME);
		assertTrue(fooVersions.containsAll(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34)));

		assertFalse(duplicateClassMapper.getDuplicates().isEmpty());
	}

	@Test
	void scanWithReset() throws IOException {
		ClassFileScanner scanner = new ClassFileScanner();
		HighestVersionMapper highestVersionMapper = new HighestVersionMapper();
		scanner.getClassStatisticMappers().add(highestVersionMapper);
		PackageVersionMapper packageVersionMapper = new PackageVersionMapper();
		scanner.getClassStatisticMappers().add(packageVersionMapper);
		DuplicateClassMapper duplicateClassMapper = new DuplicateClassMapper();
		scanner.getClassStatisticMappers().add(duplicateClassMapper);

		scanner.scanJar(fooJarFile.toFile());
		scanner.scanJar(slf4jApi179File.toFile());
		scanner.scanJar(slf4jApi1710File.toFile());
		scanner.reset();

		assertEquals(0, highestVersionMapper.getHighestVersionChar());
		assertEquals(0, packageVersionMapper.getPackageVersions().size());
		assertEquals(0, duplicateClassMapper.getClassSourceMapping().size());
		assertEquals(0, duplicateClassMapper.getDuplicates().size());
	}

	@Test
	void scanDirectories() throws IOException {
		ClassFileScanner scanner = new ClassFileScanner();
		HighestVersionMapper highestVersionMapper = new HighestVersionMapper();
		scanner.getClassStatisticMappers().add(highestVersionMapper);
		PackageVersionMapper packageVersionMapper = new PackageVersionMapper();
		scanner.getClassStatisticMappers().add(packageVersionMapper);
		SourceVersionMapper sourceVersionMapper = new SourceVersionMapper();
		scanner.getClassStatisticMappers().add(sourceVersionMapper);

		scanner.scanDirectory(unzippedFooDirectory.toFile());
		scanner.scanDirectory(unzippedSlf4jDirectory.toFile());
		scanner.scanDirectory(unzippedSlf4jDirectory.toFile(), "manualSource");

		assertEquals((char) 0x35, highestVersionMapper.getHighestVersionChar());
		assertEquals(ClassFileVersion.JAVA_9, highestVersionMapper.getHighestVersion());

		Map<String, Set<Character>> packageVersions = packageVersionMapper.getPackageVersions();
		assertEquals(5, packageVersions.size());
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34, (char) 0x35), toList(packageVersions, ""));
		assertEquals(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34), toList(packageVersions, "some.pkg"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.spi"));
		assertEquals(List.of((char) 0x31), toList(packageVersions, "org.slf4j.helpers"));

		Map<String, Set<Character>> sourceVersions = sourceVersionMapper.getSourceVersions();
		assertEquals(List.of((char) 0x31), toList(sourceVersions, SLF4J_DIRECTORY_NAME));
		List<Character> fooDirectoryVersions = toList(sourceVersions, FOO_DIRECTORY_NAME);
		assertTrue(fooDirectoryVersions.containsAll(List.of((char) 0x31, (char) 0x32, (char) 0x33, (char) 0x34)));
		assertEquals(List.of((char) 0x31), toList(sourceVersions, "manualSource"));
		assertEquals(3, sourceVersions.size());
	}

	private Path copyResource(String resourceName) throws IOException {
		Path target = temporaryFolder.resolve(resourceName);
		try (InputStream input = getClass().getResourceAsStream("/" + resourceName)) {
			assertNotNull(input, "Missing test resource: " + resourceName);
			Files.copy(input, target);
		}
		return target;
	}

	private static void unzip(Path file, Path outputDirectory) throws IOException {
		try (ZipFile zipFile = new ZipFile(file.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Path destination = outputDirectory.resolve(entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectories(destination);
				} else {
					if (destination.getParent() != null) {
						Files.createDirectories(destination.getParent());
					}
					try (InputStream stream = zipFile.getInputStream(entry)) {
						Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
	}

	private static List<Character> toList(Map<String, Set<Character>> map, String key) {
		Set<Character> value = map.get(key);
		if (value == null) {
			return List.of();
		}
		return new ArrayList<>(value);
	}
}
