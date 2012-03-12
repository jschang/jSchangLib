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
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;

/**
 * A state-full StockQuote List adjuster
 * that modifies the open, close, high, and low prices
 * of stocks based on the StockEventSplit's that it's
 * seen.
 * 
 * It keeps track of splits that it's seen and applies them
 * as well as any splits that occur during iteration over the list.
 * 
 * This way, as would be the case using the GenericQuotePublisher,
 * the Quote field values are not adjusted based only on the present
 * list.
 * @author schang
 */
public class StockEventSplitAdjuster implements QuoteAdjuster<StockQuote,Stock> {

	private Map<Stock,Map<Date,Float>> splitMults = new HashMap<Stock,Map<Date,Float>>();
	
	public void adjust(Stock quotable, List<StockQuote> quotes) {
		
		Float splitMultiplier = 1.0f;
		
		Map<Date,Float> theseMults = splitMults.get(quotable);
		if( theseMults == null ) {
			theseMults = new HashMap<Date,Float>();
			splitMults.put(quotable,theseMults);
		}
		
		Date startDate = quotes.get(0).getDate();
		for( Map.Entry<Date,Float> ent : theseMults.entrySet() ) {
			if( ent.getKey().before(startDate) )
				splitMultiplier*=ent.getValue();
		}
		
		StockEventSplit splitEvent = null;
		Float thisSplitMult = 1.0f;
		for( StockQuote q : quotes ) {	
			if( q.getStockEvents()!=null )
				for( StockEvent se : q.getStockEvents() ) {
					if( se instanceof StockEventSplit ) {
						splitEvent = (StockEventSplit)se;
						thisSplitMult = (splitEvent.getNumerator()/splitEvent.getDenominator());
						if( ! theseMults.containsKey(q.getDate()) )
							theseMults.put( (Date)q.getDate().clone(), thisSplitMult );
						splitMultiplier*=thisSplitMult;
					}
				}
			q.setPriceOpen( q.getPriceOpen()*splitMultiplier );
			q.setPriceClose( q.getPriceClose()*splitMultiplier );
			q.setPriceHigh( q.getPriceHigh()*splitMultiplier );
			q.setPriceLow( q.getPriceLow()*splitMultiplier );
		}
	}
}
