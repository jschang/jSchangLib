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

import java.util.List;
import com.jonschang.utils.*;
import com.jonschang.investing.*;
import com.jonschang.investing.model.*;

@SuppressWarnings(value={"unchecked"})
abstract public class AbstractQuoteValueSource<Q extends Quote<I>,I extends Quotable> 
	implements QuoteValueSource<Q,I>, HasQuotes<Q,I> {
	
	protected List<Q> quotes = null;
	protected TimeInterval interval = null;
	protected QuotePublisher<Q,I> quotePublisher = null;
	
	public boolean equals(AbstractQuoteValueSource<Q,I> o) {
		return o.toString().compareTo( this.toString() )==0;
	}
	
	public void setPeriods(int qrp) {}
	
	public void setQuotePublisher(QuotePublisher<Q,I> publisher) {
		quotePublisher = publisher;
	}
	public QuotePublisher<Q,I> getQuotePublisher() {
		return quotePublisher;
	}
	
	/**
	 * set the quotes the valuesource may use for determining it's value
	 */
	public void setQuotes(List<Q> quotes) {
		this.quotes = quotes;
	}
	
	public List<Q> getQuotes() {
		return this.quotes;
	}
	
	/**
	 * you may not set a value for quote value sources 
	 */
	final public void setValue(double d) {}
	/*
	public AbstractQuoteValueSource<Q,I> setQuoteInterval(TimeInterval interval) {
		this.interval = interval;
		return this;
	}
	public TimeInterval getQuoteInterval() {
		return interval;
	}*/
}
