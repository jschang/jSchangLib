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

import java.util.Date;
import java.util.List;
import java.util.Collection;
import com.jonschang.investing.model.Quotable;
import com.jonschang.investing.model.Quote;
import com.jonschang.utils.TimeInterval;
import com.jonschang.utils.DateRange;
import com.jonschang.utils.HasGetObjectByObject;

/**
 * interface to define quote services
 * the intention is that quote services are stateless when it comes to pulling quotes
 * @author schang
 * @param <Q> the quote
 * @param <I> the quotable that has the quotes, the parameter to Quotable has been intentionally left off
 */
@SuppressWarnings(value={"unchecked"})
public interface QuoteService<Q extends Quote<I>, I extends Quotable> {

	boolean supports(TimeInterval interval);
	
	/**
	 * pulls a date ordered list of quotes in a given time-frame
	 * @param quotable the quotables to look up
	 * @param start the start of the time interval; must be truncated to the marketContext date if in the future.  
	 *        if this day is not a business day, for each exchange, then it will be set to the closest prior business day
	 * @param end the end of the time interval; must be truncated to the marketContext date if in the future.  
	 *        if this day is not a business day, for each exchange, then it will be set to the closest prior business day
	 * @return a map of quotable/date ordered list pairs
	 * @throws Exception
	 */
	List<I> pullDateRange(Collection<I> quotables,DateRange range, TimeInterval interval) 
		throws ServiceException;
	
	/**
	 * pull a number of quotes of a interval from a reference date for a set of stocks
	 * @param quotables the quotables to pull
	 * @param refDate the date to either start or end on
	 * @param number number of quotes to pull...if negative, then refDate is the beginning, else refDate is the end 
	 * @param interval
	 * @return 
	 * @throws Exception
	 */
	List<I> pullNumber(Collection<I> quotables, Date refDate, int number, TimeInterval interval) 
		throws ServiceException;
}