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

import org.junit.*;

import java.util.*;

import org.apache.log4j.*;

import com.jonschang.ai.network.feedforward.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.service.StockService;
import com.jonschang.investing.valuesource.*;
import com.jonschang.utils.*;
import com.jonschang.math.vector.*;

public class QVSNNTest {
	private Date trainingStart = null;
	private Date trainingEnd = null;
	private Date runEnd = null;
	
	@SuppressWarnings(value="unchecked")
	@Test public void testQVSNN() throws Exception {
		try {
			trainingStart = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/22/2007 16:00");
			trainingEnd = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("4/22/2009 16:00");
			runEnd = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/22/2009 16:00");
			DatePublisher datePublisher = new GenericDatePublisher();
			
			turnOffLogging();
			
			NetworkBuilder<FeedForward,StockQuote,Stock> builder = new NetworkBuilder_0_0_1a(datePublisher);
			builder.setTrainingStart(trainingStart);
			builder.setTrainingEnd(trainingEnd);
			builder.setRunEnd(runEnd);
			BackPropagation trainer = (BackPropagation)builder.getTrainer();
			trainer.setDesiredMSE(0.002);
			
			StockService stockService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
			for(QuotePublisher pub : builder.getPublishers()) {
				pub.setQuotable(stockService.get("MDR"));
			}
			trainer.train();
			
			Logger.getLogger(this.getClass()).info("currentMSE = "+trainer.getCurrentMSE()+", desiredMSE = "+trainer.getDesiredMSE());
			for(QuotePublisher pub : builder.getPublishers()) {
				pub.setQuotable(stockService.get("MDR"));
			}
			Date date = (Date)trainingEnd.clone();
			datePublisher.setDate(date);
			MathVector output;
			while( date.before(runEnd) ) {
				trainer.getNetwork().calculateResponse();
				output = trainer.getNetwork().getLastOutput();
				if( output!=null && ( output.getData().get(0)>.85 || output.getData().get(1)>.85 ) )
					Logger.getLogger(this.getClass()).info(date+" - "+output.getData());
				TimeInterval.DAY.add(date);
				datePublisher.updateHasDates();
			}
			
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void turnOffLogging() {
		Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		Logger.getLogger("org.springframework").setLevel(Level.OFF);
		Logger.getLogger("com.jonschang.investing.model").setLevel(Level.OFF);
		//Logger.getLogger("com.jonschang.investing").setLevel(Level.OFF);
		Logger.getLogger("com.jonschang.investing.PrefetchQuotePublisher").setLevel(Level.OFF);
		Logger.getLogger("com.jonschang.investing.stocks.model").setLevel(Level.OFF);
		//Logger.getLogger("com.jonschang.investing.stocks").setLevel(Level.OFF);
		Logger.getLogger("com.jonschang.investing.valuesource").setLevel(Level.OFF);
		Logger.getLogger("com.jonschang.utils").setLevel(Level.OFF);
	}
}
