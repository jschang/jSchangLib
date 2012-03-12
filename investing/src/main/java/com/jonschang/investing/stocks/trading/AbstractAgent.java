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
package com.jonschang.investing.stocks.trading;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.jonschang.investing.trading.*;
import com.jonschang.investing.model.*;
import com.jonschang.utils.valuesource.ValueSource;
import com.jonschang.utils.*;

abstract public class AbstractAgent<S extends Quotable<Q,E>, Q extends Quote<S>, E extends Exchange<S>>
	implements Agent<S,Q,E> {

	protected ValueSource stopValueSource;
	public ValueSource getStopValueSource() {
		return this.stopValueSource;
	}
	public void getStopValueSource(ValueSource vs) {
		this.stopValueSource = vs;
	}
	
	protected Account<S,Q,E> tradingAccount;
	@SuppressWarnings(value="unchecked")
	public <T extends Account<S,Q,E>> T getAccount() {
		return (T)this.tradingAccount;
	}
	public <T extends Account<S,Q,E>> void setAccount(T account) {
		this.tradingAccount=account;
	}
	
	protected Platform<S,Q,E> tradingPlatform;
	@SuppressWarnings(value="unchecked")
	public <T extends Platform<S,Q,E>> T getPlatform() {
		return (T) tradingPlatform;
	}
	public <T extends Platform<S,Q,E>> void setPlatform(T tradingPlatform) {
		this.tradingPlatform = tradingPlatform;
	}
	
	protected List<S> quotables;
	@SuppressWarnings(value="unchecked")
	public <T extends Quotable<Q, E>> List<T> getQuotables() {
		return (List<T>)quotables;
	}
	@SuppressWarnings(value="unchecked")
	public <T extends Quotable<Q, E>> void setQuotables(List<T> quotables) {
		this.quotables = (List<S>)quotables;
	}
	
	protected Date date;
	public Date getDate() {
		return this.date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	protected DatePublisher datePublisher=null;
	public void setDatePublisher(DatePublisher dp) {
		this.datePublisher = dp;
	}
	public DatePublisher getDatePublisher() {
		return this.datePublisher;
	}
	
	public <T extends Transaction<S,Q,E>> List<T> run() throws AgentException {
		Logger.getLogger(GenericStockAgent.class).info("In AbstractAgent::run()");
		return null;
	}
}
