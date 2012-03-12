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
import org.apache.log4j.*;

/**
 * Adjusts the learning rate of a NetworkTrainer 
 * implementing HasLearningRate, HasMeanSquaredError and HasIterations
 * 
 * The algorithm for learning rate adjustment is very simple,
 * it simply halves the learning rate any time the slope of
 * the mean squared error drops below a certain absolute value.
 * 
 * The default MSE slope trigger is 0.000001, which I've found
 * to be fairly effective in preliminary trials.
 * 
 * When the learning rate has been halved the number of drops
 * allowed, then the max number of iterations in the trainer
 * is updated to the current iteration number and the trainer
 * will then, presumably, stop training.
 * 
 * This is implemented for 2 reasons:
 * 
 * 1) Once the slope of the training curve is consistently below
 * a certain point, training is no longer significantly beneficial
 * 
 * 2) Having an initially high learning rate increases learning speed
 * for BackPropagation because the algorithm has the opportunity to
 * leap out of local minima.
 * 
 * TODO: need to allow a number of iterations between moving average measurements
 * 
 * @author schang
 *
 * @param <N>
 */
public class SimulatedAnnealingTrainerObserver<N extends Network> extends AbstractNetworkTrainerObserver<N> {
	
	private Integer movingAverageIterations = 50;
	private Double mseSlopeTrigger = 0.000001;
	private Double lastAvgMSE = 0.0;
	private Integer numberOfDrops = 0;
	private Integer dropsAllowed = 10;
	private List<Double> lastMSEList = new ArrayList<Double>();
	private Double initialLearningRate = 0.0;
	private Integer initialIterations = 0;
	
	public void setMSESlopeTrigger(Double slope) {
		mseSlopeTrigger = slope;
	}
	public void setMovingAverageIterations(Integer iterations) {
		movingAverageIterations = iterations;
	}
	
	@Override
	public <T extends SingleNetworkTrainer<N>> void setTrainer(T trainer) throws NetworkException {
		if( ! (
			trainer instanceof HasLearningRate
			&& trainer instanceof HasMeanSquaredError
			&& trainer instanceof HasIterations
		))
			throw new NetworkException("trainer passed into setTrainer() must implement all of HasLearningRate, HasMeanSquaredError, and HasIterations");
		this.trainer = trainer;
		this.initialLearningRate = ((HasLearningRate)trainer).getLearningRate();
		this.initialIterations = ((HasIterations)trainer).getTrainingIterations();
	}
	
	public void onBegin() {
		numberOfDrops=0;
	}
	
	@Override
	public void onIterationEnd() {
		
		Double currentMSE = ((HasMeanSquaredError)trainer).getCurrentMSE();
		Double currentAvgMSE = 0.0;
		
		if( lastMSEList.size()==movingAverageIterations )
			lastMSEList.remove(0);
		lastMSEList.add(currentMSE);
		if( lastMSEList.size()!=movingAverageIterations )
			return;
		
		for( Double mse : lastMSEList )
			currentAvgMSE += mse;
		currentAvgMSE = currentAvgMSE / (double)lastMSEList.size();
		
		Double oldLearningRate = ((HasLearningRate)trainer).getLearningRate();
		Double newLearningRate = oldLearningRate*.5;
		Integer iterations = ((HasIterations)trainer).getIterations();
		
		Double thisSlope = Math.abs( currentAvgMSE - lastAvgMSE );
		Logger.getLogger(this.getClass()).trace("current MSE slope is "+thisSlope);
		if( thisSlope < mseSlopeTrigger ) {
			if( numberOfDrops==dropsAllowed ) {
				((HasIterations)trainer).setTrainingIterations(
					iterations
				);
				Logger.getLogger(this.getClass()).trace("Ending training on iteratoin "+iterations);
			}
			Logger.getLogger(this.getClass()).trace("On iteration "+iterations+", lowering learning rate from "+oldLearningRate+" to "+newLearningRate);
			((HasLearningRate)trainer).setLearningRate( newLearningRate );	
			lastMSEList.clear();
			numberOfDrops++;
		}
		
		lastAvgMSE = currentAvgMSE;
	}
	
	public void onEnd() {
		((HasLearningRate)trainer).setLearningRate(this.initialLearningRate);
		((HasIterations)trainer).setTrainingIterations(this.initialIterations);
	}
}
