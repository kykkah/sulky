/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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

package de.huxhorn.sulky.junit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * Provides the invocation contexts for {@link LoggingTest} annotated methods.
 */
final class LoggingTestInvocationContextProvider
	implements TestTemplateInvocationContextProvider
{
	private static final Boolean[] CONFIGS = {null, Boolean.TRUE, Boolean.FALSE};

	@Override
	public boolean supportsTestTemplate(ExtensionContext context)
	{
		return context.getTestMethod()
				.map(method -> method.isAnnotationPresent(LoggingTest.class))
				.orElse(false);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context)
	{
		return Arrays.stream(CONFIGS).map(LoggingInvocationContext::new);
	}

	private static final class LoggingInvocationContext
		implements TestTemplateInvocationContext
	{
		private final Boolean logging;

		private LoggingInvocationContext(Boolean logging)
		{
			this.logging = logging;
		}

		@Override
		public String getDisplayName(int invocationIndex)
		{
			return "logging=" + logging;
		}

		@Override
		public List<Extension> getAdditionalExtensions()
		{
			return List.of(new LoggingLifecycleExtension(logging));
		}
	}

	private static final class LoggingLifecycleExtension
		implements BeforeEachCallback, AfterEachCallback
	{
		private final Boolean logging;

		private LoggingLifecycleExtension(Boolean logging)
		{
			this.logging = logging;
		}

		@Override
		public void beforeEach(ExtensionContext context)
			throws Exception
		{
			Object instance = context.getRequiredTestInstance();
			if(instance instanceof LoggingTestBase)
			{
				LoggingTestBase base = (LoggingTestBase) instance;
				base.setLogging(logging);
				base.beforeEachLogging();
			}
			else
			{
				throw new IllegalStateException("@LoggingTest requires test classes to extend LoggingTestBase.");
			}
		}

		@Override
		public void afterEach(ExtensionContext context)
			throws Exception
		{
			Object instance = context.getRequiredTestInstance();
			if(instance instanceof LoggingTestBase)
			{
				((LoggingTestBase) instance).afterEachLogging();
			}
		}
	}
}
