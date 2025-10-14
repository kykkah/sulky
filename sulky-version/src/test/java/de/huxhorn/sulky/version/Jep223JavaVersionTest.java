package de.huxhorn.sulky.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Jep223JavaVersionTest {

	@ParameterizedTest(name = "{0}")
	@MethodSource("parseArguments")
	void parseMatches(String description, String versionString, Jep223JavaVersion expectedVersion) {
		Jep223JavaVersion parsed = Jep223JavaVersion.parse(versionString);
		assertEquals(expectedVersion, parsed, description);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("getterArguments")
	@SuppressWarnings("PMD.ExcessiveParameterList")
	void gettersReturnExpectedValues(String description,
			String versionString,
			List<Integer> expectedVersionNumbers,
			int expectedMajor,
			int expectedMinor,
			int expectedSecurity,
			int expectedEmergencyPatch,
			String expectedPre,
			int expectedBuild,
			String expectedOpt) {
		Jep223JavaVersion version = Jep223JavaVersion.parse(versionString);

		assertEquals(expectedVersionNumbers, version.getVersionNumbers(), description + " version numbers");
		assertEquals(expectedMajor, version.getMajor(), description + " major");
		assertEquals(expectedMajor, version.getFeature(), description + " feature");
		assertEquals(expectedMinor, version.getMinor(), description + " minor");
		assertEquals(expectedMinor, version.getInterim(), description + " interim");
		assertEquals(expectedSecurity, version.getSecurity(), description + " security");
		assertEquals(expectedSecurity, version.getPatch(), description + " patch");
		assertEquals(expectedSecurity, version.getUpdate(), description + " update");
		assertEquals(expectedEmergencyPatch, version.getEmergencyPatch(), description + " emergency patch");
		assertEquals(expectedPre, version.getPreReleaseIdentifier(), description + " pre-release");
		assertEquals(expectedBuild, version.getBuildNumber(), description + " build");
		assertEquals(expectedOpt, version.getAdditionalBuildInformation(), description + " opt");
	}

	@Test
	void parseNullThrowsException() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> Jep223JavaVersion.parse(null));
		assertEquals("versionString must not be null!", exception.getMessage());
	}

	@Test
	void intArrayConstructorWithNullThrowsException() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> new Jep223JavaVersion((int[]) null, null, 0, null));
		assertEquals("versionNumbers must not be null!", exception.getMessage());
	}

	@Test
	void integerArrayConstructorWithNullThrowsException() {
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> new Jep223JavaVersion((Integer[]) null, null, 0, null));
		assertEquals("versionNumbers must not be null!", exception.getMessage());
	}

	@Test
	void constructorWithEmptyIntArrayThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new int[0], null, 0, null));
		assertEquals("versionNumbers.length must not be zero!", exception.getMessage());
	}

	@Test
	void constructorWithEmptyIntegerArrayThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new Integer[0], null, 0, null));
		assertEquals("versionNumbers.length must not be zero!", exception.getMessage());
	}

	@Test
	void constructorWithNullElementThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new Integer[] {1, null, 2}, null, 0, null));
		assertEquals("versionNumbers must not contain null values!", exception.getMessage());
	}

	@Test
	void constructorWithNegativeBuildThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new Integer[] {1, 2}, null, -1, null));
		assertEquals("buildNumber must not be negative!", exception.getMessage());
	}

	@Test
	void constructorWithIllegalPreReleaseThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new Integer[] {1, 2}, "1#2", 0, null));
		assertEquals("preReleaseIdentifier '1#2' is illegal. It doesn't match the pattern '([a-zA-Z0-9]+)'.",
				exception.getMessage());
	}

	@Test
	void constructorWithIllegalBuildInfoThrowsException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new Jep223JavaVersion(new Integer[] {1, 2}, null, 0, "1#2"));
		assertEquals("additionalBuildInformation '1#2' is illegal. It doesn't match the pattern '([-a-zA-Z0-9.]+)'.",
				exception.getMessage());
	}

	@Test
	void equalsWithNullReturnsFalse() {
		Jep223JavaVersion version = new Jep223JavaVersion(new Integer[] {8, 1}, null, 0, null);
		assertNotEquals(null, version);
	}

	@Test
	void equalsWithDifferentClassReturnsFalse() {
		Jep223JavaVersion version = new Jep223JavaVersion(new Integer[] {8, 1}, null, 0, null);
		assertNotEquals(version, 1);
	}

	@Test
	void equalsWithSameInstanceReturnsTrue() {
		Jep223JavaVersion version = new Jep223JavaVersion(new Integer[] {8, 1}, null, 0, null);
		assertEquals(version, version);
	}

	@Test
	void compareToNullThrowsException() {
		Jep223JavaVersion version = new Jep223JavaVersion(new Integer[] {8, 1}, null, 0, null);
		NullPointerException exception = assertThrows(NullPointerException.class, () -> version.compareTo(null));
		assertEquals("other must not be null!", exception.getMessage());
	}

	@Test
	void versionNumbersArrayIsImmutable() {
		Integer[] versions = {1, 2, 3};
		Jep223JavaVersion version = new Jep223JavaVersion(versions, null, 0, null);
		versions[1] = 17;
		assertEquals("1.2.3", version.toVersionString());
	}

	@Test
	void versionNumbersListIsImmutable() {
		Integer[] versions = {1, 2, 3};
		Jep223JavaVersion version = new Jep223JavaVersion(versions, null, 0, null);
		assertThrows(UnsupportedOperationException.class, () -> version.getVersionNumbers().remove(0));
	}

	@Test
	void versionNumbersListContainsExpectedValues() {
		Integer[] versions = {1, 2, 3, 4, 5};
		Jep223JavaVersion version = new Jep223JavaVersion(versions, null, 0, null);
		assertEquals(Arrays.asList(1, 2, 3, 4, 5), version.getVersionNumbers());
	}

	@ParameterizedTest(name = "{0} vs {1}")
	@MethodSource("comparisonArguments")
	void compareAndEquals(String versionAString, String versionBString, int expectedCompareSign, boolean expectedEquals) {
		Jep223JavaVersion versionA = Jep223JavaVersion.parse(versionAString);
		Jep223JavaVersion versionB = Jep223JavaVersion.parse(versionBString);

		int compareAB = versionA.compareTo(versionB);
		int compareBA = versionB.compareTo(versionA);

		assertEquals(expectedCompareSign, Integer.signum(compareAB));
		assertEquals(-expectedCompareSign, Integer.signum(compareBA));

		assertEquals(expectedEquals, versionA.equals(versionB));
		assertEquals(expectedEquals, versionB.equals(versionA));

		if (expectedEquals) {
			assertEquals(versionA.hashCode(), versionB.hashCode());
		} else {
			assertNotEquals(versionA.hashCode(), versionB.hashCode(), "hash codes can differ when versions are not equal");
		}
	}

	@ParameterizedTest
	@MethodSource("withoutPreArguments")
	void withoutPreReleaseIdentifierWorks(Jep223JavaVersion input, Jep223JavaVersion expected) {
		Jep223JavaVersion result = input.withoutPreReleaseIdentifier();
		assertEquals(expected, result);
		if (input.getPreReleaseIdentifier() == null) {
			assertSame(input, result);
		}
	}

	private static Stream<Arguments> parseArguments() {
		return Stream.of(
				Arguments.of("9.0.0-ea+19", "9.0.0-ea+19", jep(new int[] {9, 0, 0}, "ea", 19, null)),
				Arguments.of("9.0.0+100", "9.0.0+100", jep(new int[] {9, 0, 0}, null, 100, null)),
				Arguments.of("9.0.1+20", "9.0.1+20", jep(new int[] {9, 0, 1}, null, 20, null)),
				Arguments.of("9.0.2+12", "9.0.2+12", jep(new int[] {9, 0, 2}, null, 12, null)),
				Arguments.of("9.1.2+62", "9.1.2+62", jep(new int[] {9, 1, 2}, null, 62, null)),
				Arguments.of("9.1.3+15", "9.1.3+15", jep(new int[] {9, 1, 3}, null, 15, null)),
				Arguments.of("9.1.4+8", "9.1.4+8", jep(new int[] {9, 1, 4}, null, 8, null)),
				Arguments.of("9.2.4+45", "9.2.4+45", jep(new int[] {9, 2, 4}, null, 45, null)),
				Arguments.of("9-ea", "9-ea", jep(new int[] {9}, "ea", 0, null)),
				Arguments.of("9", "9", jep(new int[] {9}, null, 0, null)),
				Arguments.of("9.0.1", "9.0.1", jep(new int[] {9, 0, 1}, null, 0, null)),
				Arguments.of("9.0.2", "9.0.2", jep(new int[] {9, 0, 2}, null, 0, null)),
				Arguments.of("9.1.2", "9.1.2", jep(new int[] {9, 1, 2}, null, 0, null)),
				Arguments.of("9.1.3", "9.1.3", jep(new int[] {9, 1, 3}, null, 0, null)),
				Arguments.of("9.1.4", "9.1.4", jep(new int[] {9, 1, 4}, null, 0, null)),
				Arguments.of("9.2.4", "9.2.4", jep(new int[] {9, 2, 4}, null, 0, null)),
				Arguments.of("7.4.10+11", "7.4.10+11", jep(new int[] {7, 4, 10}, null, 11, null)),
				Arguments.of("7.4.11+15", "7.4.11+15", jep(new int[] {7, 4, 11}, null, 15, null)),
				Arguments.of("7.5.11+43", "7.5.11+43", jep(new int[] {7, 5, 11}, null, 43, null)),
				Arguments.of("7.5.12+18", "7.5.12+18", jep(new int[] {7, 5, 12}, null, 18, null)),
				Arguments.of("7.5.13+13", "7.5.13+13", jep(new int[] {7, 5, 13}, null, 13, null)),
				Arguments.of("7.6.14+13", "7.6.14+13", jep(new int[] {7, 6, 14}, null, 13, null)),
				Arguments.of("7.6.14+19", "7.6.14+19", jep(new int[] {7, 6, 14}, null, 19, null)),
				Arguments.of("7.6.15+20", "7.6.15+20", jep(new int[] {7, 6, 15}, null, 20, null)),
				Arguments.of("7.4.10", "7.4.10", jep(new int[] {7, 4, 10}, null, 0, null)),
				Arguments.of("7.4.11", "7.4.11", jep(new int[] {7, 4, 11}, null, 0, null)),
				Arguments.of("7.5.11", "7.5.11", jep(new int[] {7, 5, 11}, null, 0, null)),
				Arguments.of("7.5.12", "7.5.12", jep(new int[] {7, 5, 12}, null, 0, null)),
				Arguments.of("7.5.13", "7.5.13", jep(new int[] {7, 5, 13}, null, 0, null)),
				Arguments.of("7.5.14", "7.5.14", jep(new int[] {7, 5, 14}, null, 0, null)),
				Arguments.of("7.6.14", "7.6.14", jep(new int[] {7, 6, 14}, null, 0, null)),
				Arguments.of("7.6.15", "7.6.15", jep(new int[] {7, 6, 15}, null, 0, null)),
				Arguments.of("9.7.6.5.4.3", "9.7.6.5.4.3", jep(new int[] {9, 7, 6, 5, 4, 3}, null, 0, null)),
				Arguments.of("9.7.6.5.4-ea-opt", "9.7.6.5.4-ea-opt", jep(new int[] {9, 7, 6, 5, 4}, "ea", 0, "opt")),
				Arguments.of("9.7.6.5.4.3.2.1-ea+49-opt", "9.7.6.5.4.3.2.1-ea+49-opt",
					jep(new int[] {9, 7, 6, 5, 4, 3, 2, 1}, "ea", 49, "opt")),
				Arguments.of("9.1.0", "9.1.0", jep(new int[] {9, 1, 0}, null, 0, null)),
				Arguments.of("9-internal+17-2015-07-14-120103.iris.verona", "9-internal+17-2015-07-14-120103.iris.verona",
					jep(new int[] {9}, "internal", 17, "2015-07-14-120103.iris.verona")),
				Arguments.of("11.0.2+13-LTS", "11.0.2+13-LTS", jep(new int[] {11, 0, 2}, null, 13, "LTS")),
				Arguments.of("11.0.2.17+13-LTS", "11.0.2.17+13-LTS", jep(new int[] {11, 0, 2, 17}, null, 13, "LTS"))
		);
	}

	private static Stream<Arguments> getterArguments() {
		return Stream.of(
				Arguments.of("9.0.0-ea+19", "9.0.0-ea+19", Arrays.asList(9, 0, 0), 9, 0, 0, 0, "ea", 19, null),
				Arguments.of("9.0.0+100", "9.0.0+100", Arrays.asList(9, 0, 0), 9, 0, 0, 0, null, 100, null),
				Arguments.of("9.0.1+20", "9.0.1+20", Arrays.asList(9, 0, 1), 9, 0, 1, 0, null, 20, null),
				Arguments.of("9.0.2+12", "9.0.2+12", Arrays.asList(9, 0, 2), 9, 0, 2, 0, null, 12, null),
				Arguments.of("9.1.2+62", "9.1.2+62", Arrays.asList(9, 1, 2), 9, 1, 2, 0, null, 62, null),
				Arguments.of("9.1.3+15", "9.1.3+15", Arrays.asList(9, 1, 3), 9, 1, 3, 0, null, 15, null),
				Arguments.of("9.1.4+8", "9.1.4+8", Arrays.asList(9, 1, 4), 9, 1, 4, 0, null, 8, null),
				Arguments.of("9.2.4+45", "9.2.4+45", Arrays.asList(9, 2, 4), 9, 2, 4, 0, null, 45, null),
				Arguments.of("9-ea", "9-ea", Arrays.asList(9), 9, 0, 0, 0, "ea", 0, null),
				Arguments.of("9", "9", Arrays.asList(9), 9, 0, 0, 0, null, 0, null),
				Arguments.of("9.0.1", "9.0.1", Arrays.asList(9, 0, 1), 9, 0, 1, 0, null, 0, null),
				Arguments.of("9.0.2", "9.0.2", Arrays.asList(9, 0, 2), 9, 0, 2, 0, null, 0, null),
				Arguments.of("9.1.2", "9.1.2", Arrays.asList(9, 1, 2), 9, 1, 2, 0, null, 0, null),
				Arguments.of("9.1.3", "9.1.3", Arrays.asList(9, 1, 3), 9, 1, 3, 0, null, 0, null),
				Arguments.of("9.1.4", "9.1.4", Arrays.asList(9, 1, 4), 9, 1, 4, 0, null, 0, null),
				Arguments.of("9.2.4", "9.2.4", Arrays.asList(9, 2, 4), 9, 2, 4, 0, null, 0, null),
				Arguments.of("7.4.10+11", "7.4.10+11", Arrays.asList(7, 4, 10), 7, 4, 10, 0, null, 11, null),
				Arguments.of("7.4.11+15", "7.4.11+15", Arrays.asList(7, 4, 11), 7, 4, 11, 0, null, 15, null),
				Arguments.of("7.5.11+43", "7.5.11+43", Arrays.asList(7, 5, 11), 7, 5, 11, 0, null, 43, null),
				Arguments.of("7.5.12+18", "7.5.12+18", Arrays.asList(7, 5, 12), 7, 5, 12, 0, null, 18, null),
				Arguments.of("7.5.13+13", "7.5.13+13", Arrays.asList(7, 5, 13), 7, 5, 13, 0, null, 13, null),
				Arguments.of("7.6.14+13", "7.6.14+13", Arrays.asList(7, 6, 14), 7, 6, 14, 0, null, 13, null),
				Arguments.of("7.6.14+19", "7.6.14+19", Arrays.asList(7, 6, 14), 7, 6, 14, 0, null, 19, null),
				Arguments.of("7.6.15+20", "7.6.15+20", Arrays.asList(7, 6, 15), 7, 6, 15, 0, null, 20, null),
				Arguments.of("7.4.10", "7.4.10", Arrays.asList(7, 4, 10), 7, 4, 10, 0, null, 0, null),
				Arguments.of("7.4.11", "7.4.11", Arrays.asList(7, 4, 11), 7, 4, 11, 0, null, 0, null),
				Arguments.of("7.5.11", "7.5.11", Arrays.asList(7, 5, 11), 7, 5, 11, 0, null, 0, null),
				Arguments.of("7.5.12", "7.5.12", Arrays.asList(7, 5, 12), 7, 5, 12, 0, null, 0, null),
				Arguments.of("7.5.13", "7.5.13", Arrays.asList(7, 5, 13), 7, 5, 13, 0, null, 0, null),
				Arguments.of("7.5.14", "7.5.14", Arrays.asList(7, 5, 14), 7, 5, 14, 0, null, 0, null),
				Arguments.of("7.6.14", "7.6.14", Arrays.asList(7, 6, 14), 7, 6, 14, 0, null, 0, null),
				Arguments.of("7.6.15", "7.6.15", Arrays.asList(7, 6, 15), 7, 6, 15, 0, null, 0, null),
			Arguments.of("9.7.6.5.4.3", "9.7.6.5.4.3", Arrays.asList(9, 7, 6, 5, 4, 3), 9, 7, 6, 5, null, 0, null),
			Arguments.of("9.7.6.5.4-ea-opt", "9.7.6.5.4-ea-opt", Arrays.asList(9, 7, 6, 5, 4), 9, 7, 6, 5, "ea", 0, "opt"),
				Arguments.of("9.7.6.5.4.3.2.1-ea+49-opt", "9.7.6.5.4.3.2.1-ea+49-opt",
					Arrays.asList(9, 7, 6, 5, 4, 3, 2, 1), 9, 7, 6, 5, "ea", 49, "opt"),
				Arguments.of("9.1.0", "9.1.0", Arrays.asList(9, 1, 0), 9, 1, 0, 0, null, 0, null),
				Arguments.of("9-internal+17-2015-07-14-120103.iris.verona", "9-internal+17-2015-07-14-120103.iris.verona",
					Arrays.asList(9), 9, 0, 0, 0, "internal", 17, "2015-07-14-120103.iris.verona"),
				Arguments.of("11.0.2+13-LTS", "11.0.2+13-LTS", Arrays.asList(11, 0, 2), 11, 0, 2, 0, null, 13, "LTS"),
				Arguments.of("11.0.2.17+13-LTS", "11.0.2.17+13-LTS", Arrays.asList(11, 0, 2, 17), 11, 0, 2, 17, null, 13, "LTS"));
	}

	private static Stream<Arguments> comparisonArguments() {
		return Stream.of(
				Arguments.of("9", "9.0.0", 0, false),
				Arguments.of("9.0", "9.0.0", 0, false),
				Arguments.of("9.0.0", "9.0.0", 0, true),
				Arguments.of("9", "10.0.0", -1, false),
				Arguments.of("9.0", "10.0.0", -1, false),
				Arguments.of("9.0.0", "10.0.0", -1, false),
				Arguments.of("9", "9.1.0", -1, false),
				Arguments.of("9.0", "9.1.0", -1, false),
				Arguments.of("9.0.0", "9.1.0", -1, false),
				Arguments.of("9", "9.0.1", -1, false),
				Arguments.of("9.0", "9.0.1", -1, false),
				Arguments.of("9.0.0", "9.0.1", -1, false),
				Arguments.of("9.0.0", "9-ea", 1, false),
				Arguments.of("9", "9-ea", 1, false),
				Arguments.of("9-ea", "9-eb", -1, false),
				Arguments.of("9.1.2-ea+49-optA", "9.1.2-ea+49-optA", 0, true),
				Arguments.of("9.1.2-ea+49-optA", "9.1.2-ea+70-optB", 0, false),
				Arguments.of("9.1.2-ea+49-optA", "9.1.2-ea+49", 0, false),
				Arguments.of("11.0.2+13-LTS", "11.0.2+13-LTS", 0, true),
				Arguments.of("11.0.2+13-LTS", "11.0.2.0+13-LTS", 0, false),
				Arguments.of("11.0.2.17+13-LTS", "11.0.2.17+13-LTS", 0, true),
				Arguments.of("11.0.2+13-LTS", "11.0.2.17+13-LTS", -1, false),
				Arguments.of("11.0.2.17+13-LTS", "11.0.2+13-LTS", 1, false));
	}

	private static Stream<Arguments> withoutPreArguments() {
		return Stream.of(
				Arguments.of(jep(new int[] {0, 0, 0, 0}, "foo", 17, "bar"), jep(new int[] {0, 0, 0, 0}, null, 17, "bar")),
				Arguments.of(jep(new int[] {1, 2, 3, 4}, "foo", 17, "bar"), jep(new int[] {1, 2, 3, 4}, null, 17, "bar")),
				Arguments.of(jep(new int[] {1, 2, 3, 4}, null, 17, "bar"), jep(new int[] {1, 2, 3, 4}, null, 17, "bar"))
		);
	}

	private static Jep223JavaVersion jep(int[] versions, String pre, int build, String opt) {
		return new Jep223JavaVersion(versions, pre, build, opt);
	}
}
