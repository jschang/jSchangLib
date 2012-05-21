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

import java.text.*;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.apache.log4j.*;
import com.jonschang.investing.*;
import com.jonschang.investing.model.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.utils.*;

public class AggregateQuoteValueSourceListTest {
	@Test public void testAggregateQuoteValueSourceListTest() throws Exception {
		
		try {			
			StockService stockService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
			
			//Logger.getRootLogger().setLevel(Level.OFF);
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			Logger.getLogger("org.springframework").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.model").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.services").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.stocks.model").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.stocks.services").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.utils").setLevel(Level.OFF);
			
			/////////////////////////////////
			
			AggregateQuoteValueSourceList aqvsl = new AggregateQuoteValueSourceList();
			
			DatePublisher datePub = new GenericDatePublisher();
			datePub.setDate(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("1/15/2008 16:00"));
			
			GenericQuotePublisher<StockQuote,Stock> pubStock = new GenericQuotePublisher<StockQuote,Stock>();  
			pubStock.setTimeInterval(TimeInterval.DAY);
			pubStock.setQuoteClass(StockQuote.class);
			pubStock.setQuotable( ((StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class)).get("MSFT") );
			datePub.subscribe(pubStock);
			
			GenericQuotePublisher<StockQuote,Stock> pubIndex = new GenericQuotePublisher<StockQuote,Stock>();  
			pubIndex.setTimeInterval(TimeInterval.DAY);
			pubIndex.setQuoteClass(StockQuote.class);
			pubIndex.setQuotable( ((StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class)).get("^DJI") );
			datePub.subscribe(pubIndex);
			
			QuoteValueSourceList<StockQuote,Stock> qvsl = new QuoteValueSourceList();
			SingleQuoteValueSource<StockQuote,Stock> qvs = new SingleQuoteValueSource();
			qvs.setReturnType(Quote.CLOSE);
			pubStock.subscribe(qvs);
			datePub.subscribe(qvsl);
			qvsl.add(qvs);
			aqvsl.add(qvsl);
			
			qvsl = new QuoteValueSourceList();
			qvs = new SingleQuoteValueSource();
			qvs.setReturnType(Quote.CLOSE);
			pubIndex.subscribe(qvs);
			datePub.subscribe(qvsl);
			qvsl.add(qvs);
			aqvsl.add(qvsl);
			
			datePub.setDate( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("1/15/2008 16:00") );
			datePub.updateHasDates();
			List<Double> data = aqvsl.getVector().getData();
			Assert.assertArrayEquals(new Integer[]{34,12501} , new Integer[]{((Double)data.get(0)).intValue(),((Double)data.get(1)).intValue()});
			
			datePub.setDate( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("1/19/2008 16:00") );
			datePub.updateHasDates();
			Assert.assertNull( aqvsl.getVector() );
			
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
}
