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
 * Evaluates the percentage difference a measure is from a basis
 * 
 * An example usage of this would be
 * ( Quote - OffsetPeriods(Quote) ) / ( OffsetPeriods(Quote) )
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class PercentageValueSource<Q extends Quote<I>,I extends Quotable>
	extends AbstractQuoteValueSource<Q,I> {

	protected QuoteValueSource<Q,I> basis;
	protected QuoteValueSource<Q,I> measure;
	
	public PercentageValueSource<Q,I> setBasis(QuoteValueSource<Q,I> valueSource) {
		basis = valueSource;
		return this;
	}
	
	public PercentageValueSource<Q,I> setMeasure(QuoteValueSource<Q,I> valueSource) {
		measure = valueSource;
		return this;
	}
	
	public int getPeriods() {
		return Math.max(basis.getPeriods(), measure.getPeriods());
	}

	public double getValue() throws ValueSourceException {
		List<Q> bQs = basis.getQuotes();
		List<Q> mQs = measure.getQuotes();
		try {
			if( basis==null || measure==null )
				throw new ValueSourceException("must pass a value source to both setMeasure() and setBasis()");
			if( quotes.size()<basis.getPeriods() || quotes.size()<measure.getPeriods() )
				throw new TooFewQuotesException("need more quotes");
			basis.setQuotes(quotes);
			measure.setQuotes(quotes);
			return (measure.getValue()-basis.getValue())/basis.getValue();
		} finally {
			basis.setQuotes(bQs);
			measure.setQuotes(mQs);
		}
	}

}
