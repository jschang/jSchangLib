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
package com.jonschang.investing.stocks.service;

import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.jonschang.investing.ExchangeService;
import com.jonschang.investing.Investing;
import com.jonschang.investing.ServiceException;
import com.jonschang.investing.stocks.model.*;

public class StockExchangeService implements ExchangeService<StockExchange> {
	
	private Map<String,StockExchange> exchanges = new HashMap<String,StockExchange>();
	private boolean exchangesPrimed = false;
	
	public void setExchanges(Map<String,StockExchange> exchanges ) {
		this.exchanges=exchanges;
	}
	public Map<String,StockExchange> getExchanges() {
		return this.exchanges;
	}
	
	/**
	 * called by the Investing singleton so that I can be sure
	 * the database at least has information about NYSE and Nasdaq
	 * 
	 * the StockExchange are pulled from the bean factory
	 * and each is tested for existence...if it's not in there,
	 * then it's inserted.
	 * 
	 * i haven't found a convenient means online of mapping the
	 * exchange listed to a list of exchanges...
	 * with yahoo! on the overview and exchange pages:
	 * - when 'NasdaqGS' appears next to the stock symbol
	 * 'NASDAQ' appears as part of the name on the exchange page
	 * - when 'NYSE' appears next to the stock symbol
	 * 'New York Stock Exchange' appears on the exchange page
	 */
	public StockExchange getExchange(String symbol) throws ServiceException, HibernateException {
		
		if( !this.exchangesPrimed )
			primeExchanges();
		
		Session session = Investing.instance().getSessionFactory().getCurrentSession();
		
		StockExchange exchange=null;
		
		session.beginTransaction();
		exchange = (StockExchange)session
			.createQuery("select se from StockExchange se left join fetch se.symbols where exists ( from StockExchangeSymbol ses where ses.parent=se and lower(ses.symbol)=:symbol )")
			.setString("symbol",symbol.toLowerCase())
			.uniqueResult();
		session.getTransaction().commit();
		
		if( exchange!=null )
			return exchange;
		
		return null;
	}
	
	private void primeExchanges() {
		exchangesPrimed=true;
		for( Map.Entry<String,StockExchange> se : exchanges.entrySet() ) {
			
			StockExchange exchange = se.getValue();
			
			Session session = Investing.instance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			StockExchange sel = (StockExchange)session
				.createQuery("select se from StockExchange se left join fetch se.symbols where se.stockExchangeId=:id")
				.setInteger("id",exchange.getStockExchangeId())
				.uniqueResult();
			session.getTransaction().commit();
			
			if( sel==null ) {
				if( exchange.getSymbols()!=null )
					for( StockExchangeSymbol symbol : exchange.getSymbols() )
						symbol.setParent(exchange);
				session = Investing.instance().getSessionFactory().getCurrentSession();
				session.beginTransaction();
				session.save(exchange);
				session.getTransaction().commit();
			} else {
				boolean updated=false;
				for( StockExchangeSymbol xmlSym : exchange.getSymbols() ) {
					boolean found=false;
					for( StockExchangeSymbol sym : sel.getSymbols() ) {
						if( sym.getSymbol().compareTo(xmlSym.getSymbol())==0 ) {
							found=true;
							break;
						}
					}
					if( !found ) {
						updated=true;
						sel.getSymbols().add( xmlSym );
						xmlSym.setParent( sel );
					}
				}
				if( updated ) {
					session = Investing.instance().getSessionFactory().getCurrentSession();
					session.beginTransaction();
					session.saveOrUpdate(sel);
					session.getTransaction().commit();
				}
			}
		}
	}
	
}
