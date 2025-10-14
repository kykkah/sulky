package de.huxhorn.sulky.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class LoggingPropertyChangeListenerTest {

	private Logger rootLogger;
	private CapturingAppender appender;

	@BeforeEach
	void setUp() {
		rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		appender = new CapturingAppender();
		appender.start();
		rootLogger.addAppender(appender);
	}

	@AfterEach
	void tearDown() {
		rootLogger.detachAppender(appender);
		appender.stop();
	}

	@Test
	void logChangeWithDefaultLogger() {
		LoggingPropertyChangeListener listener = new LoggingPropertyChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(new Object(), "valueName", "oldValue", "newValue");

		listener.propertyChange(event);

		assertEquals(1, appender.events.size(), "Expected a single logging event");
		ILoggingEvent loggingEvent = appender.events.get(0);
		assertEquals("PropertyChangeEvent:\n\tpropertyName='valueName'\n\toldValue=oldValue\n\tnewValue=newValue",
				loggingEvent.getFormattedMessage());
		assertEquals("de.huxhorn.sulky.logging.LoggingPropertyChangeListener", loggingEvent.getLoggerName());
	}

	@Test
	void logChangeWithCustomLogger() {
		LoggingPropertyChangeListener listener = new LoggingPropertyChangeListener(LoggerFactory.getLogger("foo"));
		PropertyChangeEvent event = new PropertyChangeEvent(new Object(), "valueName", "oldValue", "newValue");

		listener.propertyChange(event);

		assertEquals(1, appender.events.size(), "Expected a single logging event");
		ILoggingEvent loggingEvent = appender.events.get(0);
		assertEquals("PropertyChangeEvent:\n\tpropertyName='valueName'\n\toldValue=oldValue\n\tnewValue=newValue",
				loggingEvent.getFormattedMessage());
		assertEquals("foo", loggingEvent.getLoggerName());
	}

	@Test
	void constructorRejectsNullLogger() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new LoggingPropertyChangeListener(null));
		assertEquals("logger must not be null!", exception.getMessage());
	}

	private static class CapturingAppender extends AppenderBase<ILoggingEvent> {
		private final List<ILoggingEvent> events = new ArrayList<>();

		@Override
		protected void append(ILoggingEvent eventObject) {
			events.add(eventObject);
		}
	}
}
