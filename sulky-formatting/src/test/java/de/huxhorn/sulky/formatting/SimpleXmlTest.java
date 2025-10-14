package de.huxhorn.sulky.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

class SimpleXmlTest {

	@ParameterizedTest
	@MethodSource("escapedXmlCharacters")
	void escapeConvertsSpecialCharacters(char character, String escaped) {
		assertEquals(escaped, SimpleXml.escape(String.valueOf(character)));
	}

	@Test
	void escapeNullCharacterReturnsSpace() {
		assertEquals(" ", SimpleXml.escape(String.valueOf((char) 0)));
	}

	@ParameterizedTest
	@MethodSource("escapedXmlCharacters")
	void unescapeRestoresCharacter(char character, String escaped) {
		assertEquals(String.valueOf(character), SimpleXml.unescape(escaped));
	}

	@ParameterizedTest
	@MethodSource("xmlEdgeCaseChars")
	void charEdgeCases(int codePoint, boolean valid) {
		assertEquals(valid, SimpleXml.isValidXMLCharacter(codePoint));
	}

	@ParameterizedTest
	@MethodSource("xmlEdgeCaseIntegers")
	void integerEdgeCases(int codePoint, boolean valid) {
		assertEquals(valid, SimpleXml.isValidXMLCharacter(codePoint));
	}

	@ParameterizedTest
	@MethodSource("xmlEdgeCaseChars")
	void charEdgeCasesUsingChar(int codePoint, boolean valid) {
		boolean result = SimpleXml.isValidXMLCharacter((char) codePoint);
		assertEquals(valid, result || codePoint != (codePoint & 0xFFFF));
	}

	@ParameterizedTest
	@MethodSource("xmlEdgeCaseChars")
	void replaceNonValidXmlCharacters(int codePoint, boolean valid) {
		char replacement = '#';
		String input = new StringBuilder().appendCodePoint(codePoint).toString();
		String result = SimpleXml.replaceNonValidXMLCharacters(input, replacement);
		if (valid) {
			assertEquals(input, result);
		} else {
			assertEquals(replacement, result.charAt(0));
		}
	}

	@Test
	void replaceNonValidXmlCharactersWithInvalidReplacementThrows() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> SimpleXml.replaceNonValidXMLCharacters("foo", (char) 0xFFFF));
		assertEquals("Replacement character 0xFFFF is invalid itself!", exception.getMessage());
	}

	@Test
	void replaceNonValidXmlCharactersWithValidInputReturnsSameString() {
		String input = "foo";
		String result = SimpleXml.replaceNonValidXMLCharacters(input, ' ');
		assertEquals(input, result);
		assertTrueSameReference(input, result);
	}

	@Test
	void replaceNonValidXmlCharactersReplacesInvalidCharacters() {
		String result = SimpleXml.replaceNonValidXMLCharacters("foo\u0019\uD800bar", ' ');
		assertEquals("foo  bar", result);
	}

	private static Stream<Arguments> escapedXmlCharacters() {
		return Stream.of(
				Arguments.of('&', "&amp;"),
				Arguments.of('<', "&lt;"),
				Arguments.of('>', "&gt;"),
				Arguments.of('"', "&quot;")
		);
	}

	private static Stream<Arguments> xmlEdgeCaseChars() {
		return Stream.of(
				Arguments.of(0x8, false),
				Arguments.of(0x9, true),
				Arguments.of(0xA, true),
				Arguments.of(0xB, false),
				Arguments.of(0xC, false),
				Arguments.of(0xD, true),
				Arguments.of(0xE, false),
				Arguments.of(0x19, false),
				Arguments.of(0x20, true),
				Arguments.of(0xD7FF, true),
				Arguments.of(0xD800, false),
				Arguments.of(0xDFFF, false),
				Arguments.of(0xE000, true),
				Arguments.of(0xFFFD, true),
				Arguments.of(0xFFFE, false),
				Arguments.of(0xFFFF, false)
		);
	}

	private static Stream<Arguments> xmlEdgeCaseIntegers() {
		return Stream.of(
				Arguments.of(0x10000, true),
				Arguments.of(0x10FFFF, true),
				Arguments.of(0x10FFFF, true),
				Arguments.of(0x110000, false)
		);
	}

	private static void assertTrueSameReference(String expected, String actual) {
		assertSame(expected, actual);
	}
}
