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
package com.jonschang.investing.stocks.trading;

import java.util.*;

import org.junit.*;
import java.text.*;

import com.jonschang.investing.*;
import com.jonschang.investing.trading.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.stocks.trading.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.utils.*;
import com.jonschang.ai.network.*;
import com.jonschang.ai.network.feedforward.*;

public class NetworkTradingAgentTest {
	@Test public void testNetworkTradingAgent() throws Exception {		
		
		DatePublisher datePublisher = new GenericDatePublisher();
		datePublisher.setDate( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2009 16:00") );
		
		// add all our stuff to the agent
		NetworkBuilder builder = new NetworkBuilder_0_0_1a(datePublisher);
		builder.setRunEnd( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2009 16:00") );
		builder.setTrainingEnd( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2008 16:00") );
		builder.setTrainingStart( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2006 16:00") );
		SingleNetworkTrainer<FeedForward> trainer = builder.getTrainer();
		
		List<Stock> quotables = new ArrayList<Stock>();
		quotables.add( (Stock)Investing.instance().getQuotableServiceFactory().get(Stock.class).get("MSFT") );
		builder.getQuoteVSTrainingSetSource().setQuotables(quotables);
		builder.getQuoteVSTrainingSetSource().setQuotePublishersQuotable(
			(Stock)Investing.instance().getQuotableServiceFactory().get(Stock.class).get("MSFT"));
		
		NetworkTradingAgent agent = new NetworkTradingAgent();
		
		agent.setNetworkTrainer(trainer);
		agent.setNetwork(trainer.getNetwork());
		agent.setPlatform( new SimulatedPlatform() );
		// agent.setSellSignal( BinaryThresholdValueSource );
		// agent.setBuySignal( BinaryThresholdValueSource );
		// agent.setQuotables( List<Quotable> );
		// agent.setQVSToUpdate( List<QuoteValueSource> );
		agent.setNetworkTrainer(trainer);
		datePublisher.subscribe(agent);
		
		datePublisher.updateHasDates();
		List<Transaction<Stock,StockQuote,StockExchange>> trans = agent.run();
	}
}
