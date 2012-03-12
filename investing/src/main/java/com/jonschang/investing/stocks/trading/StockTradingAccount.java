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

import java.util.*;

import javax.persistence.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.trading.*;

@Entity @Table(name="stock_account") @PrimaryKeyJoinColumn(name="account_id")
public class StockTradingAccount extends AbstractAccount<Stock,StockQuote,StockExchange> {
	private List<Transaction<Stock,StockQuote,StockExchange>> transactions;
	private List<Position<Stock,StockQuote,StockExchange>> positions;
	
	@OneToMany(mappedBy="account",targetEntity=StockPosition.class)
	public List<Position<Stock,StockQuote,StockExchange>> getPositions() {
		return positions;
	}
	public void setPositions(List<Position<Stock,StockQuote,StockExchange>> positions) {
		this.positions=positions;
	}

	@OneToMany(mappedBy="account",targetEntity=StockTransaction.class)
	public List<Transaction<Stock,StockQuote,StockExchange>> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<Transaction<Stock,StockQuote,StockExchange>> transactions) {
		this.transactions=transactions;
	}
}
