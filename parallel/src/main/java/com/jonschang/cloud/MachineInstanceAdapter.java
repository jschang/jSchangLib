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

package com.jonschang.cloud;

import java.util.*;

abstract public class MachineInstanceAdapter<C extends CloudService<?>> implements MachineInstance<C> {

	private MachineStatus status;
	private String privateIp;
	private String privateDns;
	private String publicDns;
	private Date startDate;
	
	@Override public void setStartTime(Date date) {
		startDate = (Date)date.clone();
	}
	@Override public Date getStartTime() {
		return (Date)startDate.clone();
	}
	
	@Override public void setPublicDns(String dns) {
		publicDns = dns;
	}
	@Override public String getPublicDns() {
		return publicDns;
	}
	
	@Override public void setPrivateDns(String dns) {
		privateDns = dns;
	}
	@Override public String getPrivateDns() {
		return privateDns;
	}
	
	@Override public void setPrivateIp(String ip) {
		privateIp = ip;
	}
	@Override public String getPrivateIp() {
		return privateIp;
	}
	
	@Override public MachineStatus getStatus() {
		return this.status;
	}
	@Override public void setStatus(MachineStatus status) {
		this.status = status;
	}
	
	public boolean equals(MachineInstance inst) {
		return inst.getStartTime().compareTo(inst.getStartTime())==0;
	}
	public int hashCode() {
		return getStartTime().hashCode();
	}
	
	//public abstract <I extends MachineInstance<C>> I clone() throws CloneNotSupportedException;
	public abstract Object clone() throws CloneNotSupportedException;
}
