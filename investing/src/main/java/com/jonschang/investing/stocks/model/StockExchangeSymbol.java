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

import com.jonschang.investing.model.GenericSymbol;
import javax.persistence.*;

@Entity @Table(name="stock_exchange_symbol")
@AssociationOverride(name="parent",joinColumns=@JoinColumn(name="parent"))
public class StockExchangeSymbol extends GenericSymbol<StockExchange> {
	@ManyToOne(targetEntity=StockExchange.class)
	@JoinColumn(name="parent",insertable=false,updatable=false)
	public StockExchange getParent() {
		return this.parent;
	}
}
