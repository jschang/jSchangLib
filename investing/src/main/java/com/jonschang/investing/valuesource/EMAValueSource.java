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

import org.apache.log4j.Logger;

import com.jonschang.investing.model.*;
import com.jonschang.utils.valuesource.*;

/**
 * The Exponential Moving Average of number of Quotes
 * @author schang
 */
public class EMAValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {
	
	private int periods = 15;
	private QuoteValueSource<Q,I> valueSource;
	private AverageValueSource<Q,I> smaVs = new AverageValueSource<Q,I>();
	
	/**
	 * a value source class to use for the values of this value source
	 * will overwrite whatever quotes are set here in calculations
	 * @param valueSource
	 */
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
		this.smaVs.setValueSource(this.valueSource);
		this.smaVs.setPeriods(periods);
		int periods = this.periods+this.smaVs.getPeriods();
		Logger.getLogger(this.getClass()).info("needs "+periods);
		return periods;
	}
	
	public double getValue() throws ValueSourceException {
		List<Q> qs = valueSource.getQuotes();
		try { 
			if( this.quotes.size()<this.getPeriods() )
				throw new TooFewQuotesException("too few quotes available, unable to continue");
			if( this.valueSource==null )
				throw new ValueSourceNotSetException("this value source requires another.  use setValueSource()");
			
			int end = this.quotes.size();
			int start = end-this.periods;
			
			// get a simple moving average to start from
			this.smaVs.setQuotes(this.quotes.subList(start-this.smaVs.getPeriods(), start));
			double sma = smaVs.getValue();
			
			double exp = 2.0/(this.periods+1);
			double curVal = 0;
			
			for( int i=start; i<end; i++ ) {
				this.valueSource.setQuotes( this.quotes.subList( i - this.valueSource.getPeriods(), i + 1 ) );
				curVal = this.valueSource.getValue() * exp + ( i==start ? sma : curVal ) * ( 1 - exp );
			}
			
			return curVal;
		} finally {
			valueSource.setQuotes(qs);
		}
	}
}
