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

import java.util.ArrayList;
import java.util.List;


public class GenericNeuron implements Neuron {
	
	private List<Synapse> forwardSynapses = new ArrayList<Synapse>();
	private List<Synapse> backwardSynapses = new ArrayList<Synapse>();
	private Activator activator;
	private double inputSum;
	private double activation;
	private double error;
	private double threshold;
	private double lastThreshold;
	private double desiredActivation;
	
	public GenericNeuron()
	{
		this.lastThreshold = 
		this.threshold = Math.random()*.4;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getInputSynapses()
	 */
	public List<Synapse> getInputSynapses() {
		return this.backwardSynapses;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getOutputSynapses()
	 */
	public List<Synapse> getOutputSynapses() {
		return this.forwardSynapses;
	}

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#setActivation(double)
	 */
	public void setActivation(double activation) {
		this.activation = activation;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getActivation()
	 */
	public double getActivation() {
		return this.activation;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getInputSum()
	 */
	public double getInputValue() { 
		return this.inputSum;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#setInputSum(double)
	 */
	public void setInputValue(double inputSum){
		this.inputSum = inputSum;
	}
	
	public void setDesiredActivation(double activation){
		this.desiredActivation = activation;	
	}
	public double getDesiredActivation(){
		return this.desiredActivation;
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#setActivator(com.jonschang.ai.network.activator.Activator)
	 */
	public void setActivator(Activator activator){
		this.activator = activator;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getActivator()
	 */
	public Activator getActivator() {
		return this.activator;
	}

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#setError(double)
	 */
	public void setError(double error) {
		this.error = error;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getError()
	 */
	public double getError() {
		return this.error;
	}

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#setThreshold(double)
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#getThreshold()
	 */
	public double getThreshold() {
		return this.threshold;
	}
	
	public void storeThreshold() {
		this.lastThreshold = this.threshold;
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#restoreThreshold()
	 */
	public void restoreThreshold() { 
		this.threshold = this.lastThreshold; 
	}

	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#createSynapseTo(com.jonschang.ai.network.Neuron)
	 */
	public void createSynapseTo(Neuron neuron) {
		Synapse synapse = new GenericSynapse();
		neuron.addInput(synapse);
		this.addOutput(synapse);
	}
	
	public void setSynapseTo(Synapse synapse, Neuron neuron) {
		neuron.addInput(synapse);
		this.addOutput(synapse);
	}
	
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#addInputSynapse(com.jonschang.ai.network.Synapse)
	 */
	public void addInput(Synapse synapse) {
		if( ! this.backwardSynapses.contains(synapse) )
		{
			synapse.setOutput(this);
			this.backwardSynapses.add( synapse );
		}
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#removeInputSynapse(com.jonschang.ai.network.Synapse)
	 */
	public void removeInput(Synapse synapse) {
		if( this.backwardSynapses.contains(synapse) )
		{
			synapse.setOutput(null);
			this.backwardSynapses.remove( this.backwardSynapses.indexOf(synapse) );
		}
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#removeOutputSynapse(com.jonschang.ai.network.Synapse)
	 */
	public void removeOutput(Synapse synapse) {
		if( this.forwardSynapses.contains(synapse) )
		{
			synapse.setInput(null);
			this.forwardSynapses.remove( this.forwardSynapses.indexOf(synapse) );
		}
	}
	/* (non-Javadoc)
	 * @see com.jonschang.ai.network.Neuron#addOutputSynapse(com.jonschang.ai.network.Synapse)
	 */
	public void addOutput(Synapse synapse) {
		if( ! this.forwardSynapses.contains(synapse) )
		{
			synapse.setInput(this);
			this.forwardSynapses.add(synapse);
		}
	}
}
