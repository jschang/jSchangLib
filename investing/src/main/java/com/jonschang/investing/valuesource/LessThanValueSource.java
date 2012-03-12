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
import com.jonschang.utils.valuesource.*;
import java.util.*;

/**
 * determines if all values of a range are less than a threshold value source
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class LessThanValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {
	
	private QuoteValueSource<Q,I> threshold;
	private QuoteValueSource<Q,I> valueSource;
	private int periods = 1;
	
	public int getPeriods() {
		return Math.max( threshold.getPeriods(), valueSource.getPeriods() )+this.periods;
	}
	
	@Override
	public void setPeriods(int range) {
		this.periods=range;
	}
	
	public void setThreshold(QuoteValueSource<Q,I> vs) {
		this.threshold = vs;
	}
	public void setValueSource(QuoteValueSource<Q,I> vs) {
		this.valueSource = vs;
	}
	
	public double getValue() throws ValueSourceException {
		List<Q> vQs = valueSource.getQuotes();
		List<Q> tQs = threshold.getQuotes();
		try {
			if( this.quotes.size()<this.valueSource.getPeriods()
				|| this.quotes.size()<this.threshold.getPeriods() )
				throw new TooFewQuotesException("this value source needs more quotes");
			
			this.valueSource.setQuotes(this.quotes);
			this.threshold.setQuotes(this.quotes);
			
			int end = this.quotes.size();
			int start = end-this.periods;
			for( int i=start; i<end; i++ ) {
				valueSource.setQuotes(quotes.subList(i-valueSource.getPeriods(), i+1));
				//threshold.setQuotes(quotes.subList(i-valueSource.getQuotePeriodsRange(), i+1));
				if( this.valueSource.getValue()>this.threshold.getValue() )
					return 0.0f;
			}
			return 1.0f;
		} finally {
			valueSource.setQuotes(vQs);
			threshold.setQuotes(tQs);
		}
	}
}
