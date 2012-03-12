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

import java.util.List;
import com.jonschang.investing.model.*;

/**
 * Provides methods for adding and removing QuoteAdjusters from an implementing class
 * @author schang
 */
public interface HasQuoteAdjusters<Q extends Quote<S>, S extends Quotable> {
	void addQuoteAdjuster( QuoteAdjuster<Q,S> adjuster );
	void removeQuoteAdjuster( QuoteAdjuster<Q,S> adjuster );
	List<QuoteAdjuster<Q,S>> getQuoteAdjusters();
	void setQuoteAdjusters(List<QuoteAdjuster<Q,S>> adjusters);
}
