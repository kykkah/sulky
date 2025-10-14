package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefaultFileHeaderStrategyTest {

	private static final int MAGIC_VALUE = 0xDEADBEEF;

	@TempDir
	Path tempDir;

	private File file;
	private DefaultFileHeaderStrategy strategy;

	@BeforeEach
	void setUp() throws IOException {
		file = tempDir.resolve("header.bin").toFile();
		strategy = new DefaultFileHeaderStrategy();
	}

	@ParameterizedTest
	@MethodSource("headerParameters")
	void writeAndReadHeader(boolean sparse, Map<String, String> metaData) throws IOException {
		FileHeader written = strategy.writeFileHeader(file, MAGIC_VALUE, metaData, sparse);
		Integer readMagic = strategy.readMagicValue(file);
		FileHeader readHeader = strategy.readFileHeader(file);

		assertNotNull(written);
		assertEquals(new MetaData(metaData, sparse), written.getMetaData());
		assertEquals(MAGIC_VALUE, written.getMagicValue());

		assertNotNull(readMagic);
		assertEquals(MAGIC_VALUE, readMagic.intValue());
		assertEquals(written, readHeader);
	}

	private static Stream<Arguments> headerParameters() {
		return Stream.of(
				Arguments.of(false, null),
				Arguments.of(false, Map.of("foo", "bar", "foo2", "bar2")),
				Arguments.of(true, null),
				Arguments.of(true, Map.of("foo", "bar", "foo2", "bar2"))
		);
	}
}
