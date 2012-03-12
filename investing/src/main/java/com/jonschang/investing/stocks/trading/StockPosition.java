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

import java.util.List;
import javax.persistence.*;
import com.jonschang.investing.trading.*;
import com.jonschang.investing.stocks.model.*;

@Entity @Table(name="stock_position")
public class StockPosition implements Position<Stock,StockQuote,StockExchange> {

	private Double basisCost=null;
	private Integer quantity=null;
	private Stock quotable;
	private Long positionId;
	private Account<Stock,StockQuote,StockExchange> account=null;
	private List<Transaction<Stock,StockQuote,StockExchange>> transactions=null;
	
	@Id @Column(name="position_id")
	public Long getPositionId() {
		return positionId;
	}
	public void setPositionId(Long id) {
		this.positionId=id;
	}
	
	@Column(name="basis_cost")
	public Double getBasisCost() {
		return basisCost;
	}
	public void setBasisCost(Double cost) {
		basisCost = cost;
	}
	
	@Column(name="quantity")
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer qty) {
		quantity = qty;
	}
	
	@ManyToOne(targetEntity=StockTradingAccount.class) @JoinColumn(name="account_id")
	public Account<Stock,StockQuote,StockExchange> getAccount() {
		return account;
	}
	public void setAccount(Account<Stock,StockQuote,StockExchange> account) {
		this.account = account;
	}
	
	@OneToMany(mappedBy="position",targetEntity=StockTransaction.class)
	public List<Transaction<Stock,StockQuote,StockExchange>> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<Transaction<Stock,StockQuote,StockExchange>> transactions) {
		this.transactions = transactions;
	}

	@OneToOne @JoinColumn(name="stock_id")
	public Stock getQuotable() {
		return quotable;
	}
	public void setQuotable(Stock quotable) {
		this.quotable = quotable;
	}

	public boolean equals(Stock stock) {
		return quotable.equals(stock);
	}
	
}
