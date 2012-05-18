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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.jonschang.investing.*;
import com.jonschang.investing.stocks.model.*;
import org.htmlparser.*;
import org.htmlparser.nodes.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;
import org.hibernate.*;
import org.apache.log4j.*;

public class YahooStockService extends StockService {

	private static String STOCK_EXCHANGE_URL = "http://finance.yahoo.com/d/quotes.csv?s={SYMBOL}&f=nx";
	
	protected Stock pullStockFromWebService(Stock stock) throws ServiceException {
		 
		String urlStr = STOCK_EXCHANGE_URL.replace("{SYMBOL}", stock.getSymbol());
		try {
			URL url = new URL(urlStr);
			Logger.getLogger(this.getClass()).info("Attempting to pulling stock information from Yahoo! at url "+url);
			String content = ((String)url.getContent()).replace("\"","");
			if( content!=null ) {
				String[] parts = content.split(",");
				stock.setCompanyName(parts[0]);
				
				String exchangeSymbol = parts[1]; 
				
				// if we couldn't find the stock under the symbol
				if( stock.getStockExchange()!=null
					&& stock.getStockExchange().getSymbol()!=null
					&& stock.getStockExchange().getSymbol().compareTo(exchangeSymbol)!=0 )
					throw new NotFoundException("the stock symbol requested \""+stock.getSymbol()+"\" was found under the exchange \""+exchangeSymbol+"\", not \""+stock.getStockExchange().getSymbol()+"\"");
			
				// if the exchange does not have a pkid, then attempt to use the StockExchangeService to discover it
				StockExchange pulledExchange = (StockExchange)Investing
					.instance()
					.getExchangeServiceFactory()
					.get(StockExchange.class)
					.getExchange(exchangeSymbol);
				if( pulledExchange == null )
					throw new NotFoundException("the stock with symbol \""+stock.getSymbol()+"\" was found, but the exchange \""+exchangeSymbol+"\" was not.  do you need to add the exchange to StockExchangeService in exchange-service.xml?");
				else {
					//pulledExchange.getStocks().add(stock);
					stock.setStockExchange(pulledExchange);
				}
				
				// now we need to store the stock to the databased
				Session session = Investing.instance().getSessionFactory().getCurrentSession();
				session.beginTransaction();
				session.saveOrUpdate(stock);
				session.refresh(stock);
				session.getTransaction().commit();
			} else throw new NotFoundException("no stock information found in \""+content+"\" at url "+url);
			Logger.getLogger(YahooStockService.class).info(content);
			
			pullSectorAndIndustryFromYahoo(stock);
			
			return stock;
		} catch( MalformedURLException mue ) {
			throw new ServiceException("Parser threw a MalformedURLException looking for symbol "+stock.getSymbol()+" from Yahoo! at url "+urlStr,mue);
		} catch( IOException pe ) {
			throw new ServiceException("Parser threw a IOException looking for symbol "+stock.getSymbol()+" from Yahoo! at url "+urlStr,pe);
		} 
	}
	
	private void pullSectorAndIndustryFromYahoo(Stock stock) throws ServiceException {
		try {
			boolean saveOrUpdate = false;
			if( stock.getSymbol().charAt(0)!='^' ) {
				String url = "http://finance.yahoo.com/q/pr?s="+stock.getSymbol();
				Logger.getLogger(this.getClass()).info("Pulling sector and industry classification for stock "+stock.getSymbol()+" from Yahoo! at url "+url);
				Parser parser = new Parser(url);
				NodeList sectorNodes = parser.parse(new ExtractTwoColumnRowValue("Sector:"));
				parser.reset();
				NodeList industryNodes = parser.parse(new ExtractTwoColumnRowValue("Industry:"));
				if( sectorNodes.size()>0 && industryNodes.size()>0 ) {
					stock.setIndustry(
						industryNodes.elementAt(0) instanceof LinkTag
						? industryNodes.elementAt(0).getChildren().elementAt(0).toHtml().trim()
						: industryNodes.toHtml().trim()
					);
					stock.setSector(
						sectorNodes.elementAt(0) instanceof LinkTag
						? sectorNodes.elementAt(0).getChildren().elementAt(0).toHtml().trim()
						: sectorNodes.toHtml().trim()
					);
					saveOrUpdate = true;
				} else throw new NotFoundException("Could not find sector and industry information for "+stock.getSymbol());
			} else {
				Logger.getLogger(this.getClass()).info("Skipping attempt to obtain sector and industry for Index quotable "+stock.getSymbol());
				stock.setSector("index");
				stock.setIndustry("index");
				saveOrUpdate = true;
			}
			if( saveOrUpdate ) {
				// now we need to store the stock to the databased
				Session session = Investing.instance().getSessionFactory().getCurrentSession();
				session.beginTransaction();
				session.saveOrUpdate(stock);
				session.getTransaction().commit();
			}
		} catch( ParserException pe ) {
			throw new ServiceException("Parser could not determine sector OR industry for symbol "+stock.getSymbol()+" profile pulled from Yahoo!");
		}
	}		
	
	private class ExtractTwoColumnRowValue implements NodeFilter {
		private String heading; 
		public ExtractTwoColumnRowValue(String heading) {
			this.heading=heading;
		}
		public boolean accept(Node node) {
			if( node instanceof TagNode && node.getParent() instanceof TagNode ) {
				TagNode tag = (TagNode)node;
				if( tag!=null 
					&& tag.getParent()!=null && tag.getParent() instanceof TableColumn
					&& tag.getParent().getParent()!=null 
					&& tag.getParent().getParent() instanceof TableRow 
					&& tag.getParent().getParent().getChildren().size()==2 
					&& tag.getTagName().compareTo("A")==0 ) {
					
					TagNode tagGP = (TagNode)tag.getParent().getParent();
					
					Logger.getLogger(this.getClass()).trace("parsing node name "+tagGP.getTagName());
					
					if( tagGP.getChildren().size()>=2 
						&& tagGP.getChildren().elementAt(0) instanceof TableColumn 
						&& tagGP.getChildren().elementAt(1) instanceof TableColumn
						&& tagGP.getChildren().elementAt(0).getChildren().size()==1
						&& tagGP.getChildren().elementAt(0).getChildren().elementAt(0).toHtml().trim().matches(heading)
						&& tag.getChildren().size()==1 ) {
						String sector = tag.getChildren().elementAt(0).toHtml();
						Logger.getLogger(this.getClass()).trace("found a "+heading+", i think: "+sector);
						return true;
					}
				}
			}
			return false;
		}
	}
	
	private class ExtractNameNodeFilter implements NodeFilter {
		public boolean accept(Node node) {
			if( node instanceof TagNode && node.getParent() instanceof TagNode ) {
				TagNode tag = (TagNode)node;
				TagNode tagParent = (TagNode)node.getParent();
				
				String parentClazz = tagParent.getAttribute("class");
				String clazz = tag.getAttribute("class");
				
				if( clazz!=null && parentClazz!=null
					&& parentClazz.compareToIgnoreCase("yfi_rt_quote_summary")==0 
					&& clazz.compareToIgnoreCase("hd")==0 )
					return true;
			}
			return false;
		}
	}

}
