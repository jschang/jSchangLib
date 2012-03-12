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

import java.util.GregorianCalendar;
import java.util.Calendar;

public class FederalHolidayCalendar extends GregorianCalendar 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;

	static public enum Holidays
	{
		NONE,
		MARTIN_LUTHER_KING,
		CHRISTMAS,
		THANKSGIVING,
		NEW_YEARS,
		PRESIDENTS,
		GOOD_FRIDAY,
		MEMORIAL,
		LABOR,
		INAUGURATION,
		WASHINGTON_BDAY,
		INDEPENDENCE,
		COLUMBUS,
		VETERANS,
	}
	
	public Holidays getHoliday()
	{
		Integer dom = this.get(Calendar.DAY_OF_MONTH);
		Integer m = this.get(Calendar.MONTH);
		Integer dow = this.get(Calendar.DAY_OF_WEEK);
		Integer y = this.get(Calendar.YEAR);
		
		switch( m )
		{
	
		case Calendar.JANUARY:
			
			if( dom==1 )
				return Holidays.NEW_YEARS;
			
			if( y%4==1 && dom == 20 )
				return Holidays.INAUGURATION;
			
			if( dow == Calendar.MONDAY && this.weekdayOfMonth(3,dom) )
				return Holidays.MARTIN_LUTHER_KING;
			
			break;
			
		case Calendar.FEBRUARY:
			
			if( dow == Calendar.MONDAY && this.weekdayOfMonth(3,dom) )
				return Holidays.WASHINGTON_BDAY;
			
			break;
			
		/*
		case Calendar.MARCH:
			
			if( dow == Calendar.FRIDAY && this.weekdayOfMonth(3,dom))
				return Holidays.GOOD_FRIDAY;
			
			break;
		*/
			
		case Calendar.MAY:
			
			if( dow == Calendar.MONDAY && 31-dom < 7 )
				return Holidays.MEMORIAL;
			
			break;
			
		case Calendar.JULY:
			
			if( dom == 4 )
				return Holidays.INDEPENDENCE;
			
			break;
			
		case Calendar.SEPTEMBER:
				
			if( dow==Calendar.MONDAY && weekdayOfMonth(1,dom) )
				return Holidays.LABOR;
			
			break;
			
		case Calendar.OCTOBER:
			
			if( dow==Calendar.MONDAY && weekdayOfMonth(2,dom) ) 
				return Holidays.COLUMBUS;
			
			break;
			
		case Calendar.NOVEMBER:
			
			if( dom==11 )
				return Holidays.VETERANS;
			
			if( dow == Calendar.THURSDAY && weekdayOfMonth(4,dom) )
				return Holidays.THANKSGIVING;
			
			break;
			
		case Calendar.DECEMBER:
			
			if( dom==25 )
				return Holidays.CHRISTMAS;
			
			break;
			
		}
		
		return Holidays.NONE;
	}
	
	/**
	 * if this is the weekNumber instance of a day in a month 
	 * @param weekNumber
	 * @param day
	 * @return
	 */
	private Boolean weekdayOfMonth(Integer weekNumber, Integer day)
	{
		return (day-((weekNumber-1)*7)) > 0 && (day-((weekNumber-1)*7)) < 7;
	}
}
