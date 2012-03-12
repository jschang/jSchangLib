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
import com.jonschang.investing.model.*;
/**
 * Provides a means of adjusting the values in a list of Quote extending objects.
 * 
 * Note, the GenericQuotePublisher fetches a new StockQuote List each new date
 * so the QuoteAdjuster must remember relevant values.
 * 
 * Expect that a QuoteAdjuster is state-full.
 * 
 * @author schang
 * @param <Q>
 */
public interface QuoteAdjuster<Q extends Quote<S>, S extends Quotable> {
	void adjust(S quotable, List<Q> q);
}
