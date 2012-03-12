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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import org.apache.log4j.Logger;

public class BusinessCalendar extends GregorianCalendar 
{
	private static final long serialVersionUID = 1764897908658L;
	
	Logger m_logger = Logger.getLogger(BusinessCalendar.class);
	
	public void setClosingHour(Integer hour)
		{ m_closingHour=hour; }
	private Integer m_closingHour;
	public void setClosingMinute(Integer minute)
		{ m_closingMinute=minute; }
	private Integer m_closingMinute;
	
	public void setOpeningHour(Integer hour)
		{ m_openingHour=hour; }
	private Integer m_openingHour;
	public void setOpeningMinute(Integer minute)
		{ m_openingMinute=minute; }
	private Integer m_openingMinute;
	
	/**
	 * @param weekDays a set of integers representing days of the week
	 */
	public void setBusinessWeek( Set<Integer> weekDays )
		{ m_businessWeekDays = weekDays; }
	private Set<Integer> m_businessWeekDays = null;
	
	/**
	 * @param holidaySet a set of values from the federal holidays enum
	 */
	public void setFederalHolidays(Set<FederalHolidayCalendar.Holidays> federalHolidaySet)
		{ m_federalHolidaySet = federalHolidaySet; }
	private Set<FederalHolidayCalendar.Holidays> m_federalHolidaySet = null;
	
	/**
	 * @return true if the current time is not a holiday and is one of the days of the business week
	 */
	public Boolean isBusinessDay()
	{
		Integer dayOfWeek = this.get(Calendar.DAY_OF_WEEK);
		FederalHolidayCalendar fhc = new FederalHolidayCalendar();
		fhc.setTime(this.getTime());
		
		// if the day is in the business week
		// and isn't a holiday...then it's a business day! =)
		if ( m_businessWeekDays.contains(dayOfWeek)	&& ! m_federalHolidaySet.contains(fhc.getHoliday()) )
			return true;
		return false;
	}
	
	/**
	 * @return true if a business day within business hours, else false if not on a business day within business hours
	 */
	public Boolean isBusinessTime()
	{
		m_logger.trace("current date hour and minute: "+get(Calendar.HOUR_OF_DAY)+':'+get(Calendar.MINUTE));
		if
		( 
			isBusinessDay() 
			&& ( get(Calendar.HOUR_OF_DAY)*60+get(Calendar.MINUTE)>m_openingHour*60+m_openingMinute )
			&& ( get(Calendar.HOUR_OF_DAY)*60+get(Calendar.MINUTE)<=m_closingHour*60+m_closingMinute )
		)
			 return true;
		else return false;
	}
	
	/**
	 * Normalize the date the edge of the TimeInterval passed in
	 * - anything less than a TimeInterval.DAY is normalized to the closest intervals after the current time
	 * - if the interval is TimeInterval.DAY, then the time is just set to closing()
	 * - everything with a duration greater than DAY is still undecided and so just returns
	 * @param interval
	 */
	public void normalizeToInterval(TimeInterval interval) {
		
		if( interval.duration() > TimeInterval.DAY.duration() )
			// TODO: handle this somehow.
			;
		else if( interval.duration() == TimeInterval.DAY.duration() )
			// doesn't matter where we are in the interval, 
			// closing time is closing time
			closing();
		else if( interval.duration() < TimeInterval.DAY.duration() )
		{
			// other wise, subtract the opening time from now
			// and determine the closest interval we're to
			BusinessCalendar openingCal = (BusinessCalendar)this.clone();
			openingCal.opening();
			
			long openTime = this.getTime().getTime() - openingCal.getTime().getTime();
			int periods = (int)( openTime / ((long)interval.duration()*1000) );
			int remainder = (int)( openTime % ((long)interval.duration()*1000) );
			
			// if we are not neatly on an interval, then advance to the next interval edge
			// otherwise, just leave as it is
			Date newDate = this.getTime();
			newDate.setTime( openingCal.getTime().getTime() + ( ( periods + ((remainder>0)?1:0) )*(interval.duration()*1000) ) );
			this.setTime( newDate );
		}
	}
	
	/**
	 * zeros out the Calendar.SECOND and the Calendar.MILLISECOND
	 */
	public void normalizeToMinute()
	{
		set( Calendar.SECOND, 0 );
		set( Calendar.MILLISECOND, 0 );
	}
	
	/**
	 * adjusts the time to the closing of business on the current day
	 */
	public void closing()
	{
		normalizeToMinute();
		set( Calendar.HOUR_OF_DAY, m_closingHour );
		set( Calendar.MINUTE, m_closingMinute );
	}
	
	/**
	 * adjusts the time to the open of business on the current day
	 */
	public void opening()
	{
		normalizeToMinute();
		set( Calendar.HOUR_OF_DAY, m_openingHour );
		set( Calendar.MINUTE, m_openingMinute );
	}
		
	/**
	 * rewind to the previous business time of the interval
	 * uses {@link normalizeToInterval()}
	 */
	public void closestBefore(TimeInterval interval) {
		
		// TODO: optimize this for smaller amounts of time, it really shouldn't loop all the minutes to the next business day
		// TODO: fix this so that large time intervals fall on the first valid business day
		Date origDate = this.getTime();
		interval.subtract(this);
		normalizeToInterval(interval);
		while( isBusinessTime()!=true ) {
			interval.subtract(this);
			normalizeToInterval(interval);
		}	
		m_logger.trace("in closestBefore, the business "+interval.toString()+" interval prior to "+origDate+" was "+this.getTime().toString() );
	}
	
	/**
	 * fast-forward to the next business time of the interval
	 * uses {@link normalizeToInterval}
	 */
	public void closestAfter(TimeInterval interval) {
		
		// TODO: optimize this for smaller amounts of time, it really shouldn't loop all the minutes to the next business day
		// TODO: fix this so that large time intervals fall on the first valid business day
		Date origDate = this.getTime();
		interval.add(this);
		normalizeToInterval(interval);
		int i=0;
		while( isBusinessTime()!=true ) {
			interval.add(this);
			normalizeToInterval(interval);
			i++;
			if( i>5 )
				m_logger.error("there are no spans longer than a few days with no business.");
		}
		m_logger.trace("in closestAfter, the business "+interval.toString()+" interval after "+origDate+" is "+this.getTime().toString() );
	}
	
	/**
	 * calls closestAfter a number of times
	 * @param interval
	 * @param number
	 */
	public void add(TimeInterval interval, int number) {
		int absNum = Math.abs(number);
		for( int i=0; i<absNum; i++ )
			if( number>0 )
				this.closestAfter(interval);
			else this.closestBefore(interval);
	}
	
	/**
	 * calls closestBefore a number of times
	 * @param interval
	 * @param number
	 */
	public void subtract(TimeInterval interval, int number) {
		for( int i=0; i<number; i++ )
			this.closestBefore(interval);
	}
}
