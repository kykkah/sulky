package de.huxhorn.sulky.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class KeyStrokesTest {

	@Test
	void resolveAcceleratorKeyStrokeWithValidInputSetsExpectedModifiers() {
		KeyStroke keyStroke = KeyStrokes.resolveAcceleratorKeyStroke("command shift A");

		assertNotNull(keyStroke, "KeyStroke should be created for valid accelerator string");
		int modifiers = keyStroke.getModifiers();
		assertTrue((modifiers & KeyStrokes.COMMAND_MODIFIERS) != 0,
				"Command modifier should be present");
		assertTrue((modifiers & KeyEvent.SHIFT_MASK) != 0,
				"Legacy shift modifier mask should be present");
		assertTrue((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0,
				"Extended shift modifier mask should be present");
	}

	@Test
	void resolveAcceleratorKeyStrokeWithInvalidInputReturnsNull() {
		assertNull(KeyStrokes.resolveAcceleratorKeyStroke("foo"));
	}

	@Test
	void resolveAcceleratorKeyStrokeWithNullReturnsNull() {
		assertNull(KeyStrokes.resolveAcceleratorKeyStroke(null));
	}
}
