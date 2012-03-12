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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.jonschang.math.vector.VectorImpl;
import com.jonschang.math.vector.MathVector;

/**
 * On each iteration, appends the result input and output vector of each TrainingSetSource.
 * If one training set source ends prior to another, then the output of both are ended
 * and a NoSuchElementException is thrown.
 * @author schang
 */
public class AggregateTrainingSetSource implements TrainingSetSource {

	private List<TrainingSetSource> trainingSetSources = new ArrayList<TrainingSetSource>();
	
	public AggregateTrainingSetSource add(TrainingSetSource tss) {
		if( !this.trainingSetSources.contains(tss) )
			this.trainingSetSources.add(tss);
		return this;
	}
	
	public AggregateTrainingSetSource remove(TrainingSetSource tss) {
		if( this.trainingSetSources.contains(tss) )
			this.trainingSetSources.remove(tss);
		return this;
	}
	
	private class AggregateTrainingSetPair extends GenericVectorPair {
		AggregateTrainingSetPair(MathVector in, MathVector out) {
			this.setInput(in);
			this.setOutput(out);
		}
	}
	
	private class AggregateTrainingSetIterator implements TrainingSetSource.Iterator {

		private List<TrainingSetSource.Iterator> iterators = new ArrayList<TrainingSetSource.Iterator>(); 
		
		AggregateTrainingSetIterator() {
			for( TrainingSetSource tss : trainingSetSources )
				iterators.add(tss.iterator());
		}
		
		public boolean hasNext() {
			for( TrainingSetSource.Iterator tssi : this.iterators ) {
				if( !tssi.hasNext() )
					return false;
			}
			return true;
		}

		public Pair next() {
			if( this.hasNext() ) {
				VectorImpl vecIn = new VectorImpl();
				VectorImpl vecOut = new VectorImpl();
				for( TrainingSetSource.Iterator tssi : this.iterators ) {
					TrainingSetSource.Pair tssp = tssi.next();
					vecIn.getData().addAll(tssp.getInput().getData());
					vecOut.getData().addAll(tssp.getInput().getData());
				}
				return new AggregateTrainingSetPair(vecIn,vecOut);
			} else throw new NoSuchElementException();
		}

		/**
		 * does nothing
		 */
		public void remove() {
			// does nothing
		}	
	}
	
	public Iterator iterator() {
		return new AggregateTrainingSetIterator();
	}

}
