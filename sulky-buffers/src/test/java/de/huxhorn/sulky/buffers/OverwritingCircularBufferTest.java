/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.buffers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OverwritingCircularBufferTest
{
	private final Logger logger = LoggerFactory.getLogger(OverwritingCircularBufferTest.class);

	private static final int TEST_BUFFER_SIZE = 5;
	private OverwritingCircularBuffer<Long> instance;

	@BeforeEach
	public void setUp()
		throws Exception
	{
		instance = new OverwritingCircularBuffer<>(TEST_BUFFER_SIZE);
	}

	@Test
	public void empty()
	{
		assertTrue(instance.isEmpty(), "Instance is not empty!");
		assertFalse(instance.isFull(), "Instance is full!");
		assertEquals(0, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		assertEquals(0, instance.getOverflowCounter(), "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertFalse(iterator.hasNext(), "iterator has next!");
	}

	@Test
	public void one()
	{
		instance.add((long) 1);

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertFalse(instance.isFull(), "Instance is full!");
		assertEquals(1, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		assertEquals(0, instance.getOverflowCounter(), "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		Long element = iterator.next();
		Long getRelativeValue = instance.getRelative(0);
		Long getValue = instance.get(0);
		if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}, getValue={}", 0, element, getRelativeValue, getValue);

		assertEquals((Long) (long) 1, element, "Unexpected value returned by iterator!");
		assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
		assertEquals(element, getValue, "Iterator and get values differ!");
	}

	@Test
	public void nearlyFull()
	{
		for(int i = 0; i < TEST_BUFFER_SIZE - 1; i++)
		{
			instance.add((long) i);
		}

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertFalse(instance.isFull(), "Instance is full!");
		assertEquals(TEST_BUFFER_SIZE - 1, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		assertEquals(0, instance.getOverflowCounter(), "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE - 1; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) i, element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void full()
	{
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			instance.add((long) i);
		}

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertTrue(instance.isFull(), "Instance isn't full!");
		assertEquals(TEST_BUFFER_SIZE, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		assertEquals(0, instance.getOverflowCounter(), "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) i, element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void overflowOne()
	{
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			instance.add((long) i);
		}
		instance.add((long) TEST_BUFFER_SIZE);

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertTrue(instance.isFull(), "Instance isn't full!");
		assertEquals(TEST_BUFFER_SIZE, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		long overflowCounter = instance.getOverflowCounter();
		assertEquals(1, overflowCounter, "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i + overflowCounter);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) (i + 1), element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void overflowDouble()
	{
		for(int i = 0; i < TEST_BUFFER_SIZE * 2; i++)
		{
			instance.add((long) i);
		}

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertTrue(instance.isFull(), "Instance isn't full!");
		assertEquals(TEST_BUFFER_SIZE, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		long overflowCounter = instance.getOverflowCounter();
		assertEquals(TEST_BUFFER_SIZE, overflowCounter, "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i + overflowCounter);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) (i + TEST_BUFFER_SIZE), element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void addAllList()
	{
		List<Long> values = new ArrayList<>();
		for(int i = 0; i < 4 * TEST_BUFFER_SIZE; i++)
		{
			values.add((long) i);
		}
		if(logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		instance.addAll(values);
		if(logger.isInfoEnabled()) logger.info("Buffer after adding: {}", instance);

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertTrue(instance.isFull(), "Instance isn't full!");
		assertEquals(TEST_BUFFER_SIZE, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		long overflowCounter = instance.getOverflowCounter();
		assertEquals(3 * TEST_BUFFER_SIZE, overflowCounter, "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i + overflowCounter);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) (i + 3 * TEST_BUFFER_SIZE), element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void addAllArray()
	{
		Long[] values = new Long[4 * TEST_BUFFER_SIZE];
		for(int i = 0; i < 4 * TEST_BUFFER_SIZE; i++)
		{
			values[i] = (long) i;
		}
		if(logger.isInfoEnabled()) logger.info("Adding values: {}", (Object)values);
		instance.addAll(values);
		if(logger.isInfoEnabled()) logger.info("Buffer after adding: {}", instance);

		assertFalse(instance.isEmpty(), "Instance is empty!");
		assertTrue(instance.isFull(), "Instance isn't full!");
		assertEquals(TEST_BUFFER_SIZE, instance.getAvailableElements(), "Size doesn't match!");
		assertEquals(TEST_BUFFER_SIZE, instance.getBufferSize(), "getBufferSize doesn't match!");
		long overflowCounter = instance.getOverflowCounter();
		assertEquals(3 * TEST_BUFFER_SIZE, overflowCounter, "overflowCounter doesn't match!");
		Iterator<Long> iterator = instance.iterator();
		assertTrue(iterator.hasNext(), "iterator doesn't have next!");
		for(int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i + overflowCounter);
			if(logger.isInfoEnabled()) logger.info("Element #{}: iterValue={}, getRelativeValue={}", i, element, getRelativeValue);

			assertEquals((Long) (long) (i + 3 * TEST_BUFFER_SIZE), element, "Unexpected value returned by iterator!");
			assertEquals(element, getRelativeValue, "Iterator and getRelative values differ!");
			assertEquals(element, getValue, "Iterator and get values differ!");
		}
	}

	@Test
	public void addRemove()
	{
		internalTestRemove(instance, 0);
		internalTestRemove(instance, 3);
		internalTestRemove(instance, 7);
		internalTestRemove(instance, 17);
		internalTestRemove(instance, 4 * TEST_BUFFER_SIZE);
		instance = new OverwritingCircularBuffer<>(17);
		internalTestRemove(instance, 23);

		// absurd...
		instance = new OverwritingCircularBuffer<>(1);
		internalTestRemove(instance, 17);
	}

	@Test
	public void addRemoveAll()
	{
		internalTestRemoveAll(instance, 0);
		internalTestRemoveAll(instance, 3);
		internalTestRemoveAll(instance, 7);
		internalTestRemoveAll(instance, 17);
		internalTestRemoveAll(instance, 4 * TEST_BUFFER_SIZE);
		instance = new OverwritingCircularBuffer<>(17);
		internalTestRemoveAll(instance, 23);

		// absurd...
		instance = new OverwritingCircularBuffer<>(1);
		internalTestRemoveAll(instance, 17);
	}

	public void internalTestRemove(OverwritingCircularBuffer<Long> impl, int valueCount)
	{
		long bufferSize = impl.getBufferSize();
		if(logger.isInfoEnabled()) logger.info("Executing add-remove-reset test with valueCount={} and buffer.getBufferSize={}.", valueCount, bufferSize);

		List<Long> values = new ArrayList<>(valueCount);
		for(int i = 0; i < valueCount; i++)
		{
			values.add((long) i);
		}
		if(logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		impl.addAll(values);
		if(logger.isInfoEnabled()) logger.info("Buffer after adding: {}", impl);

		if(valueCount == 0)
		{
			assertTrue(impl.isEmpty(), "Instance isn't empty!");
		}
		else
		{
			assertFalse(impl.isEmpty(), "Instance is empty!");
		}
		long expectedElementCount = valueCount;
		long expectedOverflowCount = 0;
		if(valueCount > bufferSize)
		{
			expectedElementCount = bufferSize;
			expectedOverflowCount = valueCount - bufferSize;
			assertTrue(impl.isFull(), "Instance isn't full!");
		}
		else
		{
			assertFalse(impl.isFull(), "Instance is full!");
		}
		assertEquals(expectedElementCount, impl.getAvailableElements(), "Available doesn't match!");
		long overflowCounter = instance.getOverflowCounter();
		assertEquals(expectedOverflowCount, overflowCounter, "overflowCounter doesn't match!");
		assertEquals(valueCount, impl.getSize(), "Size doesn't match!");
		for(int i = 0; i < expectedElementCount; i++)
		{
			assertFalse(impl.isEmpty(), "Instance is empty!");
			assertEquals(expectedElementCount - i, impl.getAvailableElements(), "Size doesn't match!");
			if(logger.isDebugEnabled()) logger.debug("Size before removal of element #{}: {}", i, impl.getAvailableElements());

			Long removeValue = impl.removeFirst();
			if(logger.isDebugEnabled()) logger.debug("Size after removal of element #{}: {}", i, impl.getAvailableElements());

			if(logger.isInfoEnabled()) logger.info("Element #{}: removeValue={}", i, removeValue);
			assertEquals((Long) (expectedOverflowCount + i), removeValue, "Unexpected value returned by remove!");

			assertFalse(impl.isFull(), "Instance is full!");
			assertEquals(expectedElementCount - i - 1, impl.getAvailableElements(), "Size doesn't match!");
		}
		assertTrue(impl.isEmpty(), "Instance isn't empty!");
		Long removeValue = impl.removeFirst();
		if(logger.isInfoEnabled()) logger.info("Element #{}: removeValue={}", expectedElementCount, removeValue);

		assertNull(removeValue, "Remove after last element returned a value: " + removeValue);

		assertEquals(expectedOverflowCount, impl.getOverflowCounter(), "overflowCounter doesn't match!");
		assertEquals(valueCount, impl.getSize(), "getSize doesn't match!");
		impl.reset();
		assertEquals(0, impl.getOverflowCounter(), "overflowCounter doesn't match!");
		assertEquals(0, impl.getSize(), "getSize doesn't match!");
		assertTrue(impl.isEmpty(), "Instance isn't empty!");
		assertFalse(impl.isFull(), "Instance is full!");
	}

	public void internalTestRemoveAll(OverwritingCircularBuffer<Long> impl, int valueCount)
	{
		long bufferSize = impl.getBufferSize();
		if(logger.isInfoEnabled()) logger.info("Executing add-remove-reset test with valueCount={} and buffer.getBufferSize={}.", valueCount, bufferSize);

		List<Long> values = new ArrayList<>(valueCount);
		for(int i = 0; i < valueCount; i++)
		{
			values.add((long) i);
		}
		if(logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		impl.addAll(values);
		if(logger.isInfoEnabled()) logger.info("Buffer after adding: {}", impl);

		if(valueCount == 0)
		{
			assertTrue(impl.isEmpty(), "Instance isn't empty!");
		}
		else
		{
			assertFalse(impl.isEmpty(), "Instance is empty!");
		}
		long expectedElementCount = valueCount;
		long expectedOverflowCount = 0;
		if(valueCount > bufferSize)
		{
			expectedElementCount = bufferSize;
			expectedOverflowCount = valueCount - bufferSize;
			assertTrue(impl.isFull(), "Instance isn't full!");
		}
		else
		{
			assertFalse(impl.isFull(), "Instance is full!");
		}
		assertEquals(expectedElementCount, impl.getAvailableElements(), "Size doesn't match!");
		assertEquals(expectedOverflowCount, impl.getOverflowCounter(), "overflowCounter doesn't match!");
		assertEquals(valueCount, impl.getSize(), "getSize doesn't match!");

		List<Long> removedList = impl.removeAll();
		assertTrue(impl.isEmpty(), "Instance isn't empty!");
		assertEquals(expectedOverflowCount, impl.getOverflowCounter(), "overflowCounter doesn't match!");
		assertEquals(valueCount, impl.getSize(), "getSize doesn't match!");


		for(int i = 0; i < expectedElementCount; i++)
		{
			Long removeValue = removedList.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}: removeValue={}", i, removeValue);
			assertEquals((Long) (expectedOverflowCount + i), removeValue, "Unexpected value returned by remove!");
		}
		impl.reset();
		assertEquals(0, impl.getOverflowCounter(), "overflowCounter doesn't match!");
		assertEquals(0, impl.getSize(), "getSize doesn't match!");
		assertTrue(impl.isEmpty(), "Instance isn't empty!");
		assertFalse(impl.isFull(), "Instance is full!");
	}
}
