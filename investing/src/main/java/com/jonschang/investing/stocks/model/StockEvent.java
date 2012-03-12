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

import javax.persistence.*;

@Entity @Table(name="stock_quote_event") 
@Inheritance(strategy=InheritanceType.JOINED)
abstract public class StockEvent {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO) 
	@Column(nullable=false)
	public long getId() {
		return this.id;
	}
	public void setId(long id) {
		this.id = id;
	}
	private long id;
	
	@ManyToOne(targetEntity=StockQuote.class,fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	public StockQuote getStockQuote() {
		return this.stockQuote;
	}
	public void setStockQuote(StockQuote quote) {
		this.stockQuote = quote;
	}
	private StockQuote stockQuote=null;
}
