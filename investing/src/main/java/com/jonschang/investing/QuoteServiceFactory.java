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


import java.util.Map;
import com.jonschang.utils.GenericFactory;

@SuppressWarnings(value={"unchecked"})
public class QuoteServiceFactory extends GenericFactory<Class,QuoteService>
{
	/**
	 * so we can easily spring configure the 
	 * @param svcs
	 */
	public void setQuoteServices(Map<Class,QuoteService> svcs)
		{ m_resourceMap=svcs; }

	/**
	 * a factory for pulling back a quote service by quote class
	 * initial implementation will spring-configure the following:
	 *   StockQuote - YahooStockQuoteService
	 *   HistoricalStockQuote - YahooHistoryStockQuoteService
	 *   CurrencyQuote - YahooCurrencyQuoteService
	 * @param clazz the quote class you want a quote for
	 * @return a quote service
	 * @throws Exception
	 */
	public QuoteService getQuoteService(Class<?> clazz) throws ServiceException 
	{
		try {
			return m_resourceMap.get(clazz);
		} catch( Exception e ) {
			throw new ServiceException("Could not get service for class "+clazz,e);
		}
	}

}