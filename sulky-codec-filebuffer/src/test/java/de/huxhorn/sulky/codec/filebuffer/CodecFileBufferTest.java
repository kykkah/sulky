package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.huxhorn.sulky.buffers.ElementProcessor;
import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.codec.SerializableCodec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodecFileBufferTest {

	private static final int MAGIC_VALUE = 0xDEADBEEF;
	private static final String[] VALUES = {
			"Null, sort of nothing",
			"One",
			"Two",
			"Three",
			"Four",
			"Five",
			"Six",
			"Seven",
			"Eight",
			"Nine",
			"Ten",
	};

	@TempDir
	Path tempDir;

	private File dataFile;
	private File indexFile;
	private Codec<String> codec;
	private FileHeaderStrategy fileHeaderStrategy;

	@BeforeEach
	void setUp() {
		dataFile = tempDir.resolve("dump").toFile();
		indexFile = tempDir.resolve("dump.index").toFile();
		codec = new SerializableCodec<>();
		fileHeaderStrategy = new DefaultFileHeaderStrategy();
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addThenGetAndIterate(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertSequence(buffer, VALUES);
		assertHeader(buffer, sparse, metaData);

		CodecFileBuffer<String> reopened = newBuffer(sparse, metaData);
		assertEquals(VALUES.length, reopened.getSize());
		assertHeader(reopened, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addAllThenGetAndIterate(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		assertSequence(buffer, VALUES);
		assertHeader(buffer, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void mixedAddAndAddAll(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		for (String value : VALUES) {
			buffer.add(value);
		}
		buffer.addAll(Arrays.asList(VALUES));
		for (String value : VALUES) {
			buffer.add(value);
		}

		assertEquals(VALUES.length * 4, buffer.getSize());
		for (int i = 0; i < buffer.getSize(); i++) {
			assertEquals(VALUES[i % VALUES.length], buffer.get(i));
		}
		int index = 0;
		for (String value : buffer) {
			assertEquals(VALUES[index % VALUES.length], value);
			index++;
		}
		assertHeader(buffer, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void readInvalidEntryFromEmptyFile(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		assertNull(buffer.get(0));
		assertNull(buffer.get(10));
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void readInvalidEntryFromNonEmptyFile(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		assertNotNull(buffer.get(0));
		assertNull(buffer.get(VALUES.length));
		assertNotNull(buffer.get(VALUES.length - 1));
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void newInstanceHasExpectedHeader(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		assertEquals(0, buffer.getSize());
		assertHeader(buffer, sparse, metaData);

		CodecFileBuffer<String> reopened = newBuffer(sparse, metaData);
		assertEquals(0, reopened.getSize());
		assertHeader(reopened, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void setSupportedMatchesSparse(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		assertEquals(sparse, buffer.isSetSupported());
	}

	@ParameterizedTest
	@MethodSource("metaDataArguments")
	void setOnNonSparseThrows(Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(false, metaData);
		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
				() -> buffer.set(0, "Will fail!"));
		assertEquals("DefaultDataStrategy does not support set!", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("metaDataArguments")
	void setOnSparseStoresValues(Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(true, metaData);
		for (int i = VALUES.length - 1; i >= 0; i--) {
			buffer.set(i, VALUES[i]);
		}
		assertSequence(buffer, VALUES);
		assertHeader(buffer, true, metaData);

		CodecFileBuffer<String> reopened = newBuffer(true, metaData);
		assertEquals(VALUES.length, reopened.getSize());
		assertHeader(reopened, true, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addWithElementProcessorInvokesProcessor(boolean sparse, Map<String, String> metaData) throws IOException {
		CapturingStringElementProcessor processor = new CapturingStringElementProcessor();
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.setElementProcessors(List.of(processor));
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertEquals(Arrays.asList(VALUES), processor.list);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addAllWithElementProcessorInvokesProcessor(boolean sparse, Map<String, String> metaData) throws IOException {
		CapturingStringElementProcessor processor = new CapturingStringElementProcessor();
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.setElementProcessors(List.of(processor));
		buffer.addAll(Arrays.asList(VALUES));
		assertEquals(Arrays.asList(VALUES), processor.list);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void resetClearsBuffer(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		buffer.reset();
		assertEquals(0, buffer.getSize());
		assertHeader(buffer, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void resetThenAdd(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		buffer.reset();
		buffer.addAll(Arrays.asList(VALUES));
		assertSequence(buffer, VALUES);
		assertHeader(buffer, sparse, metaData);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addDeleteDataFileThenAdd(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertTrue(dataFile.delete());
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertEquals(8L * VALUES.length, indexFile.length());
		assertHeader(buffer, sparse, metaData);
		assertSequence(buffer, VALUES);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addAllDeleteDataFileThenGetReturnsNull(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		assertTrue(dataFile.delete());
		assertNull(buffer.get(0));
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addDeleteIndexFileThenAdd(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertTrue(indexFile.delete());
		for (String value : VALUES) {
			buffer.add(value);
		}
		assertEquals(8L * VALUES.length, indexFile.length());
		assertHeader(buffer, sparse, metaData);
		assertSequence(buffer, VALUES);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void addAllDeleteIndexFileThenAddAll(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		assertTrue(indexFile.delete());
		buffer.addAll(Arrays.asList(VALUES));
		assertEquals(8L * VALUES.length, indexFile.length());
		assertHeader(buffer, sparse, metaData);
		assertSequence(buffer, VALUES);
	}

	@ParameterizedTest
	@MethodSource("sparseMetaArguments")
	void deleteIndexFileAndReopenFails(boolean sparse, Map<String, String> metaData) throws IOException {
		CodecFileBuffer<String> buffer = newBuffer(sparse, metaData);
		buffer.addAll(Arrays.asList(VALUES));
		String indexPath = indexFile.getAbsolutePath();
		assertTrue(indexFile.delete());
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new CodecFileBuffer<>(MAGIC_VALUE, sparse, metaData, codec, dataFile, indexFile, fileHeaderStrategy));
		assertEquals("dataFile contains data but indexFile " + indexPath + " is not valid!", exception.getMessage());
	}

	private CodecFileBuffer<String> newBuffer(boolean sparse, Map<String, String> metaData) throws IOException {
		return new CodecFileBuffer<>(MAGIC_VALUE, sparse, metaData, codec, dataFile, indexFile, fileHeaderStrategy);
	}

	private static Stream<Arguments> sparseMetaArguments() {
		return Stream.of(
				Arguments.of(false, null),
				Arguments.of(true, null),
				Arguments.of(false, Map.of("foo1", "bar1", "foo2", "bar2")),
				Arguments.of(true, Map.of("foo1", "bar1", "foo2", "bar2"))
		);
	}

	private static Stream<Arguments> metaDataArguments() {
		return Stream.of(
				Arguments.of((Map<String, String>) null),
				Arguments.of(Map.of("foo1", "bar1", "foo2", "bar2"))
		);
	}

	private void assertSequence(CodecFileBuffer<String> buffer, String[] expected) {
		assertEquals(expected.length, buffer.getSize());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], buffer.get(i));
		}
		int index = 0;
		for (String value : buffer) {
			assertEquals(expected[index], value);
			index++;
		}
	}

	private void assertHeader(CodecFileBuffer<String> buffer, boolean sparse, Map<String, String> metaData) {
		FileHeader header = buffer.getFileHeader();
		assertNotNull(header);
		assertEquals(new MetaData(metaData, sparse), header.getMetaData());
		assertEquals(MAGIC_VALUE, header.getMagicValue());
	}

	private static class CapturingStringElementProcessor implements ElementProcessor<String> {
		private final List<String> list = new ArrayList<>();

		@Override
		public void processElement(String element) {
			list.add(element);
		}

		@Override
		public void processElements(List<String> elements) {
			list.addAll(elements);
		}
	}
}
