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
package com.jonschang.investing.stocks.trading;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jonschang.investing.Investing;
import com.jonschang.investing.ServiceException;
import com.jonschang.investing.stocks.model.Stock;
import com.jonschang.investing.stocks.model.StockExchange;
import com.jonschang.investing.stocks.model.StockQuote;
import com.jonschang.investing.trading.Position;
import com.jonschang.utils.BusinessCalendar;
import com.jonschang.utils.MathUtils;
import com.jonschang.utils.TimeInterval;

public class SimulatedPlatformTest {
	
	private static double TOL = .001;
	private SimulatedPlatform platform=null;
	private StockTradingAccount account=null;
	private Stock quotable=null;
	private TimeInterval interval=TimeInterval.DAY;

	@Before public void init() throws Exception {
		platform = new SimulatedPlatform();
		
		account = new StockTradingAccount();
		account.setBuyingPower(10000);
		
		Date date = DateFormat.getDateInstance().parse("March 7, 2007 16:00:00");
		platform.setDate(date);
		platform.setInterval(interval);
	}
	
	@Test public void testMarketBuySell() throws Exception {
		
		// day one
		// BUY 100 shares of MSFT
		StockTransaction trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.setDate(getNextDate()); // 3/8
		platform.refresh(account);
		
		StockPosition position = getPosition("FPX");
		Assert.assertEquals( 22.51, position.getBasisCost(), TOL );
		Assert.assertEquals( 100.0, position.getQuantity(), TOL );
		// 100*27.32=2732.0
		Assert.assertEquals( 2244.0, account.getEquity(), TOL );
		// 10000-(100*27.85)-4.95 = 7210.05
		Assert.assertEquals( 7744.05, account.getBuyingPower(), TOL );
		
		// day 2
		// BUY 100 shares of PFE
		trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("PFE") );
		platform.submit(createTransList(trans));
		
		platform.setDate(getNextDate()); // 3/9
		platform.refresh(account);
		
		position = getPosition("PFE");
		Assert.assertEquals(25.52, position.getBasisCost(), TOL );
		Assert.assertEquals(100.0, position.getQuantity(), TOL );
		// (25.41*100)+(27.29*100)=5270
		Assert.assertEquals(4785.0, account.getEquity(), TOL );
		// 7210.05-(25.55*100)-4.95=4650.10
		Assert.assertEquals(5187.1, account.getBuyingPower(), TOL );
		
		// day 3
		// SELL 50 shares of MSFT
		trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.SELL);
		trans.setQuantity( 50 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.setDate(getNextDate()); // 3/12
		platform.refresh(account);
		
		position = getPosition("FPX");
		Assert.assertEquals(22.51, position.getBasisCost(), TOL);
		Assert.assertEquals(50.0, position.getQuantity(), TOL);
		// (50*27.44)+(100*25.38)=3910.0
		Assert.assertEquals(3663.0, account.getEquity(), TOL);
		// 4650.10+(50*27.13)-4.95=6001.65
		Assert.assertEquals(6300.65, account.getBuyingPower(), TOL);
		
		// day 3
		// BUY 25 shares of MSFT
		trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 25 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.setDate(getNextDate());
		platform.refresh(account);
		
		position = getPosition("FPX");
		// TODO: determine if this is accurate
		Assert.assertEquals(22.51, position.getBasisCost(), TOL);
		Assert.assertEquals(75.0, position.getQuantity(), TOL);
		// (75*27.4)+(100*25.38)=3910.0
		Assert.assertEquals(4224.0, account.getEquity(), TOL);
		// 6001.65+(25*27.4)-4.95=5311.70
		Assert.assertEquals(5311.7, account.getBuyingPower(), TOL);
	}
	
	@Test public void testStopBuy() throws Exception {
		
		// day one
		// BUY 100 shares of MSFT
		StockTransaction trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		trans.setStop( 26.7 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// after the first day, verify the transaction has not been executed
		StockPosition position = getPosition("FPX");
		Assert.assertTrue( position==null || position.getTransactions()==null || !position.getTransactions().contains(trans) );
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// the second day, the 8th, MSFT should have sold at the day's high
		position = getPosition("FPX");
		Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
		Assert.assertEquals(27.85, position.getBasisCost(), TOL);
		Assert.assertEquals(100.0, position.getQuantity(), TOL);
		// 100*27.32=2732.0
		Assert.assertEquals(2732.0, account.getEquity(), TOL);
		// 10000-(100*27.85)-4.95 = 7210.05
		Assert.assertEquals(7210.05, account.getBuyingPower(), TOL);
	}
	
	@Test public void testStopLimitBuy() throws Exception {
	
		// day one
		// BUY 100 shares of MSFT
		StockTransaction trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		//trans.setLimit( 27.4 );
		trans.setStop( 26.7 );
		trans.setLimit( 26.8 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// after the first day, verify the transaction has not been executed
		StockPosition position = getPosition("FPX");
		Assert.assertTrue( position==null || position.getTransactions()==null || !position.getTransactions().contains(trans) );
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// the second day, the 8th, MSFT should have bought at our limit price 
		position = getPosition("FPX");
		Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
		Assert.assertTrue( MathUtils.isEqual(position.getBasisCost(), 26.8) );
		Assert.assertTrue( MathUtils.isEqual(position.getQuantity(), 100.0) );
		// 100*27.32=2732.0
		Assert.assertTrue( MathUtils.isEqual(account.getEquity(), 2732.0) );
		// 10000-(100*27.85)-4.95 = 7210.05
		Assert.assertTrue( MathUtils.isEqual(account.getBuyingPower(), 7315.05) );
	}
	
	@Test public void testStopSell() throws Exception {

		// day one
		// BUY 100 shares of MSFT
		StockTransaction trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		// place a stop sell order to trigger when the price dips below 26.7,
		// which it does the next day...down to 26.6
		trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.SELL);
		trans.setStop( 26.7 );
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// the second day, the 8th, MSFT should have sold at the day's high
		StockPosition position = getPosition("FPX");
		Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
		Assert.assertEquals(27.90, position.getBasisCost(), TOL );
		Assert.assertEquals(0.0, position.getQuantity(), TOL );
		Assert.assertEquals(0.0, account.getEquity(), TOL );
		Assert.assertEquals(9870.1, account.getBuyingPower(), TOL );

	}
	
	@Test public void testStopLimitSell() throws Exception {
		
		// day one
		// BUY 100 shares of MSFT
		StockTransaction trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		// place a stop sell order to trigger when the price dips below 26.7,
		// which it does the next day...down to 26.6
		// but also limit the sell to greater than 26.8
		trans = new StockTransaction();
		trans.setAccount(account);
		trans.setType(com.jonschang.investing.trading.Transaction.Type.SELL);
		trans.setStop( 26.8 );
		trans.setLimit( 26.7 );
		trans.setQuantity( 100 );
		trans.setQuotable( getStock("FPX") );
		platform.submit(createTransList(trans));
		
		platform.refresh(account);
		platform.setDate(getNextDate());
		platform.refresh(account);
		platform.setDate(getNextDate());
		
		// the second day, the 8th, MSFT should have sold at the day's high
		StockPosition position = getPosition("FPX");
		Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
		Assert.assertEquals(27.9, position.getBasisCost(), TOL );
		Assert.assertEquals(0.0, position.getQuantity(), TOL );
		Assert.assertEquals(0.0, account.getEquity(), TOL );
		Assert.assertEquals(9870.10, account.getBuyingPower(), TOL );

	}
	
	private StockPosition getPosition(String stock) throws Exception {
		Stock quotable = getStock(stock);
		StockPosition position = null;
		for( Position<Stock,StockQuote,StockExchange> maybe : platform.getPositions(account) ) {
			if( maybe.getQuotable().equals(quotable) && maybe instanceof StockPosition) {
				position=(StockPosition)maybe;
				break;
			}
		}
		return position;
	}
	
	private Stock getStock(String stock) throws ServiceException {
		Stock q = new Stock();
		q.setSymbol(stock);
		return (Stock)Investing.instance().getQuotableServiceFactory().get(Stock.class).get(q);
	}
	
	private Date getNextDate() throws ServiceException {
		BusinessCalendar cal = getStock("FPX").getExchange().getContext().cloneBusinessCalendar();
		cal.setTime(platform.getDate());
		cal.add(interval, 1);
		return cal.getTime();
	}
	
	private List<StockTransaction> createTransList(StockTransaction trans) {
		List<StockTransaction> transList = new ArrayList<StockTransaction>();
		transList.add(trans);
		return transList;
	}
}
