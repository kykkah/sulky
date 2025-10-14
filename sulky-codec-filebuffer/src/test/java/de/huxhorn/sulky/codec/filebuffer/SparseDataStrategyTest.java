package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.junit.jupiter.api.Test;

class SparseDataStrategyTest extends DataStrategyTestBase {

	@Override
	protected DataStrategy<String> createInstance() {
		return new SparseDataStrategy<>();
	}

	@Test
	void addAndGetMatchesInput() throws IOException, ClassNotFoundException {
		assertAddAndGet(defaultValues());
	}

	@Test
	void addAllAndGetMatchesInput() throws IOException, ClassNotFoundException {
		assertAddAllAndGet(defaultValues());
	}

	@Test
	void setStoresValuesAtSpecificIndices() throws IOException, ClassNotFoundException {
		List<String> values = defaultValues();
		long index1 = 17;
		long index2 = 42;
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			assertTrue(instance.set(index1, values.get(0), indexRaf, dataRaf, codec, indexStrategy));
			assertTrue(instance.set(index2, values.get(1), indexRaf, dataRaf, codec, indexStrategy));
			assertEquals(values.get(0), instance.get(index1, indexRaf, dataRaf, codec, indexStrategy));
			assertEquals(values.get(1), instance.get(index2, indexRaf, dataRaf, codec, indexStrategy));
			assertEquals(index2 + 1, indexStrategy.getSize(indexRaf));
			for (int i = 0; i < index2; i++) {
				if (i != index1) {
					assertNull(instance.get(i, indexRaf, dataRaf, codec, indexStrategy));
				}
			}
		}
	}

	@Test
	void setAllowsOverwriteByDefault() throws IOException, ClassNotFoundException {
		List<String> values = defaultValues();
		long index = 5;
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			assertTrue(instance.set(index, values.get(0), indexRaf, dataRaf, codec, indexStrategy));
			assertTrue(instance.set(index, values.get(1), indexRaf, dataRaf, codec, indexStrategy));
			assertEquals(values.get(1), instance.get(index, indexRaf, dataRaf, codec, indexStrategy));
		}
	}

	@Test
	void setRespectsOverwriteFlag() throws IOException, ClassNotFoundException {
		((SparseDataStrategy<String>) instance).setSupportingOverwrite(false);
		List<String> values = defaultValues();
		long index = 5;
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			assertTrue(instance.set(index, values.get(0), indexRaf, dataRaf, codec, indexStrategy));
			boolean overwriteResult = instance.set(index, values.get(1), indexRaf, dataRaf, codec, indexStrategy);
			assertFalse(overwriteResult);
			assertEquals(values.get(0), instance.get(index, indexRaf, dataRaf, codec, indexStrategy));
		}
	}

	@Test
	void setWithNullRemovesValue() throws IOException, ClassNotFoundException {
		List<String> values = defaultValues();
		long index = 7;
		try (RandomAccessFile indexRaf = new RandomAccessFile(indexFile, "rw");
			 RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw")) {
			assertTrue(instance.set(index, values.get(0), indexRaf, dataRaf, codec, indexStrategy));
			assertTrue(instance.set(index, null, indexRaf, dataRaf, codec, indexStrategy));
			assertNull(instance.get(index, indexRaf, dataRaf, codec, indexStrategy));
		}
	}

	@Test
	void setSupportedReturnsTrue() {
		assertTrue(instance.isSetSupported());
	}
}
