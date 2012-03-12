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
import java.util.GregorianCalendar;
import java.util.Date;

public enum TimeInterval {
	YEAR    (31536000,Calendar.YEAR, 1),
	MONTH_3 (7776000,Calendar.MONTH, 3),
	DAY     (86400,Calendar.DAY_OF_YEAR, 1),
	HOUR    (3600,Calendar.HOUR_OF_DAY, 1),
	MIN_5   (300,Calendar.MINUTE, 5),
	MIN     (60,Calendar.MINUTE, 1);
	private final int calendarEnum;
	private final int number;
	private final int duration;
	TimeInterval(int duration, int calendarEnum, int number) {
		this.calendarEnum=calendarEnum;
		this.number=number;
		this.duration=duration;
	}		
	static public TimeInterval get(int duration) {
		switch(duration) {
		case 31536000: return YEAR;
		case 7776000 : return MONTH_3;
		default      : 
		case 86400   : return DAY;
		case 300     : return MIN_5;
		case 60      : return MIN;
		case 3600    : return HOUR;
		}
	}
	public int duration() {
		return this.duration;
	}
	public void add(Calendar cal) {
		cal.add(this.calendarEnum,this.number);
	}
	public void subtract(Calendar cal) {
		cal.add(this.calendarEnum,-this.number);
	}
	public void add(Calendar cal,int times) {
		cal.add(this.calendarEnum,this.number*times);
	}
	public void subtract(Calendar cal, int times) {
		cal.add(this.calendarEnum,-this.number*times);
	}
	public void add(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		this.add(c);
		date.setTime(c.getTime().getTime());
	}
	public void subtract(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		this.subtract(c);
		date.setTime(c.getTime().getTime());
	}
	public void add(Date date, int times) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		this.add(c,times);
		date.setTime(c.getTime().getTime());
	}
	public void subtract(Date date, int times) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		this.subtract(c,times);
		date.setTime(c.getTime().getTime());
	}
}
