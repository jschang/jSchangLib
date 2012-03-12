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
import com.jonschang.utils.valuesource.*;
import com.jonschang.investing.model.*;
import java.util.*;

/**
 * K% component of the Stochastic Oscillator
 * 
 * the Fast Stochastic Oscillator is built from two of these:
 *   - K% = an instance of this
 *   - D% = EMA or SMA smoothed instance of this
 * 
 * the Slow Stochastic Oscillator is build from two of these as well:
 *   - K% = an EMA or SMA smoothed instance of this
 *   - D% = an EMA or SMA smoothed K%
 * 
 * According StockCharts.com, K% crossing D% is the buy-sell indicator
 * You could probably use a DeltaValueSource for a Network input/output
 * 
 * @author schang
 *
 * @param <Q>
 * @param <I>
 */
public class StochasticOscillatorValueSource<Q extends Quote<I>,I extends Quotable> 
	extends AbstractQuoteValueSource<Q,I> {

	private SingleQuoteValueSource<Q,I> highQvs;
	private SingleQuoteValueSource<Q,I> lowQvs;
	private ExtremeValueValueSource<Q,I> highest;
	private ExtremeValueValueSource<Q,I> lowest;
	private QuoteService<Q,I> quoteService;
	private int periods = 15;
	private QuoteValueSource<Q,I> valueSource;
	
	public StochasticOscillatorValueSource() {
		highQvs = new SingleQuoteValueSource<Q,I>();
		highQvs.setReturnType(Quote.HIGH);
		highest = new ExtremeValueValueSource<Q,I>();
		highest.setValueSource(highQvs);
		highest.setDirection(ExtremeValueValueSource.Direction.High);
		highest.setPeriods(periods);
		
		lowQvs = new SingleQuoteValueSource<Q,I>();
		lowQvs.setReturnType(Quote.LOW);
		lowest = new ExtremeValueValueSource<Q,I>();
		lowest.setValueSource(lowQvs);
		lowest.setDirection(ExtremeValueValueSource.Direction.Low);
		lowest.setPeriods(periods);
	}
	
	@Override
	public void setPeriods(int periods) {
		this.periods = periods;
		highest.setPeriods(periods);
		lowest.setPeriods(periods);
	}
	
	public void setValueSource(QuoteValueSource<Q,I> value) {
		valueSource = value;
	}
	
	public int getPeriods() {
		return
			Math.max(valueSource.getPeriods(),
			Math.max(highest.getPeriods(),lowest.getPeriods()));
	}

	public QuoteService<Q,I> getQuoteService() throws ValueSourceException {
		return quoteService;
	}

	public double getValue() throws ValueSourceException {
		List<Q> vQs = valueSource.getQuotes();
		try {
			highest.setQuotes(quotes);
			lowest.setQuotes(quotes);
			valueSource.setQuotes(quotes);
			double low = lowest.getValue();
			double high = highest.getValue();
			double value = valueSource.getValue(); 
			return (value-low)/(high-low);
		} finally {
			valueSource.setQuotes(vQs);
		}
	}

}
