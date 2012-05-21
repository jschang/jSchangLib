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

import org.apache.log4j.Logger;

import com.jonschang.investing.model.*;
import com.jonschang.investing.valuesource.TooFewQuotesException;
import com.jonschang.investing.*;
import com.jonschang.utils.*;

/**
 * Updates the Quotes in a HasQuote List using a QuoteService for a given Quoteable and Quote class on a particular date
 * @author schang
 *
 * @param <Q>
 * @param <S>
 */
public class GenericQuotePublisher<Q extends Quote<S>, S extends Quotable> 
		implements QuotePublisher<Q, S> {
	protected Collection<HasQuotes<Q,S>> hasQuotes = new ArrayList<HasQuotes<Q,S>>();
	protected S quotable = null;
	protected Class<?> quoteClass = com.jonschang.investing.model.Quote.class;
	protected TimeInterval timeInterval = null;
	protected Date date = null;
	protected Date lastDate = new Date();
	protected DatePublisher datePublisher = null;
	protected Boolean lastReturn = null;
	protected List<QuoteAdjuster<Q,S>> quoteAdjusters = new ArrayList<QuoteAdjuster<Q,S>>();
	
	public void setDatePublisher(DatePublisher dp) {
		this.datePublisher = dp;
	}
	public DatePublisher getDatePublisher() {
		return this.datePublisher;
	}

	public void setTimeInterval(TimeInterval interval) {
		timeInterval = interval;
	}
	public TimeInterval getTimeInterval() {
		return timeInterval;
	}
	public void setQuoteClass(Class<?> clazz) {
		this.quoteClass = clazz;
	}
	public Class<?> getQuoteClass() {
		return this.quoteClass;
	}
	
	public S getQuotable() {
		return this.quotable;
	}
	public void setQuotable(S quotable) {
		this.quotable = quotable; 
		lastDate=null;
	}

	@Override
	public void subscribe(HasQuotes<Q,S> qvs) {
		if( !hasQuotes.contains(qvs) ) {
			hasQuotes.add(qvs);
			qvs.setQuotePublisher((QuotePublisher<Q,S>)this);
		}
	}
	@Override
	public void unsubscribe(HasQuotes<Q,S> qvs) {
		if( hasQuotes.contains(qvs) ) {
			hasQuotes.remove(qvs);
			qvs.setQuotePublisher(null);
		}
	}
	@Override
	public boolean updateQuotes() throws TooFewQuotesException, ServiceException {
		
		// so each QuoteValueSource can call this
		// without mediation by the QuoteVSTrainingSetSource or QuoteValueSourceList
		if( lastDate!=null && lastDate.getTime()==date.getTime() )
			return lastReturn;
		else lastDate=(Date)date.clone();
		
		QuoteService<Q,S> quoteService = Investing.instance().getQuoteServiceFactory().getQuoteService(quoteClass);
		Collection<S> quotables = new HashSet<S>();
		quotables.add(quotable);
		int maxNum=0, thisNum;
		for( HasQuotes<Q,S> qvs : hasQuotes ) {
			thisNum = qvs.getPeriods();
			maxNum = thisNum > maxNum ? thisNum : maxNum; 
		}
		Logger.getLogger(this.getClass()).trace("Getting "+maxNum+" quotes for "+quotable.getSymbol()+" on "+date);
		List<S> quotes = quoteService.pullNumber(quotables, this.date, -(maxNum+10), timeInterval);
		if( quotes.size() > 0 && quotes.get(0).getQuotes()!=null && quotes.get(0).getQuotes().size()>=maxNum ) {
			Date lastQuoteDate = ((List<Q>)quotes.get(0).getQuotes()).get(quotes.get(0).getQuotes().size()-1).getDate();
			Logger.getLogger(this.getClass()).info("for quotable "+quotable.getSymbol()+", actual last quote date "+lastQuoteDate+", while expected was "+date);
			for( HasQuotes<Q,S> qvs : hasQuotes ) {
				qvs.setQuotes( (List<Q>)quotes.get(0).getQuotes() );
			}
			List<Q> theseQuotes = quotes.get(0).getQuotes();
			if( theseQuotes.get(theseQuotes.size()-1).getDate().compareTo( date ) == 0 )
				lastReturn = true;
			else lastReturn = false;
			for( QuoteAdjuster adjuster : quoteAdjusters )
				adjuster.adjust(quotable,theseQuotes);
		} else throw new TooFewQuotesException("No quotes returned by the QuoteService for "+quoteClass);
		return lastReturn;
	}
	@Override
	public void addQuoteAdjuster(QuoteAdjuster<Q,S> adjuster) {
		quoteAdjusters.add(adjuster);
	}
	@Override
	public List<QuoteAdjuster<Q,S>> getQuoteAdjusters() {
		return this.quoteAdjusters;
	}
	@Override
	public void removeQuoteAdjuster(QuoteAdjuster<Q,S> adjuster) {
		List<Integer> toRem = new ArrayList<Integer>();
		int i = 0;
		for( QuoteAdjuster<Q,S> adj : quoteAdjusters ) {
			if( adj == adjuster )
				toRem.add(i);
		}
		for( Integer remIdx : toRem )
			quoteAdjusters.remove(remIdx);
	}
	@Override
	public void setQuoteAdjusters(List<QuoteAdjuster<Q,S>> adjusters) {
		this.quoteAdjusters = adjusters;
	}
	
	@Override
	public void setDate(Date date) {
		this.date=date;
	}
	@Override
	public Date getDate() {
		return this.date;
	}
}
