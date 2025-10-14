package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.codec.SerializableCodec;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

abstract class DataStrategyTestBase {

	protected final Codec<String> codec = new SerializableCodec<>();
	protected final IndexStrategy indexStrategy = new DefaultIndexStrategy();

	@TempDir
	Path tempDir;

	protected File indexFile;
	protected File dataFile;
	protected DataStrategy<String> instance;

	@BeforeEach
	void setUpFiles() throws IOException {
		indexFile = tempDir.resolve("index.tst").toFile();
		dataFile = tempDir.resolve("data.tst").toFile();
		instance = createInstance();
	}

	protected abstract DataStrategy<String> createInstance();

	protected void assertAddAndGet(List<String> values) throws IOException, ClassNotFoundException {
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			for (String value : values) {
				instance.add(value, indexRaf, dataRaf, codec, indexStrategy);
			}
			for (int i = 0; i < values.size(); i++) {
				String read = instance.get(i, indexRaf, dataRaf, codec, indexStrategy);
				assertEquals(values.get(i), read, "Value at index " + i);
			}
		}
	}

	protected void assertAddAllAndGet(List<String> values) throws IOException, ClassNotFoundException {
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			instance.addAll(values, indexRaf, dataRaf, codec, indexStrategy);
			for (int i = 0; i < values.size(); i++) {
				String read = instance.get(i, indexRaf, dataRaf, codec, indexStrategy);
				assertEquals(values.get(i), read, "Value at index " + i);
			}
		}
	}

	protected static List<String> defaultValues() {
		return Arrays.asList("Foo", "Bar");
	}
}
