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

import java.util.List;

import com.jonschang.investing.model.*;
import com.jonschang.utils.valuesource.*;

/**
 * The most extreme value of a set of Quotes in a given direction (High or Low)
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class ExtremeValueValueSource<Q extends Quote<I>,I extends Quotable>
	extends AbstractQuoteValueSource<Q,I> {

	public enum Direction {
		High,
		Low
	}
	
	private Direction direction;
	private QuoteValueSource<Q,I> valueSource;
	private int periods;
	
	public ExtremeValueValueSource<Q,I> setDirection(Direction d) {
		direction = d;
		return this;
	}
	
	public void setValueSource(QuoteValueSource<Q,I> valueSource) {
		this.valueSource = valueSource;
	}
	public QuoteValueSource<Q,I> getValueSource() {
		return valueSource;
	}
	
	@Override
	public void setPeriods(int periods) {
		this.periods = periods;
	}
	
	public int getPeriods() {
		return valueSource.getPeriods()+this.periods;
	}

	public double getValue() throws ValueSourceException {
		List<Q> qs = valueSource.getQuotes();
		try {
			if( valueSource==null )
				throw new ValueSourceNotSetException("requires a value source");
			if( quotes.size() < getPeriods() )
				throw new TooFewQuotesException("needs more quotes");
			int end = quotes.size();
			int start = quotes.size()-periods;
			double value;
			Double extremeValue=null;
			for( int i=start; i<end; i++ ) {
				valueSource.setQuotes(quotes.subList(i-valueSource.getPeriods(), i+1));
				value = valueSource.getValue();
				if( extremeValue==null )
					extremeValue=value;
				else if( direction == Direction.High )
					extremeValue = extremeValue>value?extremeValue:value;
				else extremeValue = extremeValue<value?extremeValue:value;
			}
			return extremeValue;
		} finally {
			valueSource.setQuotes(qs);
		}
	}

}
