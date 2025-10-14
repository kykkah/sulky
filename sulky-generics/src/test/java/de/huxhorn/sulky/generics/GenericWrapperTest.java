package de.huxhorn.sulky.generics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GenericWrapperTest {

	@Test
	void toStringReflectsWrappedType() {
		assertEquals("wrapper-Integer[wrapped=1]", new GenericWrapper<>(1).toString());
		assertEquals("wrapper-null[wrapped=null]", new GenericWrapper<>(null).toString());
	}

	@Test
	void equalsIsBasedOnWrappedValue() {
		GenericWrapper<Number> nullWrapper = new GenericWrapper<>(null);
		GenericWrapper<Number> oneIntWrapper = new GenericWrapper<>(1);
		GenericWrapper<Number> oneLongWrapper = new GenericWrapper<>(1L);

		assertNotEquals(null, oneIntWrapper);
		assertNotEquals(1, oneIntWrapper);
		assertEquals(oneIntWrapper, oneIntWrapper);
		assertEquals(new GenericWrapper<>(1), oneIntWrapper);
		assertNotEquals(oneLongWrapper, oneIntWrapper);
		assertNotEquals(oneIntWrapper, oneLongWrapper);
		assertNotEquals(nullWrapper, oneIntWrapper);
		assertNotEquals(oneIntWrapper, nullWrapper);
		assertEquals(new GenericWrapper<>(null), nullWrapper);
	}

	@Test
	void hashCodeMatchesWrappedValue() {
		assertEquals(0, new GenericWrapper<>(null).hashCode());
		assertEquals(Integer.valueOf(1).hashCode(), new GenericWrapper<>(1).hashCode());
		assertEquals(Long.valueOf(1L).hashCode(), new GenericWrapper<>(1L).hashCode());
	}

	@Test
	void wrappersReportSupportedTypes() {
		GenericWrapper<Number> nullWrapper = new GenericWrapper<>(null);
		assertFalse(nullWrapper.isWrapperFor(Integer.class));
		assertFalse(nullWrapper.isWrapperFor(Number.class));
		assertFalse(nullWrapper.isWrapperFor(String.class));

		GenericWrapper<Number> intWrapper = new GenericWrapper<>(1);
		assertTrue(intWrapper.isWrapperFor(Integer.class));
		assertTrue(intWrapper.isWrapperFor(Number.class));
		assertFalse(intWrapper.isWrapperFor(String.class));
		assertEquals(1, intWrapper.getWrapped());

		GenericWrapper<Wrapper> nestedWrapper = new GenericWrapper<>(new GenericWrapper<>(1));
		assertTrue(nestedWrapper.isWrapperFor(Integer.class));
		assertTrue(nestedWrapper.isWrapperFor(Number.class));
		assertFalse(nestedWrapper.isWrapperFor(String.class));
		assertTrue(nestedWrapper.getWrapped() instanceof Wrapper);
	}

	@Test
	void unwrapFromNullWrapperThrows() {
		GenericWrapper<Number> nullWrapper = new GenericWrapper<>(null);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> nullWrapper.unwrap(Integer.class));
		assertEquals("This Wrapper wraps null!", exception.getMessage());
	}

	@Test
	void unwrapToSupportedTypeReturnsValue() {
		GenericWrapper<Number> wrapper = new GenericWrapper<>(1);
		assertEquals(1, wrapper.unwrap(Integer.class));
		assertEquals(1, wrapper.unwrap(Number.class));
	}

	@Test
	void unwrapToUnsupportedTypeThrows() {
		GenericWrapper<Number> wrapper = new GenericWrapper<>(1);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> wrapper.unwrap(String.class));
		assertEquals("This Wrapper does not wrap an instance of the given interface!", exception.getMessage());
	}

	@Test
	void nestedWrapperCanUnwrapSupportedTypes() {
		GenericWrapper<Wrapper> nestedWrapper = new GenericWrapper<>(new GenericWrapper<>(1));
		assertEquals(1, nestedWrapper.unwrap(Number.class));
	}

	@Test
	void nestedWrapperRejectsUnsupportedTypes() {
		GenericWrapper<Wrapper> nestedWrapper = new GenericWrapper<>(new GenericWrapper<>(1));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> nestedWrapper.unwrap(String.class));
		assertEquals("This Wrapper does not wrap an instance of the given interface!", exception.getMessage());
	}
}
