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
package com.jonschang.investing.model;

import java.util.Date;

import com.jonschang.utils.BusinessCalendar;

public class GenericExchangeContext<E extends Exchange> implements ExchangeContext
{
	
	/**
	 * a factory method for pulling back new instances of the business calendar
	 * @return the business calendar of the particular exchange
	 */
	public BusinessCalendar newBusinessCalendar()
	{ return this.cloneBusinessCalendar(); }
	
	/**
	 * @return the exchange associated with this exchange context
	 */
	public E getExchange() throws Exception
		{ return m_exchange; }
	public void setExchange(E e)
		{ m_exchange = e; }
	protected E m_exchange;
	
	private BusinessCalendar businessCalendar;
	
	public void setBusinessCalendar(BusinessCalendar calendar)
	{ this.businessCalendar = calendar; }
	public BusinessCalendar cloneBusinessCalendar()
	{ return (BusinessCalendar)businessCalendar.clone(); }
	
	/**
	 * @return the current date used for all operations; so that we can easily benchmark algorithms against historical market data 
	 */
	public Date getCurrentDate()
		{ return businessCalendar.getTime(); }
	public void setCurrentDate(Date date)
		{ businessCalendar.setTime(date); }
}
