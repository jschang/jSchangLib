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
package com.jonschang.investing.stocks;

import com.jonschang.utils.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import java.util.*;
import org.hibernate.*;

public class ReutersFinancialStatementService extends FinancialStatementService
{
	/**
	 * an interval name/interval symbol map for the reuters.com service
	 */
	private Map<String,String> m_statementTypeMap;
	public void setStatementTypeMap(Map<String,String> statementType)
	{ m_statementTypeMap=statementType; }
	
	/**
	 * an exchange symbol/reuters equivalent map
	 */
	private Map<String,String> m_exchangeSymbolMap;
	public void setExchangeSymbolMap(Map<String,String> exchangeSymbolMap)
	{ m_exchangeSymbolMap = exchangeSymbolMap; }
	
	// filingMonths
	private List<Integer> m_filingMonths;
	public void setFilingMonths(List<Integer> months)
	{ m_filingMonths = months; }
	
	/**
	 * the page url template for where we pull financial info from on reuters.com
	 */
	private String m_serviceUrl;
	public void setServiceUrl(String url)
	{ m_serviceUrl = url; }
	
	/**
	 * the interval name to use for this service object
	 */
	private String m_intervalType;
	public void setIntervalType(String intervalType)
	{ m_intervalType = intervalType; }
	
	/** 
	 * a map of interval name/reuters symbol pairs
	 */
	private Map<String,String> m_intervalTypes;
	public void setIntervalTypes(Map<String,String> intervalMap)
	{ m_intervalTypes = intervalMap; }
	
	/**
	 * the plan here is that we'll pull the financials
	 * for the period immediately preceding the start
	 * and all in between that and then end
	 */
	@Override
	public List<Stock> pullDateRange
	(
		Collection<Stock> quotables,
		DateRange range, 
		TimeInterval interval
	) throws ServiceException, HibernateException 
	{
		// adjust date range so end is at most the current day
		Date dateLow = range.getStart();
		Date dateHigh = range.getEnd();
		List<Stock> quotes = null;
		
		// group quotables by stock exchange
		// iterate over quotables and pack into a map indexed by stock exchange
		Map<StockExchange,Set<Stock>> exchangeMap 
			= new HashMap<StockExchange,Set<Stock>>();
		for( Stock stock : quotables )
		{
			// if we haven't categorized the stock by exchange yet,
			// then create a new HashSet for the stocks exchange
			// and add it to the exchangeMap map
			Set<Stock> stockSet = exchangeMap.get(stock.getStockExchange()); 
			if( stockSet == null )
			{
				stockSet = new HashSet<Stock>();
				exchangeMap.put(stock.getStockExchange(),stockSet);
			}
			stockSet.add(stock);
		}
		
		// for each stock exchange
		for( Map.Entry<StockExchange,Set<Stock>> ent : exchangeMap.entrySet() )
		{
			// for each stock
				// attempt to pull range from local db
				// if the end range is
		}
		return null;
	}

	public boolean supports(TimeInterval interval) {
		return false;
	}

	/**
	 * the plan here is that we'll pull the financials
	 * for the period immediately preceding the start
	 * and all in between that and then end
	 */
	@Override
	public List<Stock> pullNumber(
			Collection<Stock> quotables, Date refDate, int number,
			TimeInterval interval) throws ServiceException, HibernateException {
		// TODO Auto-generated method stub
		return null;
	}
}
