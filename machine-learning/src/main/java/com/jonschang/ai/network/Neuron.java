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


public interface Neuron {

	List<Synapse> getInputSynapses();

	List<Synapse> getOutputSynapses();

	void setActivation(double activation);

	double getActivation();

	double getInputValue();

	void setInputValue(double inputSum);

	void setActivator(Activator activator);

	Activator getActivator();

	void setError(double error);

	double getError();

	void setThreshold(double threshold);

	double getThreshold();
	
	void storeThreshold();

	void restoreThreshold();

	void createSynapseTo(Neuron neuron);
	
	void setSynapseTo(Synapse synapse, Neuron neuron);

	void addInput(Synapse synapse);

	void removeInput(Synapse synapse);

	void removeOutput(Synapse synapse);

	void addOutput(Synapse synapse);public void setDesiredActivation(double activation);
	
	public double getDesiredActivation();

}