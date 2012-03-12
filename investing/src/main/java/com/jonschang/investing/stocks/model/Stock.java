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

import java.util.*;
import com.jonschang.investing.model.*;
import javax.persistence.*;

@Entity @Table(name="stock")
@org.hibernate.annotations.Table(appliesTo = "stock", 
	indexes = { 
		@org.hibernate.annotations.Index(name = "idx_symbol", columnNames = { "symbol" }) 
	}
)
public class Stock implements Quotable<StockQuote,StockExchange>
{
	/**
	 * @return the pk of the stock table
	 */
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="stock_id")
	public Integer getStockId()
		{ return m_stockId; }
	public void setStockId(Integer stockId)
		{ m_stockId=stockId; }
	private Integer m_stockId=null;
	
	@Transient
	public StockExchange getExchange()
		{ return this.getStockExchange(); }
	public void setExchange(StockExchange exchange)
	{ this.setStockExchange(exchange); }
	
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
	 * @return the company name
	 */
	@Column(name="company_name",unique=true)
	public String getCompanyName()
		{ return m_companyName; }
	public void setCompanyName(String companyName)
		{ m_companyName=companyName; }
	private String m_companyName;
	
	@Transient
	public List<StockQuote> getQuotes()
	{ return this.getStockQuotes(); }
	public void setQuotes(List<StockQuote> quotes)
	{ this.setStockQuotes(quotes); }
	
	/**
	 * @return a set of the historical stock quotes pulled for this stock 
	 */
	@OneToMany(mappedBy="stock",fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=StockQuote.class)
	public List<StockQuote> getStockQuotes()
		{ return m_stockQuotes; }
	public void setStockQuotes(List<StockQuote> stockQuotes)
		{ m_stockQuotes = stockQuotes; }
	private List<StockQuote> m_stockQuotes = null;
	
	/**
	 * @return the stock exchange this stock is a member of
	 */
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.ALL},targetEntity=StockExchange.class)
	@JoinColumn(name="stock_exchange_id")
	public StockExchange getStockExchange()
		{ return m_stockExchange; }
	public void setStockExchange(StockExchange se)
		{ m_stockExchange=se; }
	private StockExchange m_stockExchange = null;
	
	@Column(name="sector")
	public String getSector() {
		return this.sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	private String sector;
	
	@Column(name="industry")
	public String getIndustry() {
		return this.industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	private String industry;
	
	/**
	 * so i can use stocks as the key in maps
	 * @param s
	 * @return
	 */
	@Override
	public boolean equals(Object s)
	{
		if( !s.getClass().isAssignableFrom(Stock.class) )
			return false;
		
		Stock t1 = (Stock)s;
		if( m_symbol.compareTo(t1.getSymbol())==0 )
			return true;
		else return false;
	}
	
	@Override
	public int hashCode()
	{
		return ((m_stockId!=null?m_stockId:0) 
			+ (m_stockExchange!=null?m_stockExchange.hashCode():0)
			+ (m_companyName!=null?m_companyName.hashCode():0)
			+ (m_symbol!=null?m_symbol.hashCode():0))/4;
	}

}
