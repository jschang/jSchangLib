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

import com.jonschang.ai.network.TrainingSetSource.Pair;
import com.jonschang.math.vector.MathVector;

/**
 * Pair of input and output vectors
 * @access package
 * @author schang
 */
public class GenericVectorPair implements Pair {

	private MathVector inputVector;
	private MathVector outputVector;
	
	public GenericVectorPair() {
	}
	public GenericVectorPair(MathVector in, MathVector out) {
		this.inputVector=in;
		this.outputVector=out;
	}
	public GenericVectorPair setInput(MathVector v) {
		this.inputVector=v;
		return this;
	}
	
	public GenericVectorPair setOutput(MathVector v) {
		this.outputVector=v;
		return this;
	}
	
	public MathVector getInput() {
		return this.inputVector;
	}

	public MathVector getOutput() {
		return this.outputVector;
	}

}
