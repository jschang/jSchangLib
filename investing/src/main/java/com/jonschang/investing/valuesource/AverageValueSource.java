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
 * The average of a QuoteValueSource over a number periods
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
@SuppressWarnings(value={"unchecked"})
public class AverageValueSource<Q extends Quote<I>, I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {

	private int periods;
	private QuoteValueSource valueSource;
	
	public void setValueSource(QuoteValueSource valueSource) {
		this.valueSource = valueSource;
	}

	@Override
	public void setPeriods(int periods) {
		this.periods=periods;
	}
	
	public int getPeriods() {
		return this.periods+this.valueSource.getPeriods();
	}
	
	public double getValue() throws ValueSourceException {	
		double accum=0;
		List<Q> qs = valueSource.getQuotes();
		try {
			if( this.quotes.size()<this.getPeriods() ) {
				throw new TooFewQuotesException("not enough quotes to continue: "+this.getPeriods()+" needed, "+this.quotes.size()+" found");
			}
			if( this.valueSource==null ) {
				throw new ValueSourceNotSetException("this value source uses another to pull values to average.  you must call setValueSource()");
			}
			int start = this.quotes.size()-this.periods;
			int end = this.quotes.size();
			for( int i=start; i<end; i++ ) {
				this.valueSource.setQuotes(this.quotes.subList(i-this.valueSource.getPeriods(), i+1));
				accum += this.valueSource.getValue();
			}
			accum = accum/(end-start);
			return accum;
		} finally {
			this.valueSource.setQuotes(qs);
		}
	}

}
