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

import com.jonschang.investing.*;
import com.jonschang.investing.model.*;
import com.jonschang.utils.valuesource.*;

/**
 * The Directional Index of a set of Quotes
 * 
 * where:
 *   - DI+ = an EMA smoothed +DirectionalMovement
 *   - DI- = an EMA smoothed -DirectionalMovement
 *   - ATR = an EMA smoothed TrueRange
 * return is ( [(DI+)/ATR] - [(DI-)/ATR] ) / ( [(DI+)/ATR] + [(DI-)/ATR] )
 * 
 * To get an ADX, just use one of the smoothing functions (EMA or SMA)
 * 
 * Formula found at {@link http://en.wikipedia.org/wiki/Average_Directional_Index}
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class DirectionalIndexValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {

	private EMAValueSource<Q,I> posDIVS = new EMAValueSource<Q,I>();
	private EMAValueSource<Q,I> negDIVS = new EMAValueSource<Q,I>();
	private EMAValueSource<Q,I> atrEMAVS = new EMAValueSource<Q,I>();
	private DirectionalMovementValueSource<Q,I> posDMVS = new DirectionalMovementValueSource<Q,I>();
	private DirectionalMovementValueSource<Q,I> negDMVS = new DirectionalMovementValueSource<Q,I>().setDirection(DirectionalMovementValueSource.Direction.Negative);
	private TrueRangeValueSource<Q,I> atrVS = new TrueRangeValueSource<Q,I>();
	
	public DirectionalIndexValueSource() {
		posDIVS.setValueSource(posDMVS);
		negDIVS.setValueSource(negDMVS);
		atrEMAVS.setValueSource(atrVS);
	}
	
	@Override
	public void setPeriods(int qpr) {
		posDIVS.setPeriods(qpr);
		negDIVS.setPeriods(qpr);
		atrEMAVS.setPeriods(qpr);
	}
	public int getPeriods() {
		// these are all the same, so any one should be good
		return atrEMAVS.getPeriods();
	}
	
	QuoteService<Q,I> quoteService;
	public DirectionalIndexValueSource<Q,I> setQuoteService(QuoteService<Q,I> service) {
		quoteService = service;
		return this;
	}
	public QuoteService<Q,I> getQuoteService() throws ValueSourceException {
		return quoteService;
	}

	public double getValue() throws ValueSourceException {
		posDIVS.setQuotes(quotes);
		negDIVS.setQuotes(quotes);
		atrEMAVS.setQuotes(quotes);
		double atrAvg = atrEMAVS.getValue();
		double posDI = ( posDIVS.getValue()/atrAvg );
		double negDI = ( negDIVS.getValue()/atrAvg );
		return (Math.abs(posDI-negDI)/(posDI+negDI));
	}

}
