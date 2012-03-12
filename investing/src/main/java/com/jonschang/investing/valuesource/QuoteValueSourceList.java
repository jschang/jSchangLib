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
package com.jonschang.investing.valuesource;

import java.util.*;
import com.jonschang.utils.valuesource.*;
import com.jonschang.investing.QuotePublisher;
import com.jonschang.investing.model.*;
import com.jonschang.math.vector.*;
import com.jonschang.utils.*;

/**
 * Aggregates the values from a set of QuoteValueSources into an IVector
 * 
 * If no values are pull, then the IVector returned by getVector() is null
 * 
 * @author schang
 * @param <V>
 */
public class QuoteValueSourceList<Q extends Quote<S>, S extends Quotable> 
	extends ValueSourceList<QuoteValueSource<Q,S>>
	implements HasDatePublisher {
	
	protected Date date=null;
	private QuotePublisher<Q,S> publisher=null;
	private DatePublisher datePublisher=null;
	private boolean dateMustMatch = true;
	
	public boolean equals(QuoteValueSourceList<Q,S> o) {
		return o.toString().compareTo( this.toString() )==0;
	}
	
	/**
	 * Enables the client to specify whether the date of the last quote
	 * must match the date set by the client or not.
	 * 
	 * May be useful if varying intervals are used.
	 * 
	 * Defaults to true
	 *  
	 * @param dmm true if the last quote date must match the date, else false
	 * @return this instance
	 */
	public QuoteValueSourceList<Q,S> setDateMustMatch(boolean dmm) {
		dateMustMatch = dmm;
		return this;
	}
	public boolean getDateMustMatch() {
		return dateMustMatch;
	}
	
	/**
	 * Set the QuotePublisher for QuoteValueSource's in this List
	 * This is a convenience method.
	 * 
	 * @param publisher
	 */
	public void setQuotePublisher(QuotePublisher<Q,S> publisher) {
		// we want to make sure that value sources existing in the list
		// get their publisher updated.
		if( publisher!=null ) {
			for( QuoteValueSource<Q,S> qvs : this ) {
				this.publisher.subscribe(qvs);
			}
		}
		// if the publisher is being set to null,
		// then we want the value sources publishers set to null
		else {
			for( QuoteValueSource<Q,S> qvs : this ) {
				this.publisher.unsubscribe(qvs);
			}
		}
		this.publisher=publisher;
	}
	
	public QuotePublisher<Q,S> getQuotePublisher() {
		return this.publisher;
	}
	
	/**
	 * Add a QuoteValueSource, subscribing it to the current QuotePublisher
	 * If no QuotePublisher is set, then no subscription is made
	 */
	@Override
	public boolean add(QuoteValueSource<Q,S> valueSource) {
		boolean toRet = super.add(valueSource);
		if( toRet && publisher!=null )
			publisher.subscribe(valueSource);
		return toRet;			
	}
	
	/**
	 * Add a QuoteValueSource, un-subscribing it from the current QuotePublisher
	 * If no QuotePublisher is set, then no un-subscribing is done
	 */
	public void remove(QuoteValueSource<Q,S> valueSource) {
		super.remove(valueSource);
		if( publisher!=null )
			publisher.unsubscribe(valueSource);
	}
	
	public void setDate(Date cal) {
		this.date = cal;
	}
	public Date getDate() {
		return this.date;
	}
	public void setDatePublisher(DatePublisher dp) {
		this.datePublisher = dp;
	}
	public DatePublisher getDatePublisher() {
		return this.datePublisher;
	}
	
	/**
	 * If no values are pull, then the IVector returned by getVector() is null
	 * 
	 * @return An ordered vector of the return values of List<QuoteValueSource>, null if the last quote date doesn't match the date published
	 */
	@Override
	public MathVector getVector() throws Exception {
		// build an input vector by evaluating each quote list via the input quotevaluesource's
		MathVector input = new com.jonschang.math.vector.VectorImpl();
		for( QuoteValueSource<Q,S> qvs : this ) {
			
			// this is safe because the QuotePublisher will 
			// only update the QuoteValueSource's subscribing
			// if the last date used has changed
			try {
				qvs.getQuotePublisher().updateQuotes();
			} catch(TooFewQuotesException e) {
				return null;
			}
			Date thisDate = qvs.getQuotes().get(qvs.getQuotes().size()-1).getDate();
			if( dateMustMatch && thisDate.getTime()!=date.getTime() )
				return null;
			input.getData().add(qvs.getValue());
		}
		return input;
	}
}
