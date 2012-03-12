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

import com.jonschang.utils.*;
import com.jonschang.investing.model.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;

/**
 * a QuoteValueSource TrainingSetSource
 * 
 * Pulls an input/output pairs from QuoteValueSourceLists through a given date range.
 * If a date has only an input or an output, then it is skipped. 
 * 
 * @author schang
 *
 * @param <S> the quotable
 * @param <Q> the quote type of the quotable
 * @param <QS> the quote service handling to the quote type
 */
@SuppressWarnings(value={"unchecked"})
public class QuoteVSTrainingSetSource<Q extends Quote<S>, S extends Quotable>
	extends AbstractValueSourceTrainingSetSource<QuoteValueSourceList<Q,S>, QuoteValueSource<Q,S>>
	implements HasInterval, HasDateRange {
	
	public QuoteVSTrainingSetSource() {
		super();
		inputSources = new QuoteValueSourceList<Q,S>();
		outputSources = new QuoteValueSourceList<Q,S>();
	}

	private DatePublisher datePublisher;
	private DateRange dateRange;
	private TimeInterval quotePeriod;
	
	private List<S> quotables = new ArrayList<S>();	
	public void setQuotables(List<S> quotables) {
		this.quotables = quotables;
	}
	public List<S> getQuotables() {
		return quotables;
	}
	
	private List<QuotePublisher<Q,S>> quotablesPublishers = new ArrayList<QuotePublisher<Q,S>>();
	public void setQuotePublishersToUpdate(List<QuotePublisher<Q,S>> pubs) {
		this.quotablesPublishers = pubs;
	}
	public List<QuotePublisher<Q,S>> getQuotePublishersToUpdate() {
		return quotablesPublishers;
	}
	public void setQuotePublishersQuotable(S currentQuotable) {
		for( QuotePublisher<Q,S> pub : quotablesPublishers ) {
			pub.setQuotable( currentQuotable );
		}
	}
	
	public void setDatePublisher(DatePublisher dp) {
		this.datePublisher = dp;
	}
	public DatePublisher getDatePublisher() {
		return this.datePublisher;
	}
	
	public void setQuoteInterval(TimeInterval interval) {
		this.quotePeriod=interval;
	}
	public TimeInterval getQuoteInterval() {
		return this.quotePeriod;
	}
	public void setInterval(TimeInterval interval) {
		this.setQuoteInterval(interval);
	}
	public TimeInterval getInterval() {
		return this.quotePeriod;
	}
	
	/**
	 * the current date to pull quotes for
	 * this will be the end of the date range passed in to the quoteservice supplied
	 * @param date
	 */
	public void setDateRange(DateRange dateRange) {
		this.dateRange=dateRange;
	}
	public DateRange getDateRange() {
		return dateRange;
	}
	
	/**
	 * only allows one iterator to be instantiated at a time
	 * if an iterator already exists and has not reached it's end
	 * then return is null
	 * @return null if an active iterator exists, else a new iterator
	 */
	public Iterator iterator() {
		try {
			return new QuoteVSTrainingSetIterator();
		} catch( Exception e ) {
			Logger.getLogger(this.getClass()).error(com.jonschang.utils.StringUtils.stackTraceToString(e));
		}  
		return null;
	}
	
	private class QuoteVSTrainingSetPair extends GenericVectorPair {
		QuoteVSTrainingSetPair(MathVector in, MathVector out) {
			super(in,out);
		}
	}
	
	/**
	 * @author schang
	 */
	private class QuoteVSTrainingSetIterator implements TrainingSetSource.Iterator {

		private QuoteVSTrainingSetPair nextPair;
		private Date currentDate;
		private java.util.Iterator<QuotePublisher<Q,S>> pubIter
			= quotablesPublishers.iterator();
		private java.util.Iterator<S> quotablesIter
			= quotables.iterator();
		private S currentQuotable = null;
		
		QuoteVSTrainingSetIterator() throws NetworkException {
			resetDate();
			if( quotablesIter.hasNext() ) {
				updateCurrentQuotable();
			} else throw new NetworkException("QuoteVSTrainingSetSource must be primed with Quotables to pull training data for");
		}	
		
		public boolean hasNext() {		
			
			// determine if we've pulled all quotes up to the end date
			try {
				if( nextPair == null )
					nextPair = pullNext();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(com.jonschang.utils.StringUtils.stackTraceToString(e));
				return false;
			}
			
			if( nextPair == null ) {
				return false;
			} else return true;
		}
		
		public TrainingSetSource.Pair next() {
			try {
				if( nextPair == null )
					nextPair = pullNext();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(com.jonschang.utils.StringUtils.stackTraceToString(e));
			}
			QuoteVSTrainingSetPair nextPair = this.nextPair;
			this.nextPair=null;
			return nextPair;
		}

		public void remove() {}
		
		/**
		 * if a quote for a quotable on the current date passed in is not available
		 * then it iterates to the next prior to return
		 *  
		 * @return
		 * @throws Exception
		 */
		private QuoteVSTrainingSetPair pullNext() throws Exception {			
			MathVector input = null;
			MathVector output = null;
			while( ! currentDate.after(dateRange.getEnd()) ) {
				
				datePublisher.setDate(currentDate);
				datePublisher.updateHasDates();
				
				// build an output vector by evaluating each quote list via the output quotevaluesource's
				input = inputSources.getVector();
				if( input==null ) {
					quotePeriod.add(currentDate);
					continue;
				}
				
				// build an output vector by evaluating each quote list via the output quotevaluesource's
				output = outputSources.getVector();
				if( output==null ) {
					quotePeriod.add(currentDate);
					continue;
				}
				
				// advance to the next expected date for the quote interval
				Logger.getLogger(this.getClass()).trace("Got a TrainingSetSource.Pair for "+currentDate);
				Logger.getLogger(this.getClass()).trace("input: "+input.getData());
				Logger.getLogger(this.getClass()).trace("output: "+output.getData());
				
				quotePeriod.add(currentDate);
				
				if( output!=null && input!=null )
					break;
			}
			if( output==null || input==null ) {
				if( !quotablesIter.hasNext() )
					return null;
				else {
					updateCurrentQuotable();
					return pullNext();
				}
			}
			return new QuoteVSTrainingSetPair(input,output);
		}
		
		private void updateCurrentQuotable() {
			currentQuotable = quotablesIter.next();
			Logger.getLogger(this.getClass()).trace("switching to "+currentQuotable.getSymbol());
			setQuotePublishersQuotable(currentQuotable);
			resetDate();
		}
		
		private void resetDate() {
			currentDate = (Date)dateRange.getStart().clone();
			datePublisher.setDate(currentDate);
			datePublisher.updateHasDates();
		}
	}
}
