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

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import com.jonschang.math.vector.MathVector;

/**
 * Attempts to buffer the entire set of IVectors from a TrainingSetSource
 * 
 * Users may also fill the BufferedTrainingSetSource with their own 
 * input/output vector pairs using addPair()
 * 
 * @author schang
 */
public class BufferedTrainingSetSource implements TrainingSetSource {

	protected TrainingSetSource.Iterator trainingSetIterator;
	protected TrainingSetSource trainingSetSource;
	protected List<BufferedTrainingPair> trainingData = new ArrayList<BufferedTrainingPair>();
	protected boolean firstIteration = true;
	
	public BufferedTrainingSetSource() {
	}
	
	public BufferedTrainingSetSource(TrainingSetSource source) {
		this.setTrainingSetSource(source);
	}
	
	public BufferedTrainingSetSource setTrainingSetSource(TrainingSetSource source) {
		this.trainingSetSource = source;
		return this;
	}
	public TrainingSetSource getTrainingSetSource() {
		return this.trainingSetSource;
	}
	
	public BufferedTrainingPair newPair(MathVector in, MathVector out) {
		BufferedTrainingPair p = new BufferedTrainingPair();
		p.setInput(in);
		p.setOutput(out);
		return p;
	}
	
	public BufferedTrainingSetSource addPair(MathVector in, MathVector out) {
		this.addPair( this.newPair(in, out) );
		return this;
	}
	
	public BufferedTrainingSetSource addPair(BufferedTrainingPair p) {
		this.trainingData.add(p);
		return this;
	}
	
	public int size() {
		return this.trainingData.size();
	}
	
	protected class BufferedTrainingSetIterator implements TrainingSetSource.Iterator {
		
		private int position = -1; // set to -1 so next will be at index 0
		private boolean iteratorHasNext=true;
		
		public boolean hasNext() {
			
			if( trainingData.size() > this.position+1 )
				return true;
			
			if( trainingSetIterator!=null ) {
				if( trainingSetIterator.hasNext() ) {
					iteratorHasNext=true;
					return true;
				} else {
					iteratorHasNext=false;
					trainingSetIterator=null;
				}
			}
			
			return false;
		}
		
		/**
		 * does nothing 
		 */
		public void remove() {}
		
		public Pair getNext() throws NoSuchElementException {
			return this.next();
		}
		
		public Pair next() throws NoSuchElementException {
			// if we've gotten to the end of what we have cached,
			// and there is a source
			// and there are still more pairs in the source
			// then get the next from there
			if( trainingSetIterator!=null && iteratorHasNext )
			{
				TrainingSetSource.Pair newPair = trainingSetIterator.next();
				addPair(newPair.getInput(),newPair.getOutput());
			}
			// if we just added one from the source,
			// then we should have a next value now
			if( this.hasNext() )
			{
				this.position++;
				return trainingData.get(this.position);
			} else throw new NoSuchElementException();
		}
		
	}
	
	private class BufferedTrainingPair extends GenericVectorPair {}

	public Iterator iterator() {
		if( firstIteration && trainingSetSource!=null ) {
			this.trainingSetIterator = trainingSetSource.iterator();
			firstIteration = false;
		} else firstIteration = false;
		return new BufferedTrainingSetIterator();
	}
}
