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
package com.jonschang.investing.model;

import java.util.Date;
import java.lang.Comparable;
import com.jonschang.utils.*;

/**
 * A set of methods that are applicable to pretty much all quotes
 * 
 * @author schang
 */
public interface Quote<Q extends Quotable> extends HasDate, Comparable<Quote<Q>>
{
	static public int HIGH=1;
	static public int LOW=2;
	static public int CLOSE=3;
	static public int OPEN=4;
	
	/**
	 * Uses the fieldNum passed in to switch between accessors
	 * and return the value from the accessor correlated with
	 * the integer value passed in.
	 * 
	 * There could have been an enum to facilitate this,
	 * but I ran into issues with inheritence.  I want subclasses
	 * to easily be able to add other fields returnable by this method
	 * to promote reuse in the QuoteValueSources.
	 * 
	 * So long as each field has a unique number in any hierarchy
	 * extending from here, the functionality should be preserved.
	 * 
	 * @param fieldNum
	 * @return
	 */
	double getField(int fieldNum);
		
	/**
	 * @return the quotable
	 */
	Q getQuotable();
	
	/**
	 * @return the opening price of this quote during the quote interval
	 */
	Float getPriceOpen();
	void setPriceOpen(Float price);
	
	/**
	 * @return the closing price of this quote during the quote interval
	 */
	Float getPriceClose();
	void setPriceClose(Float price);
	
	/**
	 * @return the high of this quote during the quote interval
	 */
	Float getPriceHigh();
	void setPriceHigh(Float price);
	
	/**
	 * @return the low price of this quote during the quote interval 
	 */
	Float getPriceLow();
	void setPriceLow(Float price);
	
	/**
	 * @return the interval, in seconds, of this quote
	 */
	Integer getInterval();		
}
