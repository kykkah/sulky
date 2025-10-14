package de.huxhorn.sulky.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SafeStringTest {

	private static final Map<String, Object> RECURSIVE_MAP;
	private static final String RECURSIVE_MAP_EXPECTED;

	private static final List<List<?>> RECURSIVE_LIST;
	private static final String RECURSIVE_LIST_EXPECTED;

	private static final Object[] RECURSIVE_OBJECT_ARRAY;
	private static final String RECURSIVE_OBJECT_ARRAY_EXPECTED;

	private static final ProblematicToString PROBLEMATIC_1;
	private static final String PROBLEMATIC_1_EXPECTED;

	private static final ProblematicToString PROBLEMATIC_2;
	private static final String PROBLEMATIC_2_EXPECTED;

	private static final ProblematicToString PROBLEMATIC_3;
	private static final String PROBLEMATIC_3_EXPECTED;

	private static final Map<String, List<String>> LIST_INSIDE_TREE_MAP;
	private static final Map<String, String[]> ARRAY_INSIDE_TREE_MAP;

	private static final List<List<String>> LIST_INSIDE_LIST;
	private static final List<String[]> ARRAY_INSIDE_LIST;

	private static final Object[][] VALID_VALUE_CASES;
	private static final Object[][] INVALID_VALUE_CASES;

	static {
		Map<String, Object> aMap = new HashMap<>();
		Map<String, Object> bMap = new HashMap<>();
		bMap.put("bar", aMap);
		aMap.put("foo", bMap);
		RECURSIVE_MAP = aMap;
		RECURSIVE_MAP_EXPECTED = "{foo={bar=" + SafeString.RECURSION_PREFIX + SafeString.identityToString(aMap)
				+ SafeString.RECURSION_SUFFIX + "}}";

		List<List<?>> outerList = new ArrayList<>();
		List<List<?>> innerList = new ArrayList<>();
		innerList.add(outerList);
		outerList.add(innerList);
		RECURSIVE_LIST = outerList;
		RECURSIVE_LIST_EXPECTED = "[[" + SafeString.RECURSION_PREFIX + SafeString.identityToString(outerList)
				+ SafeString.RECURSION_SUFFIX + "]]";

		Object[] outerArray = new Object[2];
		Object[] innerArray = new Object[2];
		innerArray[1] = outerArray;
		outerArray[1] = innerArray;
		RECURSIVE_OBJECT_ARRAY = outerArray;
		RECURSIVE_OBJECT_ARRAY_EXPECTED = "[null, [null, " + SafeString.RECURSION_PREFIX + SafeString.identityToString(outerArray)
				+ SafeString.RECURSION_SUFFIX + "]]";

		PROBLEMATIC_1 = new ProblematicToString(null);
		PROBLEMATIC_1_EXPECTED = SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_1)
				+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_SUFFIX;

		PROBLEMATIC_2 = new ProblematicToString(FooThrowable.class.getName());
		PROBLEMATIC_2_EXPECTED = SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_2)
				+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_SUFFIX;

		String message = "Message";
		PROBLEMATIC_3 = new ProblematicToString(message);
		PROBLEMATIC_3_EXPECTED = SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_3)
				+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_MSG_SEPARATOR + message
				+ SafeString.ERROR_SUFFIX;

		List<String> sharedList = new ArrayList<>(List.of("One", "Two"));
		LIST_INSIDE_TREE_MAP = new TreeMap<>();
		LIST_INSIDE_TREE_MAP.put("foo", sharedList);
		LIST_INSIDE_TREE_MAP.put("bar", sharedList);

		String[] sharedArray = new String[] {"One", "Two"};
		ARRAY_INSIDE_TREE_MAP = new TreeMap<>();
		ARRAY_INSIDE_TREE_MAP.put("foo", sharedArray);
		ARRAY_INSIDE_TREE_MAP.put("bar", sharedArray);

		List<String> sharedListForList = new ArrayList<>(List.of("One", "Two"));
		LIST_INSIDE_LIST = new ArrayList<>();
		LIST_INSIDE_LIST.add(sharedListForList);
		LIST_INSIDE_LIST.add(sharedListForList);

		ARRAY_INSIDE_LIST = new ArrayList<>();
		ARRAY_INSIDE_LIST.add(sharedArray);
		ARRAY_INSIDE_LIST.add(sharedArray);

		byte[] bytePrimitiveArray = new byte[] {1, 2, 3, 0, Byte.MAX_VALUE, Byte.MIN_VALUE, -1, (byte) 0xCA, (byte) 0xFE,
				(byte) 0xBA, (byte) 0xBE};
		Byte[] byteObjectArray = toBoxed(bytePrimitiveArray);
		short[] shortPrimitiveArray = new short[] {1, 2, 3, 0, Short.MAX_VALUE, Short.MIN_VALUE};
		Short[] shortObjectArray = toBoxed(shortPrimitiveArray);
		int[] intPrimitiveArray = new int[] {1, 2, 3, 0, Integer.MAX_VALUE, Integer.MIN_VALUE};
		Integer[] intObjectArray = toBoxed(intPrimitiveArray);
		long[] longPrimitiveArray = new long[] {1, 2, 3, 0, Long.MAX_VALUE, Long.MIN_VALUE};
		Long[] longObjectArray = toBoxed(longPrimitiveArray);
		float[] floatPrimitiveArray = new float[] {3.14159265f, 42.0f, -3.14159265f, 0.0f, Float.NaN, Float.MAX_VALUE,
				Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
		Float[] floatObjectArray = toBoxed(floatPrimitiveArray);
		double[] doublePrimitiveArray = new double[] {3.14159265d, 42.0d, -3.14159265d, 0.0d, Double.NaN,
				Double.MAX_VALUE, Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		Double[] doubleObjectArray = toBoxed(doublePrimitiveArray);
		boolean[] booleanPrimitiveArray = new boolean[] {true, false};
		Boolean[] booleanObjectArray = toBoxed(booleanPrimitiveArray);
		char[] charPrimitiveArray = new char[] {'b', 'a', 'r', 0, '!'};
		Character[] charObjectArray = toBoxed(charPrimitiveArray);

		ArrayList<Object> listContainingNull = new ArrayList<>();
		listContainingNull.add(null);

		Map<String, Object> mapWithNullValue = new HashMap<>();
		mapWithNullValue.put("foo", null);

		Map<String, Object> mapWithStringNullValue = new HashMap<>();
		mapWithStringNullValue.put("bar", "null");

		Map<String, Object> mapWithNullKey = new HashMap<>();
		mapWithNullKey.put(null, "foo");

		Map<String, Object> mapWithStringNullKey = new HashMap<>();
		mapWithStringNullKey.put("null", "bar");

		Map<Object, Object> mapWithObjectKey = new HashMap<>();
		mapWithObjectKey.put(new UnproblematicToString(), "foo");

		Object[] emptyObjectArray = new Object[0];
		ArrayList<Object> emptyList = new ArrayList<>();
		HashSet<Object> emptySet = new HashSet<>();
		Object[] objectArrayWithEmptyString = new Object[] {""};
		ArrayList<Object> listWithEmptyString = new ArrayList<>(List.of(""));
		HashSet<Object> setWithEmptyString = new HashSet<>(List.of(""));
		String[] stringArrayABC = new String[] {"a", "b", "c"};
		String[] stringArrayWithNull = new String[] {"a", null, "c"};
		String[] stringArrayWithStringNull = new String[] {"a", "null", "c"};

		VALID_VALUE_CASES = new Object[][] {
				{null, null, "null", null, null},
				{"foo", String.class, "foo", null, "'foo'"},
				{new UnproblematicToString(), UnproblematicToString.class, "UnproblematicToString", null, null},
				{new Date(1234567890000L), Date.class, "2009-02-13T23:31:30.000Z", null, null},
				{new Date(1234567890017L), Date.class, "2009-02-13T23:31:30.017Z", null, null},
				{Instant.ofEpochMilli(1234567890000L), Instant.class, "2009-02-13T23:31:30.000Z", null, null},
				{Instant.ofEpochMilli(1234567890017L), Instant.class, "2009-02-13T23:31:30.017Z", null, null},
				{DayOfWeek.SATURDAY, DayOfWeek.class, "SATURDAY", null, null},
				{Byte.valueOf((byte) 0), Byte.class, "0x00", null, null},
				{Byte.valueOf((byte) -1), Byte.class, "0xFF", null, null},
				{Byte.valueOf(Byte.MIN_VALUE), Byte.class, "0x80", null, null},
				{Byte.valueOf(Byte.MAX_VALUE), Byte.class, "0x7F", null, null},
				{bytePrimitiveArray, byte[].class, "[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]", null,
					"[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]"},
				{byteObjectArray, Byte[].class, "[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]", null,
					"[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]"},
				{shortPrimitiveArray, short[].class, "[1, 2, 3, 0, 32767, -32768]", null, "[1, 2, 3, 0, 32767, -32768]"},
				{shortObjectArray, Short[].class, "[1, 2, 3, 0, 32767, -32768]", null, "[1, 2, 3, 0, 32767, -32768]"},
				{intPrimitiveArray, int[].class, "[1, 2, 3, 0, 2147483647, -2147483648]", null,
					"[1, 2, 3, 0, 2147483647, -2147483648]"},
				{intObjectArray, Integer[].class, "[1, 2, 3, 0, 2147483647, -2147483648]", null,
					"[1, 2, 3, 0, 2147483647, -2147483648]"},
				{longPrimitiveArray, long[].class, "[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]", null,
					"[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]"},
				{longObjectArray, Long[].class, "[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]", null,
					"[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]"},
				{floatPrimitiveArray, float[].class,
					"[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]", null,
					"[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]"},
				{floatObjectArray, Float[].class,
					"[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]", null,
					"[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]"},
				{doublePrimitiveArray, double[].class,
					"[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]", null,
					"[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]"},
				{doubleObjectArray, Double[].class,
					"[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]", null,
					"[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]"},
				{booleanPrimitiveArray, boolean[].class, "[true, false]", null, "[true, false]"},
				{booleanObjectArray, Boolean[].class, "[true, false]", null, "[true, false]"},
				{charPrimitiveArray, char[].class, "[b, a, r, \00, !]", null, "[b, a, r, \00, !]"},
				{charObjectArray, Character[].class, "[b, a, r, \00, !]", null, "[b, a, r, \00, !]"},
		{LIST_INSIDE_TREE_MAP, TreeMap.class, "{bar=[One, Two], foo=[One, Two]}",
			"{\"bar\"=[\"One\", \"Two\"], \"foo\"=[\"One\", \"Two\"]}",
			"['bar':['One', 'Two'], 'foo':['One', 'Two']]"},
		{ARRAY_INSIDE_TREE_MAP, TreeMap.class, "{bar=[One, Two], foo=[One, Two]}",
			"{\"bar\"=[\"One\", \"Two\"], \"foo\"=[\"One\", \"Two\"]}",
			"['bar':['One', 'Two'], 'foo':['One', 'Two']]"},
		{LIST_INSIDE_LIST, ArrayList.class, "[[One, Two], [One, Two]]",
			"[[\"One\", \"Two\"], [\"One\", \"Two\"]]",
			"[['One', 'Two'], ['One', 'Two']]"},
		{ARRAY_INSIDE_LIST, ArrayList.class, "[[One, Two], [One, Two]]",
			"[[\"One\", \"Two\"], [\"One\", \"Two\"]]",
			"[['One', 'Two'], ['One', 'Two']]"},
				{listContainingNull, ArrayList.class, "[null]", null, null},
				{mapWithNullValue, HashMap.class, "{foo=null}", "{\"foo\"=null}", "['foo':null]"},
				{mapWithStringNullValue, HashMap.class, "{bar=null}", "{\"bar\"=\"null\"}", "['bar':'null']"},
				{mapWithNullKey, HashMap.class, "{null=foo}", "{null=\"foo\"}", "[null:'foo']"},
				{mapWithStringNullKey, HashMap.class, "{null=bar}", "{\"null\"=\"bar\"}", "['null':'bar']"},
				{mapWithObjectKey, HashMap.class, "{UnproblematicToString=foo}",
					"{UnproblematicToString=\"foo\"}", "[UnproblematicToString:'foo']"},
				{emptyObjectArray, Object[].class, "[]", null, null},
				{emptyList, ArrayList.class, "[]", null, null},
				{emptySet, HashSet.class, "[]", null, null},
				{objectArrayWithEmptyString, Object[].class, "[]", "[\"\"]", "['']"},
				{listWithEmptyString, ArrayList.class, "[]", "[\"\"]", "['']"},
				{setWithEmptyString, HashSet.class, "[]", "[\"\"]", "['']"},
				{stringArrayABC, String[].class, "[a, b, c]", "[\"a\", \"b\", \"c\"]", "['a', 'b', 'c']"},
				{stringArrayWithNull, String[].class, "[a, null, c]", "[\"a\", null, \"c\"]", "['a', null, 'c']"},
				{stringArrayWithStringNull, String[].class, "[a, null, c]", "[\"a\", \"null\", \"c\"]", "['a', 'null', 'c']"}
		};

		INVALID_VALUE_CASES = new Object[][] {
				{RECURSIVE_MAP, StackOverflowError.class, RECURSIVE_MAP_EXPECTED},
				{RECURSIVE_LIST, StackOverflowError.class, RECURSIVE_LIST_EXPECTED},
		{RECURSIVE_OBJECT_ARRAY, null, RECURSIVE_OBJECT_ARRAY_EXPECTED},
				{PROBLEMATIC_1, FooThrowable.class, PROBLEMATIC_1_EXPECTED},
				{PROBLEMATIC_2, FooThrowable.class, PROBLEMATIC_2_EXPECTED},
				{PROBLEMATIC_3, FooThrowable.class, PROBLEMATIC_3_EXPECTED}
		};
	}

	@ParameterizedTest
	@MethodSource("invalidValueArguments")
	void explodingValueToStringThrows(Object value, Class<? extends Throwable> expected) {
		if (expected == null) {
			value.toString();
		} else {
			assertThrows(expected, value::toString);
		}
	}

	@ParameterizedTest
	@MethodSource("validValueArguments")
	void validValuesSanityCheck(Object value, Class<?> expectedClass, String expectedResult,
			String expectedQuoted, String expectedQuoted2) {
		if (value == null) {
			assertNull(expectedClass);
		} else {
			assertEquals(expectedClass, value.getClass());
		}
	}

	@ParameterizedTest
	@MethodSource("invalidValueArguments")
	void safeStringToStringExplodingValue(Object value, Class<? extends Throwable> expectedException, String expectedResult) {
		assertEquals(expectedResult, SafeString.toString(value));
	}

	@ParameterizedTest
	@MethodSource("validValueArguments")
	void safeStringToStringValidValue(Object value, Class<?> expectedClass, String expectedResult,
			String expectedQuoted, String expectedQuoted2) {
		String result = SafeString.toString(value);
		assertEquals(expectedResult, result);
		if (value instanceof String) {
			assertSame(value, result);
		}

		String quoted = SafeString.toString(value, SafeString.StringWrapping.CONTAINED, SafeString.StringStyle.JAVA,
				SafeString.MapStyle.JAVA);
		assertEquals(expectedQuoted == null ? expectedResult : expectedQuoted, quoted);

		String quoted2 = SafeString.toString(value, SafeString.StringWrapping.ALL, SafeString.StringStyle.GROOVY,
				SafeString.MapStyle.GROOVY);
		assertEquals(expectedQuoted2 == null ? expectedResult : expectedQuoted2, quoted2);
	}

	@ParameterizedTest
	@MethodSource("validValueArguments")
	void safeStringAppendValidValue(Object value, Class<?> expectedClass, String expectedResult,
			String expectedQuoted, String expectedQuoted2) {
		StringBuilder resultBuilder = new StringBuilder();
		StringBuilder quotedBuilder = new StringBuilder();
		StringBuilder quotedBuilder2 = new StringBuilder();

		SafeString.append(value, resultBuilder);
		SafeString.append(value, quotedBuilder, SafeString.StringWrapping.CONTAINED, SafeString.StringStyle.JAVA,
				SafeString.MapStyle.JAVA);
		SafeString.append(value, quotedBuilder2, SafeString.StringWrapping.ALL, SafeString.StringStyle.GROOVY,
				SafeString.MapStyle.GROOVY);

		assertEquals(expectedResult, resultBuilder.toString());
		assertEquals(expectedQuoted == null ? expectedResult : expectedQuoted, quotedBuilder.toString());
		assertEquals(expectedQuoted2 == null ? expectedResult : expectedQuoted2, quotedBuilder2.toString());
	}

	@ParameterizedTest
	@MethodSource("appendExceptionArguments")
	void safeStringAppendThrowsExpectedExceptions(String expectedMessagePart, SafeString.StringWrapping wrapping,
			SafeString.StringStyle stringStyle, SafeString.MapStyle mapStyle, boolean omitBuilder) {
		StringBuilder builder = omitBuilder ? null : new StringBuilder();
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> SafeString.append(null, builder, wrapping, stringStyle, mapStyle));
		assertEquals(expectedMessagePart + " must not be null!", exception.getMessage());
	}

	@Test
	void identityToStringNullReturnsNull() {
		assertNull(SafeString.identityToString(null));
	}

	@Test
	void enumCoverage() {
		assertEquals(SafeString.StringWrapping.ALL, SafeString.StringWrapping.valueOf("ALL"));
		assertEquals(SafeString.StringStyle.JAVA, SafeString.StringStyle.valueOf("JAVA"));
		assertEquals(SafeString.MapStyle.GROOVY, SafeString.MapStyle.valueOf("GROOVY"));
		assertTrue(Arrays.asList(SafeString.StringWrapping.values()).contains(SafeString.StringWrapping.CONTAINED));
		assertTrue(Arrays.asList(SafeString.StringStyle.values()).contains(SafeString.StringStyle.GROOVY));
		assertTrue(Arrays.asList(SafeString.MapStyle.values()).contains(SafeString.MapStyle.JAVA));
	}

	private static Stream<Arguments> validValueArguments() {
		return Arrays.stream(VALID_VALUE_CASES).map(args -> Arguments.of(args[0], args[1], args[2], args[3], args[4]));
	}

	private static Stream<Arguments> invalidValueArguments() {
		return Arrays.stream(INVALID_VALUE_CASES).map(args -> Arguments.of(args[0], args[1], args[2]));
	}

	private static Stream<Arguments> appendExceptionArguments() {
		return Stream.of(
				Arguments.of("stringBuilder", SafeString.StringWrapping.ALL, SafeString.StringStyle.JAVA,
						SafeString.MapStyle.JAVA, true),
				Arguments.of("stringWrapping", null, SafeString.StringStyle.JAVA, SafeString.MapStyle.JAVA, false),
				Arguments.of("stringStyle", SafeString.StringWrapping.ALL, null, SafeString.MapStyle.JAVA, false),
				Arguments.of("mapStyle", SafeString.StringWrapping.ALL, SafeString.StringStyle.JAVA, null, false));
	}

	private static Byte[] toBoxed(byte[] array) {
		Byte[] boxed = new Byte[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Short[] toBoxed(short[] array) {
		Short[] boxed = new Short[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Integer[] toBoxed(int[] array) {
		Integer[] boxed = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Long[] toBoxed(long[] array) {
		Long[] boxed = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Float[] toBoxed(float[] array) {
		Float[] boxed = new Float[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Double[] toBoxed(double[] array) {
		Double[] boxed = new Double[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Boolean[] toBoxed(boolean[] array) {
		Boolean[] boxed = new Boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static Character[] toBoxed(char[] array) {
		Character[] boxed = new Character[array.length];
		for (int i = 0; i < array.length; i++) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	private static class UnproblematicToString {
		@Override
		public String toString() {
			return "UnproblematicToString";
		}
	}

	private static class ProblematicToString {
		private final String message;

		private ProblematicToString(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			throw new FooThrowable(message);
		}
	}

	private static class FooThrowable extends RuntimeException {
		private FooThrowable(String message) {
			super(message);
		}
	}
}
