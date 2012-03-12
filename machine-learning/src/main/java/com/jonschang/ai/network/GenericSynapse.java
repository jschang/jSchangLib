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

public class GenericSynapse implements Synapse {

	private Neuron inputNeuron = null;
	private Neuron outputNeuron = null;
	private double weight;
	private double lastWeight;
	
	public GenericSynapse()
	{
		this.lastWeight =
		this.weight = Math.random()*0.4;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#setWeight(double)
	 */
	public void setWeight(double weight) {
		this.lastWeight = this.weight;
		this.weight = weight;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#getWeight()
	 */
	public double getWeight() {
		return this.weight;
	}
	
	public void storeWeight() {
		this.lastWeight = this.weight;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#restoreWeight()
	 */
	public void restoreWeight() { 
		this.weight = this.lastWeight; 
	}	

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#setInputNeuron(com.jonschang.ai.network.Neuron)
	 */
	public void setInput(Neuron inputNeuron) {
		this.inputNeuron = inputNeuron;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#getInputNeuron()
	 */
	public Neuron getInput() {
		return this.inputNeuron;
	}

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#setOutputNeuron(com.jonschang.ai.network.GenericNeuron)
	 */
	public void setOutput(Neuron outputNeuron) {
		this.outputNeuron = outputNeuron;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Synapse#getOutputNeuron()
	 */
	public Neuron getOutput() {
		return this.outputNeuron;
	}

}
