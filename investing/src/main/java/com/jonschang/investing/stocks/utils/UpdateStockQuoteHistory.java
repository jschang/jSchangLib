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
package com.jonschang.investing.stocks.utils;

import org.apache.log4j.*;

import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.service.StockQuoteService;
import com.jonschang.investing.stocks.service.StockService;
import com.jonschang.investing.stocks.*;
import com.jonschang.utils.*;

import java.text.*;
import java.util.*;

import org.apache.log4j.Logger;

public class UpdateStockQuoteHistory {
	public static void main(String[] args) throws Exception {
		StockService stockService = 
			(StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
		StockQuoteService stockQuoteService =
			(StockQuoteService)Investing.instance().getQuoteServiceFactory().getQuoteService(StockQuote.class);
		Logger.getRootLogger().setLevel(Level.INFO);
		if( args.length>0 ) {
			String[] quotables = args[0].split(",");
			List<Stock> stocks = new ArrayList<Stock>();
			for( String quotableSymbol : quotables ) {
				stocks.add(stockService.get(quotableSymbol));
			}
			stockQuoteService.pullDateRange(
				stocks, 
				new DateRange(
					new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("01/01/1986 16:00"),
					new Date()
				), TimeInterval.DAY );
		} else {
			Logger.getLogger(UpdateStockQuoteHistory.class).error("You must specify a ',' delimited list of symbols.");
		}
	}
}
