/*
###############################
# Copyright (C) 2012 Jon Schang
# 
# This file is part of jSchangLib, released under the LGPLv3
# 
# jSchangLib is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# jSchangLib is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with jSchangLib.  If not, see <http://www.gnu.org/licenses/>.
###############################
*/

package com.jonschang.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;

import org.apache.log4j.*;

import com.jonschang.utils.BusinessCalendar;
import com.jonschang.utils.FederalHolidayCalendar.Holidays;

public class BusinessCalendarTest 
{
	BusinessCalendar m_bc=null;
	Logger m_logger;
	
	@Before public void setupCalendar()
	{
		// setup the logger
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		m_logger = Logger.getLogger(this.getClass());
		
		m_bc = new BusinessCalendar();
		
		Set<Holidays> holidaySet = new HashSet<Holidays>();
		holidaySet.add(Holidays.MARTIN_LUTHER_KING);
		holidaySet.add(Holidays.INAUGURATION);
		m_bc.setFederalHolidays(holidaySet);
		
		Set<Integer> businessDays = new HashSet<Integer>();
		businessDays.add(Calendar.MONDAY);
		businessDays.add(Calendar.TUESDAY);
		businessDays.add(Calendar.WEDNESDAY);
		businessDays.add(Calendar.THURSDAY);
		businessDays.add(Calendar.FRIDAY);
		m_bc.setBusinessWeek(businessDays);	
		
		m_bc.setOpeningHour(9);
		m_bc.setOpeningMinute(30);
		m_bc.setClosingHour(16);
		m_bc.setClosingMinute(0);
		
		m_bc.closing();
	}
	
	@Test public void testClosestAfter()
	{
		m_bc.set(2007, Calendar.JANUARY, 12, 22, 22);
		m_bc.closestAfter(TimeInterval.DAY);
		
		Calendar d = new java.util.GregorianCalendar();
		d.set(2007, Calendar.JANUARY, 16);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
		
		m_bc.set(2008, Calendar.FEBRUARY, 9, 22, 22);
		m_bc.closestAfter(TimeInterval.DAY);
		
		d = new java.util.GregorianCalendar();
		d.set(2008, Calendar.FEBRUARY, 11);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
	}
	
	@Test public void testClosestBefore()
	{
		m_bc.set(2007, Calendar.JANUARY, 16, 22, 22);
		m_bc.closestBefore(TimeInterval.DAY);
		
		Calendar d = new java.util.GregorianCalendar();
		d.set(2007, Calendar.JANUARY, 12);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
	}
	
	@Test public void testClosestAfter5Min()
	{
		m_bc.set(2007, Calendar.JANUARY, 12, 22, 22);
		m_bc.closestAfter(TimeInterval.MIN_5);
		
		Calendar d = new java.util.GregorianCalendar();
		d.set(2007, Calendar.JANUARY, 16, 9, 35);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(d.get(Calendar.HOUR),m_bc.get(Calendar.HOUR));
		Assert.assertEquals(d.get(Calendar.MINUTE),m_bc.get(Calendar.MINUTE));
		
		m_bc.set(2008, Calendar.FEBRUARY, 9, 0, 22, 22);
		m_bc.closestAfter(TimeInterval.MIN_5);
		
		d = new java.util.GregorianCalendar();
		d.set(2008, Calendar.FEBRUARY, 11, 9, 35);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(d.get(Calendar.HOUR),m_bc.get(Calendar.HOUR));
		Assert.assertEquals(d.get(Calendar.MINUTE),m_bc.get(Calendar.MINUTE));
	}
	
	@Test public void testClosestBefore5Min()
	{
		m_bc.set(2007, Calendar.JANUARY, 16, 1, 22, 22);
		m_bc.closestBefore(TimeInterval.MIN_5);
		
		Calendar d = new java.util.GregorianCalendar();
		d.set(2007, Calendar.JANUARY, 12, 16, 00);
		
		Assert.assertEquals(d.get(Calendar.YEAR),m_bc.get(Calendar.YEAR));
		Assert.assertEquals(d.get(Calendar.MONTH),m_bc.get(Calendar.MONTH));
		Assert.assertEquals(d.get(Calendar.DAY_OF_MONTH),m_bc.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(d.get(Calendar.HOUR),m_bc.get(Calendar.HOUR));
		Assert.assertEquals(d.get(Calendar.MINUTE),m_bc.get(Calendar.MINUTE));
	}
}
