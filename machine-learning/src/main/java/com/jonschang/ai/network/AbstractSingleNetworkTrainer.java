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

public abstract class AbstractSingleNetworkTrainer<N extends Network> extends AbstractNetworkTrainer<N> implements SingleNetworkTrainer<N> {

	protected List<NetworkTrainerObserver<N>> observers = new ArrayList<NetworkTrainerObserver<N>>();
	protected N network;
	
	public N getNetwork(){ return this.network; }

	public void setNetwork(N networkToTrain) { this.network=networkToTrain; }
	
	public void attachObserver(NetworkTrainerObserver<N> trainingObserver) throws NetworkException {
		if( !this.observers.contains(trainingObserver) )
		{
			this.observers.add(trainingObserver);
			trainingObserver.setTrainer(this);
		}
	}

	public void detachObserver(NetworkTrainerObserver<N> trainingObserver) throws NetworkException {
		if( this.observers.contains(trainingObserver) )
		{
			this.observers.remove(trainingObserver);
			trainingObserver.setTrainer(null);
		}
	}

	@Override protected void onBegin() throws Exception {
		for( NetworkTrainerObserver<N> observer : this.observers )
			observer.onBegin();
	}
	@Override protected void onIterationBegin() throws Exception {
		for( NetworkTrainerObserver<N> observer : this.observers )
			observer.onIterationBegin();
	}
	@Override protected void onIterationEnd() throws Exception {
		for( NetworkTrainerObserver<N> observer : this.observers )
			observer.onIterationEnd();
	}
	@Override protected void onEnd() throws Exception {
		for( NetworkTrainerObserver<N> observer : this.observers )
			observer.onEnd();
	}
	
}
