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

import com.jonschang.math.vector.*;
import com.jonschang.math.vector.VectorImpl;
import com.jonschang.investing.model.*;
import java.util.*;

/**
 * Aggregate of any number of QuoteValueSourceList's IVector outputs into a single IVector
 * 
 * Justification: There are frequent times where the Vector output of two or more QuoteValueSourceList's
 * must be presented together as a single Vector to another class.
 *  
 * @author schang
 *
 * @param <V>
 */
public class AggregateQuoteValueSourceList<Q extends Quote<S>, S extends Quotable> extends QuoteValueSourceList<Q,S> {
	
	private List<QuoteValueSourceList<Q,S>> valueSourceLists = new ArrayList<QuoteValueSourceList<Q,S>>();
	
	public boolean add(QuoteValueSourceList<Q,S> v) {
		valueSourceLists.add(v);
		return true;
	}
	public boolean remove(QuoteValueSourceList<Q,S> v) {
		for( int i=0; i<valueSourceLists.size(); i++ ) {
			if( valueSourceLists.get(i)==v ) {
				valueSourceLists.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the combined IVector of all QuoteValueSourceList::getVector() results
	 * 
	 * If any QuoteValueSourceList::getVector() returns null, then the return is null
	 * 
	 * @return an combined IVector of all QuoteValueSourceList::getVector() result, null if any return null 
	 */
	@Override
	public MathVector getVector() throws Exception {
		MathVector toRet = new VectorImpl();
		MathVector thisValue = new VectorImpl();
		for( QuoteValueSourceList<Q,S> qvsl : valueSourceLists ) {
			thisValue = qvsl.getVector();
			if( thisValue==null )
				return null;
			else toRet.getData().addAll(thisValue.getData());
		}
		return toRet;
	}
}
