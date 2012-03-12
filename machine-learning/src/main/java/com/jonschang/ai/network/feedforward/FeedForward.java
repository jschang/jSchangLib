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

package com.jonschang.ai.network.feedforward;

import java.util.*;
import javax.persistence.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import com.jonschang.utils.*;
import java.io.*;

@Entity @Table(name="feedforward_network")
public class FeedForward extends AbstractNetwork {
	
	public FeedForward() {}
	
	public FeedForward(Activator activator, int... layers) {
		for( int i : layers )
			this.addLayer(i, activator);
	}
	
	static public FeedForward create(Activator activator, int... layers) {
		return new FeedForward(activator, layers);
	}
	
	private List<Neuron> inputNeurons = new ArrayList<Neuron>();
	private List<Neuron> outputNeurons = new ArrayList<Neuron>();
	
	/**
	 * it's really not practical to try and pull this from the neurons themselves
	 * better to just keep track as we add layers
	 */
	private List<List<Neuron>> allLayers = new ArrayList<List<Neuron>>();

	@Column(name="xml") @Lob
	public String getXml() throws XmlException {
		CharArrayWriter cw = new CharArrayWriter();
		FeedForwardXmlMarshaller m = new FeedForwardXmlMarshaller();
		m.marshal(this, cw);
		return cw.toString();
	}
	public void setXml(String xml) throws XmlException {
		StringReader sr = new StringReader(xml);
		FeedForwardXmlUnmarshaller m = new FeedForwardXmlUnmarshaller();
		m.unmarshal(this, sr);
	}
	
	@Transient
	public List<Neuron> getInputNeurons()
	{ return this.inputNeurons; }
	
	@Transient
	public List<Neuron> getOutputNeurons()
	{ return this.outputNeurons; }
	
	@Transient
	public List<List<Neuron>> getAllLayers()
	{ return this.allLayers; }
	
	/**
	 * Adds a fully connected layer to the network.
	 * 
	 * The last layer added becomes the output layer,
	 * while the first added is the input layer
	 * 
	 * @param number The number of Neurons in the layer
	 * @param activator The activation function to use
	 */
	public void addLayer(int number, Activator activator)
	{
		ArrayList<Neuron> newOutputs = new ArrayList<Neuron>();
		
		if( this.inputNeurons == null )
			this.inputNeurons = new ArrayList<Neuron>();
		
		// create the number of neurons desired, attaching the activator function
		// and creating a forward synapse from each outputNeuron to each new Neuron
		for( int i=0; i<number; i++ )
		{
			Neuron newNeuron = new GenericNeuron();
			newNeuron.setActivator(activator);
			if( this.outputNeurons.size()>0 )
				for( Neuron n : this.outputNeurons )
					n.createSynapseTo(newNeuron);
			newOutputs.add(newNeuron);
		}
		
		this.allLayers.add(newOutputs);		
		this.outputNeurons = newOutputs;
		if( this.inputNeurons.size()==0 )
			this.inputNeurons = this.allLayers.get(0);
	}
	
	@Override
	public MathVector calculateResponse(MathVector input) {
			
		if( input == null )
			return null;
		
		com.jonschang.math.vector.VectorImpl result = new com.jonschang.math.vector.VectorImpl();
		
		// set the activation of the input neurons
		for( int j=0; j<this.inputNeurons.size(); j++ )
		{
			Neuron n = this.inputNeurons.get(j);
			n.setActivation( input.valueOf(j) );
		}
		
		// now calculate the activations of each layer
		Double activationInput, activation;
		for( int i=1; i<this.allLayers.size(); i++ )
		{
			ArrayList<Neuron> thisLayer = (ArrayList<Neuron>)this.allLayers.get(i);
			
			// iterate over each neuron in the layer, calculating it's activation
			for( int j=0; j<thisLayer.size(); j++ )
			{
				Neuron thisNeuron = thisLayer.get(j);
				List<Synapse> synapses = thisNeuron.getInputSynapses();
				activationInput = 0.0;
				
				// sum the each synapse weight multiplied by the connecting neurons activation 
				for( int k=0; k<synapses.size(); k++ )
				{
					Synapse thisSynapse = synapses.get(k);
					activationInput += thisSynapse.getInput().getActivation()*thisSynapse.getWeight();
				}
				
				thisNeuron.setInputValue(activationInput - thisNeuron.getThreshold());
				activation = thisNeuron.getActivator().valueAt(thisNeuron.getInputValue());
				thisNeuron.setActivation(activation);
				
				// output neurons have no output synapses
				if( thisNeuron.getOutputSynapses().size() == 0 )
					result.getData().add(activation);
			}
		}
		
		super.calculateResponse(input);
		
		// return results
		this.setLastOutput(result);
		return result;
	}
}
