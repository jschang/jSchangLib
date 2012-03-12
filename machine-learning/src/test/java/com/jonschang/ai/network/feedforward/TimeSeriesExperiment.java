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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.jonschang.ai.network.GenericTrainingSetSource;
import com.jonschang.ai.network.SigmoidActivator;
import com.jonschang.math.vector.MathVector;
import com.jonschang.math.vector.VectorImpl;

/**
 * This is an experiment to determine the viability of storing sequences of 
 * data in a feed-forward neural network.
 * 
 * The network is trained to respond with S(t+1) for an input of S(t).
 * 
 * Two additional output pins are added: "percent complete" and "trigger".
 * 
 * When the "trigger" pin activates, it is the application's queue 
 * to begin comparing network previous output against network current input.
 * 
 * When the magnitude of the difference between previous output and current 
 * input is too great, the application can be reasonably certain that incoming
 * input has deviated from the series.
 * 
 * The "percent complete" pin is intended to let the application know when
 * the knowledge of the network has been exhausted.
 * 
 * @throws Exception
 */
public class TimeSeriesExperiment {

	private int numSamplesMinusOne = 30;
	
	public static void main(String argv[])
	{
		try {
			new TimeSeriesExperiment().run();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		
		Logger.getRootLogger().setLevel(Level.ALL);
		Logger.getLogger(com.jonschang.ai.network.feedforward.BackPropagation.class).setLevel(Level.ERROR);
		
		Double sample[][] = createSample();
		
		FeedForwardNetworkTrainer trainer = this.createTrainer(sample);
		
		FeedForward net = trainer.getNetwork();
		trainer.train();
		Logger.getLogger(this.getClass()).info("Training took "+trainer.getIterations()+" iterations, and last MSE "+trainer.getCurrentMSE());
			
		// validate the time series storage
		VectorImpl lastResponse = null;
		for( int i=0; i<numSamplesMinusOne; i++ ) {
			
			VectorImpl vec = new VectorImpl(sample[i]);
			
			// calculate the response of the sample set
			VectorImpl response = (VectorImpl)net.calculateResponse(vec);
			Double dist = distFromLast(vec,lastResponse);
			Logger.getLogger(this.getClass()).trace("Sample set "+formatResponse(vec,response,dist));
			
			if( dist!=null && dist > .05 ) {
				Logger.getLogger(this.getClass()).error("Series has been broken.");
			}
			
			lastResponse = response;
		}
		
		// view the network response to the set out of order set
		List<Double[]> sampleList = Arrays.asList(sample);
		Collections.shuffle(sampleList);
		lastResponse=null;
		for( Double[] thisSample : sampleList ) {
			VectorImpl vec = new VectorImpl(thisSample);
			VectorImpl response = (VectorImpl)net.calculateResponse(vec);
			Logger.getLogger(this.getClass()).trace("Out-of-order set "+formatResponse(vec,response,distFromLast(vec,lastResponse)));
			lastResponse = response;
		}
		
		// view the network response to random data
		lastResponse=null;
		for( int i=0; i<numSamplesMinusOne; i++ ) {
			VectorImpl vec = new VectorImpl(new Double[]{
					Math.random(),
					Math.random()}
				);
			VectorImpl response = (VectorImpl)net.calculateResponse(vec);
			Logger.getLogger(this.getClass()).trace("Random set "+formatResponse(vec,response,distFromLast(vec,lastResponse)));
			lastResponse = response;
		}
	}
	
	public Double[][] createSample() throws Exception {
		Double sample[][] = new Double[numSamplesMinusOne+1][2];
		for( int i=0; i<numSamplesMinusOne+1; i++ ) {
			for( int j=0; j<2; j++ ) {
				sample[i][j]=Math.random();
			}
		}
		return sample;
	}
	
	public FeedForwardNetworkTrainer createTrainer(Double sample[][]) throws Exception 
	{
		FeedForward net = new FeedForward();
		BackPropagation trainer = new BackPropagation();
		SigmoidActivator activator = new SigmoidActivator();
		net.addLayer(2, activator);
		net.addLayer(numSamplesMinusOne+1, activator);
		net.addLayer(numSamplesMinusOne+1, activator);
		net.addLayer(4, activator);
		
		GenericTrainingSetSource trainingData = new GenericTrainingSetSource();
		Double d[][][] = new Double[numSamplesMinusOne+1][2][4];
		Double multiplier = ( 1.0 / Double.valueOf(numSamplesMinusOne) );
		for( int i=0; i<numSamplesMinusOne ; i++ )
		{			
			int iPlusOne = i+1;
			
			// inputs - 2 values in series
			d[i][0][0] = sample[i][0];
			d[i][0][1] = sample[i][1];
			
			// outputs - % complete, trigger boolean, next 2 values in series
			d[i][1][0] = Double.valueOf(iPlusOne) * multiplier;
			d[i][1][1] = i==0 ? 0.99 : 0.01;
			d[i][1][2] = sample[iPlusOne][0];
			d[i][1][3] = sample[iPlusOne][1];
		};
		for( int i=0; i<numSamplesMinusOne; i++ ) {
			VectorImpl input = new VectorImpl(new Double[]{d[i][0][0],d[i][0][1]});
			VectorImpl output = new VectorImpl(d[i][1]);
			Logger.getLogger(this.getClass()).debug("Adding pair "+input+", "+output);
			trainingData.addPair(input, output);
		}
		
		trainer.setNetwork(net);
		trainer.setTrainingSetSource(trainingData);
		trainer.setLearningRate(.02);
		
		return trainer;
	}
	
	public Double distFromLast(VectorImpl in, VectorImpl lastOut) {
		if( lastOut==null )
			return null;
		return lastOut.slice(2,3).minus(in).magnitude();
	}
	
	public String formatResponse(VectorImpl in, VectorImpl out, Double dist) {
		String ret = null;
		if( dist!=null ) {
			ret = String.format("%s yields %s, where input is %.3f %% different from previous response",in,out,dist);
		} else ret = String.format("%s returned %s",in,out);
		return ret;
	}
}
