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

import org.junit.Test;
import org.junit.Assert;

import java.util.Calendar;

import com.jonschang.utils.FederalHolidayCalendar;
import com.jonschang.utils.FederalHolidayCalendar.Holidays;

public class FederalHolidayCalendarTest 
{
	@Test public void testChristmas()
	{
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		
		// test christmas, a day by precise date
		fhc.clear();fhc.set(2007, Calendar.DECEMBER, 24);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.DECEMBER, 25);
		Assert.assertEquals(Holidays.CHRISTMAS,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.DECEMBER, 26);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
	}
	
	@Test public void testMartinLutherKingDay()
	{
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		
		fhc.clear();fhc.set(2007, Calendar.JANUARY, 14);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.JANUARY, 15);
		Assert.assertEquals(Holidays.MARTIN_LUTHER_KING,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.JANUARY, 16);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
	}
		
	@Test public void testThanksgiving()
	{
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		
		fhc.clear();fhc.set(2007, Calendar.NOVEMBER, 21);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.NOVEMBER, 22);
		Assert.assertEquals(Holidays.THANKSGIVING,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.NOVEMBER, 23);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
	}
	
	@Test public void testMemorialDay()
	{
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		
		fhc.clear();fhc.set(2007, Calendar.MAY, 26);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.MAY, 28);
		Assert.assertEquals(Holidays.MEMORIAL,fhc.getHoliday());
		fhc.clear();fhc.set(2007, Calendar.MAY, 27);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
	}
	
	@Test public void testInaugurationDay()
	{
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		
		fhc.clear();fhc.set(2009, Calendar.JANUARY, 19);
		Assert.assertEquals(Holidays.MARTIN_LUTHER_KING,fhc.getHoliday());
		fhc.clear();fhc.set(2009, Calendar.JANUARY, 20);
		Assert.assertEquals(Holidays.INAUGURATION,fhc.getHoliday());
		fhc.clear();fhc.set(2009, Calendar.JANUARY, 21);
		Assert.assertEquals(Holidays.NONE,fhc.getHoliday());
	}
}
