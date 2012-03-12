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

import com.jonschang.investing.model.*;
import javax.persistence.*;
import java.util.*;

/**
 * a class for minute-by-minute and hour-by-hour stock quotes
 * @author schang
 */
@Entity @Table(name="stock_quote")
@org.hibernate.annotations.Table(appliesTo = "stock_quote", 
	indexes = { 
		@org.hibernate.annotations.Index(name = "idx_stock_interval_date", columnNames = { "stock_id","intrvl","date" }) 
	}
)
public class StockQuote extends GenericQuote<Stock>
{
	static public int VOLUME=5;
	
	@Override
	@Transient
	public double getField(int fieldNum) {
		if( fieldNum==VOLUME )
			return getVolume();
		return super.getField(fieldNum);
	}
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="sq_id")
	public Long getSqId()
		{ return m_stockQuoteId; }
	public void setSqId(Long sqi)
		{ m_stockQuoteId=sqi; }
	Long m_stockQuoteId = null;
	
	@ManyToOne(targetEntity=Stock.class,fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	@JoinColumn(name="stock_id")
	public Stock getStock()
		{ return this.getQuotable(); }
	public void setStock(Stock stock)
		{ this.setQuotable(stock); }
	
	@Column(name="volume")
	public Long getVolume()
		{ return m_volume; }
	public void setVolume(Long volume)
		{ m_volume = volume; }
	private Long m_volume;
	
	@OneToMany(mappedBy="stockQuote",cascade={CascadeType.ALL},targetEntity=StockEvent.class)
	public List<StockEvent> getStockEvents() 
		{ return this.events; }
	public void setStockEvents(List<StockEvent> events) 
		{ this.events = events; }
	List<StockEvent> events=null;
}
