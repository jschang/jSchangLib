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

import java.util.Date;

public class DateRange 
{
	Date m_start;
	Date m_end;
	
	/**
	 * create a date range by specifying the begin and end dates
	 * @param start
	 * @param end
	 */
	public DateRange(Date start, Date end)
	{ 
		m_start=start; 
		m_end=end; 
	}
	
	/**
	 * create a date range by specifying the total interval of time (in seconds) from the start
	 * @param start
	 * @param intervalSeconds
	 * @param periods
	 */
	public DateRange(Date start, TimeInterval interval, int periods) {
		m_start = start;
		m_end = new Date();
		m_end.setTime(m_start.getTime());
		interval.add(m_end,periods);
	}
	
	/**
	 * create a date range by specifying the total interval of time (in seconds) up to the end
	 * @param intervalSeconds
	 * @param periods
	 * @param end
	 */
	public DateRange(TimeInterval interval, int periods, Date end) {
		m_end = end;
		m_start = new Date();
		m_start.setTime(m_end.getTime());
		interval.add(m_start,periods);
	}
	
	public Date getStart()
	{ return m_start; }
	
	public Date getEnd()
	{ return m_end; }
	
	@Override
	public String toString() {
		return this.m_start+" to "+this.m_end;
	} 
}
