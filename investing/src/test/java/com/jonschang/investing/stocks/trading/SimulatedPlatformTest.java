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

import java.text.*;
import java.util.*;
import org.junit.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.stocks.trading.*;
import com.jonschang.investing.trading.Position;
import com.jonschang.investing.trading.Transaction;
import com.jonschang.investing.trading.Transaction.Type;
import com.jonschang.utils.*;

public class SimulatedPlatformTest {
	
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
	
	@Test public void testMarketBuySell() {
		try {
			// day one
			// BUY 100 shares of MSFT
			StockTransaction trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.setDate(getNextDate());
			platform.refresh(account);
			
			StockPosition position = getPosition("MSFT");
			Assert.assertTrue( position.getBasisCost() == 27.850000381469727 );
			Assert.assertTrue( position.getQuantity() == 100.0 );
			// 100*27.32=2732.0
			Assert.assertTrue( account.getEquity()==2732.0 );
			// 10000-(100*27.85)-4.95 = 7210.05
			Assert.assertTrue( account.getBuyingPower()==7210.0499618530275 );
			
			// day 2
			// BUY 100 shares of PFE
			trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			trans.setQuotable( getStock("PFE") );
			platform.submit(createTransList(trans));
			
			platform.setDate(getNextDate());
			platform.refresh(account);
			
			position = getPosition("PFE");
			Assert.assertTrue( position.getBasisCost() == 25.549999237060547 );
			Assert.assertTrue( position.getQuantity() == 100.0 );
			// (25.41*100)+(27.29*100)=5270
			Assert.assertTrue( account.getEquity()==5270.0 );
			// 7210.05-(25.55*100)-4.95=4650.10
			Assert.assertTrue( account.getBuyingPower()==4650.100038146973 );
			
			// day 3
			// SELL 50 shares of MSFT
			trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.SELL);
			trans.setQuantity( 50 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.setDate(getNextDate());
			platform.refresh(account);
			
			position = getPosition("MSFT");
			Assert.assertTrue( position.getBasisCost() == 27.850000381469727 );
			Assert.assertTrue( position.getQuantity() == 50.0 );
			// (50*27.44)+(100*25.38)=3910.0
			Assert.assertTrue( account.getEquity()==3910.0 );
			// 4650.10+(50*27.13)-4.95=6001.65
			Assert.assertTrue( account.getBuyingPower()==6001.649996185303 );
			
			// day 3
			// BUY 25 shares of MSFT
			trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 25 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.setDate(getNextDate());
			platform.refresh(account);
			
			position = getPosition("MSFT");
			// TODO: determine if this is accurate
			Assert.assertTrue( position.getBasisCost() == 27.700000127156574 );
			Assert.assertTrue( position.getQuantity() == 75.0 );
			// (75*27.4)+(100*25.38)=3910.0
			Assert.assertTrue( account.getEquity()==4498.0 );
			// 6001.65+(25*27.4)-4.95=5311.70
			Assert.assertTrue( account.getBuyingPower()==5311.700005722047 );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Test public void testStopBuy() {
		try {
			// day one
			// BUY 100 shares of MSFT
			StockTransaction trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			trans.setStop( 26.7 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// after the first day, verify the transaction has not been executed
			StockPosition position = getPosition("MSFT");
			Assert.assertTrue( position==null || position.getTransactions()==null || !position.getTransactions().contains(trans) );
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// the second day, the 8th, MSFT should have sold at the day's high
			position = getPosition("MSFT");
			Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
			Assert.assertTrue( position.getBasisCost() == 27.850000381469727 );
			Assert.assertTrue( position.getQuantity() == 100.0 );
			// 100*27.32=2732.0
			Assert.assertTrue( account.getEquity()==2732.0 );
			// 10000-(100*27.85)-4.95 = 7210.05
			Assert.assertTrue( account.getBuyingPower()==7210.0499618530275 );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Test public void testStopLimitBuy() {
		try {
			// day one
			// BUY 100 shares of MSFT
			StockTransaction trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			//trans.setLimit( 27.4 );
			trans.setStop( 26.7 );
			trans.setLimit( 26.8 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// after the first day, verify the transaction has not been executed
			StockPosition position = getPosition("MSFT");
			Assert.assertTrue( position==null || position.getTransactions()==null || !position.getTransactions().contains(trans) );
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// the second day, the 8th, MSFT should have bought at our limit price 
			position = getPosition("MSFT");
			Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
			Assert.assertTrue( position.getBasisCost() == 26.8 );
			Assert.assertTrue( position.getQuantity() == 100.0 );
			// 100*27.32=2732.0
			Assert.assertTrue( account.getEquity()==2732.0 );
			// 10000-(100*27.85)-4.95 = 7210.05
			Assert.assertTrue( account.getBuyingPower()==7315.05 );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Test public void testStopSell() {
		try {
			// day one
			// BUY 100 shares of MSFT
			StockTransaction trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			// place a stop sell order to trigger when the price dips below 26.7,
			// which it does the next day...down to 26.6
			trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.SELL);
			trans.setStop( 26.7 );
			trans.setQuantity( 100 );
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// the second day, the 8th, MSFT should have sold at the day's high
			StockPosition position = getPosition("MSFT");
			Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
			Assert.assertTrue( position.getBasisCost() == 27.899999618530273 );
			Assert.assertTrue( position.getQuantity() == 0.0 );
			Assert.assertTrue( account.getEquity()==0.0 );
			Assert.assertTrue( account.getBuyingPower()==9870.100038146973 );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Test public void testStopLimitSell() {
		try {
			// day one
			// BUY 100 shares of MSFT
			StockTransaction trans = new StockTransaction();
			trans.setAccount(account);
			trans.setType(com.jonschang.investing.trading.Transaction.Type.BUY);
			trans.setQuantity( 100 );
			trans.setQuotable( getStock("MSFT") );
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
			trans.setQuotable( getStock("MSFT") );
			platform.submit(createTransList(trans));
			
			platform.refresh(account);
			platform.setDate(getNextDate());
			platform.refresh(account);
			platform.setDate(getNextDate());
			
			// the second day, the 8th, MSFT should have sold at the day's high
			StockPosition position = getPosition("MSFT");
			Assert.assertTrue( position.getTransactions()!=null && position.getTransactions().contains(trans) );
			Assert.assertTrue( position.getBasisCost() == 27.899999618530273 );
			Assert.assertTrue( position.getQuantity() == 0.0 );
			Assert.assertTrue( account.getEquity()==0.0 );
			Assert.assertTrue( account.getBuyingPower()==9870.100038146973 );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public StockPosition getPosition(String stock) throws Exception {
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
	
	public Stock getStock(String stock) throws ServiceException {
		Stock q = new Stock();
		q.setSymbol(stock);
		return (Stock)Investing.instance().getQuotableServiceFactory().get(Stock.class).get(q);
	}
	
	public Date getNextDate() throws ServiceException {
		BusinessCalendar cal = getStock("MSFT").getExchange().getContext().cloneBusinessCalendar();
		cal.setTime(platform.getDate());
		cal.add(interval, 1);
		return cal.getTime();
	}
	
	public List<StockTransaction> createTransList(StockTransaction trans) {
		List<StockTransaction> transList = new ArrayList<StockTransaction>();
		transList.add(trans);
		return transList;
	}
}
