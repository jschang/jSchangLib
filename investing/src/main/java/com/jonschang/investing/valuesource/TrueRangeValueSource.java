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
package com.jonschang.investing.valuesource;

import com.jonschang.investing.model.*;

/**
 * determine the Average True Range of the stock period
 * @author schang
 */
@SuppressWarnings(value={"unchecked"})
public class TrueRangeValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {
	
	/**
	 * the average true range of a stock requires only the last two quotes
	 */
	public int getPeriods() {
		return 2;
	}
	
	/**
	 * get the average true range of the current quote
	 */
	public double getValue() throws TooFewQuotesException
	{
		if( this.quotes.size()<2 )
			throw new TooFewQuotesException("there were too few quotes: "+this.quotes.size()+" available, 2 quotes needed.");
		
		Quote today = this.quotes.get( this.quotes.size()-1 );
		Quote yesterday = this.quotes.get( this.quotes.size()-2 );
		
		double diffCurHighCurLow    = today.getPriceHigh() - today.getPriceLow();
		double diffCurHighPrevClose = today.getPriceHigh() - yesterday.getPriceClose();
		double diffCurLowPrevClose  = today.getPriceLow()  - yesterday.getPriceClose();
		
		double atr = 0;
		
		atr = diffCurHighCurLow    > atr ? diffCurHighCurLow    : atr;
		atr = diffCurHighPrevClose > atr ? diffCurHighPrevClose : atr;
		atr = diffCurLowPrevClose  > atr ? diffCurLowPrevClose  : atr;
		
		return atr;
	}
}
