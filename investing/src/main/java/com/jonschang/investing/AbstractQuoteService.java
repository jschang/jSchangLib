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

import com.jonschang.investing.model.Quotable;
import com.jonschang.investing.model.Quote;
import com.jonschang.utils.HasGetObjectByObject;

/**
 * i was originally of the mind that these should pull directly
 * from each given webservice, be it yahoo! or reuters or whatever
 * but now i'm thinking that an updater should run and this service
 * should only pull from the database
 * 
 * @author schang
 *
 * @param <Q> any quote (eg. stock quote, historical stock quote, currency quote)
 * @param <I> any quotable (eg. stock, currency, etc)
 */
abstract public class AbstractQuoteService<Q extends Quote<I>, I extends Quotable> 
	implements QuoteService<Q, I> 
{
}
