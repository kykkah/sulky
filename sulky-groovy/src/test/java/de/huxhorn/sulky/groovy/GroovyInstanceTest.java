/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2018 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.groovy;

import de.huxhorn.sulky.junit.JUnitTools;
import de.huxhorn.sulky.junit.LoggingTest;
import de.huxhorn.sulky.junit.LoggingTestBase;
import groovy.lang.Script;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroovyInstanceTest
	extends LoggingTestBase
{
	private static final long ONE_MINUTE = 60_000;

	@TempDir
	Path tempDir;

	private File groovyFile;

	@BeforeEach
	public void setUp()
		throws IOException
	{
		groovyFile = tempDir.resolve("Foo.groovy").toFile();
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@LoggingTest
	public void normal()
			throws IOException
	{
		JUnitTools.copyResourceToFile("/Foo.groovy", groovyFile, System.currentTimeMillis() - ONE_MINUTE);

		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(groovyFile.getAbsolutePath());
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script = (Script) object;
		String result = (String) script.run();
		assertEquals("Foo", result);

		assertNull(instance.getErrorCause());
		assertNull(instance.getErrorMessage());

		Object newObject = instance.getNewInstance();
		assertNotNull(object);
		assertTrue(newObject instanceof Script);
		Script newScript = (Script) newObject;
		String newResult = (String) newScript.run();
		assertEquals("Foo", newResult);
		assertNotSame(newScript, script);
		assertNotSame(instance.getNewInstance(), newScript);
		assertSame(instance.getInstance(), script);

		assertSame(script, instance.getInstanceAs(Script.class));
		assertNotSame(script, instance.getNewInstanceAs(Script.class));
		newScript = instance.getNewInstanceAs(Script.class);
		newResult = (String) newScript.run();
		assertEquals("Foo", newResult);

		assertNull(instance.getInstanceAs(Comparable.class));
		assertNull(instance.getNewInstanceAs(Comparable.class));
	}

	@LoggingTest
	public void refresh()
		throws IOException, InterruptedException
	{
		JUnitTools.copyResourceToFile("/Foo.groovy", groovyFile, System.currentTimeMillis() - ONE_MINUTE);

		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(groovyFile.getAbsolutePath());
		instance.setRefreshInterval(1);
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script = (Script) object;
		String result = (String) script.run();
		assertEquals("Foo", result);

		JUnitTools.copyResourceToFile("/Bar.groovy", groovyFile, System.currentTimeMillis());

		Thread.sleep(100);

		object = instance.getInstance();
		assertTrue(object instanceof Script);
		script = (Script) object;
		result = (String) script.run();
		assertEquals("Bar", result);
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@LoggingTest
	public void broken()
		throws IOException, InterruptedException
	{
		JUnitTools.copyResourceToFile("/Foo.groovy", groovyFile, System.currentTimeMillis() - 2 * ONE_MINUTE);

		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(groovyFile.getAbsolutePath());
		instance.setRefreshInterval(1);
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script = (Script) object;
		String result = (String) script.run();
		assertEquals("Foo", result);

		JUnitTools.copyResourceToFile("/Broken.b0rken", groovyFile, System.currentTimeMillis() - ONE_MINUTE);

		Thread.sleep(100);

		assertNull(instance.getInstanceClass());
		assertNull(instance.getInstance());
		// error should be logged only once...
		Thread.sleep(10);
		assertNull(instance.getInstanceClass());
		assertNull(instance.getInstance());
		Thread.sleep(10);
		assertNull(instance.getInstanceClass());
		assertNull(instance.getInstance());
		Thread.sleep(10);
		assertNotNull(instance.getErrorCause());
		assertNotNull(instance.getErrorMessage());

		JUnitTools.copyResourceToFile("/Bar.groovy", groovyFile, System.currentTimeMillis());

		object = instance.getInstance();
		assertTrue(object instanceof Script);
		script = (Script) object;
		result = (String) script.run();
		assertEquals("Bar", result);

		assertNull(instance.getErrorCause());
		assertNull(instance.getErrorMessage());
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@LoggingTest
	public void nullFile()
	{
		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(null);
		Class instanceClass = instance.getInstanceClass();
		assertNull(instanceClass);
		Object object = instance.getInstance();
		assertNull(object);
		assertNull(instance.getErrorCause());
		assertEquals("groovyFileName must not be null!", instance.getErrorMessage());
	}
}
