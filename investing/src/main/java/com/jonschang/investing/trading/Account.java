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

public interface Account<S extends Quotable<Q,E>, Q extends Quote<S>, E extends Exchange<S>> {
	void setBuyingPower(double cash);
	double getBuyingPower();
	void setEquity(double cash);
	double getEquity();
	List<Transaction<S,Q,E>> getTransactions();
	void setTransactions(List<Transaction<S,Q,E>> transactions);
	List<Position<S,Q,E>> getPositions();
	void setPositions(List<Position<S,Q,E>> positions);
	Long getAccountId();
	void setAccountId(Long acctId);
}
