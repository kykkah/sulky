package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultIndexStrategyTest {

	@TempDir
	Path tempDir;

	private File indexFile;
	private DefaultIndexStrategy strategy;

	@BeforeEach
	void setUp() {
		indexFile = tempDir.resolve("index.bin").toFile();
		strategy = new DefaultIndexStrategy();
	}

	@Test
	void emptyFileHasSizeZero() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
			assertEquals(0L, strategy.getSize(raf));
		}
	}

	@Test
	void emptyFileReturnsNegativeOffset() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
			assertEquals(-1L, strategy.getOffset(raf, 17));
		}
	}

	@Test
	void firstOffsetRegistersCorrectly() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
			strategy.setOffset(raf, 0, 17);
			assertEquals(1L, strategy.getSize(raf));
			assertEquals(17L, strategy.getOffset(raf, 0));
		}
	}

	@Test
	void arbitraryOffsetRegistersGapWithDefaultValues() throws IOException {
		long index = 17;
		long value = 42;
		try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
			strategy.setOffset(raf, index, value);
			assertEquals(index + 1, strategy.getSize(raf));
			assertEquals(value, strategy.getOffset(raf, index));
			for (int i = 0; i < index; i++) {
				assertEquals(-1L, strategy.getOffset(raf, i));
			}
		}
	}

	@Test
	void multipleOffsetsMaintainExistingValues() throws IOException {
		long index1 = 17;
		long index2 = 42;
		long value1 = 1;
		long value2 = 2;
		try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
			strategy.setOffset(raf, index1, value1);
			strategy.setOffset(raf, index2, value2);
			assertEquals(index2 + 1, strategy.getSize(raf));
			assertEquals(value1, strategy.getOffset(raf, index1));
			assertEquals(value2, strategy.getOffset(raf, index2));
			for (int i = 0; i < index2; i++) {
				if (i == index1) {
					continue;
				}
				assertEquals(-1L, strategy.getOffset(raf, i));
			}
		}
	}
}
