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
package com.jonschang.investing;

import java.util.*;

import com.jonschang.investing.model.Quotable;
import com.jonschang.investing.model.Quote;

public interface HasQuotes<Q extends Quote<S>, S extends Quotable> {
	public void setQuotes(List<Q> quotes);
	public List<Q> getQuotes(); 
	public void setQuotePublisher(QuotePublisher<Q,S> pub);
	public QuotePublisher<Q,S> getQuotePublisher();
	public int getPeriods();
}
