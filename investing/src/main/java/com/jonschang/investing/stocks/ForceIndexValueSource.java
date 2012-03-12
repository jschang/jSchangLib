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
package com.jonschang.investing.stocks;

import com.jonschang.investing.model.Quote;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.valuesource.AbstractQuoteValueSource;
import com.jonschang.investing.valuesource.TooFewQuotesException;
import com.jonschang.utils.valuesource.*;

public class ForceIndexValueSource extends AbstractQuoteValueSource<StockQuote,Stock> {

	public int getPeriods() {
		return 2;
	}

	public double getValue() throws ValueSourceException {
		if( quotes.size() < getPeriods() )
			throw new TooFewQuotesException("Need at least "+getPeriods()+" quotes for the ForceIndexValueSource");
		int size = this.quotes.size();
		StockQuote last = this.quotes.get(size-1);
		return (last.getField(Quote.CLOSE)-this.quotes.get(size-2).getField(Quote.CLOSE))*last.getField(StockQuote.VOLUME);
	}

}
