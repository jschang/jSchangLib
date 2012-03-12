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
import com.jonschang.investing.*;
import com.jonschang.utils.valuesource.*;

/**
 * The Directional Movement of a set of Quotes
 * 
 * if the direction is negative:
 *   v = yesterdays low - todays low
 *   return v > 0 ? v : 0
 * 
 * if the direction is positive:
 *   v = todays high - yesterdays high
 *   return v > 0 ? v : 0
 * 
 * Formula found at {@link http://en.wikipedia.org/wiki/Average_Directional_Index}
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class DirectionalMovementValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {

	public enum Direction {
		Positive,
		Negative
	}
	
	private Direction direction = Direction.Positive;
	
	public int getPeriods() {
		return 2;
	}
	
	public DirectionalMovementValueSource<Q,I> setDirection(Direction d){
		direction=d;
		return this;
	}

	public double getValue() throws ValueSourceException {
		if( this.quotes==null || this.quotes.size()<2 )
			throw new TooFewQuotesException("the DirectionalMovementValueSource requires at least 2 quotes.");
		Quote<I> today = this.quotes.get(this.quotes.size()-1);
		Quote<I> yesterday = this.quotes.get(this.quotes.size()-2);
		double value;
		if( direction==Direction.Positive )
			value = today.getPriceHigh() - yesterday.getPriceHigh();
		else value = yesterday.getPriceLow() - today.getPriceLow();
		return value > 0 ? value : 0;
	}
}
