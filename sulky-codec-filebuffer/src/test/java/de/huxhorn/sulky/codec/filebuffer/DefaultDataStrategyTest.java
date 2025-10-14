package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultDataStrategyTest extends DataStrategyTestBase {

	@Override
	protected DataStrategy<String> createInstance() {
		return new DefaultDataStrategy<>();
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
	void setIsUnsupported() {
		assertThrows(UnsupportedOperationException.class,
				() -> instance.set(0, null, null, null, null, null));
	}

	@Test
	void setSupportedReturnsFalse() {
		assertFalse(instance.isSetSupported());
	}
}
