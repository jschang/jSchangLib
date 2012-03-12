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

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Set;

//import com.jonschang.investing.model.Quote;
//import com.jonschang.investing.stocks.model.StockQuote;
import com.jonschang.utils.BusinessCalendar;
import com.jonschang.utils.DateRange;
import com.jonschang.utils.TimeInterval;
import com.jonschang.utils.DateUtils;
import com.jonschang.utils.FederalHolidayCalendar.Holidays;

public class DateUtilsTest 
{
	BusinessCalendar m_bc=null;
	
	@Before public void setupCalendar()
	{
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
	
	@Test public void testBusinessCalendar() {
		
	}
	
	private boolean sameDay(Date one, Date two)
	{
		Calendar cal1 = new GregorianCalendar();
		cal1.setTime(one);
		Calendar cal2 = new GregorianCalendar();
		cal2.setTime(two);
		return
			(
				(cal1.get(Calendar.DAY_OF_YEAR)==cal2.get(Calendar.DAY_OF_YEAR))
				&& (cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR))
			);
	}
}
