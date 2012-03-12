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


public interface NetworkTrainerObserver<N extends Network> {
	
	public <T extends SingleNetworkTrainer<N>> void setTrainer(T trainer) throws NetworkException;
	
	public <T extends SingleNetworkTrainer<N>> T getTrainer();
	
	/**
	 * attach a network observer for use by the training observer
	 */
	public <T extends NetworkObserver<N>> void attach(T networkObserver);
	
	/**
	 * detach an available network observer
	 */
	public <T extends NetworkObserver<N>> void detach(T networkObserver);
	
	/**
	 * called at the beginning of each training iteration
	 */
	public void onIterationBegin() throws NetworkException;
	
	/**
	 * called at the end of each training iteration
	 */
	public void onIterationEnd() throws NetworkException;
	
	/**
	 * called at the end of all training iterations
	 */
	public void onEnd() throws NetworkException;
	
	/**
	 * called at the beginning of all training iterations
	 */
	public void onBegin() throws NetworkException;
}
