package de.huxhorn.sulky.formatting;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class ReplaceInvalidXmlCharacterReaderTest {

	@Test
	void defaultReplacementCharacterIsUsed() {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(new StringReader("foo"));
		assertEquals(ReplaceInvalidXmlCharacterReader.DEFAULT_REPLACEMENT_CHARACTER, reader.getReplacementChar());
	}

	@Test
	void customReplacementCharacterIsRespected() {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(new StringReader("foo"), '!');
		assertEquals('!', reader.getReplacementChar());

		ReplaceInvalidXmlCharacterReader space = new ReplaceInvalidXmlCharacterReader(new StringReader("foo"), ' ');
		assertEquals(' ', space.getReplacementChar());
	}

	@Test
	void invalidReplacementCharacterThrows() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new ReplaceInvalidXmlCharacterReader(new StringReader("foo"), (char) 0xFFFF));
		assertEquals("Replacement character 0xFFFF is invalid itself!", exception.getMessage());
	}

	@Test
	void invalidCharactersAreReplacedWhenReadingSingleChars() throws IOException {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(
				new StringReader("foo" + (char) 0 + (char) 0xFFFF + "bar"), '#');

		assertEquals('f', reader.read());
		assertEquals('o', reader.read());
		assertEquals('o', reader.read());
		assertEquals('#', reader.read());
		assertEquals('#', reader.read());
		assertEquals('b', reader.read());
		assertEquals('a', reader.read());
		assertEquals('r', reader.read());
	}

	@Test
	void invalidCharactersAreReplacedWhenReadingIntoBuffer() throws IOException {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(
				new StringReader("foo" + (char) 0 + (char) 0xFFFF + "bar"), '#');
		char[] buffer = new char[8];
		reader.read(buffer, 0, 8);
		assertArrayEquals(new char[] {'f', 'o', 'o', '#', '#', 'b', 'a', 'r'}, buffer);
	}

	@Test
	void invalidCharactersAreReplacedWhenReadingWithOffset() throws IOException {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(
				new StringReader("f" + (char) 0 + (char) 0xFFFF + "bar"), '#');
		char[] buffer = new char[8];
		int read = reader.read(buffer, 1, 7);
		assertArrayEquals(new char[] {0, 'f', '#', '#', 'b', 'a', 'r', 0}, buffer);
		assertEquals(6, read);
	}

	@Test
	void endOfStreamReturnsMinusOne() throws IOException {
		ReplaceInvalidXmlCharacterReader reader = new ReplaceInvalidXmlCharacterReader(new StringReader(""));
		char[] buffer = new char[8];
		int read = reader.read(buffer, 0, 8);
		assertArrayEquals(new char[] {0, 0, 0, 0, 0, 0, 0, 0}, buffer);
		assertEquals(-1, read);
	}
}
