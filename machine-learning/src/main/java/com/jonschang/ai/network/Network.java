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

import com.jonschang.math.vector.MathVector;
import com.jonschang.utils.valuesource.ValueSource;
import com.jonschang.utils.valuesource.ValueSourceList;

@SuppressWarnings(value="unchecked")
public interface Network {

	/**
	 * add a NetworkObserver
	 * @param networkObserver
	 */
	void attach(NetworkObserver networkObserver);

	/**
	 * remove a NetworkObserver
	 * @param networkObserver
	 */
	void detach(NetworkObserver networkObserver);

	List<NetworkObserver> getObservers();

	/**
	 * Call this last, immediately prior to return, in subclasses
	 * 
	 * Overrides shall:
	 * 
	 *   - make available a last output IVector from getLastOutput
	 *   - call each NetworkObserver::onCalculate() immediately after calculating the response
	 * 
	 * @param input
	 * @return always null, return a vector in your overriding class and call this last before return
	 */
	MathVector calculateResponse(MathVector input);

	/**
	 * A means of calculating a response using the ValueSources
	 * 
	 * Overrides shall:
	 * 
	 *   - make available a last output IVector from getLastOutput
	 *   - aggregate the input vector calling getValue() on the input ValueSourceList<ValueSource> elements
	 *   - call setValue() on the output ValueSourceList<ValueSource> elements for each value in the response vector
	 *   - call each NetworkObserver::onCalculate() immediately after calculating the response
	 * 
	 * @throws Exception if the output size is not equal to the response vector size
	 */
	void calculateResponse() throws Exception;
	
	/**
	 * The result of the last calculateResponse() call
	 * @return The last executions response IVector
	 */
	MathVector getLastOutput();

	/**
	 * get the ValueSourceList that the Network can use to generate an IVector for calculateResponse
	 * @return 
	 */
	ValueSourceList<ValueSource> getInputs();
	
	/**
	 * get the ValueSourceList that the Network will store the IVector resulting from calculateResponse
	 * @return 
	 */
	ValueSourceList<ValueSource> getOutputs();
	
	/**
	 * set the ValueSourceList that the Network can use to generate an IVector for calculateResponse
	 * @param in
	 */
	void setInputs(ValueSourceList<ValueSource> in);
	
	/**
	 * set the ValueSourceList that the Network will store the IVector resulting from calculateResponse
	 * @param out
	 */
	void setOutputs(ValueSourceList<ValueSource> out);

}