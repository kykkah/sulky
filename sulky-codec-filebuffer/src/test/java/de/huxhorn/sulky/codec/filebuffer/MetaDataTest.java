package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MetaDataTest {

	@Test
	void emptyNotSparse() {
		MetaData metaData = new MetaData(false);
		assertEquals(new MetaData(false), metaData);
		assertEquals(0, metaData.getData().size());
		assertNotEquals(new MetaData(true), metaData);
	}

	@Test
	void emptySparse() {
		MetaData metaData = new MetaData(true);
		assertEquals(new MetaData(true), metaData);
		assertEquals(0, metaData.getData().size());
		assertNotEquals(new MetaData(false), metaData);
	}

	@Test
	void metaDataWithValues() {
		MetaData metaData = new MetaData(Map.of("foo", "bar"), true);
		assertEquals("bar", metaData.getData().get("foo"));
	}
}
