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
import javax.persistence.*;

@Entity 
@Table(name="account") 
@Inheritance(strategy=InheritanceType.JOINED)
abstract public class AbstractAccount<S extends Quotable<Q,E>,Q extends Quote<S>, E extends Exchange<S>> implements Account<S,Q,E> {

	private double buyingPower;
	private double equity;
	private Long accountId;
	
	@Id @Column(name="account_id")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long acctId) {
		accountId=acctId;		
	}
	
	@Column(name="buying_power")
	public double getBuyingPower() {
		return buyingPower;
	}
	public void setBuyingPower(double cash) {
		buyingPower = cash;
	}

	@Column(name="equity")
	public double getEquity() {
		return equity;
	}
	public void setEquity(double cash) {
		equity = cash;
	}
}