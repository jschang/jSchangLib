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

@Entity @Table(name="stock_quote_event_insider_transaction")
public class StockEventInsiderTransaction extends StockEvent {

	@Column(name="name",length=80)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name=name;
	}
	private String name = null;
	
	@Column(name="title",length=80)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	private String title = null;
	
	@Column(name="`transaction`",length=255)
	public String getTransaction() {
		return transaction;
	}
	public void setTransaction(String trans) {
		this.transaction=trans;
	}
	private String transaction = null;
}
