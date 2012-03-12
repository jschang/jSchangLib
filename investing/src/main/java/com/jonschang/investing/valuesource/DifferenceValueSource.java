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
 * The difference between to QuoteValueSources where result=(left-right)
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
@SuppressWarnings(value={"unchecked"})
public class DifferenceValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {
	
	private QuoteValueSource<Q,I> leftValueSource;
	private QuoteValueSource<Q,I> rightValueSource;
	
	public DifferenceValueSource<Q,I> setLeft(QuoteValueSource<Q,I> valueSource) {
		leftValueSource=valueSource;
		return this;
	}
	
	public DifferenceValueSource<Q,I> setRight(QuoteValueSource<Q,I> valueSource) {
		rightValueSource=valueSource;
		return this;
	}	
	
	public int getPeriods() {
		return Math.max(leftValueSource.getPeriods(), rightValueSource.getPeriods());
	}

	public double getValue() throws ValueSourceException {
		double val;
		List<Q> lQs = leftValueSource.getQuotes();
		List<Q> rQs = rightValueSource.getQuotes();
		try {
			rightValueSource.setQuotes(quotes);
			leftValueSource.setQuotes(quotes);
			val = leftValueSource.getValue()-rightValueSource.getValue();
		} finally {
			leftValueSource.setQuotes(lQs);
			rightValueSource.setQuotes(rQs);
		}
		return val;
	}

}
