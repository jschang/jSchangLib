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
package com.jonschang.investing;

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

import com.jonschang.investing.model.Quote;
import com.jonschang.investing.stocks.model.StockQuote;
import com.jonschang.utils.BusinessCalendar;
import com.jonschang.utils.DateRange;
import com.jonschang.utils.TimeInterval;
import com.jonschang.utils.DateUtils;
import com.jonschang.utils.FederalHolidayCalendar.Holidays;

public class QuoteDateUtilsTest 
{
	BusinessCalendar m_bc=null;
	
	public void setupCalendar()
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
	
	@Test public void testGetQuoteDateRangeGaps()
		throws Exception
	{
		setupCalendar();
		
		m_bc.normalizeToMinute();
		
		m_bc.set(2008,Calendar.OCTOBER,15);
		Date start = m_bc.getTime();
		m_bc.set(2009,Calendar.JANUARY,17);
		Date end = m_bc.getTime();
		
		List<StockQuote> quotes = new ArrayList<StockQuote>();
		StockQuote newQ; 
		newQ = new StockQuote();
		m_bc.set(2008, Calendar.OCTOBER, 15);
		newQ.setDate( m_bc.getTime() );
		quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 14);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 15);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 16);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 17);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 21);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 22);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.OCTOBER, 23);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.NOVEMBER, 17);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.NOVEMBER, 18);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2008, Calendar.NOVEMBER, 19);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 14);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 15);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 16);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 17);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 18);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		newQ = new StockQuote();m_bc.set(2009, Calendar.JANUARY, 19);newQ.setDate( m_bc.getTime() );quotes.add(newQ);
		java.util.Collections.sort(quotes);
		
		List<DateRange> missingRanges = QuoteDateUtils.getQuoteDateRangeGaps(m_bc,TimeInterval.DAY,new DateRange(start,end),quotes);
		
		Assert.assertEquals(3, missingRanges.size());
		m_bc.set(2008,Calendar.OCTOBER,20);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "1st gap, start day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(0).getStart()) );
		m_bc.set(2008,Calendar.OCTOBER,20);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "1st gap, end day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(0).getEnd()) );
		
		Assert.assertEquals(3, missingRanges.size());
		m_bc.set(2008,Calendar.OCTOBER,24);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "2nd gap, start day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(1).getStart()) );
		m_bc.set(2008,Calendar.NOVEMBER,14);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "2nd gap, end day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(1).getEnd()) );

		Assert.assertEquals(3, missingRanges.size());
		m_bc.set(2008,Calendar.NOVEMBER,20);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "3rd gap, start day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(2).getStart()) );
		m_bc.set(2009,Calendar.JANUARY,13);newQ.setDate( m_bc.getTime() );
		Assert.assertTrue( "3rd gap, end day: expecting "+newQ, sameDay(newQ.getDate(),missingRanges.get(2).getEnd()) );
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
