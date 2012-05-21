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

import com.jonschang.investing.model.*;
import com.jonschang.investing.valuesource.QuoteValueSource;
import com.jonschang.investing.valuesource.TooFewQuotesException;
import com.jonschang.investing.*;
import com.jonschang.utils.*;

/**
 * Publishes a List<Quote> to subscribing QuoteValueSource's whenever the Date changes
 * 
 * Allows QuoteValueSource's pulling information from a variety 
 * of sources to co-exist in the same QuoteValueSourceList,
 * or even in the same QuoteValueSource composite object.
 * 
 * In order to implement a functional QuoteValueSourceList, the 
 * application will need 3 elements:
 *  - QuoteValueSource
 *  - QuotePublisher
 *  - QuoteValueSourceList
 * 
 * @author schang
 *
 * @param <Q>
 * @param <S>
 */
public interface QuotePublisher<Q extends Quote<S>,S extends Quotable> 
		extends HasDatePublisher, HasQuoteAdjusters<Q,S> {
	
	/**
	 * Adds the QuoteValueSource to this QuotePublisher,
	 * also sets the QuotePublisher in the QuoteValueSource to this
	 * @param qvs
	 */
	void subscribe(HasQuotes<Q,S> qvs);
	
	/**
	 * Removes the QuoteValueSource from this QuotePublisher, 
	 * also sets the QuotePublisher in the QuoteValueSource to null
	 * @param qvs
	 */
	void unsubscribe(HasQuotes<Q,S> qvs);
	
	/**
	 * Calls setQuotes() on all subscribing QuoteValueSource's
	 * 
	 * Only pulls new Quote's when the Date is changed from the last update
	 * 
	 * @return true if the date of the last quote matches the date set by the date publisher, else false
	 * @throws TooFewQuotesException
	 * @throws ServiceException
	 */
	boolean updateQuotes() throws TooFewQuotesException, ServiceException;
	
	public S getQuotable();
	public void setQuotable(S quotable);
}
