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

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.jonschang.ai.network.GenericTrainingSetSource;
import com.jonschang.ai.network.SigmoidActivator;
import com.jonschang.math.vector.MathVector;
import com.jonschang.math.vector.VectorImpl;

/**
 * This test was originally written to experiment with the effect of 
 * random noise on one of the input channels.
 * 
 * I was testing to prove to myself that noise would make training
 * less effective.
 * 
 * @throws Exception
 */
public class NoisyChannelExperiment {

	public static int main(String argv[])
	{
		try {
			new NoisyChannelExperiment().run();
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void run() throws Exception {
		org.apache.log4j.BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		
		FeedForwardNetworkTrainer trainer = this.createTrainer();
		
		FeedForward net = trainer.getNetwork();
		trainer.train();
		
		Logger.getLogger(this.getClass()).trace("is "+trainer.getDesiredMSE()+" > "+trainer.getCurrentMSE()+" ?");
		Assert.assertTrue(trainer.getDesiredMSE()>trainer.getCurrentMSE());
		
		MathVector vec = new VectorImpl(.01,.01,Math.random());
		VectorImpl response = (VectorImpl)net.calculateResponse(vec);
		Logger.getLogger(this.getClass()).trace(vec+" returned "+response);
		Assert.assertTrue(response.valueOf(0)<.1);
		
		vec.setValue(.99,0.01,Math.random());
		response = (VectorImpl)net.calculateResponse(vec);
		Logger.getLogger(this.getClass()).trace(vec+" returned "+response);
		Assert.assertTrue(response.valueOf(0)>.9);
		
		vec.setValue(0.01,.99,Math.random());
		response = (VectorImpl)net.calculateResponse(vec);
		Logger.getLogger(this.getClass()).trace(vec+" returned "+response);
		Assert.assertTrue(response.valueOf(0)>.9);
		
		vec.setValue(.99,.99,Math.random());
		response = (VectorImpl)net.calculateResponse(vec);
		Logger.getLogger(this.getClass()).trace(vec+" returned "+response);
		Assert.assertTrue(response.valueOf(0)<.1);
	}
	
	public FeedForwardNetworkTrainer createTrainer()
	{
		FeedForward net = new FeedForward();
		BackPropagation trainer = new BackPropagation();
		SigmoidActivator activator = new SigmoidActivator();
		net.addLayer(3, activator);
		net.addLayer(3, activator);
		net.addLayer(4, activator);
		net.addLayer(6, activator);
		net.addLayer(2, activator);
		
		GenericTrainingSetSource trainingData = new GenericTrainingSetSource();
		Double d[][][] =
		{
			{{.01,.01,Math.random()},{.01,Math.random()}},
			{{.99,.01,Math.random()},{.99,Math.random()}},
			{{.01,.99,Math.random()},{.99,Math.random()}},
			{{.99,.99,Math.random()},{.01,Math.random()}},
		};
		for( int i=0; i<d.length; i++ ) {
			Logger.getLogger(this.getClass()).debug("Adding pair "+new VectorImpl(d[i][0])+", "+new VectorImpl(d[i][1]));
			trainingData.addPair(new VectorImpl(d[i][0]), new VectorImpl(d[i][1]));
		}
		
		trainer.setNetwork(net);
		trainer.setTrainingSetSource(trainingData);
		
		return trainer;
	}
}
