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
package com.jonschang.investing.stocks.model;

import com.jonschang.investing.Investing;
import com.jonschang.investing.model.GenericExchangeContext;

public class StockExchangeContext extends GenericExchangeContext<StockExchange>
{
	/**
	 * @return the StockExchange this StockExchangeContext is associated with
	 * @note on the first call, it will try to pull the exchange information from the database, elsewise it will return the cached copy
	 */
	@Override
	public StockExchange getExchange() throws Exception
	{
		if( Investing.instance().getSessionFactory()!=null 
				&& m_exchange!=null 
				&& m_exchange.getSymbol()!=null 
		)
		{
			StockExchange m_exchange = (StockExchange)Investing
			.instance()
			.getExchangeServiceFactory()
			.get(StockExchange.class)
			.getExchange(this.m_exchange.getSymbol());
			
			return this.m_exchange; 
		}
		return this.m_exchange; 
	}
}
