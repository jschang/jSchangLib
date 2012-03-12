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

abstract public class AbstractNetworkTrainer<N extends Network> implements NetworkTrainer<N> {
	
	protected TrainingSetSource trainingData;
	
	public void setTrainingSetSource(TrainingSetSource trainingData) {
		this.trainingData = trainingData;
	}
	
	public TrainingSetSource getTrainingSetSource() {
		return this.trainingData;
	}

	protected void onBegin() throws Exception {}
	protected void onIterationBegin()throws Exception {}
	protected void onIterationEnd()throws Exception {}
	protected void onEnd()throws Exception {}
	
	public boolean train() throws NetworkTrainingException
	{		
		boolean cont=true;
		try {
			
			this.onBegin();
			
			do
			{
				this.onIterationBegin();
				
				cont = this.trainingIteration();
			
				this.onIterationEnd();

			} while( cont );
			
			this.onEnd();
			
		} catch( Exception e ) {
			throw new NetworkTrainingException(e);
		}
		return false;
	}	

}
