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

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import org.dom4j.*;
import org.dom4j.io.*;
import org.apache.log4j.Logger;
import org.hibernate.*;

import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.service.StockQuoteService.QuoteRequest;
import com.jonschang.utils.*;

import java.io.IOException;

/**
 * An extension of StockQuoteService, overriding the
 * pullNeededQuotesFromWebService() method to pull missing
 * quotes from Yahoo! Finance. 
 * @author schang
 */
public class YahooHistoricalStockQuoteService extends StockQuoteService
{
	public YahooHistoricalStockQuoteService() {
		m_logger = Logger.getLogger(this.getClass());
	}
	
	/**
	 * The Yahoo! url returning historical quotes in CSV format 
	 * 
	 * SYMBOL should be the stock symbol (eg. "msft")
	 * MONTH_LOW is the start month, where Jan is 0, Feb is 1, etc
	 * DAY_LOW is the start day, 
	 * YEAR_LOW is the start year
	 * MONTH_END is the end month, where Jan is 0, Feb is 1, etc
	 * DAY_END is the end day, 
	 * YEAR_END is the end year
	 */
	private static String m_serviceUrl="http://ichart.finance.yahoo.com/table.csv?s={SYMBOL}&amp;a={MONTH_LOW}&amp;b={DAY_LOW}&amp;c={YEAR_LOW}&amp;d={MONTH_HIGH}&amp;e={DAY_HIGH}&amp;f={YEAR_HIGH}&amp;g=d&amp;ignore=.csv";
	
	/**
	 * The Yahoo! url returning "Stock Split" Xml
	 * 
	 * SYMBOL should be the stock symbol (eg. "msft")
	 * DATE should be YYYY
	 * YEARS should be the number of years following DATE
	 */
	private static String m_splitUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/{SYMBOL}/chartdata;type=split;ys={DATE};yz={YEARS}/xml/";
	
	/**
	 * The Yahoo! url returning "Dividend Payment" Xml
	 * 
	 * SYMBOL should be the stock symbol (eg. "msft")
	 * DATE should be YYYY
	 * YEARS should be the number of years following DATE
	 */
	private static String m_dividendUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/{SYMBOL}/chartdata;type=dividend;ys={DATE};yz={YEARS}/xml/";
	
	/**
	 * The Yahoo! url returning "Insider Transactions" KeyEvents Xml
	 * 
	 * SYMBOL should be the stock symbol (eg. "msft")
	 * DATE_START and DATE_END should be in the format of YYYYMMDD
	 */
	private static String m_insiderTradingUrl = "http://finance.yahoo.com/webservice/v1/keyevents/{SYMBOL}/it;range={DATE_START},{DATE_END}";

	/**
	 * The format we expect the date to be in the CSV pulled from Yahoo!
	 */
	private static String m_expectedDateFormat="yyyy-MM-dd";
	
	/**
	 * when the quotes get pulled from yahoo, dates that i expect to have
	 * that don't return anything get inserted as quotes with a closing
	 * price of -404.0
	 * @param qr
	 * @throws ServiceException
	 * @throws HibernateException
	 */
	protected void pullNeededQuotesFromWebService(QuoteRequest qr) throws ServiceException {
		// iterate over the stocks we need to pull quotes for from yahoo
		// and pull them from yahoo and persist them
		BusinessCalendar tempCal = newClosestAfterStart(qr);
		tempCal.normalizeToMinute();
		
		for( Map.Entry<Stock,List<DateRange>> entry : qr.needToPull.entrySet() ) {
			
			Stock stock = entry.getKey();
			List<DateRange> missingDateRanges = entry.getValue();
			
			if( stock.getStockQuotes()==null )
				stock.setStockQuotes(new ArrayList<StockQuote>());
			
			m_logger.trace("PROCESSING missing date ranges for "+stock.getSymbol()+" between "+qr.busCalLow.getTime().toString()+" and "+qr.busCalHigh.getTime().toString());
			for( DateRange dateRange : missingDateRanges ) {
				
				m_logger.trace("about to pull missing date range from Yahoo! between "+stock.getSymbol()+" between "+dateRange.getStart()+" and "+dateRange.getEnd());
				
				try {
					try {
						pullQuotesBetween( stock, dateRange.getStart(), dateRange.getEnd(), qr.interval, tempCal );
					} catch( java.io.FileNotFoundException fnfe ) {
						// there be no quotes in all of yahoo! for this period, arrrr.
						m_logger.warn("no quotes returned from \"Yahoo!\" for "+stock.getSymbol()+" between "+dateRange.getStart().toString()+" and "+dateRange.getEnd().toString()+".");
						// there is no result, so i should just go to the next date range
						continue;
					}
					
					Map<Date,StockQuote> dateToQuoteMap = new HashMap<Date,StockQuote>();
					for( StockQuote sq : stock.getStockQuotes() )
						dateToQuoteMap.put(sq.getDate(),sq);

					pullKeyEventData(m_splitUrl,stock,newClosestAfterStart(qr),qr.interval,dateRange,dateToQuoteMap);
					pullKeyEventData(m_dividendUrl,stock,newClosestAfterStart(qr),qr.interval,dateRange,dateToQuoteMap);

					// http://chartapi.finance.yahoo.com/instrument/1.0/mdr/chartdata;type=dividend;ys=1980;yz=100/xml/
					// http://finance.yahoo.com/webservice/v1/keyevents/msft/it;range=20070101,20080101
				} catch( DocumentException de ) {
					throw new ServiceException("A DocumentException occurred trying to pull event data from Yahoo!",de);
				} catch( IOException ioe ) {
					throw new ServiceException("An IOException occurred trying to pull quotes from Yahoo!",ioe);
				} catch( Exception e ) {
					throw new ServiceException("An unhandled exception occurred trying to pull quote information from Yahoo!",e);
				}
				
				m_logger.trace("verifying that quotes are ordered by date");
				StockQuote lastQ = null;
				for( StockQuote sq : stock.getStockQuotes() ) {
					m_logger.trace("verifying quote sort: quote date is "+sq.getDate()+" and closing is "+sq.getPriceClose() );
					if( lastQ!=null && !lastQ.getDate().before(sq.getDate()) )
						m_logger.error("damn, i've sorted this twice and it's still out of order?");
					lastQ = sq;
				}
				
				Session session = Investing.instance().getSessionFactory().getCurrentSession();
				List<StockQuote> cachedQuotes = stock.getStockQuotes();
				session.beginTransaction();
				session.saveOrUpdate(stock);
				session.getTransaction().commit();
				stock.setStockQuotes(cachedQuotes); // so we don't end up with one of those damn persistence bags
			} // iteration over missingDateRanges per each stock 
		} // iteration over stocks not pulled
	}

	private void pullQuotesBetween( Stock stock, Date start, Date end, TimeInterval interval, BusinessCalendar closestAfter ) throws IOException, ServiceException {
		
		// create a url to the text of the csv from Yahoo!
		String quote=null;
		
		URL url = getServiceUrl(stock, start, end);
		m_logger.info("connecting to "+url.toString());
		
		// TODO: handle 404s more intelligently
		quote = readInputStream(url.openConnection().getInputStream());
		
		m_logger.trace("quote result for "+stock.getSymbol()+" between "+start.toString()+" and "+end.toString()+" is: \n"+quote);
		
		// extract the relevant information from the yahoo csv
		// and pack into a new StockQuote object
		Map<String,List<String>> csvData = CSVUtils.digestCSVWithHeadings(",",quote);
		
		// we should be able to count on the yahoo historical quotes having a "Date" column
		Integer iters=csvData.get("Date").size();
		BusinessCalendar tempCal = (BusinessCalendar)closestAfter.clone();
		for( int i=0; i<iters; i++ ) {
			
			// create a new StockQuote object
			// and prime it's map of values
			StockQuote closingQuote = new StockQuote();
			closingQuote.setStock(stock);
			closingQuote.setInterval(interval.duration());
			stock.getQuotes().add(closingQuote);
			
			// fill in the values set with the values of each csv column
			for( Map.Entry<String,List<String>> column : csvData.entrySet() ) {
				
				String colName = column.getKey();
				if( colName.compareTo("Open")==0 )
					closingQuote.setPriceOpen( Float.valueOf((column.getValue()).get(i)) );
				else if( colName.compareTo("Close")==0 )
					closingQuote.setPriceClose( Float.valueOf((column.getValue()).get(i)) );
				else if( colName.compareTo("High")==0 )
					closingQuote.setPriceHigh( Float.valueOf((column.getValue()).get(i)) );
				else if( colName.compareTo("Low")==0 )
					closingQuote.setPriceLow( Float.valueOf((column.getValue()).get(i)) );
				else if( colName.compareTo("Volume")==0 )
					closingQuote.setVolume( Long.valueOf((column.getValue()).get(i)) );
				else if( colName.compareTo("Date")==0 ) {
					DateFormat df = new SimpleDateFormat(m_expectedDateFormat);
					try {
						tempCal.setTime( df.parse(column.getValue().get(i)) );
					} catch( ParseException p ) {
						throw new ServiceException("a parsing exception occurred trying to determine the quote date",p);
					}
					
					tempCal.closing();
					closingQuote.setDate( tempCal.getTime() );
				}
			}
		}
		
		// must be in order before we can do the next operations
		java.util.Collections.sort(stock.getStockQuotes());
		
		// so, we're going to go over the quotes
		// and the dates that the business calendar expects,
		// but we still don't have are getting inserted
		// as 404 errors.
		// i can strip these out easily before passing back the 
		// the quotes and will never bug yahoo for them again
		m_logger.trace("adding 404 quotes for missing calendar dates");
		BusinessCalendar cal = (BusinessCalendar)closestAfter.clone();
		List<StockQuote> newQs = new ArrayList<StockQuote>();
		for( StockQuote sq : stock.getStockQuotes() ) {
			m_logger.trace("calendar date "+cal.getTime()+" and quote date "+sq.getDate());
			if( cal.getTime().before(sq.getDate()) )
				while( cal.getTime().before(sq.getDate()) ) {
					
					StockQuote q = new StockQuote();
					q.setInterval(interval.duration());
					q.setDate((Date)cal.getTime().clone());
					q.setStock(stock);
					q.setPriceClose(-404.0f);
					q.setPriceOpen(null);
					q.setPriceHigh(null);
					q.setPriceLow(null);
					q.setVolume(null);
					
					newQs.add(q);
					
					cal.closestAfter(interval);
					
					m_logger.trace("404 quote inserted in "+stock.getSymbol()+" for calendar date "+cal.getTime());
				}
			if( !cal.getTime().after(sq.getDate()) )
				cal.closestAfter(interval);
		}
		stock.getStockQuotes().addAll(newQs);
		
		// last operation may have gotten us out of order
		Collections.sort(stock.getStockQuotes());
	}
	
	private URL getServiceUrl(Stock stock, Date start, Date end) throws IOException
	{
		Calendar startDate = new GregorianCalendar();
		startDate.setTime(start);
		
		Calendar endDate = new GregorianCalendar();
		endDate.setTime(end);
		
		String stringUrl = m_serviceUrl;
		
		stringUrl = stringUrl.replace("{SYMBOL}", stock.getSymbol().toUpperCase() );
		
		stringUrl = stringUrl.replace("{DAY_LOW}", String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)) );
		stringUrl = stringUrl.replace("{MONTH_LOW}", String.valueOf(startDate.get(Calendar.MONTH)) );
		stringUrl = stringUrl.replace("{YEAR_LOW}", String.valueOf(startDate.get(Calendar.YEAR)) );
		
		stringUrl = stringUrl.replace("{DAY_HIGH}", String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)) );
		stringUrl = stringUrl.replace("{MONTH_HIGH}", String.valueOf(endDate.get(Calendar.MONTH)) );
		stringUrl = stringUrl.replace("{YEAR_HIGH}", String.valueOf(endDate.get(Calendar.YEAR)) );
		
		m_logger.trace("in getServiceUrl, returning: "+stringUrl);
		
		return new URL(stringUrl);
	}
	
	private URL getDateYearsUrl(String url,String symbol,Date startDate,Date endDate) throws MalformedURLException {
		String stringUrl = url;
		
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startDate.getTime());
		
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis(endDate.getTime());
		end.add(Calendar.YEAR, -1*start.get(Calendar.YEAR));
		
		stringUrl = stringUrl.replace("{SYMBOL}", symbol);
		stringUrl = stringUrl.replace("{DATE}", ((Integer)start.get(Calendar.YEAR)).toString());
		stringUrl = stringUrl.replace("{YEARS}", ((Integer)end.get(Calendar.YEAR)).toString());
		
		m_logger.info("pulling from "+stringUrl);
		
		return new URL(stringUrl);
	}
	
	private void pullKeyEventData(String urlString, Stock stock, BusinessCalendar cal, TimeInterval interval, DateRange range, Map<Date,StockQuote> dateToQuoteMap) 
		throws Exception {
		URL url = getDateYearsUrl(urlString,stock.getSymbol(),range.getStart(),range.getEnd());
		SAXReader reader = new SAXReader();
		Document doc = reader.read(url.openConnection().getInputStream());
		Element rootElement = doc.getRootElement();
		
		// ok, so the date range specifiable to the keyevents service is not very fine-grained,
		// therefore, we'll assume that if there are any StockEvent's at all in a StockQuote,
		// that we pulled them here at an earlier call.
		List<Element> seriesList = rootElement.elements("series");
		if( seriesList.size()!=1 )
			throw new ServiceException("Expecting only a single 'series' tag in the XML document returned from "+url.toURI()); 
		Element series = seriesList.get(0);
		
		List<Element> valuesList = series.elements("values");
		if( valuesList.size()!=1 )
			throw new ServiceException("Expecting only a single 'values' tag in the XML document returned from "+url.toURI()); 
		List<Element> values = valuesList.get(0).elements("value");
		
		int type=0;
		if( urlString.compareTo(m_splitUrl)==0 )
			type=1; // split
		else if( urlString.compareTo(m_dividendUrl)==0 )
			type=2; // dividend
		StockEventSplit split = null;
		StockEventDividend div = null;
		
		List<Element> ps = series.elements("p");
		for( Element p : ps ) {
			
			if( urlString.compareTo(m_splitUrl)==0 )
				split = new StockEventSplit();
			else if( urlString.compareTo(m_dividendUrl)==0 )
				div = new StockEventDividend();
			
			List<Element> theseValues = p.elements("v");
			if( theseValues.size()!=values.size() )
				throw new ServiceException("Expecting the number of 'v' tags to match the number of 'values'");
			
			Date vDate = new SimpleDateFormat("yyyyMMdd").parse( p.attribute("ref").getText() );
			cal.setTimeInMillis(vDate.getTime());
			cal.normalizeToInterval(interval);
			vDate = cal.getTime();
			
			StockQuote quote = dateToQuoteMap.get(vDate);
			if( quote==null )
				continue;
			if( 
				quote.getStockEvents()!=null 
				&& ! (
					( type==1 && stockEventsHas( StockEventSplit.class, quote.getStockEvents()) ) 
					|| ( type==2 && stockEventsHas( StockEventDividend.class, quote.getStockEvents()) )
				)
			)
				continue;
			
			List<StockEvent> events = quote.getStockEvents();
			if( events==null ) {
				events = new ArrayList<StockEvent>();
				quote.setStockEvents(events);
			}
			
			int idx = 0;
			for( Element v : theseValues ) {
				Element value=values.get(idx);
				if( value.attribute("id").getText().compareTo("denominator")==0 ) {
					split.setDenominator( Double.valueOf(v.getTextTrim()).floatValue() );
				} else if( value.attribute("id").getText().compareTo("numerator")==0 ) {
					split.setNumerator( Double.valueOf(v.getTextTrim()).floatValue() );
				} else if( value.attribute("id").getText().compareTo("dividend")==0 ) {
					div.setDividend( Double.valueOf(v.getTextTrim()) );
				}
				idx++;
			}
			
			// split
			if( type == 1 ) {
				split.setStockQuote(quote);
				events.add(split);
			} else
			// dividend
			if( type == 2 ) {
				div.setStockQuote(quote);
				events.add(div);
			}
		}	
	}
	
	private boolean stockEventsHas(Class clazz, List<StockEvent> events) {
		for( StockEvent event : events ) {
			if( event.getClass()==clazz )
				return true;
		}
		return false;
	}
}
