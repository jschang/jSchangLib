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

import java.util.*;
import org.hibernate.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.utils.*;

/**
 * A service to either fetch from WWW or the database information about a given stock
 * 
 * Implementations should make every attempt to fill in "sector" and "industry" information
 *
 * @author schang
 */
abstract public class StockService implements QuotableService<Stock> {
	
	abstract protected Stock pullStockFromWebService(Stock stock) throws ServiceException;
	
	public Stock get(String symbol) throws ServiceException {
		Stock stock = new Stock();
		stock.setSymbol(symbol);
		return get(stock);
	}
	
	public Stock get(Stock quotable) throws ServiceException, HibernateException {
		
		if( quotable.getSymbol()==null )
			throw new IncompleteRequestException("the quotable you pass in must define it's symbol.");
		
		// first attempt to get the stock under the given exchange from the database
		Stock stock = pullStock(quotable);
		
		if( quotable.getStockExchange()!=null
			&& stock.getStockExchange()!=null
			&& quotable.getStockExchange().getSymbol()!=null
			&& quotable.getStockExchange().containsSymbol(stock.getStockExchange().getSymbol())==false )
			throw new NotFoundException("the stock symbol requested \""+stock.getSymbol()+"\" was found under the exchange \""+stock.getStockExchange().getSymbol()+"\". the stock exchange passed in has the symbol \""+quotable.getStockExchange().getSymbol()+"\"");
		
		// failing that, try to find it's overview page on yahoo
		if( stock==null ) {
			stock = pullStockFromWebService(quotable);
			
			// ok, so now that we've got the stock
			// let's go ahead and pull the last 5 years from yahoo
			// so we don't hit their service too often.
			BusinessCalendar endDate = stock.getExchange().getContext().cloneBusinessCalendar();
			endDate.setTime(new Date());
			endDate.normalizeToMinute();
			endDate.closestBefore(TimeInterval.DAY);
			
			BusinessCalendar startDate = (BusinessCalendar)endDate.clone();
			startDate.set(Calendar.YEAR,startDate.get(Calendar.YEAR)-5);
			
			StockQuoteService quoteService;
			try {
				quoteService = (StockQuoteService)Investing.instance().getQuoteServiceFactory().getQuoteService(StockQuote.class);
			} catch(Exception e) {
				throw new ServiceException("an exception was thrown getting the quote service",e);
			}
			Set<Stock> set = new HashSet();
			set.add(stock);
			quoteService.pullDateRange(set, new DateRange(startDate.getTime(),endDate.getTime()), TimeInterval.DAY);
			
			// if the stock is simply not found,
			// throw a QuotableNotFoundException
			if( stock==null )
				throw new NotFoundException("the quotable "+quotable.getSymbol()+" was not found in the Yahoo! service");
		}
		
		// pass the stock determined back
		return stock;
	}
	
	@SuppressWarnings(value={"unchecked"})
	private Stock pullStock(Stock quotable) throws ServiceException, HibernateException {
		
		List<Stock> s=null;
		
		Session session = Investing.instance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		s = session.createQuery("select s from Stock s "
				+"left join fetch s.stockExchange "
				+"where lower(s.symbol)=:symbol ")
				.setString("symbol",quotable.getSymbol().toLowerCase())
				.list();
		session.getTransaction().commit();
		
		if( s!=null && s.size()==1 )
			return s.get(0);
		
		else if( quotable.getCompanyName()!=null ) {
			// ok then, maybe they gave a company name
			session = Investing.instance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			s = session.createQuery("select s from Stock s left join fetch s.stockExchange "
					+"where lower(s.companyName)=:symbol ")
					//+"and lower(s.stockExchange.symbol)=:exchangeSymbol")
					.setString("symbol",quotable.getCompanyName().toLowerCase())
					//.setString("exchangeSymbol",quotable.getExchange().getSymbol().toLowerCase())
					.list();
			session.getTransaction().commit();
			if( s!=null && s.size()==1 )
				return s.get(0);
		}
		return null;
	}
}
