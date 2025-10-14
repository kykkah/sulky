package de.huxhorn.sulky.stax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Date;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateTimeFormatterTest {

	@ParameterizedTest(name = "{index}: parse {0} -> default format {1}")
	@MethodSource("formatWithDefaultArguments")
	void parseAndDefaultFormatWithMillis(String input, String expected) throws ParseException {
		DateTimeFormatter formatter = new DateTimeFormatter();
		Date date = formatter.parse(input);
		assertEquals(expected, formatter.format(date));
	}

	@ParameterizedTest(name = "{index}: parse {0} -> format(withMillis) {1}")
	@MethodSource("formatWithMillisArguments")
	void parseAndFormatWithMillis(String input, String expected) throws ParseException {
		DateTimeFormatter formatter = new DateTimeFormatter();
		Date date = formatter.parse(input);
		assertEquals(expected, formatter.format(date, true));
	}

	@ParameterizedTest(name = "{index}: parse {0} -> format(withoutMillis) {1}")
	@MethodSource("formatWithoutMillisArguments")
	void parseAndFormatWithoutMillis(String input, String expected) throws ParseException {
		DateTimeFormatter formatter = new DateTimeFormatter();
		Date date = formatter.parse(input);
		assertEquals(expected, formatter.format(date, false));
	}

	private static Stream<Arguments> formatWithDefaultArguments() {
		return Stream.of(
				Arguments.of("2009-11-15T00:00:00.000+0100", "2009-11-14T23:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+01:00", "2009-11-14T23:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+0000", "2009-11-15T00:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+00:00", "2009-11-15T00:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000-0800", "2009-11-15T08:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000-08:00", "2009-11-15T08:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.000Z", "2009-11-15T00:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00Z", "2009-11-15T00:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00.017+0100", "2009-11-14T23:00:00.017+00:00"),
				Arguments.of("2009-11-15T00:00:00.017+01:00", "2009-11-14T23:00:00.017+00:00"),
				Arguments.of("2009-11-15T00:00:00+0100", "2009-11-14T23:00:00.000+00:00"),
				Arguments.of("2009-11-15T00:00:00+01:00", "2009-11-14T23:00:00.000+00:00"));
	}

	private static Stream<Arguments> formatWithMillisArguments() {
		return formatWithDefaultArguments();
	}

	private static Stream<Arguments> formatWithoutMillisArguments() {
		return Stream.of(
				Arguments.of("2009-11-15T00:00:00.000+0100", "2009-11-14T23:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+01:00", "2009-11-14T23:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+0000", "2009-11-15T00:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000+00:00", "2009-11-15T00:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000-0800", "2009-11-15T08:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000-08:00", "2009-11-15T08:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.000Z", "2009-11-15T00:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00Z", "2009-11-15T00:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.017+0100", "2009-11-14T23:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00.017+01:00", "2009-11-14T23:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00+0100", "2009-11-14T23:00:00+00:00"),
				Arguments.of("2009-11-15T00:00:00+01:00", "2009-11-14T23:00:00+00:00"));
	}
}
