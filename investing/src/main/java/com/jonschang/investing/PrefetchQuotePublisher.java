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

import java.util.*;
import org.apache.log4j.*;
import com.jonschang.investing.*;
import com.jonschang.investing.model.*;
import com.jonschang.investing.*;
import com.jonschang.utils.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.valuesource.QuoteValueSource;
import com.jonschang.investing.valuesource.TooFewQuotesException;

public class PrefetchQuotePublisher<Q extends Quote<S>, S extends Quotable> extends GenericQuotePublisher<Q,S> {
	protected DateRange dateRange;
	protected List<Q> quotes = null;
	protected Map<Date,Integer> dateToIndexMap = null;
	protected int periods = 0;
	protected boolean doPrefetch = true;
	protected HasDateListIndexMap dateIndexMap = null;
	private Boolean lastReturn = null;
	
	public void setQuotable(S quotable) {
		super.setQuotable(quotable);
		quotes = new ArrayList<Q>();
		dateToIndexMap = new HashMap<Date,Integer>();
	}
	
	/**
	 * So we can fall-back to the "fetch each update" behavior easily
	 * @param doPrefetch true to prefetch the DateRange, false to fetch each update
	 */
	public void setPrefetch(boolean doPrefetch) {
		this.doPrefetch=doPrefetch;
	}
	public Boolean getPrefetch() {
		return doPrefetch;
	}
	public void setDateRange( DateRange range ) {
		quotes = new ArrayList<Q>();
		dateToIndexMap = new HashMap<Date,Integer>();
		dateRange = range;
	}
	public DateRange getDateRange() {
		return dateRange;
	}
	@Override
	public boolean updateQuotes() throws TooFewQuotesException, ServiceException {
		
		if( ! doPrefetch ) {
			return super.updateQuotes();
		}
		
		if( quotes == null )
			throw new ServiceException("You must specify a DateRange before using PrefetchQuotePublisher");
		
		// if we haven't already, fetch the entire date range, plus the number of quotes needed for the first date
		if( quotes.size()==0 ) {
			pullDateRange();
		}
		
		if( quotes==null || quotes.size()==0 ) {
			lastReturn = null;
			return lastReturn;
		}
		
		// so each QuoteValueSource can call this
		// without mediation by the QuoteVSTrainingSetSource or QuoteValueSourceList
		if( lastDate!=null && lastDate.getTime()==date.getTime() )
			return lastReturn;
		else lastDate=(Date)date.clone();
		
		// determine what chunk of the cached quotes to pull
		Integer i = dateIndexMap.getClosestIndexOfOrBefore( date );
		List<Q> theseQuotes = null;
		if( i==null ) { 
			// if no quote date matched, then just return the last quote we have
			throw new TooFewQuotesException("The date requested must be outside the range of dates contained in the list");
		} else theseQuotes = quotes.subList(i-periods, i+1);
		Logger.getLogger(this.getClass()).trace("on date "+date+", quote list is from "+theseQuotes.get(0).getDate()+" to "+theseQuotes.get(theseQuotes.size()-1).getDate());
		for( HasQuotes<Q,S> qvs : hasQuotes ) {
			qvs.setQuotes(theseQuotes);
		}
		if( theseQuotes.get(theseQuotes.size()-1).getDate().compareTo( date ) == 0 )
			lastReturn = true;
		else lastReturn = false;		
		return lastReturn;
	}
	
	private void pullDateRange() throws TooFewQuotesException, ServiceException {
		Collection<S> quotables = new HashSet<S>();
		quotables.add(quotable);
		
		// determine the maximum number fo quotes we'll need among all QuoteValueSources
		int maxNum=0, thisNum;
		for( HasQuotes<Q,S> qvs : hasQuotes ) {
			thisNum = qvs.getPeriods();
			maxNum = thisNum > maxNum ? thisNum : maxNum; 
		}
		periods = maxNum;
		
		QuoteService<Q,S> quoteService = Investing.instance().getQuoteServiceFactory().getQuoteService(quoteClass);
		
		Logger.getLogger(this.getClass()).trace("Getting "+maxNum+" quotes for "+quotable.getSymbol()+" on "+dateRange.getStart());
		
		List<S> preStartQuotables = quoteService.pullNumber(quotables, dateRange.getStart(), -(maxNum+10), timeInterval);
		if( preStartQuotables==null || preStartQuotables.size()!=1 )
			throw new TooFewQuotesException("No quotables were returned from "+quoteService.getClass().getSimpleName());
		this.quotes = preStartQuotables.get(0).getQuotes();
		
		// fetch the entire date range, plus the number of quotes needed for the first date
		List<S> fullRangeQuotables = quoteService.pullDateRange(quotables, dateRange, timeInterval);
		if( fullRangeQuotables==null || fullRangeQuotables.size()!=1 )
			throw new TooFewQuotesException("For the date range, no quotables were returned from "+quoteService.getClass().getSimpleName());
		List<Q> fullRangeQuotes = fullRangeQuotables.get(0).getQuotes();
		
		this.quotes.addAll(fullRangeQuotes);
		Collections.sort(this.quotes);
		
		// we only run the quote adjuster on the initial fetch,
		// running it on each publish would result in quotes getting
		// updated multiple times
		for( QuoteAdjuster adjuster : quoteAdjusters )
			adjuster.adjust(quotable,quotes);
		
		dateIndexMap = new HasDateListIndexMap(quotes);
	}
}
