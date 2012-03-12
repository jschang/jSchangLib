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

/**
 * Combines the output values of each input and output ValueSources into an IVector pair
 * 
 * Implementors must concern themselves with providing an iterator
 * that will update the ValueSource's with information that varies
 * by iteration. 
 *  
 * @author schang
 *
 * @param <V>
 */
public interface ValueSourceTrainingSetSource<L extends ValueSourceList<V>,V extends ValueSource> 
	extends TrainingSetSource {

	public L getInputs();
	public L getOutputs();
	
	public <E extends L> void setInputs(E sources);
	public <E extends L> void setOutputs(E sources);
}
