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

import org.junit.*;
import com.jonschang.ai.network.*;
import com.jonschang.ai.network.feedforward.*;
import com.jonschang.math.vector.*;
import com.jonschang.math.vector.MathVector;
import java.util.*;

public class FeedForwardTest {
	@Test public void testXmlFunctions() throws Exception {
		try {
			FeedForward net = new FeedForward();
			SigmoidActivator activator = new SigmoidActivator();
			
			// create a small 3 layer network
			net.addLayer(1,activator);
			net.addLayer(2,activator);
			net.addLayer(3,activator);
			
			VectorImpl in = new VectorImpl(1);
			MathVector out=null;
			in.getData().set(0, 0.8234873);
			out = net.calculateResponse( in );
			
			// save the current thresholds and weights
			// so that we can compare them later
			double[][] layerThresholds = new double[3][];
			double[][][] layerWeights = new double[3][][];
			int i=0;
			for( List<Neuron> neurons : net.getAllLayers() ) {
				layerThresholds[i] = new double[i+1];
				layerWeights[i] = new double[i+1][];
				int j=0;
				for( Neuron neuron : neurons ) {
					layerThresholds[i][j] = neuron.getThreshold();
					if( neuron.getOutputSynapses()!=null && neuron.getOutputSynapses().size()>0  ) {
						int k=0;
						layerWeights[i][j] = new double[neuron.getOutputSynapses().size()];
						for( Synapse s : neuron.getOutputSynapses() ) {
							layerWeights[i][j][k]=s.getWeight();
						}
						k++;
					}
					j++;
				}
				i++;
			}
			
			String str = net.getXml();
			
			// now re-create the network from xml
			net = new FeedForward();
			net.setXml(str);
			
			MathVector out2 = net.calculateResponse(in);
			
			Assert.assertTrue( out2.squared().sum()==out.squared().sum() );
			Assert.assertTrue( out2.sum()==out.sum() );
			
		} catch( Exception ioe ) {
			ioe.printStackTrace();
			throw ioe;
		}
	}
	
	@Test public void testAddLayer()
	{
		FeedForward net = new FeedForward();
		SigmoidActivator activator = new SigmoidActivator();
		
		// assert that each layer is: 
		// updating the output layer
		// and has the correct number of elements
		
		net.addLayer(3,activator);
		Assert.assertEquals( 3, net.getInputNeurons().size() );
		Assert.assertEquals( 3, net.getOutputNeurons().size() );
		Assert.assertEquals(1, net.getAllLayers().size() );
		
		net.addLayer(4,activator);
		Assert.assertEquals( 3, net.getInputNeurons().size() );
		Assert.assertEquals( 4, net.getOutputNeurons().size() );
		Assert.assertEquals(2, net.getAllLayers().size() );
		
		net.addLayer(2,activator);
		Assert.assertEquals( 3, net.getInputNeurons().size() );
		Assert.assertEquals( 2, net.getOutputNeurons().size() );
		Assert.assertEquals(3, net.getAllLayers().size() );
		
		// assert that each neuron in each non-input layer
		// has a synapse to each neuron in the previous layer
		List<List<Neuron>> layers = net.getAllLayers();
		for( int i = 1; i < layers.size(); i++ )
		{
			@SuppressWarnings(value={"unchecked"})
			List<Neuron> thisLayer = (List<Neuron>)layers.toArray()[i];
			@SuppressWarnings(value={"unchecked"})
			List<Neuron> lastLayer = (List<Neuron>)layers.toArray()[i-1];
			for( int j = 0; j < thisLayer.size(); j++ )
			{
				Neuron n = (Neuron)thisLayer.toArray()[j];
				Assert.assertEquals(n.getInputSynapses().size(), lastLayer.size());
			}
		}
	}
	
	@Test public void testCalculateResponse()
	{
		FeedForward net = new FeedForward();
		SigmoidActivator activator = new SigmoidActivator();
		
		// create a small 3 layer network
		net.addLayer(2,activator);
		net.addLayer(2,activator);
		net.addLayer(1,activator);
		
		for( List<Neuron> layer : net.getAllLayers() )
			for(Neuron n : layer) {
				n.setThreshold(2.0);
				if(n.getInputSynapses()!=null)
					for(Synapse s : n.getInputSynapses())
						s.setWeight(0.5);
			}
		
		// test the response function
		VectorImpl v = new VectorImpl(1.0,2.0);
		MathVector r = net.calculateResponse(v);
		System.out.println( Math.round(r.valueOf(0)*10000000) );
		Assert.assertTrue( Math.round(r.valueOf(0)*10000000)==1648660 );
	}
}
