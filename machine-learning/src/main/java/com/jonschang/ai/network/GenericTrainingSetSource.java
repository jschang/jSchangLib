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
 * A TrainingSetSource that you must manually add data to using addPair.
 * The entire set of pairs is stored in memory.
 * 
 * @author schang
 */
public class GenericTrainingSetSource implements TrainingSetSource {

	protected TrainingSetSource.Iterator trainingSetIterator;
	protected List<GenericTrainingSetPair> trainingData = new ArrayList<GenericTrainingSetPair>();
	protected boolean firstIteration = true;
	
	public GenericTrainingSetSource() {
	}
	
	public GenericTrainingSetPair newPair(MathVector in, MathVector out) {
		GenericTrainingSetPair p = new GenericTrainingSetPair();
		p.setInput(in);
		p.setOutput(out);
		return p;
	}
	
	public GenericTrainingSetSource addPair(MathVector in, MathVector out) {
		this.addPair( this.newPair(in, out) );
		return this;
	}
	
	public GenericTrainingSetSource addPair(GenericTrainingSetPair p) {
		this.trainingData.add(p);
		return this;
	}
	
	public int size() {
		return this.trainingData.size();
	}
	
	protected class GenericTrainingSetIterator implements TrainingSetSource.Iterator {
		
		private int position = -1; // set to -1 so next will be at index 0
		
		public boolean hasNext() {
			
			if( trainingData.size() > this.position+1 )
				return true;
			
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
			// if we just added one from the source,
			// then we should have a next value now
			if( this.hasNext() )
			{
				this.position++;
				return trainingData.get(this.position);
			} else throw new NoSuchElementException();
		}
		
	}
	
	private class GenericTrainingSetPair extends GenericVectorPair {}

	public Iterator iterator() {
		return new GenericTrainingSetIterator();
	}
}
