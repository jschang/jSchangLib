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

import org.apache.log4j.*;
import org.jocl.cl_program;

import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import com.jonschang.opencl.*;
import com.jonschang.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A BackPropagation algorithm training multiple networks,
 * using the same TrainingSetSource
 * @author schang
 */
public class GABPInputOptimizer extends AbstractNetworkTrainer<FeedForward> 
	implements InputOptimizer<FeedForward>, NetworkTrainer<FeedForward>, HasIterations, HasMeanSquaredError, HasLearningRate, RequiresOpenCL {
	
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
	
	public void setInputMasks(List<MathVector> inputMasks) {
		this.inputMasks = inputMasks;
	}
	public List<MathVector> getInputMasks() {
		return inputMasks;
	}
	
	public void setLearningRate(double learningRate) { 
		this.learningRate = learningRate; 
	}	
	public double getLearningRate(){
		return this.learningRate; 
	}

	public void resetTrainingStatus() {
		this.currentIteration = 0;
		this.currentMSE = 100000;
	}

	@Override public int getTrainingIterations() {
		return this.numberOfIterations;
	}
	@Override public void setTrainingIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
	
	@Override protected void onBegin() throws NetworkTrainingException {
		feedForwards = new GABPFeedForwards(context,commandQueue,prototype,inputMasks,trainingData);
		feedForwards.createBuffers();
		feedForwards.createProgram();
	}
	
	@Override public void run() {
		try {
			this.train();
		} catch( NetworkTrainingException nte ) {
			throw new RuntimeException(nte);
		}
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
		double newMSE=0;
		int size=0;
		double error=0;
		this.lastMaxError=0;
		
		// if it has not been done since the last time train() was called
			// fill a data texture as many entries from the training set source as possible
		
		// if it has not been done since the last time train() was called
		/*
		 * In each texture, each row represents a network
		 * 
		 * there is a master configuration texture that holds:
		 * 	- the number of layers in the network
		 *  - the number of neurons in each layer
		 *  
		 * these textures scale to the width of the texture with the most values
		 * 
		 * there is a texture for each layer for each
		 * 	- neuron threshold
		 *  - neuron activation
		 *  - neuron error
		 *  
		 * there is a texture for each layer for each
		 *  - synapse weight, where each row represents the synapses from one neuron
		 */
		
		for( TrainingSetSource.Pair entry: this.trainingData )
		{
			MathVector inputVec = entry.getInput();
			MathVector outputVec = entry.getOutput();
			
			// perform training updates
			
			//newMSE += this.calculateResponseMSE(inputVec,outputVec);
			size++;
			
			this.lastMaxError=this.lastMaxError<error?error:this.lastMaxError;
			
		}
		
		this.currentMSE = newMSE/size;
		
		// save the current MSE
		this.currentIteration++;
		
		Logger.getLogger(this.getClass()).info("iteration: "+currentIteration+", MSE: "+currentMSE+", max error: "+this.lastMaxError);
		
		// TODO: adjust this for multiple networks
		return (this.currentMSE>this.desiredMSE && this.currentIteration<this.numberOfIterations)?true:false;
	}

}
