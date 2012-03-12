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

import com.jonschang.utils.valuesource.*;
import com.jonschang.investing.model.*;
import java.util.*;

/**
 * Determine the standard deviation of a value source over a collection of quotes
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class StandardDeviationValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {
	
	protected int periods;
	protected QuoteValueSource<Q,I> valueSource;
	protected AverageValueSource<Q,I> avgVS = new AverageValueSource<Q,I>();
	
	public StandardDeviationValueSource<Q,I> setValueSource(QuoteValueSource<Q,I> vs) {
		this.valueSource = vs;
		return this;
	}
	
	@Override
	public void setPeriods(int periods) {
		this.periods=periods;
		this.avgVS.setPeriods(periods);
	}
	
	public int getPeriods() {
		this.avgVS.setValueSource(this.valueSource);
		return this.avgVS.getPeriods();
	}
	
	public double getValue() throws ValueSourceException {
		List<Q> vQs = valueSource.getQuotes();
		try {
			if( this.quotes==null )
				throw new TooFewQuotesException("the quotes list available was null");
			if( this.quotes.size()<this.periods )
				throw new TooFewQuotesException("the size of quotes list available was "+this.quotes.size()+", whereas the number of quotes required is "+this.periods);
			
			this.avgVS.setQuotes(this.quotes);
			double avg = this.avgVS.getValue();
			double totalSq=0;
			int num=0;
			for( int i=this.quotes.size()-1; i>=0; i-- ) {
				this.valueSource.setQuotes( this.quotes.subList( i-this.valueSource.getPeriods(), i ) );
				totalSq+=Math.pow(this.valueSource.getValue()-avg,2);
				num++;
			}		
			return Math.sqrt( totalSq/num );
		} finally {
			valueSource.setQuotes(vQs);
		}
	}
}
