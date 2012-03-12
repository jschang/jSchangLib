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

import com.jonschang.investing.Investing;

abstract public class AbstractExchange<Q extends Quotable> implements Exchange<Q> {
	
	public ExchangeContext getContext() {
		return Investing.instance().getExchangeContextFactory().get(this.getSymbol());
	}
	
	/**
	 * so i can use stock exchanges as the key in a map
	 * @param s
	 * @return
	 */
	public <T extends Exchange<? extends Q>> Boolean equals(T s)
	{
		if( getSymbols()!=null
			&& s.getSymbol()!=null 
			&& getSymbol().compareTo(s.getSymbol())==0 
			&& getName().compareTo(s.getName())==0 )
			return true;
		else return false;
	}
	
	public <T extends Exchange<? extends Q>> Boolean compareTo(T s)
	{ return equals(s); }
	
	@Override
	public int hashCode()
	{
		return ((getId()!=(-1)?getId():0)
			+ (getName()!=null?getName().hashCode():0)
			+ (getSymbol()!=null?getSymbol().hashCode():0))/4;
	}
}
