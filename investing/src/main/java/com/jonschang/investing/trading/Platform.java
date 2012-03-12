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
package com.jonschang.investing.trading;

import java.util.*;
import com.jonschang.investing.model.*;

public interface Platform<S extends Quotable<Q,E>,Q extends Quote<S>,E extends Exchange<S>> {
	
	List<Position<S,Q,E>> getPositions(Account<S,Q,E> account) 
		throws PlatformException;
	
	List<Transaction<S,Q,E>> getTransactions(Account<S,Q,E> account) 
		throws PlatformException;
		
	/**
	 * 
	 * @param <T> any class extending R
	 * @param transactions the List of Transactions to submit
	 * @return a List of Transactions that could not be submitted or null
	 * @throws PlatformException
	 */
	<T extends Transaction<S,Q,E>> List<T> submit(List<T> transactions) 
		throws PlatformException;
	
	/**
	 * 
	 * @param <T> any class extending R
	 * @param transactions The list of Transactions to cancel
	 * @return a List of Transactions that could not be canceled or null
	 * @throws PlatformException
	 */
	<T extends Transaction<S,Q,E>> List<T> cancel(List<T> transactions)
		throws PlatformException;
	
	<T extends Account<S,Q,E>> void refresh(T acct) throws Exception;
}
