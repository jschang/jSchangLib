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
import org.htmlparser.*;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @deprecated this does not work at all currently
 * @author schang
 */
@Deprecated
public class YahooStockExchangeService extends StockExchangeService {

	@Override
	public StockExchange getExchange(String symbol) throws ServiceException, HibernateException {
		
		// attempt to pull the StockExchange by symbol from the database
		StockExchange exchange = pullStockExchange(symbol);
		
		// if the stock exchange is not found in the database,
		if( exchange!=null )
			return exchange;
		/*
		if( exchange==null ) {
			try {
				// then attempt to pull from Yahoo's list of stock exchanges
				Parser parser = new Parser("http://finance.yahoo.com/exchanges");
				NodeList nodes = parser.parse(null);
				nodes.visitAllNodesWith( 
					new ExchangeExtractorNodeVisitor(name);
				);
			} catch(ParserException e) {
				
			}
		}
		*/
		
		return null;
	}
	
	private class ExchangeExtractorNodeVisitor extends NodeVisitor {
		private String name = "";
		ExchangeExtractorNodeVisitor(String symbol) {
			this.name=symbol;
		}
		@Override
		public void visitTag (Tag tag) {
			if( tag.getTagName().toLowerCase()=="" )
	         System.out.println ("\n" + tag.getTagName () + tag.getStartPosition ());
		}

		@Override
		public void visitStringNode (Text string) {
	         System.out.println (string);
		}
	}
	
	private StockExchange pullStockExchange(String symbol) {
		Session session = Investing.instance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<StockExchange> exchanges = session
			.createQuery("select se from StockExchange se where lower(se.symbol) like :symbol")
			.setString("symbol",symbol.toLowerCase()+'%')
			.list();
		session.getTransaction().commit();
		if( exchanges!=null && exchanges.size()==1 )
			return exchanges.get(0);
		else return null;
	}

}
