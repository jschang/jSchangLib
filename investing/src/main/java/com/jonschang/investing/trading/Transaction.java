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

import com.jonschang.investing.model.*;

public interface Transaction<S extends Quotable<Q,E>, Q extends Quote<S>, E extends Exchange<S>> {
	
	static int NO_LIMIT = (-1);
	static int NO_STOP = (-1);
	
	enum Status {
		NEW,
		PENDING,
		CANCELED,
		CANCELING,
		EXECUTED
	}
	
	enum Type {
		BUY,
		SELL
	}
	
	void setLimit(double limit);
	double getLimit();
	
	void setStop(double stop);
	double getStop();

	void setStatus(Status status);
	Status getStatus();
	
	void setMessage(String mesg);
	String getMessage();
	
	void setType(Type transType);
	Type getType();
	
	Q getQuote();
	void setQuote(Q quote);
	
	double getQuantity();
	void setQuantity(double qty);
	
	S getQuotable();
	void setQuotable(S quotable);
	
	Account<S,Q,E> getAccount();
	void setAccount(Account<S,Q,E> account);
	
	Position<S,Q,E> getPosition();
	void setPosition(Position<S,Q,E> position);
	
	Long getTransactionId();
	void setTransactionId(Long id);
}
