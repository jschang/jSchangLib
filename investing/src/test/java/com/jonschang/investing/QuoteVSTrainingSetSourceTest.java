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
package com.jonschang.investing;

import java.text.*;
import java.util.*;
import org.junit.*;
import org.apache.log4j.*;

import com.jonschang.ai.network.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.investing.*;
import com.jonschang.utils.*;
import com.jonschang.investing.model.*;

public class QuoteVSTrainingSetSourceTest {
	@Test public void testQuoteVSTrainingSetSourceTest() throws Exception {
		
		try {			
			// should start the logger
			Investing.instance();
			
			Logger.getRootLogger().setLevel(Level.OFF);
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			Logger.getLogger("org.springframework").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.model").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.services").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.stocks.model").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.investing.stocks.services").setLevel(Level.OFF);
			Logger.getLogger("com.jonschang.utils").setLevel(Level.OFF);
			
			/////////////////////////////////
			
			@SuppressWarnings(value="unchecked")
			StockService quotableService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
			GenericQuotePublisher<StockQuote,Stock> pub = new GenericQuotePublisher<StockQuote,Stock>();
			pub.setQuotable(quotableService.get("MSFT"));
			pub.setQuoteClass(StockQuote.class);
			pub.setTimeInterval(TimeInterval.DAY);
			
			DatePublisher dp = new GenericDatePublisher();
			dp.subscribe(pub);
			QuoteVSTrainingSetSource<StockQuote,Stock> trainingSet = new QuoteVSTrainingSetSource<StockQuote,Stock>();
			trainingSet.setDatePublisher(dp);
			trainingSet.setDateRange(new DateRange(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("01/01/2007 16:00"), new Date()));
			Logger.getLogger(this.getClass()).info("date range is "+trainingSet.getDateRange().toString());
			trainingSet.setQuoteInterval(TimeInterval.DAY);
			
			SingleQuoteValueSource<StockQuote,Stock> ssqvs = new SingleQuoteValueSource<StockQuote,Stock>();
			ssqvs.setReturnType(Quote.HIGH);
			QuoteValueSourceList<StockQuote,Stock> qvsl = new QuoteValueSourceList<StockQuote,Stock>();
			qvsl.setQuotePublisher(pub);
			qvsl.add(ssqvs);
			dp.subscribe(qvsl);
			trainingSet.setOutputs(qvsl);
			
			qvsl = new QuoteValueSourceList<StockQuote,Stock>();
			qvsl.setQuotePublisher(pub);
			
			OffsetPeriodsValueSource<StockQuote,Stock> ofpvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
			ofpvs.setPeriods(10);
			ofpvs.setValueSource(ssqvs);
			qvsl.add( ofpvs );
			
			dp.subscribe(qvsl);
			trainingSet.setInputs(qvsl);
			
			/////////////////////////////////
			java.util.Iterator<TrainingSetSource.Pair> iter = trainingSet.iterator();
			TrainingSetSource.Pair pair = null;
			Assert.assertTrue(iter.hasNext());
			pair = iter.next();
			Assert.assertTrue(
					pair.getInput().getData()!=null && pair.getInput().getData().size()==1 
					&& pair.getInput().getData().get(0) == 30.229999542236328
					&& pair.getOutput().getData()!=null && pair.getOutput().getData().size()==1 
					&& pair.getOutput().getData().get(0) == 30.25
				);
			Assert.assertTrue(iter.hasNext());
			pair = iter.next();
			Assert.assertTrue(
					pair.getInput().getData()!=null && pair.getInput().getData().size()==1 
					&& pair.getInput().getData().get(0) == 30.260000228881836
					&& pair.getOutput().getData()!=null && pair.getOutput().getData().size()==1 
					&& pair.getOutput().getData().get(0) == 29.969999313354492
				);
			Assert.assertTrue(iter.hasNext());
			pair = iter.next();
			Assert.assertTrue(
					pair.getInput().getData()!=null && pair.getInput().getData().size()==1 
					&& pair.getInput().getData().get(0) == 30.170000076293945
					&& pair.getOutput().getData()!=null && pair.getOutput().getData().size()==1 
					&& pair.getOutput().getData().get(0) == 29.75
				);
			iter=null;
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
}
