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

package com.jonschang.ai.network.feedforward;

import java.util.*;
import javax.swing.*;

import com.jonschang.ai.network.feedforward.*;
import com.jonschang.ai.network.*;

public class JPanelFeedForwardObserver<N extends FeedForward>
	extends JPanel
	implements JPanelNetworkTrainerObserver<N> {

	protected List<NetworkObserver<N>> observers;
	protected SingleNetworkTrainer<N> trainer;
	
	public <T extends NetworkObserver<N>> void attach(T networkObserver) {
		if( ! this.observers.contains(networkObserver) )
		{
			this.observers.add( networkObserver );
			networkObserver.setNetwork(this.getTrainer().getNetwork());
		}		
	}

	public <T extends NetworkObserver<N>> void detach(T networkObserver) {
		if( this.observers.contains(networkObserver) )
		{
			this.observers.remove(networkObserver);
			networkObserver.setNetwork(null);
		}
	}

	@Override
	public <T extends SingleNetworkTrainer<N>> T getTrainer() {
		return (T)trainer;
	}

	@Override
	public <T extends SingleNetworkTrainer<N>> void setTrainer(T trainer) {
		this.trainer = trainer;
	}
	
	@Override
	public void onBegin() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIterationBegin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIterationEnd() {
		// TODO Auto-generated method stub
		
	}
}
