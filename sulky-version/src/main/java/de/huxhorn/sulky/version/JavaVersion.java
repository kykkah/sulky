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

package de.huxhorn.sulky.version;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class handles parsing and comparison of
 * <a href="http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html">Java version numbers</a>.
 *
 * The static JVM attribute contains the version retrieved from the "java.version" system property.
 * If parsing that property fails (because security prevents access or the content is invalid) then
 * "java.specification.version" is used as a fallback. If parsing that property also fails (for similar reasons)
 * then the JVM attribute is initialized with MIN_VALUE, i.e. new JavaVersion(0,0,0,0,"!").
 *
 * This class does already handle the proposed version string suggested in http://openjdk.java.net/jeps/223
 *
 * The heuristic currently used is the following:
 * If the version string starts with "1." it is parsed as an YeOldeJavaVersion, otherwise Jep223JavaVersion is used.
 *
 * The COMPARATOR
 */
@SuppressWarnings({"TryWithIdenticalCatches", "Convert2Lambda", "PMD.AvoidThrowingNullPointerException", "PMD.IdenticalCatchBranches"})
// this is compiled with source and target 1.6
public abstract class JavaVersion
{
	/*
	 e.g. 1.8.0_25
	 */
	private static final String JAVA_VERSION_PROPERTY_NAME = "java.version";

	/*
	 e.g. 1.8
	 */
	private static final String JAVA_SPECIFICATION_VERSION_PROPERTY_NAME = "java.specification.version";

	private static final PropertyProvider DEFAULT_PROPERTY_PROVIDER = System::getProperty;
	private static final AtomicReference<PropertyProvider> PROPERTY_PROVIDER = new AtomicReference<>(DEFAULT_PROPERTY_PROVIDER);

	static void setPropertyProvider(PropertyProvider provider)
	{
		PROPERTY_PROVIDER.set(provider == null ? DEFAULT_PROPERTY_PROVIDER : provider);
	}

	static void resetPropertyProvider()
	{
		PROPERTY_PROVIDER.set(DEFAULT_PROPERTY_PROVIDER);
	}

	private static PropertyProvider propertyProvider()
	{
		return PROPERTY_PROVIDER.get();
	}

	/**
	 * The best possible approximation to the JVM JavaVersion.
	 *
	 * @return the JVM JavaVersion.
	 */
	public static JavaVersion getSystemJavaVersion()
	{
		JavaVersion version=null;
		try
		{
			String versionString = propertyProvider().getProperty(JAVA_VERSION_PROPERTY_NAME);
			if(versionString != null)
			{
				version = parse(versionString);
			}
		}
		catch(SecurityException ex)
		{
			// ignore
		}
		catch(IllegalArgumentException ex)
		{
			// didn't parse. Probably some strangeness like 1.8.0_25.1
		}

		if(version == null)
		{
			// either SecurityException or missing/broken standard property
			// fall back to specification version
			try
			{
				String versionString = propertyProvider().getProperty(JAVA_SPECIFICATION_VERSION_PROPERTY_NAME);
				if(versionString != null)
				{
					version = parse(versionString);
				}
			}
			catch(SecurityException ex)
			{
				// ignore
			}
			catch(IllegalArgumentException ex)
			{
				// didn't parse.
			}
		}
		if(version != null)
		{
			return version;
		}
		return YeOldeJavaVersion.MIN_VALUE;
	}

	/**
	 * Parses a Java version and returns the corresponding JavaVersion instance.
	 *
	 * @param versionString the String to be parsed
	 * @return the JavaVersion corresponding to the given versionString
	 * @throws java.lang.NullPointerException if versionString is null.
	 * @throws java.lang.IllegalArgumentException if versionString is invalid.
	 */
	public static JavaVersion parse(String versionString)
	{
		if(versionString == null)
		{
			throw new NullPointerException("versionString must not be null!");
		}
		if(versionString.startsWith("1."))
		{
			return YeOldeJavaVersion.parse(versionString);
		}
		return Jep223JavaVersion.parse(versionString);
	}

	/**
	 * Returns true, if the JVM version is bigger or equals to the given versionString.
	 *
	 * This is a convenience method that is simply a shortcut for
	 * (getSystemJavaVersion().compareTo(parse(versionString)) &gt;= 0).
	 *
	 * @param versionString the version to compare with the JVM version.
	 * @return true, if the JVM version is bigger or equals to the given versionString.
	 * @throws java.lang.NullPointerException if versionString is null.
	 * @throws java.lang.IllegalArgumentException if versionString is invalid.
	 */
	public static boolean isAtLeast(String versionString)
	{
		return isAtLeast(versionString, false);
	}

	/**
	 * Returns true, if the JVM version is bigger or equals to the given versionString.
	 *
	 * This is a convenience method that is simply a shortcut for
	 * (getSystemJavaVersion().compareTo(parse(versionString)) &gt;= 0).
	 *
	 * @param versionString the version to compare with the JVM version.
	 * @param ignoringPreReleaseIdentifier whether or not a potential pre-release identifier should be ignored.
	 * @return true, if the JVM version is bigger or equals to the given versionString.
	 * @throws java.lang.NullPointerException if versionString is null.
	 * @throws java.lang.IllegalArgumentException if versionString is invalid.
	 */
	public static boolean isAtLeast(String versionString, boolean ignoringPreReleaseIdentifier)
	{
		return isAtLeast(parse(versionString), ignoringPreReleaseIdentifier);
	}

	/**
	 * Returns true, if the JVM version is bigger or equals to the given versionString.
	 *
	 * This is a convenience method that is simply a shortcut for
	 * (getSystemJavaVersion().compareTo(version) &gt;= 0).
	 *
	 * @param version the version to compare with the JVM version.
	 * @return true, if the JVM version is bigger or equals to the given versionString.
	 * @throws java.lang.NullPointerException if version is null.
	 */
	public static boolean isAtLeast(JavaVersion version)
	{
		return isAtLeast(version, false);
	}

	/**
	 * Returns true, if the JVM version is bigger or equals to the given versionString.
	 *
	 * This is a convenience method that is simply a shortcut for
	 * (getSystemJavaVersion().compareTo(version) &gt;= 0).
	 *
	 * @param version the version to compare with the JVM version.
	 * @param ignoringPreReleaseIdentifier whether or not a potential pre-release identifier should be ignored.
	 * @return true, if the JVM version is bigger or equals to the given versionString.
	 * @throws java.lang.NullPointerException if version is null.
	 */
	public static boolean isAtLeast(JavaVersion version, boolean ignoringPreReleaseIdentifier)
	{
		if(version == null)
		{
			throw new NullPointerException("version must not be null!");
		}
		JavaVersion systemJavaVersion = getSystemJavaVersion();
		if(ignoringPreReleaseIdentifier)
		{
			systemJavaVersion = systemJavaVersion.withoutPreReleaseIdentifier();
			version = version.withoutPreReleaseIdentifier();
		}
		return COMPARATOR.compare(systemJavaVersion, version) >= 0;
	}

	/**
	 * The feature-release counter, incremented for every feature release
	 * regardless of release content. Features may be added in a feature
	 * release; they may also be removed, if advance notice was given
	 * at least one feature release ahead of time. Incompatible
	 * changes may be made when justified.
	 *
	 * (Formerly Major.)
	 *
	 * @return the feature-release counter
	 */
	public int getFeature()
	{
		return getMajor();
	}

	/**
	 * The interim-release counter, incremented for non-feature releases
	 * that contain compatible bug fixes and enhancements but no incompatible
	 * changes, no feature removals, and no changes to standard APIs.
	 *
	 * (Formerly Minor.)
	 *
	 * @return the interim-release counter
	 */
	public int getInterim()
	{
		return getMinor();
	}

	/**
	 * The update-release counter, incremented for compatible update releases
	 * that fix security issues, regressions, and bugs in newer features.
	 *
	 * (Formerly Security and Patch, but with a non-trivial incrementation rule.)
	 *
	 * @return the update-release counter
	 */
	public int getUpdate()
	{
		return getPatch();
	}

	/**
	 * The emergency patch-release counter, incremented only when it's necessary
	 * to produce an emergency release to fix a critical issue.
	 *
	 * (Using an additional element for this purpose minimizes disruption to
	 * both developers and users of in-flight update releases.)
	 *
	 * @return the emergency patch-release counter
	 */
	public abstract int getEmergencyPatch();

	/**
	 * Returns the "major" part of this version.
	 *
	 * @return the "major" part of this version.
	 */
	public abstract int getMajor();

	/**
	 * Returns the "minor" part of this version.
	 *
	 * @return the "minor" part of this version.
	 */
	public abstract int getMinor();

	/**
	 * Returns the "patch" (or update) part of this version.
	 *
	 * @return the "patch" (or update) part of this version.
	 */
	public abstract int getPatch();

	/**
	 * Returns the "identifier" part of this version.
	 *
	 * @return the "identifier" part of this version.
	 */
	public abstract String getPreReleaseIdentifier();

	/**
	 * Returns the full version string of this version.
	 *
	 * @return the full version string of this version.
	 */
	public abstract String toVersionString();

	/**
	 * Returns the short version string of this version.
	 *
	 * @return the short version string of this version.
	 */
	public abstract String toShortVersionString();

	/**
	 * Returns this JavaVersion without pre-release identifier.
	 *
	 * @return this JavaVersion without pre-release identifier.
	 */
	public abstract JavaVersion withoutPreReleaseIdentifier();

	public static final Comparator<JavaVersion> COMPARATOR = new Comparator<JavaVersion>()
	{
		@Override
		public int compare(JavaVersion o1, JavaVersion o2)
		{
			if(o1 == null && o2 == null)
			{
				return 0;
			}
			if(o1 == null)
			{
				return -1;
			}
			if(o2 == null)
			{
				return 1;
			}

			if(o1 instanceof YeOldeJavaVersion)
			{
				if(o2 instanceof YeOldeJavaVersion)
				{
					return ((YeOldeJavaVersion)o1).compareTo((YeOldeJavaVersion) o2);
				}
				if(o2 instanceof Jep223JavaVersion)
				{
					return -1;
				}
				throw new ClassCastException("Unexpected JavaVersion of class "+o2.getClass().getName()+"!");
			}
			if(o1 instanceof Jep223JavaVersion)
			{
				if(o2 instanceof Jep223JavaVersion)
				{
					return ((Jep223JavaVersion)o1).compareTo((Jep223JavaVersion) o2);
				}
				if(o2 instanceof YeOldeJavaVersion)
				{
					return 1;
				}
				throw new ClassCastException("Unexpected JavaVersion of class "+o2.getClass().getName()+"!");
			}
			throw new ClassCastException("Unexpected JavaVersion of class "+o1.getClass().getName()+"!");
		}
	};

	@FunctionalInterface
	interface PropertyProvider
	{
		String getProperty(String name);
	}

}
