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

import javax.persistence.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.trading.*;

@Entity @Table(name="stock_transaction")
public class StockTransaction implements Transaction<Stock,StockQuote,StockExchange> {

	private double limit=NO_LIMIT, stop=NO_STOP, quantity=0;
	private StockQuote quote = null;
	private Stock quotable = null;
	private Type type = Type.BUY;
	private Status status = Status.NEW;
	private Account<Stock,StockQuote,StockExchange> account = null;
	private String message = "none";
	private Position<Stock,StockQuote,StockExchange> position = null;
	private Long transactionId;
	
	@Id @Column(name="transaction_id")
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long id) {
		this.transactionId=id;
	}

	@Column(name="limit")
	public double getLimit() {
		return limit;
	}
	public void setLimit(double limit) {
		this.limit = limit;
	}

	@Column(name="quantity")
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double qty) {
		quantity=qty;
	}

	@Column(name="stop")
	public double getStop() {
		return stop;
	}
	public void setStop(double stop) {
		this.stop=stop;
	}
	
	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String mesg) {
		this.message = mesg;
	}

	@Enumerated(EnumType.STRING) @Column(name="type")
	public com.jonschang.investing.trading.Transaction.Type getType() {
		return type;
	}
	public void setType(com.jonschang.investing.trading.Transaction.Type transType) {
		this.type = transType;
	}
	
	@Enumerated(EnumType.STRING) @Column(name="status")
	public com.jonschang.investing.trading.Transaction.Status getStatus() {
		return status;
	}
	public void setStatus(com.jonschang.investing.trading.Transaction.Status status) {
		this.status=status;
	}
	
	@OneToOne @JoinColumn(name="stock_id")
	public Stock getQuotable() {
		return quotable;
	}
	public void setQuotable(Stock quotable) {
		this.quotable = quotable;
	}
	
	@OneToOne @JoinColumn(name="quote_id")
	public StockQuote getQuote() {
		return quote;
	}
	public void setQuote(StockQuote quote) {
		this.quote = quote;
	}

	@ManyToOne(targetEntity=StockTradingAccount.class) @JoinColumn(name="account_id")
	public Account<Stock,StockQuote,StockExchange> getAccount() {
		return account;
	}
	public void setAccount(Account<Stock,StockQuote,StockExchange> account) {
		this.account=account;
	}

	@ManyToOne(targetEntity=StockPosition.class) @JoinColumn(name="position_id")
	public Position<Stock,StockQuote,StockExchange> getPosition() {
		return position;
	}
	public void setPosition(Position<Stock,StockQuote,StockExchange> position) {
		this.position = position;
	}

}
