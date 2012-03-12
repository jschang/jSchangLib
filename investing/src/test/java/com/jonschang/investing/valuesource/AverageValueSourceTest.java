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

import org.junit.Test;
import org.junit.Assert;
import java.util.List;
import java.util.ArrayList;

import com.jonschang.investing.Investing;
import com.jonschang.investing.model.Quote;
import com.jonschang.investing.stocks.model.StockQuote;
import com.jonschang.investing.valuesource.AverageValueSource;

public class AverageValueSourceTest {
	
	@Test public void testAverageValueSource() throws Exception {
		List<Quote> quotes = createQuotes();
		AverageValueSource ema = new AverageValueSource();
		SingleQuoteValueSource ssqvs = new SingleQuoteValueSource();
		ssqvs.setReturnType(StockQuote.HIGH);
		ema.setValueSource(ssqvs);
		ema.setPeriods(15);
		ema.setQuotes(quotes);
		Assert.assertTrue(ema.getValue()==92.0);
	}
	
	public List<Quote> createQuotes() {
		List<Quote> quotes = new ArrayList<Quote>();
		StockQuote newQ;
		for( int i=0; i<100; i++ ) {
			newQ = new StockQuote();
			newQ.setPriceClose((float)i);
			newQ.setPriceOpen((float)i);
			newQ.setPriceHigh((float)i);
			newQ.setPriceLow((float)i);
			newQ.setVolume(new Long(i));
			quotes.add( newQ );
		}
		return quotes;
	}

}
