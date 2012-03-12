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

package com.jonschang.ai.network;

import java.util.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import com.jonschang.math.vector.VectorImpl;
import com.jonschang.utils.valuesource.*;
import javax.persistence.*;

@SuppressWarnings(value="unchecked")
@Entity @Table(name="networks") @Inheritance(strategy=InheritanceType.JOINED)
public abstract class AbstractNetwork implements Network {

	protected ValueSourceList<ValueSource> inputs = null;
	protected ValueSourceList<ValueSource> outputs = null;
	protected MathVector lastOutput = new VectorImpl();
	protected List<NetworkObserver> observers = new ArrayList<NetworkObserver>();
	private Long networkId;
	
	@Id @Column(name="network_id")
	public Long getId() {
		return this.networkId;
	}
	public void setId(Long id) {
		this.networkId=id;
	}
	
	public void attach(NetworkObserver networkObserver) {
		if( !this.observers.contains(networkObserver) )
		{
			networkObserver.setNetwork(this);
			this.observers.add(networkObserver);
		}
	}

	public void detach(NetworkObserver networkObserver) {
		if( this.observers.contains(networkObserver) )
		{
			this.observers.remove(networkObserver);
			networkObserver.setNetwork(null);
		}
	}
	
	@Transient
	public List<NetworkObserver> getObservers() {
		return this.observers;
	}
	
	public MathVector calculateResponse(MathVector input)
	{
		for( NetworkObserver observer : this.observers )
			observer.onCalculate();
		return null;
	}
	
	public void calculateResponse() throws Exception {	

		// in order to use this metho
		if( this.inputs!=null ) {
			MathVector response = this.calculateResponse( this.inputs.getVector() );
			this.setLastOutput(response);
			if( outputs!=null && outputs.size()!=response.getData().size() )
				throw new Exception("output ValueSourceList<ValueSource> size does not match response IVector size");
			if( outputs!=null ) {
				int i=0;
				for( Double d : response.getData() ) {
					outputs.get(i).setValue(d);
					i++;
				}
			}			
		} else throw new Exception("need to setup at least an input ValueSourceList<ValueSource>");
	}
	
	@Transient
	public MathVector getLastOutput() {
		return lastOutput;
	}
	protected void setLastOutput(MathVector lastOutput) {
		this.lastOutput=lastOutput;
	}	
	
	@Transient
	public ValueSourceList<ValueSource> getInputs() { 
		return inputs; 
	}
	public void setInputs(ValueSourceList<ValueSource> in) {
		inputs = in; 
	}
	
	@Transient
	public ValueSourceList<ValueSource> getOutputs() { 
		return outputs; 
	}
	public void setOutputs(ValueSourceList<ValueSource> out) { 
		outputs = out;
	}
}
