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
package com.jonschang.investing.stocks.service;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.utils.*;

//TODO: add setQuotableService()
//TODO: i'm not happy with passing the QuoteRequest around everywhere
abstract public class StockQuoteService
	extends AbstractQuoteService<StockQuote,Stock> 
{
	protected Logger m_logger = Logger.getLogger(StockQuoteService.class);
	
	public boolean supports(TimeInterval interval) {
		if( interval == TimeInterval.DAY )
			return true;
		else return false;
	}
	
	/**
	 * Holds request related information so that the quote service 
	 * can more conveniently remain stateless.
	 * @author schang
	 */
	protected class QuoteRequest {
		BusinessCalendar busCalLow, busCalHigh;
		DateRange range;
		TimeInterval interval;
		StockExchange exchange;
		StockExchangeContext exchangeContext;
		Map<StockExchange,Collection<Stock>> exchangeMap;
		Collection<Stock> quotables;
		List<Stock> quotes;
		Map<Stock,List<DateRange>> needToPull;
	}
	
	@Override
	public List<Stock> pullNumber(Collection<Stock> quotables, Date startDate, int number, TimeInterval interval) 
		throws ServiceException, HibernateException {
		
		// TODO: should this be normalized to the interval?
		DateUtils.normalizeToMinute(startDate);
		
		if( interval != TimeInterval.DAY )
			throw new UnsupportedException("StockQuoteService only supports DAY intervals.");
		
		// ultimately, we'll be returning the same stocks passed in
		// except with the StockQuote list updated with the result
		// of the request
		List<Stock> toRet = new ArrayList<Stock>();
		
		// reset each Stock object's StockQuote list
		for( Stock stock : quotables )
			stock.setStockQuotes(new ArrayList<StockQuote>());
		
		// create the quote request context object
		QuoteRequest qr = new QuoteRequest();
		qr.quotables = quotables;
		qr.interval = interval;
		
		// iterate over quotables and pack into a map indexed by stock exchange
		qr.exchangeMap = keyByExchange(qr.quotables);
		
		// iterate over each exchange in the exchangeMap
		// and attempt to pull each historical stock quote value from the database
		// if it can't be found in the database,
		// then pull it from yahoo.qr.exchangeContext = 
		for( StockExchange stockExchange : qr.exchangeMap.keySet() )
		{
			// get the exchange context for the stock exchange this stock is in
			// TODO: i think i want to merge the context in with the exchange...need to investigate the difficulty of this
			qr.exchangeContext = (StockExchangeContext)stockExchange.getContext();
			qr.exchange = stockExchange;
			qr.quotables = qr.exchangeMap.get(qr.exchange);
			
			// create start and end date business calendars from the exchange calendar
			qr.busCalHigh = qr.exchangeContext.cloneBusinessCalendar();
			qr.busCalLow = qr.exchangeContext.cloneBusinessCalendar();
			qr.busCalHigh.setTimeInMillis( startDate.getTime() );
			qr.busCalHigh.normalizeToInterval(qr.interval);
			
			qr.quotes = pullQuotesNumber(qr,number);
			if( qr.quotes.size()>0 && qr.quotes.get(0).getQuotes().size()>=Math.abs(number) ) {
				List<StockQuote> pulledQuotes = qr.quotes.get(0).getQuotes();
				qr.range = new DateRange(pulledQuotes.get(0).getDate(),pulledQuotes.get(pulledQuotes.size()-1).getDate());
			} else {
				qr.busCalLow.setTimeInMillis( qr.busCalHigh.getTimeInMillis() );
				qr.busCalLow.add(interval,number);
				qr.range = number > 0 
					? new DateRange(qr.busCalHigh.getTime(),qr.busCalLow.getTime()) 
					: new DateRange(qr.busCalLow.getTime(),qr.busCalHigh.getTime());
			}
			qr.busCalLow.setTimeInMillis( qr.range.getStart().getTime() );
			qr.busCalHigh.setTimeInMillis( qr.range.getEnd().getTime() );
			m_logger.info("business calendar low and high are "+qr.busCalLow.getTime()+" and "+qr.busCalHigh.getTime());
			m_logger.info("date range for "+qr.exchange.getSymbol()+" is "+qr.busCalLow.getTime()+" to "+qr.busCalHigh.getTime());
			
			// pull quotes
			//qr.quotes = pullQuotes(qr);
			
			// iterate over the original quotables to create a list of missing quote ranges
			qr.needToPull = determineNeedToPull(qr);
			
			// pull any ranges we didn't get from the database from yahoo
			pullNeededQuotesFromWebService(qr);
			
			// we need to so this again, cause yahoo 
			trimErrorQuotes(qr);
			
			// pack the stocks w/ quotes into the map for return
			for( Stock stock : qr.quotes )
				toRet.add(stock);
		} // end iteration over exchangeMap
		
		return toRet;
	}
	
	/**
	 * @return a map of Stock/List<StockQuote> pairs.  Note that these are the same objects passed in the quotables parameter. 
	 * {@inheritDoc}
	 */
	@Override
	public List<Stock> pullDateRange(Collection<Stock> quotables, DateRange range, TimeInterval interval)
		throws ServiceException, HibernateException {
		
		if( interval != TimeInterval.DAY )
			throw new UnsupportedException("YahooHistoricalStockQuoteService only supports DAY intervals.");
		
		List<Stock> toRet = new ArrayList<Stock>();
		
		// reset each Stock object's StockQuote list
		for( Stock stock : quotables )
			stock.setStockQuotes(new ArrayList<StockQuote>());
		
		QuoteRequest qr = new QuoteRequest();
		qr.interval = interval;
		qr.quotables = quotables;
		
		// currently, there are no mid-minute quotes 
		// TODO: decide if normalizing the date range to the minute is really a responsibility of this class, 
		// if so then i should mention it in the interface doc
		qr.range = new DateRange(new Date(range.getStart().getTime()),new Date(range.getEnd().getTime()));
		
		// iterate over quotables and pack into a map indexed by stock exchange
		qr.exchangeMap = keyByExchange(qr.quotables);
		
		// iterate over each exchange in the exchangeMap
		// and attempt to pull each historical stock quote value from the database
		// if it can't be found in the database,
		// then pull it from yahoo.
		for( StockExchange stockExchange : qr.exchangeMap.keySet() )
		{
			// get the exchange context for the stock exchange this stock is in
			qr.exchangeContext = (StockExchangeContext)stockExchange.getContext();
			qr.exchange = stockExchange;
			qr.quotables = qr.exchangeMap.get(qr.exchange);
			
			// determine the start and end dates using the exchanges business calendar
			qr.busCalLow = newClosestAfterStart(qr);
			qr.busCalHigh = newClosestBeforeEnd(qr);
			qr.range = new DateRange(qr.busCalLow.getTime(),qr.busCalHigh.getTime());
			
			m_logger.info("date range for "+qr.exchange.getSymbol()+" is "+qr.busCalLow.getTime()+" to "+qr.busCalHigh.getTime());
			
			// pull quotes
			qr.quotes = pullQuotes(qr);
			
			// iterate over the original quotables to create a list of missing quote ranges
			qr.needToPull = determineNeedToPull(qr);

			// pull any ranges we didn't get from the database from yahoo
			pullNeededQuotesFromWebService(qr);
			
			trimErrorQuotes(qr);
			
			// pack the stocks w/ quotes into the map for return
			for( Stock stock : qr.quotes )
				toRet.add(stock);
		} // end iteration over exchangeMap
		
		return toRet;
	}
	
	/**
	 * Trims quotes not available from the StockQuote List's in qr.quotes
	 * 
	 * When a webservice returns a 404 for a date range, or a quote is missing
	 * on a day the StockExchange's BusinessCalendar expects it to have one,
	 * insert those quotes into the database anyways with a closing price of
	 * -404.0.  Here's where they are trimmed out before passing the
	 * quotes back to the requester.
	 * 
	 * @param qr
	 */
	protected void trimErrorQuotes(QuoteRequest qr) {
		for( Stock stock : qr.quotes ) {
			List<StockQuote> removeList = new ArrayList<StockQuote>();
			if( stock.getStockQuotes()!=null )
				for( StockQuote sq : stock.getStockQuotes() ) {
					if( sq.getPriceClose()<0 )
						removeList.add(sq);
				}
			stock.getStockQuotes().removeAll(removeList);
		}
	}
	
	/**
	 * A convenience method for reading in the content of a CSV we pull from some webservice.
	 * @param is
	 * @return
	 * @throws Exception
	 */
	protected String readInputStream(InputStream is)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int r = is.read();
		while( r!=(-1) )
		{
			sb.append( (char)r );
			r = is.read();
		}
		return sb.toString();
	}
	
	/**
	 * Creates a map of sets of Stock objects keyed by the StockExchange
	 */
	protected Map<StockExchange,Collection<Stock>> keyByExchange(Collection<Stock> quotables) {
		// iterate over quotables and pack into a map indexed by stock exchange
		Map<StockExchange,Collection<Stock>> exchangeMap 
			= new HashMap<StockExchange,Collection<Stock>>();
		for( Stock stock : quotables ) {
			// if we haven't categorized the stock by exchange yet,
			// then create a new HashSet for the stocks exchange
			// and add it to the exchangeMap map
			Collection<Stock> stockSet = exchangeMap.get(stock.getStockExchange()); 
			if( stockSet == null ) {
				stockSet = new HashSet<Stock>();
				exchangeMap.put(stock.getStockExchange(),stockSet);
			}
			stockSet.add(stock);
		}
		return exchangeMap;
	}	
	
	/**
	 * Determine the closest Date before the end of the DateRange
	 * @param qr
	 * @return
	 */
	protected BusinessCalendar newClosestBeforeEnd(QuoteRequest qr) {
		BusinessCalendar busCalHigh = qr.exchangeContext.newBusinessCalendar();
		busCalHigh.setTime(qr.range.getEnd());
		busCalHigh.normalizeToInterval(qr.interval);
		if( ! busCalHigh.isBusinessTime() )
			busCalHigh.closestBefore(qr.interval);
		return busCalHigh;
	}
	
	/**
	 * Determine the closest Date after the start of the DateRange
	 * @param qr
	 * @return
	 */
	protected BusinessCalendar newClosestAfterStart(QuoteRequest qr) {
		BusinessCalendar busCalLow = qr.exchangeContext.newBusinessCalendar();
		busCalLow.setTime(qr.range.getStart());
		busCalLow.normalizeToInterval(qr.interval);
		if( !busCalLow.isBusinessTime() )
			busCalLow.closestAfter(qr.interval);
		return busCalLow;
	}
	
	/**
	 * A template method for descendents fetching quotes from a web service.
	 * 
	 * Note that, in order for the StockQuote to be complete, the web service
	 * must:
	 *  - provide quotes that are unadjusted
	 *  - provide (at bare minimum) split events (StockEventSplit)
	 *  
	 * It would be best if the web service chosen also provided dividend events
	 * (StockEventDividend)
	 * 
	 * @param qr
	 * @throws ServiceException
	 */
	protected void pullNeededQuotesFromWebService(QuoteRequest qr) throws ServiceException {
	}
	
	/**
	 * Pulls the quotes for a DateRange from the database.
	 * @param qr
	 * @return
	 * @throws HibernateException
	 */
	private List<Stock> pullQuotes(QuoteRequest qr) throws HibernateException {
		
		//StringBuilder inList = createInListFromExchangeMap(qr);
		List<Stock> toRet = new ArrayList<Stock>();
		
		// iterate over the quotables in this exchange
		for( Stock stock : qr.quotables ) {
			
			// create the query we'll use to pull back these stocks
			String hql = "select distinct sq from StockQuote sq "
				+"left join sq.stock s "
				+"left outer join fetch sq.stockEvents "
				+"where s.symbol=:symbol "
				+"and sq.interval=:interval "
				+"and sq.date >= :dateLow and sq.date <= :dateHigh "
				+"order by sq.date asc";
			m_logger.trace("in pullCurrent, sql used: "+hql);
			
			// attempt to pull the most recent list of quotes from the database
			Session session = Investing.instance().getSessionFactory().getCurrentSession();
	
			session.beginTransaction();
			@SuppressWarnings(value={"unchecked"})
			List<StockQuote> quotes = session
				.createQuery(hql)
				.setTimestamp("dateLow",qr.busCalLow.getTime())
				.setTimestamp("dateHigh",qr.busCalHigh.getTime())
				.setInteger("interval",qr.interval.duration())
				.setString("symbol", stock.getSymbol())
				.list();
			session.getTransaction().commit();
			
			if( quotes!=null && quotes.size()>0 ) {
				stock.setStockQuotes(quotes);
				toRet.add(stock);
			}			
		}
		
		// update quote references to the stock passed in
		if( toRet!=null )
			for( Stock stock : toRet ) {
				m_logger.info("pulled "+stock.getStockQuotes().size()+" quotes for "+stock.getSymbol());
				/*
				if( stock.getStockQuotes()!=null )
					for( StockQuote quote : stock.getStockQuotes() ) {
						quote.setStock(stock);
						m_logger.trace("pulled quote for date "+quote.getDate()+" with close at "+quote.getPriceClose());
					}
				*/
			}
		
		return toRet;
	}
	
	/**
	 * Pulls a number of quotes from the database either forward 
	 * or backward from the StartDate, dependent on the sign of the number.
	 * The start date is inclusive. 
	 * @param qr
	 * @param number
	 * @return
	 * @throws HibernateException
	 */
	private List<Stock> pullQuotesNumber(QuoteRequest qr, int number) throws HibernateException {
		
		//StringBuilder inList = createInListFromExchangeMap(qr);
		List<Stock> toRet = new ArrayList<Stock>();
		
		// iterate over the quotables in this exchange
		for( Stock stock : qr.quotables ) {
			
			// create the query we'll use to pull back these stocks
			String hql = "select distinct sq from StockQuote sq "
				+"left join sq.stock s "
				+"left outer join fetch sq.stockEvents "
				+"where s.symbol=:symbol "
				+"and sq.interval=:interval "
				+"and sq.date "+((number>0)?">=":"<=")+" :dateHigh "
				+"order by sq.date "+((number>0)?"asc":"desc")
				+"";
			m_logger.trace("in pullCurrent, sql used: "+hql);
			
			// attempt to pull the most recent list of quotes from the database
			Session session = Investing.instance().getSessionFactory().getCurrentSession();
	
			session.beginTransaction();
			@SuppressWarnings(value={"unchecked"})
			List<StockQuote> quotes = session
				.createQuery(hql)
				.setTimestamp("dateHigh",qr.busCalHigh.getTime())
				.setInteger("interval",qr.interval.duration())
				.setString("symbol", stock.getSymbol())
				.setMaxResults( Math.abs(number) )
				.list();
			session.getTransaction().commit();
			
			if( quotes!=null && quotes.size()>0 ) {
				stock.setStockQuotes(quotes);
				java.util.Collections.sort(stock.getStockQuotes());
			} else stock.setStockQuotes( new ArrayList<StockQuote>() );
			
			toRet.add(stock);
		}
		
		// update quote references to the stock passed in
		if( toRet!=null )
			for( Stock stock : toRet ) {
				m_logger.info("pulled "+stock.getStockQuotes().size()+" quotes for "+stock.getSymbol());
			}
		
		return toRet;
	}

	/**
	 * Uses the BusinessCalendar of a StockExchange to determine what 
	 * DateRange's are missing from the list of quotes pulled from the database.
	 * @param qr
	 * @return A Map of DateRange Lists keyed by Stock
	 * @throws ServiceException
	 * @throws HibernateException
	 */
	private Map<Stock,List<DateRange>> determineNeedToPull(QuoteRequest qr) throws ServiceException, HibernateException {
		// iterate over the original quotables for this exchange to create a list  of missing quote ranges
		Map<Stock,List<DateRange>> needToPull = new HashMap<Stock,List<DateRange>>();
		for( Stock stock : qr.quotables ) {
			// check to see if we were able to pull quotes for this stock from the database
			Boolean found = qr.quotes.contains(stock);
			if( found ) {
				java.util.Collections.sort(stock.getStockQuotes());
				BusinessCalendar cal = qr.exchangeContext.newBusinessCalendar();
				List<DateRange> missingDateRanges=null;
				try {
					missingDateRanges = QuoteDateUtils.getQuoteDateRangeGaps(cal,qr.interval,qr.range,stock.getStockQuotes());
				} catch( Exception e ) {
					throw new ServiceException("an exception occurred processing the date range gaps", e);
				}
				if( missingDateRanges!=null && missingDateRanges.size()>0 )
					needToPull.put(stock, missingDateRanges);
			} else {
				List<DateRange> missingDateRanges = new ArrayList<DateRange>();
				missingDateRanges.add(new DateRange((Date)qr.range.getStart().clone(),(Date)qr.range.getEnd().clone()));
				needToPull.put(stock,missingDateRanges);
			}
		}
		return needToPull;
	}	
}
