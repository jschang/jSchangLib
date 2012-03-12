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

package com.jonschang.ai.network.restrictedboltzmann;

import java.util.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;

public class RestrictedBoltzmann extends AbstractNetwork {

	public enum Direction {
		FORWARD,
		BACKWARD
	}
	
	private Direction direction = Direction.FORWARD;
	private List<GenericNeuron> inputNeurons = new ArrayList<GenericNeuron>();
	private List<GenericNeuron> outputNeurons = new ArrayList<GenericNeuron>();
	
	public void setDirection(Direction dir) {
		direction = dir;
	}
	public Direction getDirection() {
		return direction;
	}
	
	public void create(Activator activator, int numInputNodes, int numOutputNodes) {
		GenericNeuron outputNeuron;
		for( int i=0; i<numInputNodes; i++ ) {
			GenericNeuron neuron = new GenericNeuron();
			this.inputNeurons.add( neuron );
			for( int j=0; j<numOutputNodes; j++ ) {
				if( j==0 ) {
					outputNeuron = new GenericNeuron();
					this.outputNeurons.add( outputNeuron );
				} else outputNeuron = this.outputNeurons.get(j);
				neuron.createSynapseTo(outputNeuron);
			}
		}
	}
	
	@Override
	public MathVector calculateResponse(MathVector input)
	{
		MathVector toRet = null;
		if( direction == Direction.FORWARD )
			toRet = calculateForward(input);
		else toRet = calculateBackward(input);
		super.calculateResponse(input);
		return toRet;
	}
	public MathVector calculateForward(MathVector input) {
		return null;
	}
	public MathVector calculateBackward(MathVector input) {
		return null;
	}
}
