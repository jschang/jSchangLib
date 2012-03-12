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

import java.util.List;

@SuppressWarnings(value={"unchecked"})
public interface Exchange<Q extends Quotable> {
	List<? extends Symbol> getSymbols();
	String getSymbol();
	String getName();
	int getId();
	List<Q> getQuotables();
	ExchangeContext getContext();
	
	<T extends Exchange<? extends Q>> Boolean equals(T s);
	int hashCode();
}
