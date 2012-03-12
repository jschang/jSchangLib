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
import com.jonschang.investing.stocks.model.*;
import java.util.*;

/**
 * offsets the evaluation of a value source by some number of periods
 * 
 * for instance, if you want to evaluate an EMA 5 periods prior to current
 * then you would create an EMA and pass it in as a value source
 * to an instance of this class, then set the delay to 5
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
@SuppressWarnings(value={"unchecked"})
public class OffsetPeriodsValueSource<Q extends Quote<I>,I extends Quotable>
	extends AbstractQuoteValueSource<Q,I> {

	protected QuoteValueSource<Q,I> valueSource;
	protected int delay;
	
	public void setValueSource(QuoteValueSource qvs) {
		this.valueSource = qvs;
	}
	
	@Override
	public void setPeriods(int delay) {
		this.delay = delay;
	}
	
	public int getPeriods() {
		return this.valueSource.getPeriods()+delay;
	}

	public double getValue() throws ValueSourceException {
		List<Q> qs = valueSource.getQuotes();
		try {
			this.valueSource.setQuotes(this.quotes.subList(0, this.quotes.size()-delay));
			return this.valueSource.getValue();
		} finally {
			valueSource.setQuotes(qs);
		}
	}

}
