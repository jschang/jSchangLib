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

import org.apache.log4j.Logger;

import com.jonschang.investing.trading.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.service.StockQuoteService;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.*;
import com.jonschang.utils.*;

/**
 * A Simulated Trading Platform for benchmarking different Agents
 * 
 * The SimulatedTradingPlatform assumes the worst-case scenerio for all transactions
 * A market buy/sell executes at the days high and low, respectively
 * A stop buy/sell executes on the stop price
 * 
 * @author schang
 *
 */
public class SimulatedPlatform extends AbstractPlatform<Stock,StockQuote,StockExchange>  {

	Map<Account<Stock,StockQuote,StockExchange>,List<Transaction<Stock,StockQuote,StockExchange>>> transactions 
		= new HashMap<Account<Stock,StockQuote,StockExchange>,List<Transaction<Stock,StockQuote,StockExchange>>>();
	
	Map<Account<Stock,StockQuote,StockExchange>,List<Position<Stock,StockQuote,StockExchange>>> positions 
		= new HashMap<Account<Stock,StockQuote,StockExchange>,List<Position<Stock,StockQuote,StockExchange>>>();
	
	TimeInterval interval = null;
	StockQuoteService service = null;
	Double tradeCost = 4.95;
	Date currentDate = null;
	DatePublisher datePublisher = null;
	
	public SimulatedPlatform() throws Exception {
		service = (StockQuoteService)Investing.instance().getQuoteServiceFactory().getQuoteService(StockQuote.class);
	}
	
	public double getTradeCost() {
		return tradeCost;
	}
	public void setTradeCost(double cost) {
		tradeCost = cost;
	}
	
	public void setDate(Date date) {
		currentDate = date;
	}
	public Date getDate() {
		return currentDate;
	}
	
	public void setDatePublisher(DatePublisher dp) {
		this.datePublisher = dp;
	}
	public DatePublisher getDatePublisher() {
		return this.datePublisher;
	}
	
	public void setInterval(TimeInterval interval) {
		this.interval = interval;
	}
	public TimeInterval getInterval() {
		return this.interval;
	}
	
	public List<Position<Stock,StockQuote,StockExchange>> getPositions(Account<Stock,StockQuote,StockExchange> account) throws PlatformException {
		primeMaps(account);
		return positions.get(account);
	}

	public List<Transaction<Stock,StockQuote,StockExchange>> getTransactions(Account<Stock,StockQuote,StockExchange> account) throws PlatformException{
		primeMaps(account);
		return transactions.get(account);
	}

	public <T extends Transaction<Stock,StockQuote,StockExchange>> List<T> cancel(List<T> transactions) throws PlatformException{
		List<T> unableToCancel=new ArrayList<T>();
		for( T trans : transactions ) {
			primeMaps(trans.getAccount());
			if( // the transaction is not pending
				trans.getStatus() != Transaction.Status.PENDING 
			) {
				trans.setMessage("cannot cancel as Transaction is PENDING");
				unableToCancel.add(trans);
			} else if(!( // transaction has not been submitted
				this.transactions.get(trans.getAccount())!=null 
				&& this.transactions.get(trans.getAccount()).contains(trans) 
			)) {
				trans.setMessage("cannot cancel as Transaction has not been submitted");
				unableToCancel.add(trans);
			} else { // otherwise set status to CANCELING
				trans.setMessage("canceling...");
				trans.setStatus(Transaction.Status.CANCELING);
			}
		}
		if( unableToCancel.size()>0 )
			return unableToCancel;
		else return null;
	}
	
	public <T extends Transaction<Stock,StockQuote,StockExchange>> List<T> submit(List<T> transactions) throws PlatformException {
		
		List<T> unableToSubmit=new ArrayList<T>();
		for( T trans : transactions ) {
			primeMaps(trans.getAccount());
			if( 
				// the transaction is neither new nor pending
				!( 
					trans.getStatus() == Transaction.Status.NEW ||
					trans.getStatus() == Transaction.Status.PENDING
				)
				// or we already have the transaction
				|| (
					this.transactions.get(trans.getAccount())!=null 
					&& this.transactions.get(trans.getAccount()).contains(trans) 
				) 
			)
				unableToSubmit.add(trans);
			else // otherwise add the transaction to the queue
				{
				this.transactions.get(trans.getAccount()).add(trans);
				trans.setMessage("processing...");
				trans.setStatus(Transaction.Status.PENDING);
			}
		}
		if( unableToSubmit.size()>0 )
			return unableToSubmit;
		else return null;
	}
	
	public <T extends Account<Stock,StockQuote,StockExchange>> void refresh(T acct) throws PlatformException {
		process();
	}
	
	public void process() throws PlatformException {
		processTransactions();
		refreshAccount();
	}
	
	/**
	 * Determine the equity and buying power of the accounts held in the positions map
	 * @throws ServiceException
	 */
	private void refreshAccount() throws PlatformException {
		List<Position<Stock,StockQuote,StockExchange>> thesePos = null;
		Account<Stock,StockQuote,StockExchange> acct = null;
		StockQuote quote = null;
		
		double equity=0;
		for( Map.Entry<Account<Stock,StockQuote,StockExchange>, List<Position<Stock,StockQuote,StockExchange>>> entry : positions.entrySet() ) {
			acct=entry.getKey();
			thesePos=entry.getValue();
			for( Position<Stock,StockQuote,StockExchange> thisPos : thesePos ) {
				try {
					quote = getQuote(thisPos.getQuotable());
				} catch(ServiceException se) {
					throw new PlatformException("Could not fetch "+this.currentDate+" days quote for "+thisPos.getQuotable().getSymbol(),se);
				}
				if( quote!=null ) {
					equity += quote.getPriceClose()*thisPos.getQuantity();
				} else {
					equity += thisPos.getBasisCost()*thisPos.getQuantity();
					Logger.getLogger(this.getClass()).warn("no quote for "+thisPos.getQuotable().getSymbol()+" for the date "+currentDate+" in the interval "+interval+", using cost basis instead");
				}
			}
			acct.setEquity(equity);
		}
	}
	
	/**
	 * Iterate over PENDING transactions and execute according to type (BUY or SELL)
	 * @throws Exception
	 */
	private void processTransactions() throws PlatformException {
		List<Transaction<Stock,StockQuote,StockExchange>> theseTrans = null;
		Account<Stock,StockQuote,StockExchange> thisAcct = null;
		List<Stock> quotes = null;
		StockQuote quote = null;
		Date date = null;
		Set<Stock> quotables = new HashSet<Stock>();
		
		// cycle over accounts
		for( Map.Entry<Account<Stock,StockQuote,StockExchange>, List<Transaction<Stock,StockQuote,StockExchange>>> entry : transactions.entrySet() ) {
			
			thisAcct = entry.getKey();
			theseTrans = entry.getValue();
			
			// cycle over each transaction
			for( Transaction<Stock,StockQuote,StockExchange> trans : theseTrans ) {
				
				try {
					quote = getQuote(trans.getQuotable());
				} catch( ServiceException se ) {
					throw new PlatformException("Could not fetch "+this.currentDate+" days quote for "+trans.getQuotable().getSymbol(),se);
				}
				if( quote==null ) continue;
				
				// verify that the last quote pulled is correct
				// for the currentDate and the interval
				date = (Date)currentDate.clone();
				//StockExchange exchange = Investing.instance().getExchangeServiceFactory().get(Stock.class).getExchange(quotable);
				BusinessCalendar cal = trans.getQuotable().getExchange().getContext().cloneBusinessCalendar();
				cal.setTime(date);
				cal.normalizeToInterval(interval);
				long t2 = quote.getDate().getTime();
				long t1 = cal.getTime().getTime();
				if( t1 != t2 ) {
					Logger.getLogger(this.getClass()).warn(currentDate+" normalized to "+cal.getTime()+" was not equal to the quote date "+quote.getDate());
					continue;
				}
				
				// if the transaction status is PENDING
				if( trans.getStatus() == Transaction.Status.PENDING ) {
					// if the transaction Type is BUY
					if( trans.getType() == Transaction.Type.BUY ) {
						executeBuy(thisAcct,trans,quote);
					} 
					// if the transaction Type is SELL
					else if( trans.getType() == Transaction.Type.SELL ) {
						executeSell(thisAcct,trans,quote);
					}
				} 
			}
		}
	}
	
	/**
	 * Get a StockQuote for a Stock on the date and interval the TradingPlatform is on
	 * @param q The Stock to acquire a StockQuote for
	 * @return null or the StockQuote of the TradingPlatform's current date and interval
	 * @throws ServiceException
	 */
	private StockQuote getQuote(Stock q) throws ServiceException {
		// obtain a quote for this date and interval
		// or cycle to the next transaction
		List<Stock> quotes = new ArrayList<Stock>();
		quotes.add(q);
		quotes = service.pullNumber(quotes, currentDate, -1, interval);
		if( quotes==null || quotes.size()==0 || quotes.get(0).getQuotes()==null || quotes.get(0).getQuotes().size()==0 ) {
			Logger.getLogger(this.getClass()).warn("no quotes pulled for "+q.getSymbol()+" in interval "+interval+" on "+currentDate);
			return null;
		} else {
			int last = quotes.get(0).getQuotes().size()-1;
			return quotes.get(0).getQuotes().get(last);
		}
	}
	
	/**
	 * Determines the actual transaction cost, including cost-per-trade
	 * 
	 * Sell transactions should only return the cost-per-trade
	 * Buy transactions will return the quantity * price-per-share + cost-per-trade
	 * 
	 * @param thisAcct The account the transaction is for
	 * @param trans The transaction 
	 * @param buyPrice The price-per-share
	 * @return The cost of the trade
	 */
	private double getCost(Account<Stock,StockQuote,StockExchange> thisAcct, Transaction<Stock,StockQuote,StockExchange> trans, double buyPrice) {
		if( trans.getType()==Transaction.Type.BUY )
			return trans.getQuantity()*buyPrice+tradeCost;
		if( trans.getType()==Transaction.Type.SELL )
			return tradeCost;
		return 100000000000.0;
	}
	
	/**
	 * Determine if a particular StockTransaction is affordable at a specific price-per-share 
	 * @param thisAcct the account we're placing the transaction for
	 * @param trans the transaction to evaluate for affordability
	 * @param buyPrice the price-per-share to buy at
	 * @return true if the StockTransaction is affordable, else false
	 */
	private boolean verifyBuyingPower(Account<Stock,StockQuote,StockExchange> thisAcct, Transaction<Stock,StockQuote,StockExchange> trans, double buyPrice) {
		if( getCost(thisAcct,trans,buyPrice) < thisAcct.getBuyingPower() )
			return true;
		return false;
	}
	
	/**
	 * Process a BUY type StockTransaction
	 * @param thisAcct
	 * @param trans
	 * @param quote
	 * @throws Exception
	 */
	private void executeBuy(Account<Stock,StockQuote,StockExchange> thisAcct, Transaction<Stock,StockQuote,StockExchange> trans, StockQuote quote) throws PlatformException {
		double buyPrice=(-1.0);
		List<Position<Stock,StockQuote,StockExchange>> thesePos = null;
		Position<Stock,StockQuote,StockExchange> position = null;
		
		// if the day's low went below the stop
		// then assume we got whatever the default price
		// for the interval would've been
		// only a limit can save us from getting the day's high
		if( trans.getStop() != Transaction.NO_STOP ) {
			if( quote.getPriceLow() > trans.getStop() )
				return;
			else trans.setStop(Transaction.NO_STOP);
		}
		
		// if there is a limit
		// and the low price is less than the limit
		if( trans.getLimit() != Transaction.NO_LIMIT && quote.getPriceLow() < trans.getLimit() ) {
			/*
			 * If we do the below, then the high price will be less than the limit
			 * meaning they will get a better price.
			 * Given that we're tuning to the worst case scenerio,
			 * we do not want them to get any breaks.
			 * 
			 * <code>
			 * if( quote.getPriceHigh() >= trans.getLimit() )
			 *	buyPrice=trans.getLimit();
			 * else buyPrice=quote.getPriceHigh();
			 * </code>
			*/
			
			// better to stick with this, rather than the commented out above
			buyPrice=trans.getLimit();
		} else if( buyPrice<0 ) buyPrice = quote.getPriceHigh();
			
		// if there is a stop
		
		if( buyPrice>0 ) {
			double cost = getCost(thisAcct,trans,buyPrice);
			
			// verify that the account can afford the purchase
			if( !verifyBuyingPower(thisAcct,trans,buyPrice) ) {
				Logger.getLogger(this.getClass()).error(cost+" funds are unavailabe");
				return;
			}
			
			position = getPosition(thisAcct,trans.getQuotable());
			if( position!=null ) {
				position.setBasisCost( 
					( (int)trans.getQuantity()*buyPrice + position.getQuantity()*position.getBasisCost() ) 
					/ (position.getQuantity()+(int)trans.getQuantity()) 
				);
				position.setQuantity( (int)trans.getQuantity() + position.getQuantity() );
			} else {
				position = new StockPosition();
				position.setBasisCost(buyPrice);
				position.setQuantity((int)trans.getQuantity());
				position.setQuotable(trans.getQuotable());
				positions.get(thisAcct).add(position);
			}
			
			if( position.getTransactions()==null )
				position.setTransactions( new ArrayList<Transaction<Stock,StockQuote,StockExchange>>() );
			position.getTransactions().add(trans);
			
			trans.setQuote(quote);
			trans.setStatus(Transaction.Status.EXECUTED);
			trans.setMessage("transaction complete");
			thisAcct.setBuyingPower( thisAcct.getBuyingPower()-cost );
			
			Logger.getLogger(this.getClass()).info("BUY "+trans.getQuantity()+" executed at "+buyPrice+" per share for "+trans.getQuotable().getSymbol());
		}
	}
	
	/**
	 * Process a SELL type StockTransaction
	 * @param thisAcct
	 * @param trans
	 * @param quote
	 */
	private void executeSell(Account<Stock,StockQuote,StockExchange> thisAcct, Transaction<Stock,StockQuote,StockExchange> trans, StockQuote quote) {
		double sellPrice=(-1.0);
		List<Position<Stock,StockQuote,StockExchange>> thesePos = null;
		Position<Stock,StockQuote,StockExchange> position = null;
		
		thesePos = positions.get(thisAcct);
		boolean foundPos=false;
		int index=0;
		for( Position<Stock,StockQuote,StockExchange> pos : thesePos ) {
			if( pos.getQuotable().equals(trans.getQuotable()) ) {
				foundPos = true;
				break;
			}
			index++;
		}
		if( foundPos ) {
			position = thesePos.get( index );
		} else {
			Logger.getLogger(this.getClass()).error("The SELL transaction on "+trans.getQuotable().getSymbol()+" is not a held position.");
			return;
		}
		
		if( position.getQuantity() < trans.getQuantity() ) {
			Logger.getLogger(this.getClass()).error("The SELL transaction on "+trans.getQuotable().getSymbol()+" requested "+trans.getQuantity()+" shares.  The position is only long "+position.getQuantity()+".");
			return;
		}
		
		// if the interval's high went beyond the stop
		// then assume we got the stop price
		if( trans.getStop() != Transaction.NO_STOP ) {
			if( quote.getPriceLow() < trans.getStop() ) {
				Logger.getLogger(this.getClass()).error("The SELL transaction on "+trans.getQuotable().getSymbol()+", with STOP at "+trans.getStop()+" WAS triggered on "+currentDate+".");
				sellPrice = trans.getStop();
				trans.setStop(Transaction.NO_STOP);
			} else {
				Logger.getLogger(this.getClass()).info("The SELL transaction on "+trans.getQuotable().getSymbol()+", with STOP at "+trans.getStop()+" was NOT triggered on "+currentDate+".");
				return;
			}
		}
		
		// if there is a limit
		// and the low price is less than the limit
		if( trans.getLimit() != Transaction.NO_LIMIT 
			&& quote.getPriceLow() < trans.getLimit()
			
		) {
			// if the intervals range encompasses the limit
			// then assume we got the limit price
			// otherwise, we'll get the day's low
			if( (quote.getPriceLow() < trans.getLimit() && quote.getPriceHigh() >= trans.getLimit()) )
				sellPrice=trans.getLimit();
			// otherwise assume we sold at the worst price
			else if( sellPrice<0 ) sellPrice = quote.getPriceLow();
		}
		// if the buyPrice hasn't been established by the stop
		// then we can only assume the worst-case of selling
		// at the intervals low
		else if( sellPrice<0 ) sellPrice = quote.getPriceLow();
		
		if( sellPrice>0 ) {
			double cost = getCost(thisAcct,trans,sellPrice);
			
			// verify that the account can afford the purchase
			if( !verifyBuyingPower(thisAcct,trans,sellPrice) ) {
				Logger.getLogger(this.getClass()).error(cost+" funds are unavailabe");
				return;
			}
			
			position.setQuantity( position.getQuantity() - (int)trans.getQuantity() );
			if( position.getTransactions()==null )
				position.setTransactions( new ArrayList<Transaction<Stock,StockQuote,StockExchange>>() );
			position.getTransactions().add(trans);
			
			trans.setQuote(quote);
			trans.setStatus(Transaction.Status.EXECUTED);
			trans.setMessage("transaction complete");
			thisAcct.setBuyingPower( thisAcct.getBuyingPower()+(sellPrice*(int)trans.getQuantity()-tradeCost) );
			
			Logger.getLogger(this.getClass()).info("SELL "+trans.getQuantity()+" executed at "+sellPrice+" per share for "+trans.getQuotable().getSymbol());
		}
	}
	
	/**
	 * A little helper method to find the position corresponding to the stock passed in
	 * @param acct Used to lookup the account within the TradingAccount maps
	 * @param stock The StockPosition we're looking for will have this stock, but not necessarily this object instance
	 * @return null or the StockPosition
	 * @throws Exception
	 */
	private Position<Stock,StockQuote,StockExchange> getPosition(Account<Stock,StockQuote,StockExchange> acct, Stock stock) throws PlatformException {
		Position<Stock,StockQuote,StockExchange> position = null;
		for( Position<Stock,StockQuote,StockExchange> maybe : positions.get(acct) ) {
			if( maybe.getQuotable().equals(stock) )
				return maybe;
		}
		return null;
	}
	
	/**
	 * Insures that the transactions and positions maps contain the account
	 * called pretty much at the top of all methods
	 * @param account
	 */
	private void primeMaps(Account<Stock,StockQuote,StockExchange> account) {
		if( !transactions.containsKey(account) )
			transactions.put(account, new ArrayList<Transaction<Stock,StockQuote,StockExchange>>());
		if( !positions.containsKey(account) )
			positions.put(account, new ArrayList<Position<Stock,StockQuote,StockExchange>>());
	}
}
