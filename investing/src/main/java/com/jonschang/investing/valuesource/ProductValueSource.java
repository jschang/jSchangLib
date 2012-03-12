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
 * The product of two QuoteValueSources values
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class ProductValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {

	QuoteValueSource<Q,I> left;
	QuoteValueSource<Q,I> right;
	
	public ProductValueSource<Q,I> setLeft(QuoteValueSource<Q,I> vs) {
		left = vs;
		return this;
	}
	public ProductValueSource<Q,I> setRight(QuoteValueSource<Q,I> vs) {
		right = vs;
		return this;
	}
	public int getPeriods() {
		return Math.max(left.getPeriods(), right.getPeriods());
	}

	@Override
	public double getValue() throws ValueSourceException {
		List<Q> lQs = left.getQuotes();
		List<Q> rQs = right.getQuotes();
		try {
			left.setQuotes(quotes);
			right.setQuotes(quotes);
			return left.getValue()*right.getValue();
		} finally {
			left.setQuotes(lQs);
			right.setQuotes(rQs);
		}
	}

}
