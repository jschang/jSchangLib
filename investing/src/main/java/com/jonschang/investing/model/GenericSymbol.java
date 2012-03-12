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
package com.jonschang.investing.model;

import javax.persistence.*;

@MappedSuperclass
abstract public class GenericSymbol<E> implements Symbol<E> {
	protected String symbol=null;
	protected E parent=null;
	protected Integer symbolId=null;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getSymbolId() {
		return this.symbolId;
	}
	public void setSymbolId(Integer steid) {
		this.symbolId=steid;
	}
	
	@Column(name="symbol")
	public String getSymbol() {
		return this.symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol=symbol;
	}

	public void setParent(E steid) {
		this.parent=steid;
	}
}
