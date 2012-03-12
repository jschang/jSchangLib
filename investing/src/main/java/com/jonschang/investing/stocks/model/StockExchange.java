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

import java.util.List;
import com.jonschang.investing.model.AbstractExchange;
import javax.persistence.*;

@Entity @Table(name="stock_exchange")
public class StockExchange extends AbstractExchange<Stock> {
	
	/**
	 * @return the stock exchange id
	 */
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="stock_exchange_id")
	public Integer getStockExchangeId()
		{ return m_stockExchangeId; }
	public void setStockExchangeId(Integer sei)
		{ m_stockExchangeId=sei; }
	private Integer m_stockExchangeId=null;
	
	@Transient
	public int getId() { return getStockExchangeId()!=null?getStockExchangeId():-1; }
	
	/**
	 * @return the stock symbol
	 */
	@Column(name="symbol")
	public String getSymbol()
		{ return m_symbol; }
	public void setSymbol(String sym)
		{ m_symbol=sym; }
	private String m_symbol;
	
	/**
	 * @return the stock exchange name
	 */
	@Column(name="name")
	public String getName()
		{ return m_name; }
	public void setName(String sym)
		{ m_name=sym; }
	private String m_name;
	
	/**
	 * @return the stock symbol
	 */
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=StockExchangeSymbol.class)
	public List<StockExchangeSymbol> getSymbols()
		{ return m_symbols; }
	public void setSymbols(List<StockExchangeSymbol> syms)
	{ 
		/*
		if( syms!=null )
			for(StockExchangeSymbol symbol:syms) {
				if(symbol!=null && symbol.getParent()==null)
					symbol.setParent(this);
			}*/
		m_symbols=syms; 
	}
	private List<StockExchangeSymbol> m_symbols;
	
	@Transient
	public List<Stock> getQuotables() {
		return getStocks();
	}
	
	/**
	 * @return a set of stocks belonging to this exchange
	 */
	@OneToMany(mappedBy="stockExchange",fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=Stock.class)
	public List<Stock> getStocks()
	{ return m_stocks; }
	public void setStocks( List<Stock> stocks )
	{ m_stocks = stocks; }
	private List<Stock> m_stocks;
	
	public boolean containsSymbol(String symbol) {
		for( StockExchangeSymbol s : this.m_symbols ) {
			if( s!=null && s.getSymbol()!=null 
				&& s.getSymbol().compareTo(symbol)==0 )
			return true;
		}
		return false;
	}
}
