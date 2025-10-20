/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2021 Joern Huxhorn
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
 * Copyright 2007-2021 Joern Huxhorn
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

package de.huxhorn.sulky.conditions;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanValuesTest
	extends ConditionTestBase
{
	@Test
	void testTrue()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		BooleanValues condition = BooleanValues.TRUE;
		assertTrue(condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	void testFalse()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		BooleanValues condition = BooleanValues.FALSE;
		assertFalse(condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	void testHashCode()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertNotEquals(BooleanValues.FALSE.hashCode(), BooleanValues.TRUE.hashCode());
	}

	@Test
	void testEquals()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertNotEquals(BooleanValues.FALSE, BooleanValues.TRUE);
	}

	@Test
	@SuppressWarnings({"PMD.EqualsNull", "PMD.UseAssertEqualsInsteadOfAssertTrue", "PMD.SimplifiableTestAssertion"})
	void testBasicEquals()
	{
		Condition condition = BooleanValues.TRUE;
		//noinspection ObjectEqualsNull
		assertFalse(condition.equals(null));
		assertFalse(condition.equals(new Object()));
		//noinspection EqualsWithItself
		assertTrue(condition.equals(condition));
	}

	@Test
	void testString()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertEquals("false", BooleanValues.FALSE.toString());
		assertEquals("true", BooleanValues.TRUE.toString());
	}
}
