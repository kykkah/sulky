package de.huxhorn.sulky.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HumanReadableTest {

	@ParameterizedTest(name = "{index}: size={0}, binary={1}, symbol={2}")
	@MethodSource("data")
	void getHumanReadableSize(long size, boolean useBinaryUnits, boolean useSymbol, String expected) {
		assertEquals(expected, HumanReadable.getHumanReadableSize(size, useBinaryUnits, useSymbol));
	}

	private static Stream<Arguments> data() {
		return Stream.of(
				Arguments.of(-1L, false, false, "-1 "),
				Arguments.of(-1L, false, true, "-1 "),
				Arguments.of(-1L, true, false, "-1 "),
				Arguments.of(-1L, true, true, "-1 "),

				Arguments.of(0L, false, false, "0 "),
				Arguments.of(0L, false, true, "0 "),
				Arguments.of(0L, true, false, "0 "),
				Arguments.of(0L, true, true, "0 "),

				Arguments.of(1L, false, false, "1 "),
				Arguments.of(1L, false, true, "1 "),
				Arguments.of(1L, true, false, "1 "),
				Arguments.of(1L, true, true, "1 "),

				Arguments.of(999L, false, false, "999 "),
				Arguments.of(999L, false, true, "999 "),
				Arguments.of(999L, true, false, "999 "),
				Arguments.of(999L, true, true, "999 "),

				Arguments.of(1000L, false, false, "1.00 kilo"),
				Arguments.of(1000L, false, true, "1.00 k"),
				Arguments.of(1000L, true, false, "1000 "),
				Arguments.of(1000L, true, true, "1000 "),

				Arguments.of(1001L, false, false, "1.00 kilo"),
				Arguments.of(1001L, false, true, "1.00 k"),
				Arguments.of(1001L, true, false, "1001 "),
				Arguments.of(1001L, true, true, "1001 "),

				Arguments.of(1023L, false, false, "1.02 kilo"),
				Arguments.of(1023L, false, true, "1.02 k"),
				Arguments.of(1023L, true, false, "1023 "),
				Arguments.of(1023L, true, true, "1023 "),

				Arguments.of(1024L, false, false, "1.02 kilo"),
				Arguments.of(1024L, false, true, "1.02 k"),
				Arguments.of(1024L, true, false, "1.00 kibi"),
				Arguments.of(1024L, true, true, "1.00 Ki"),

				Arguments.of(1337L, false, false, "1.34 kilo"),
				Arguments.of(1337L, false, true, "1.34 k"),
				Arguments.of(1337L, true, false, "1.31 kibi"),
				Arguments.of(1337L, true, true, "1.31 Ki"),

				Arguments.of(-1337L, false, false, "-1.34 kilo"),
				Arguments.of(-1337L, false, true, "-1.34 k"),
				Arguments.of(-1337L, true, false, "-1.31 kibi"),
				Arguments.of(-1337L, true, true, "-1.31 Ki"),

				Arguments.of(2_000_000L, false, false, "2.00 mega"),
				Arguments.of(2_000_000L, false, true, "2.00 M"),
				Arguments.of(2_000_000L, true, false, "1.91 mebi"),
				Arguments.of(2_000_000L, true, true, "1.91 Mi"),

				Arguments.of(2_097_152L, false, false, "2.10 mega"),
				Arguments.of(2_097_152L, false, true, "2.10 M"),
				Arguments.of(2_097_152L, true, false, "2.00 mebi"),
				Arguments.of(2_097_152L, true, true, "2.00 Mi"),

				Arguments.of(1_000_000_000_000_000_000L, false, false, "1.00 exa"),
				Arguments.of(1_000_000_000_000_000_000L, false, true, "1.00 E"),
				Arguments.of(1_000_000_000_000_000_000L, true, false, "888.18 pebi"),
				Arguments.of(1_000_000_000_000_000_000L, true, true, "888.18 Pi"),

				Arguments.of(1_152_921_504_606_846_976L, false, false, "1.15 exa"),
				Arguments.of(1_152_921_504_606_846_976L, false, true, "1.15 E"),
				Arguments.of(1_152_921_504_606_846_976L, true, false, "1.00 exbi"),
				Arguments.of(1_152_921_504_606_846_976L, true, true, "1.00 Ei"),

				Arguments.of((long) Short.MAX_VALUE, false, false, "32.77 kilo"),
				Arguments.of((long) Short.MAX_VALUE, false, true, "32.77 k"),
				Arguments.of((long) Short.MAX_VALUE, true, false, "32.00 kibi"),
				Arguments.of((long) Short.MAX_VALUE, true, true, "32.00 Ki"),

				Arguments.of((long) Short.MIN_VALUE, false, false, "-32.77 kilo"),
				Arguments.of((long) Short.MIN_VALUE, false, true, "-32.77 k"),
				Arguments.of((long) Short.MIN_VALUE, true, false, "-32.00 kibi"),
				Arguments.of((long) Short.MIN_VALUE, true, true, "-32.00 Ki"),

				Arguments.of((long) Integer.MAX_VALUE, false, false, "2.15 giga"),
				Arguments.of((long) Integer.MAX_VALUE, false, true, "2.15 G"),
				Arguments.of((long) Integer.MAX_VALUE, true, false, "2.00 gibi"),
				Arguments.of((long) Integer.MAX_VALUE, true, true, "2.00 Gi"),

				Arguments.of((long) Integer.MIN_VALUE, false, false, "-2.15 giga"),
				Arguments.of((long) Integer.MIN_VALUE, false, true, "-2.15 G"),
				Arguments.of((long) Integer.MIN_VALUE, true, false, "-2.00 gibi"),
				Arguments.of((long) Integer.MIN_VALUE, true, true, "-2.00 Gi"),

				Arguments.of(Long.MAX_VALUE, false, false, "9.22 exa"),
				Arguments.of(Long.MAX_VALUE, false, true, "9.22 E"),
				Arguments.of(Long.MAX_VALUE, true, false, "8.00 exbi"),
				Arguments.of(Long.MAX_VALUE, true, true, "8.00 Ei"),

				Arguments.of(Long.MIN_VALUE, false, false, "-9.22 exa"),
				Arguments.of(Long.MIN_VALUE, false, true, "-9.22 E"),
				Arguments.of(Long.MIN_VALUE, true, false, "-8.00 exbi"),
				Arguments.of(Long.MIN_VALUE, true, true, "-8.00 Ei")
		);
	}
}
