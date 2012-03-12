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
import org.apache.log4j.*;
import org.hibernate.*;
import org.junit.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.xml.*;
import org.springframework.core.io.*;

import com.jonschang.investing.model.*;
import com.jonschang.utils.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.utils.*;

public class ReutersFinancialStatementServiceTest 
{
	private Logger m_logger;
	@Test public void testPullDateRange() throws Exception
	{
		// setup the logger
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		m_logger = Logger.getLogger(this.getClass());
		
		// initialize the application context
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("conf/spring/spring.xml"));
		
		// pull the hibernate session factory in from the application context
		SessionFactory sf = (SessionFactory)bf.getBean("HSF");
		
		@SuppressWarnings(value={"all"})
		QuoteService<FinancialStatement,Stock> qs = (QuoteService) 
			((QuoteServiceFactory)bf.getBean("QuoteServiceFactory"))
			.getQuoteService(FinancialStatement.class);
		
		// create entries in the stock exchange table
		createStockExchanges(sf);
		
		// pull back an exchange factory and get the AMEX exchange
		HasGetObjectByObject ecf = (HasGetObjectByObject)
			(bf.getBean("ExchangeContextFactory"));
		StockExchangeContext sec = (StockExchangeContext)ecf.get("AMEX");
		
		// create the stock we want to fetch data for
		Stock stock = createStocks(sf,sec.getExchange());

		// setup the date range
		java.util.Calendar cal = new java.util.GregorianCalendar();
		cal.set(2008, 3, 1);
		Date date1 = cal.getTime();
		cal.set(2008, 5, 1);
		Date date2 = cal.getTime();

		// create a set of stocks to pull historical quotes for
		FinancialStatement hsq = new FinancialStatement();
		java.util.Set<Stock> stockSet = new HashSet<Stock>();
		stockSet.add(stock);
		stockSet.add( createStock(sf,sec.getExchange(),"Microsoft","MSFT") );
		
		// pull the most recent HistoricalStockQuotes for the passed in set
		List<Stock> range = qs.pullDateRange(stockSet, new DateRange(date1, date2), TimeInterval.YEAR );
		
		// verify that entire range is covered
		// verify that only one entry per stock for each business day
	
		cal.set(2008,2,1);
		date1 = cal.getTime();
		cal.set(2008,9,1);
		date2 = cal.getTime();
		
		range = qs.pullDateRange(stockSet, new DateRange(date1, date2), TimeInterval.YEAR );
		
		// verify that entire range is covered
		// verify that only one entry per stock for each business day
		
	}
	
	private Stock createStocks(SessionFactory sf,StockExchange se) throws Exception
	{
		
		Stock stock = createStock(sf,se,"Pfizer","PFE");
		Assert.assertNotNull(stock.getStockId());		
		return stock;
	}
	
	private Stock createStock(SessionFactory sf,StockExchange se, String companyName,String symbol) throws Exception
	{		 
		// create the stock w/ the info passed in
		Stock stock = new Stock();
		stock.setCompanyName(companyName);
		stock.setSymbol(symbol);
		stock.setStockExchange(se);
		
		Session s = sf.getCurrentSession();
		try
		{
			s.beginTransaction();
			s.saveOrUpdate(stock);
			s.getTransaction().commit();
		} catch( org.hibernate.HibernateException he )
		{
			s.getTransaction().rollback();
			he.printStackTrace();
		}
		return stock;
	}
	
	private void createStockExchanges(SessionFactory sessionFactory)
		throws Exception
	{
		createStockExchange(sessionFactory,"National Association of Securities Dealers Automated Quotation","NASDAQ");
		createStockExchange(sessionFactory,"New York Stock Exchange","NYSE");
		createStockExchange(sessionFactory,"American Stock Exchange","AMEX");
	}
	
	private void createStockExchange(SessionFactory sessionFactory, String name, String symbol)
		throws Exception
	{
		StockExchange se = new StockExchange();
		se.setSymbol(symbol);
		se.setName(name);
		se.setStocks( new ArrayList<Stock>() );
		
		Session s = sessionFactory.getCurrentSession();
		s.beginTransaction();
		s.saveOrUpdate(se);
		s.getTransaction().commit();
	}

}
