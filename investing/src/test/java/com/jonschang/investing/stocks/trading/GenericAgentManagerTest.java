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

import java.text.*;
import java.util.*;
import org.junit.*;
import com.jonschang.investing.Investing;
import com.jonschang.investing.QuotableService;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.trading.*;
import com.jonschang.investing.model.*;
import com.jonschang.investing.*;
import com.jonschang.utils.*;

public class GenericAgentManagerTest {
	@Test public void testGenericAgentManager() throws Exception {
		try {
			DatePublisher datePublisher = new GenericDatePublisher();
			
			AgentManager agentManager = new GenericAgentManager();
			
			// create the generic agent, effectively a null pattern
			
			Agent<Stock,StockQuote,StockExchange> agent = new GenericStockAgent();
			datePublisher.subscribe(agent);
			agent.setPlatform( new SimulatedPlatform() );
			agent.setAccount( new StockTradingAccount() );
			
			// get a quotable for the agent to look at
			
			QuotableService<Stock> quotableService = Investing.instance().getQuotableServiceFactory().get(Stock.class);
			Stock quotable = new Stock();
			quotable.setSymbol("MSFT");
			quotable = quotableService.get( quotable );
			
			List<Stock> quotables = new ArrayList<Stock>();
			quotables.add( quotable );
			agent.setQuotables( quotables );
			
			List<Agent> agentList = new ArrayList<Agent>();
			agentList.add(agent);
			
			agentManager.setAgents(agentList);
			
			Date date = DateFormat.getDateInstance().parse("January 1, 2007 16:00:00");
			datePublisher.setDate(date);
			datePublisher.updateHasDates();
			agentManager.run();
			
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
}
