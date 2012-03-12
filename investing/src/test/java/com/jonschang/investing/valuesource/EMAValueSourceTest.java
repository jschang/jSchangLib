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

import org.apache.log4j.Logger;
import org.junit.*;
import java.util.*;

import com.jonschang.investing.Investing;
import com.jonschang.investing.stocks.model.*;

public class EMAValueSourceTest {
	
	@Test public void testEMAValueSource() throws Exception {
		try{
			Investing.instance();
			List<StockQuote> quotes = createQuotes();
			EMAValueSource<StockQuote,Stock> ema = new EMAValueSource<StockQuote,Stock>();
			SingleQuoteValueSource ssqvs = new SingleQuoteValueSource();
			ssqvs.setReturnType(StockQuote.HIGH);
			ema.setValueSource(ssqvs);
			ema.setPeriods(15);
			ema.setQuotes(quotes);
			Logger.getLogger(this.getClass()).info("ema(15) returns "+ema.getValue());
			Assert.assertTrue(ema.getValue()==92);
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public List<StockQuote> createQuotes() {
		List<StockQuote> quotes = new ArrayList<StockQuote>();
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
