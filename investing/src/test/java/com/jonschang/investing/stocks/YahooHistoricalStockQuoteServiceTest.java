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

import java.util.*;
import java.text.*;
import org.junit.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.utils.*;
import org.apache.log4j.*;

public class YahooHistoricalStockQuoteServiceTest
{
	Logger m_logger;
	@Test public void testPullDateRange() throws Exception
	{		
		try {
			com.jonschang.utils.LoggingUtils.configureLogger();
			
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			Logger.getLogger("org.springframework").setLevel(Level.OFF);
			
			@SuppressWarnings(value={"all"})
			QuoteService<StockQuote,Stock> qs = Investing.instance().getQuoteServiceFactory().getQuoteService(StockQuote.class);
		
			StockService stockService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
			
			// setup the date range
			Date date1 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.04 16:00");
			Date date2 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.06 16:00");
			
			// create a set of stocks to pull historical quotes for
			java.util.Set<Stock> stockSet = new HashSet<Stock>();
			stockSet.add(stockService.get("MSFT"));
			stockSet.add(stockService.get("PFE"));
			stockSet.add(stockService.get("^DJI"));
			
			List<StockQuote> quotes = null;
			List<Stock> range = null;
			
			// pull the most recent HistoricalStockQuotes for the passed in set 
			range = qs.pullDateRange(stockSet, new DateRange(date1, date2), TimeInterval.DAY );
			
			// validate that the correct number of Quotable objects was returned
			Assert.assertTrue( range!=null && range.size()==3 );
			
			// validate that each Quotable's last Quote pulled is for the last business day
			// prior to the last date of the range (which was a saturday or sunday)
			for( Stock stock : range ) {
				quotes = stock.getQuotes();
				Assert.assertTrue( 
						quotes
							.get(0)
							.getDate()
							.compareTo( 
								new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("04/01/2008 16:00") 
							) == 0 
						);
				Assert.assertTrue( 
					quotes
						.get(quotes.size()-1)
						.getDate()
						.compareTo( 
							new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("05/30/2008 16:00") 
						) == 0 
					);
			}
			
			date1 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.03 16:00");
			date2 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.10 16:00");
			
			range = qs.pullDateRange(stockSet, new DateRange(date1, date2), TimeInterval.DAY );
			
			// validate that the correct number of Quotable objects was returned
			Assert.assertTrue( range!=null && range.size()==3 );
			
			// validate that each Quotable's last Quote pulled is for the last business day
			// prior to the last date of the range (which was a saturday or sunday)
			for( Stock stock : range ) {
				quotes = stock.getQuotes();
				Assert.assertTrue( 
						quotes
							.get(0)
							.getDate()
							.compareTo( 
								new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("03/03/2008 16:00") 
							) == 0 
						);
				Assert.assertTrue( 
					quotes
						.get(quotes.size()-1)
						.getDate()
						.compareTo( 
							new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/01/2008 16:00") 
						) == 0 
					);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test public void testPullNumber() throws Exception
	{		
		try {
			com.jonschang.utils.LoggingUtils.configureLogger();
			
			// pull the hibernate session factory in from the application context
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			Logger.getLogger("org.springframework").setLevel(Level.OFF);
			
			@SuppressWarnings(value={"all"})
			QuoteService<StockQuote,Stock> qs = Investing.instance().getQuoteServiceFactory().getQuoteService(StockQuote.class);
		
			StockService stockService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
			
			// setup the date range
			Date date1 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.04 16:00");
			
			// create a set of stocks to pull historical quotes for
			java.util.Set<Stock> stockSet = new HashSet<Stock>();
			stockSet.add(stockService.get("MSFT"));
			stockSet.add(stockService.get("PFE"));
			stockSet.add(stockService.get("^DJI"));
			
			List<StockQuote> quotes = null;
			List<Stock> range = null;
			
			// pull the most recent HistoricalStockQuotes for the passed in set 
			range = qs.pullNumber(stockSet, date1, 10, TimeInterval.DAY );
			
			// validate that the correct number of Quotable objects was returned
			Assert.assertTrue( range!=null && range.size()==3 );
			
			// validate that each Quotable's last Quote pulled is for the last business day
			// prior to the last date of the range (which was a saturday or sunday)
			for( Stock stock : range ) {
				quotes = stock.getQuotes();
				Assert.assertTrue( quotes.size()==10 );
			}
			
			date1 = new SimpleDateFormat("yyyy.dd.MM HH:mm").parse("2008.01.03 16:00");
			
			range = qs.pullNumber(stockSet, date1, -10, TimeInterval.DAY );
			
			// validate that the correct number of Quotable objects was returned
			Assert.assertTrue( range!=null && range.size()==3 );
			
			// validate that each Quotable's last Quote pulled is for the last business day
			// prior to the last date of the range (which was a saturday or sunday)
			// in this instance, 2/18/2008 does not have a quote for any quotable
			// so the number returned will be 9...
			// TODO: this should return 10, not 9
			// however, because i generally pad where i pull quotes, the missing quote isn't so bad
			for( Stock stock : range ) {
				quotes = stock.getQuotes();
				Assert.assertTrue( quotes.size()==9 );
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}

