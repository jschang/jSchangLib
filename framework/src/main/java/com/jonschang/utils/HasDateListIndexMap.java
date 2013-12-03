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

import java.util.*;

/**
 * So that Quotes of a different interval can be included in the same 
 * @author schang
 *
 */
public class HasDateListIndexMap extends HashMap<Date,Integer> {
	
	private List<? extends HasDate> haveDates;
	
	public HasDateListIndexMap(List<? extends HasDate> haveDates) {
		this.haveDates = haveDates;
		reindex();
	}
	
	public void reindex() {
		this.clear();
		Integer i =0;
		for( HasDate hasDate : haveDates ) {
			this.put(hasDate.getDate(), i);
			i++;
		}
	}
	
	/**
	 * @param date the date to determine the index of
	 * @return the index of the quote closest before or on the date, null if the date is outside the bounds of the list
	 */
	public Integer getClosestIndexOfOrBefore(Date date) {
		if( this.get(date)!=null )
			return this.get(date);
		Integer i = 0;
		for( HasDate hasDate : haveDates ) {
			if( hasDate.getDate().compareTo(date)==0 )
				return i;
			if( hasDate.getDate().after(date) ) {
				if( i>0 )
					return i-1;
				else return null;
			}
			i++;
		}
		return null;
	}
}
