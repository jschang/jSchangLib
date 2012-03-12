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

import com.jonschang.investing.model.*;
import com.jonschang.utils.valuesource.*;

/**
 * Provides a the given field value for the last Quote in a set
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class SingleQuoteValueSource<Q extends Quote<I>,I extends Quotable>
	extends AbstractQuoteValueSource<Q,I> {
	
	private int type;
	
	public void setReturnType(int type) {
		this.type=type;
	}
	public int getReturnType() {
		return this.type;
	}
	
	public int getPeriods() {
		return 1;
	}
	
	public double getValue() throws ValueSourceException {
		if( this.quotes.size()<1 )
			throw new TooFewQuotesException("too few quotes");
		return this.quotes.get(this.quotes.size()-1).getField(this.type);
	}
}
