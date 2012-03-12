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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.jonschang.utils.BusinessCalendar;
import com.jonschang.utils.DateRange;
import com.jonschang.utils.DateUtils;
import com.jonschang.utils.TimeInterval;
import com.jonschang.investing.model.Quote;

public class QuoteDateUtils {
	
	static private Logger m_logger = Logger.getLogger(QuoteDateUtils.class);
	
	/**
	 * determines a list of DateRanges missing from the set of Quotes
	 * using the BusinessCalendar created by the passed in ExchangeContext
	 * @param exchange the exchange the quotes are from
	 * @param interval in seconds
	 * @param range the date range we have quotes for
	 * @param quotes the quotes we want to extract date ranges for (should be pre-sorted by Date)
	 * @return a list of DateRanges' missing from the list of quotes
	 */
	static public List<DateRange> getQuoteDateRangeGaps(BusinessCalendar calendar, TimeInterval interval, DateRange range, List<? extends Quote> quotes)
		throws Exception
	{
		m_logger.trace("in getQuoteDateRangeGaps");
		
		BusinessCalendar cal = (BusinessCalendar)calendar.clone();
		List<DateRange> toRet = new ArrayList<DateRange>();
		Date expectedDate=null;
		Date lastDate=null;
		
		// no reason to continue if there aren't any quotes
		if( quotes==null || quotes.size()==0 ) {
			toRet.add(new DateRange((Date)range.getStart().clone(),(Date)range.getEnd().clone()));
			return toRet;
		}
		
		// if the range start isn't a business day,
		// then cycle it to the next business day
		cal.setTime(range.getStart());
		if( !cal.isBusinessDay() )
			cal.closestAfter(interval);
		else cal.normalizeToInterval(interval);
			
		Collections.sort(quotes);
		
		// validate that the quotes are actually ordered by date
		for( Quote quote : quotes ) {
			if( lastDate == null )
				lastDate = quote.getDate();
			else if( quote.getDate().before(lastDate) ) 
				throw new Exception("Date List is not ordered by Date.  Expecting "+quote.getDate()+" to be after "+lastDate);
			lastDate = quote.getDate();
		}
		
		// the first quote we should expect to have the first business day after the range start
		expectedDate = cal.getTime();
		lastDate = cal.getTime();
		m_logger.trace("next expected date: "+expectedDate);
		
		for( Quote quote : quotes )
		{
			// if the quote date is after the expected date
			// then we have a date range gap 
			// and should create a range
			if( quote.getDate().after(expectedDate) )
			{
				cal.setTime(quote.getDate());
				while( cal.getTime().before(expectedDate) ) {
					cal.closestAfter(interval);
				}
				cal.closestBefore(interval);
				m_logger.trace( "adding date range: "+expectedDate+" to "+cal.getTime() );
				toRet.add( new DateRange(expectedDate,cal.getTime()) );
			}
			
			if( ! quote.getDate().before(expectedDate) ) {
				// set the expected date to the next business day of the calendar
				cal.setTime(quote.getDate());
				cal.closestAfter(interval);
				expectedDate = cal.getTime();
				m_logger.trace("next expected date: "+expectedDate);
			}
		}
		
		if( expectedDate.before(range.getEnd()) )
		{
			toRet.add( new DateRange(expectedDate, range.getEnd()) );
			m_logger.trace( "adding date range: "+expectedDate+" to "+range.getEnd() );
		}
		
		return toRet;
	}
}
