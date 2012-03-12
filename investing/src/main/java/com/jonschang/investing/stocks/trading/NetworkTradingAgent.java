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

import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import com.jonschang.utils.valuesource.*;
import com.jonschang.investing.QuotePublisher;
import com.jonschang.investing.trading.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.investing.stocks.model.*;

// TODO: move this down to a more generic level
public class NetworkTradingAgent<N extends Network> extends GenericStockAgent {
	private N network;
	private SingleNetworkTrainer<N> trainer;
	private List<QuotePublisher<StockQuote,Stock>> updateQuotableQP;
	
	NetworkTradingAgent() {
	}
	
	/**
	 * @return The list of QuoteValueSource's to update with the current Quotable on each iteration
	 */
	public List<QuotePublisher<StockQuote,Stock>> getQPToUpdate() {
		return this.updateQuotableQP;
	}
	public void setQPToUpdate(List<QuotePublisher<StockQuote,Stock>> listToUpdate) {
		this.updateQuotableQP = listToUpdate;
	}	
	
	/**
	 * @return The network to use for Network::calculateResponse()
	 */
	public N getNetwork() {
		return network;
	}
	public void setNetwork(N net) {
		this.network = net;
	}
	
	/**
	 * @return The network trainer that this agent will use
	 */
	public SingleNetworkTrainer<N> getNetworkTrainer() {
		return this.trainer;
	}
	public void setNetworkTrainer(SingleNetworkTrainer<N> netTrainer) {
		this.trainer = netTrainer;
	}
	
	/**
	 * @return The trigger that a given Quotable would be a good buy
	 */
	public ValueSource getBuySignal() {
		return this.buyValueSource;		
	}
	public void setBuySignal(ValueSource buyVS) {
		this.buyValueSource=buyVS;
	}
	private ValueSource buyValueSource;
	
	/**
	 * @return The trigger that a given Quotable would be a good sell
	 */
	public ValueSource getSellSignal() {
		return this.sellValueSource;		
	}
	public void setSellSignal(ValueSource buyVS) {
		this.sellValueSource=buyVS;
	}
	private ValueSource sellValueSource;
 
	/**
	 * Iterates over the Quotables, running the Network for each
	 * Each QuoteValueSource in the updateQuotableQVSL will be updated with the current quotable during iteration
	 * @return A List of Transactions submitted to the Platform  
	 */
	@Override
	public <T extends Transaction<Stock,StockQuote,StockExchange>> List<T> run() throws AgentException {
		
		try {
			trainer.getNetwork().calculateResponse();
			
			tradingPlatform.refresh(tradingAccount);
			
			List<Position<Stock,StockQuote,StockExchange>> positions = tradingPlatform.getPositions(tradingAccount);
			List<Transaction<Stock,StockQuote,StockExchange>> transactions = tradingPlatform.getTransactions(tradingAccount);
		} catch( Exception e ) {
			throw new AgentException("An exception occurred on "+date,e);
		}
		
		return null;
	}
}
