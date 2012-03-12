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

import com.jonschang.ai.network.feedforward.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import org.apache.log4j.*;

public class BackPropagation extends AbstractSingleNetworkTrainer<FeedForward> implements FeedForwardNetworkTrainer {
	
	private int numberOfIterations = 1000000;
	private int currentIteration   = 0;
	private double currentMSE      = 100000;
	private double desiredMSE      = 0.0025;
	private double learningRate    = 0.1;
	private double lastMaxError    = 0;      // just for debuggin, not actually used just yet.
	
	public BackPropagation() {
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
	
	public void setLearningRate(double learningRate) { 
		this.learningRate = learningRate; 
	}
	
	public double getLearningRate(){
		return this.learningRate; 
	}

	public int getTrainingIterations() {
		return this.numberOfIterations;
	}

	public void resetTrainingStatus() {
		this.currentIteration = 0;
		this.currentMSE = 100000;
	}

	public void setTrainingIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
	
	public boolean trainingIteration()
	{
		double newMSE=0;
		int size=0;
		double error=0;
		this.lastMaxError=0;
		
		for( TrainingSetSource.Pair entry: this.trainingData )
		{
			MathVector inputVec = entry.getInput();
			MathVector outputVec = entry.getOutput();
			
			// perform training updates
			
			newMSE += this.calculateResponseMSE(inputVec,outputVec);
			size++;
			
			error = determineLastMaxError();
			this.lastMaxError=this.lastMaxError<error?error:this.lastMaxError;
			
			this.calculateOutputError(outputVec);
			this.propagateErrorBackwards();
			this.updateWeights();
			this.updateThresholds();
		}
		
		this.currentMSE = newMSE/size;
		
		// save the current MSE
		this.currentIteration++;
		
		Logger.getLogger(this.getClass()).info("iteration: "+currentIteration+", MSE: "+currentMSE+", max error: "+this.lastMaxError);
		
		return (this.currentMSE>this.desiredMSE && this.currentIteration<this.numberOfIterations)?true:false;
	}
	
	private double determineLastMaxError(){
		// iterate over each output neuron, record the one with the max error
		double maxError = 0;
		List<Neuron> outputNeurons = this.network.getOutputNeurons();
		for(Neuron n : outputNeurons)
		{
			double thisError = Math.abs(n.getDesiredActivation() - n.getActivation());
			maxError=maxError<thisError?thisError:maxError;
		}
		return maxError;
	}
	
	private void calculateOutputError(MathVector outputError) {
		// update the error for the output layer
		List<Neuron> outputNeurons = this.network.getOutputNeurons();
		double slopeAtInputSum, errorSum, x, y;
		Activator a;
		for(Neuron n : outputNeurons)
		{
			a = n.getActivator();
			slopeAtInputSum = a.slopeAt( n.getInputValue() );
			errorSum = ( n.getDesiredActivation() - n.getActivation() );
			n.setError( slopeAtInputSum *  errorSum );
//			y = n.getDesiredActivation() - n.getActivation();
//			x = a.inverse( n.getDesiredActivation() ) - n.getInputValue();
//			n.setError( -1 * y/x );
		}
	}
	
	private void propagateErrorBackwards() {
		// now walk backwards over each layer, propagating the error as we go
		List<List<Neuron>> layers = this.network.getAllLayers();
		double errorSum, slopeAtInputSum;
		for( int i=layers.size()-2; i>=0; i-- )
		{
			List<Neuron> thisLayer = layers.get(i);
			for( Neuron thisLayerNeuron : thisLayer )
			{
				errorSum = 0;
				List<Synapse> theseOutputSynapses = thisLayerNeuron.getOutputSynapses();
				for( Synapse thisOutputSynapse : theseOutputSynapses )
				{
					Neuron outputLayerNeuron = thisOutputSynapse.getOutput();
					errorSum += outputLayerNeuron.getError()*thisOutputSynapse.getWeight();
				}
				slopeAtInputSum = thisLayerNeuron.getActivator().slopeAt(thisLayerNeuron.getInputValue());
				thisLayerNeuron.setError( slopeAtInputSum * errorSum );
			}
		}
	}
	
	private void updateWeights() {
		// for each synapse, in each neuron, in each layer,
		// update the weight of the synapse proportional to the error, last activation, and learning rate
		List<List<Neuron>> layers = this.network.getAllLayers();
		for( List<Neuron> thisLayer : layers )
		{
			for( Neuron thisNeuron : thisLayer )
			{
				List<Synapse> synapses = thisNeuron.getOutputSynapses();
				for( Synapse thisSynapse : synapses )
					thisSynapse.setWeight( thisSynapse.getWeight() + thisSynapse.getOutput().getError() * thisNeuron.getActivation() * this.learningRate );
			}
		}
	}
	
	private void updateThresholds() {
		// for each synapse, in each neuron, in each layer,
		// update the threshold of the synapse proportional to the error, last activation, and learning rate
		List<List<Neuron>> layers = this.network.getAllLayers();
		for( List<Neuron> thisLayer : layers )
			for( Neuron thisNeuron : thisLayer )
				thisNeuron.setThreshold( thisNeuron.getThreshold() - thisNeuron.getError() * thisNeuron.getActivation() * this.learningRate );
	}
	
	private double calculateResponseMSE(MathVector inputVec, MathVector outputVec) {

		MathVector responseVec = this.calculateTrainingResponse(inputVec,outputVec);
		return this.calculateVectorMSE(responseVec,outputVec);
	}
	
	private double calculateVectorMSE(MathVector actual, MathVector desired) {
		return desired.minus(actual).squared().sum()/(desired.size());
	}
	
	private MathVector calculateTrainingResponse(MathVector inputVec, MathVector outputVec) {
		MathVector responseVec = this.network.calculateResponse(inputVec);
		for( int i=0; i<this.network.getOutputNeurons().size(); i++ )
			this.network.getOutputNeurons().get(i).setDesiredActivation(outputVec.valueOf(i));
		return responseVec;
	}
}
