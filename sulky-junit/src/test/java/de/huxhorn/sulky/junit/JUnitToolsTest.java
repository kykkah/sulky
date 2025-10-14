package de.huxhorn.sulky.junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitToolsTest
{
	@TempDir
	Path tempDir;

	private static Stream<Arguments> cloneValuesWithSame()
	{
		return Stream.of(
			Arguments.of(new WorkingCloneableClass("foo"), false),
			Arguments.of(HackyCloneableSingletonClass.INSTANCE, true)
		);
	}

	private static Stream<WorkingCloneableClass> differentCloneValues()
	{
		return Stream.of(new WorkingCloneableClass("foo"));
	}

	private static Stream<HackyCloneableSingletonClass> sameCloneValues()
	{
		return Stream.of(HackyCloneableSingletonClass.INSTANCE);
	}

	private static Stream<Arguments> serializableValuesWithSame()
	{
		return Stream.of(
			Arguments.of(distinctInteger(), false),
			Arguments.of("a string", false),
			Arguments.of(DayOfWeek.FRIDAY, true)
		);
	}

	private static Stream<Serializable> differentSerializableValues()
	{
		return Stream.of(distinctInteger(), "a string");
	}

	private static Stream<Serializable> sameSerializableValues()
	{
		return Stream.of(DayOfWeek.FRIDAY);
	}

	private static Stream<Arguments> xmlSerializationFailureValues()
	{
		return Stream.of(Arguments.of(new ClassWithoutDefaultConstructor("nope")));
	}

	@ParameterizedTest
	@MethodSource("cloneValuesWithSame")
	void testCloneWorksAsExpected(Cloneable value, boolean same) throws Exception
	{
		Cloneable result = JUnitTools.testClone(value, same);
		assertNotNull(result);

		assertThrows(AssertionError.class, () -> JUnitTools.testClone(value, !same));
	}

	@ParameterizedTest
	@MethodSource("differentCloneValues")
	void testCloneDoesNotFail(Cloneable value) throws Exception
	{
		assertNotNull(JUnitTools.testClone(value));
	}

	@ParameterizedTest
	@MethodSource("sameCloneValues")
	void testCloneFails(Cloneable value)
	{
		assertThrows(AssertionError.class, () -> JUnitTools.testClone(value));
	}

	@ParameterizedTest
	@MethodSource("serializableValuesWithSame")
	void testSerializationWorksAsExpected(Serializable value, boolean same) throws Exception
	{
		assertNotNull(JUnitTools.testSerialization(value, same));
		assertThrows(AssertionError.class, () -> JUnitTools.testSerialization(value, !same));
	}

	@ParameterizedTest
	@MethodSource("differentSerializableValues")
	void testSerializationDoesNotFail(Serializable value) throws Exception
	{
		assertNotNull(JUnitTools.testSerialization(value));
	}

	@ParameterizedTest
	@MethodSource("sameSerializableValues")
	void testSerializationFails(Serializable value)
	{
		assertThrows(AssertionError.class, () -> JUnitTools.testSerialization(value));
	}

	@ParameterizedTest
	@MethodSource("serializableValuesWithSame")
	void testXmlSerializationWorksAsExpected(Serializable value, boolean same) throws Exception
	{
		assertNotNull(JUnitTools.testXmlSerialization(value, same));
		assertThrows(AssertionError.class, () -> JUnitTools.testXmlSerialization(value, !same));
	}

	@ParameterizedTest
	@MethodSource("differentSerializableValues")
	void testXmlSerializationDoesNotFail(Serializable value) throws Exception
	{
		assertNotNull(JUnitTools.testXmlSerialization(value));
	}

	@ParameterizedTest
	@MethodSource("sameSerializableValues")
	void testXmlSerializationFails(Serializable value)
	{
		assertThrows(AssertionError.class, () -> JUnitTools.testXmlSerialization(value));
	}

	@ParameterizedTest
	@MethodSource("xmlSerializationFailureValues")
	void testXmlSerializationAlsoFails(Serializable value)
	{
		assertThrows(Throwable.class, () -> JUnitTools.testXmlSerialization(value));
	}

	@Test
	void equalWithNullArgumentsAcceptsAnySameValue()
	{
		assertDoesNotThrow(() -> {
			JUnitTools.equal(null, null, true);
			JUnitTools.equal(null, null, false);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = "logback-test.xml")
	void copyResourceToFileWithSpecificTimeWorks(String resourceName) throws Exception
	{
		Path rootDirectory = Files.createDirectory(tempDir.resolve(UUID.randomUUID().toString()));
		Path targetFile = rootDirectory.resolve(resourceName);
		long lastModified = System.currentTimeMillis();
		lastModified = (lastModified / 1000L) * 1000L;
		lastModified = lastModified - (3600L * 1000L);

		JUnitTools.copyResourceToFile("/" + resourceName, targetFile.toFile(), lastModified);

		assertTrue(Files.isRegularFile(targetFile));
		assertEquals(lastModified, Files.getLastModifiedTime(targetFile).toMillis());
	}

	@ParameterizedTest
	@ValueSource(strings = "logback-test.xml")
	void copyResourceToFileWithNegativeTimeWorks(String resourceName) throws Exception
	{
		Path rootDirectory = Files.createDirectory(tempDir.resolve(UUID.randomUUID().toString()));
		Path targetFile = rootDirectory.resolve(resourceName);
		long lastModified = System.currentTimeMillis();
		lastModified = (lastModified / 1000L) * 1000L;

		JUnitTools.copyResourceToFile("/" + resourceName, targetFile.toFile(), -1);

		assertTrue(Files.isRegularFile(targetFile));
		assertTrue(Files.getLastModifiedTime(targetFile).toMillis() >= lastModified);
	}

	@ParameterizedTest
	@ValueSource(strings = "logback-test.xml")
	void copyResourceToFileWorks(String resourceName) throws Exception
	{
		Path rootDirectory = Files.createDirectory(tempDir.resolve(UUID.randomUUID().toString()));
		Path targetFile = rootDirectory.resolve(resourceName);
		long lastModified = System.currentTimeMillis();
		lastModified = (lastModified / 1000L) * 1000L;

		JUnitTools.copyResourceToFile("/" + resourceName, targetFile.toFile());

		assertTrue(Files.isRegularFile(targetFile));
		assertTrue(Files.getLastModifiedTime(targetFile).toMillis() >= lastModified);
	}

	@ParameterizedTest
	@ValueSource(strings = "missing-resource.snafu")
	void copyResourceToFileFails(String resourceName)
	{
		Path rootDirectory = tempDir.resolve(UUID.randomUUID().toString());
		File targetFile = rootDirectory.resolve(resourceName).toFile();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> JUnitTools.copyResourceToFile("/" + resourceName, targetFile, System.currentTimeMillis()));
		assertEquals("Could not find resource '/" + resourceName + "' in classpath!", exception.getMessage());
	}

	private static final class WorkingCloneableClass
		implements Cloneable
	{
		private final String value;

		private WorkingCloneableClass(String value)
		{
			this.value = value;
		}

		@Override
		public WorkingCloneableClass clone() throws CloneNotSupportedException
		{
			return (WorkingCloneableClass) super.clone();
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}
			WorkingCloneableClass that = (WorkingCloneableClass) o;
			return value != null ? value.equals(that.value) : that.value == null;
		}

		@Override
		public int hashCode()
		{
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public String toString()
		{
			return "WorkingCloneableClass{" +
				"value='" + value + '\'' +
				'}';
		}
	}

	private static final class HackyCloneableSingletonClass
		implements Cloneable
	{
		private static final HackyCloneableSingletonClass INSTANCE = new HackyCloneableSingletonClass("Connor MacLeod");

		private final String value;

		private HackyCloneableSingletonClass(String value)
		{
			this.value = value;
		}

		@Override
		public HackyCloneableSingletonClass clone()
		{
			return INSTANCE;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}
			HackyCloneableSingletonClass that = (HackyCloneableSingletonClass) o;
			return value != null ? value.equals(that.value) : that.value == null;
		}

		@Override
		public int hashCode()
		{
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public String toString()
		{
			return "HackyCloneableSingletonClass{" +
				"value='" + value + '\'' +
				'}';
		}
	}

	private static final class ClassWithoutDefaultConstructor
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String value;

		private ClassWithoutDefaultConstructor(String value)
		{
			this.value = value;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}
			ClassWithoutDefaultConstructor that = (ClassWithoutDefaultConstructor) o;
			return value != null ? value.equals(that.value) : that.value == null;
		}

		@Override
		public int hashCode()
		{
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public String toString()
		{
			return "ClassWithoutDefaultConstructor{" +
				"value='" + value + '\'' +
				'}';
		}
	}

	private static Integer distinctInteger()
	{
		return Integer.valueOf(256);
	}
}
