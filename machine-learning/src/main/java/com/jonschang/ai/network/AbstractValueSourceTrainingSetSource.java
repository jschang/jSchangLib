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

import com.jonschang.utils.valuesource.*;

abstract public class AbstractValueSourceTrainingSetSource<L extends ValueSourceList<V>, V extends ValueSource> 
	implements ValueSourceTrainingSetSource<L,V> {
	
	protected L inputSources;
	protected L outputSources;
	
	public <E extends L> void setInputs(E sources) {
		inputSources = sources;
	}
	
	public <E extends L> void setOutputs(E sources) {
		outputSources = sources;
	}
	
	public L getInputs() {
		return inputSources;
	}
	
	public L getOutputs() {
		return outputSources;
	}
}
