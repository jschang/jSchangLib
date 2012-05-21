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

package com.jonschang.ai.network.feedforward.gaio;

import java.util.List;

import org.apache.log4j.Logger;

import com.jonschang.ai.ga.GenerationEvaluator;
import com.jonschang.ai.ga.GeneticAlgException;
import com.jonschang.ai.ga.GeneticAlgFactory;
import com.jonschang.ai.network.feedforward.FeedForward;
import com.jonschang.ai.network.AbstractNetworkTrainer;
import com.jonschang.ai.network.HasIterations;
import com.jonschang.ai.network.HasLearningRate;
import com.jonschang.ai.network.HasMeanSquaredError;
import com.jonschang.ai.network.InputOptimizer;
import com.jonschang.ai.network.NetworkTrainer;
import com.jonschang.ai.network.NetworkTrainingException;
import com.jonschang.ai.network.TrainingSetSource;
import com.jonschang.math.vector.MathVector;
import com.jonschang.opencl.OCLCommandQueue;
import com.jonschang.opencl.OCLContext;
import com.jonschang.opencl.RequiresOpenCL;

/**
 * A BackPropagation algorithm training multiple networks,
 * using the same TrainingSetSource.
 * @author schang
 */
public class GABPInputOptimizer extends AbstractNetworkTrainer<FeedForward> 
	implements InputOptimizer<FeedForward>, 
		HasIterations, 
		HasMeanSquaredError, 
		HasLearningRate, 
		RequiresOpenCL {
	
	private int numberOfIterations = 1000000;
	private int currentIteration   = 0;
	private double currentMSE      = 100000;
	private double desiredMSE      = 0.0025;
	private double learningRate    = 0.1;
	private double lastMaxError    = 0;      // just for debuggin, not actually used just yet.
	
	private OCLContext context;
	private OCLCommandQueue commandQueue;
	
	private FeedForward prototype;
	private List<MathVector> inputMasks = null;
	
	private GABPFeedForwards feedForwards = null;
	
	public void resetTrainingStatus() {
		this.currentIteration = 0;
		this.currentMSE = 100000;
	}
	
	@Override protected void onBegin() throws NetworkTrainingException {
		feedForwards = new GABPFeedForwards(context,commandQueue,prototype,inputMasks,trainingData);
		feedForwards.createBuffers();
		feedForwards.createProgram();
	}
	
	public void setInputMasks(List<MathVector> inputMasks) {
		
	}

	@Override public boolean train() throws NetworkTrainingException {
		try {
			return super.train();
		} finally {
			feedForwards.releaseBuffers();
			feedForwards = null;
		}		
	}
	
	public boolean trainingIteration()
	{
		double newMSE=100000;
		this.lastMaxError=0;
		
		// create a set of input masks using the provided algorithm
		
		// assign the input masks to the GABPFeedForwards instance
		
		// train all the network clones on the available training data
		int dataRow = 0;
		for( TrainingSetSource.Pair entry: this.trainingData )
		{
			MathVector inputVec = entry.getInput();
			MathVector outputVec = entry.getOutput();
			
			// perform training updates
			feedForwards.computeInputLayer(dataRow);
			for( int i=1; i<prototype.getAllLayers().size(); i++ ) {
				feedForwards.computeNextLayer(i-1,i);
			}
			feedForwards.computeOutputError(dataRow);
			for( int i=prototype.getAllLayers().size()-1; i>=1; i-- ) {
				feedForwards.computePrevLayerError(i-1,i);
			}
			for( int i=prototype.getAllLayers().size()-1; i>0; i-- ) {
				feedForwards.updateSynapses(i);
			}
			for( int i=prototype.getAllLayers().size()-1; i>=0; i-- ) {
				feedForwards.updateThresholds(i);
			}
			
			feedForwards.readResults();
			
			List<float[]> allLayerActivations = feedForwards.getActivation();
			float[] outputActivations = allLayerActivations.get(allLayerActivations.size()-1);
			
			// determine which network has the greatest error
			float thisMaxError = 0.0f;
			int numNets = feedForwards.getCopies();
			for( int n=0; n<numNets; n+=outputVec.size() ) {
				for( int i=0; i<outputVec.size(); i++ ) {
					double thisActivation = outputActivations[n*outputVec.size()+i];
					float thisError = (float) Math.abs( outputVec.getData().get(i) - thisActivation );
					thisMaxError = thisMaxError < thisError ? thisError : thisMaxError;
				}
			}
			
			this.lastMaxError=this.lastMaxError<thisMaxError?thisMaxError:this.lastMaxError;
			
			newMSE += Math.pow(thisMaxError, 2);
			dataRow++;
		}
		
		this.currentMSE = newMSE / dataRow;
		
		// save the current MSE
		this.currentIteration++;
		
		Logger.getLogger(this.getClass()).info("iteration: "+currentIteration+", MSE: "+currentMSE+", max error: "+this.lastMaxError);
		
		// TODO: adjust this for multiple networks
		return (this.currentMSE>this.desiredMSE && this.currentIteration<this.numberOfIterations)?true:false;
	}
	
	/*
	 * ACCESSORS
	 */
	
	@Override public void setOCLCommandQueue(OCLCommandQueue queue) {
		this.commandQueue = queue;
	}
	@Override public OCLCommandQueue getOCLCommandQueue() {
		return this.commandQueue;
	}
	@Override public void setOCLContext(OCLContext context) {
		this.context = context;
	}
	@Override public OCLContext getOCLContext() {
		return context;
	}	
	
	public int getIterations()
	{ return this.currentIteration; }
	
	public void setDesiredMSE(double MSE){
		this.desiredMSE=MSE;
	}
	
	public void setDesiredError(double error){
		this.desiredMSE=Math.pow(error,2);
	}
	
	public double getDesiredMSE(){
		return this.desiredMSE;
	}
	public double getCurrentMSE(){
		return this.currentMSE;
	}
	
	public void setPrototype(FeedForward network) {
		prototype = network;
	}
	public FeedForward getPrototype() {
		return prototype;
	}
	
	public void setLearningRate(double learningRate) { 
		this.learningRate = learningRate; 
	}	
	public double getLearningRate(){
		return this.learningRate; 
	}

	@Override public int getTrainingIterations() {
		return this.numberOfIterations;
	}
	@Override public void setTrainingIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
}
