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

import java.awt.Graphics;

import com.jonschang.ai.network.feedforward.*;

/**
 * Charts the Square Root of the Mean Squared Error of a 
 * FeedForward Neural Network over the course of training
 * 
 * @author schang
 *
 * @param <N>
 */
public class MSEChartFFO<N extends FeedForward> extends JPanelFeedForwardObserver<N> {
	
	/**
	 * the number of seconds between update
	 */
	private int samplePeriod=1;
	
	public MSEChartFFO() {
		
	}
	
	public void setSamplePeriod(int seconds) {
		samplePeriod = seconds;
	}
	public int getSamplePeriod() {
		return samplePeriod;
	}
	
	@Override
	public void onIterationEnd() {
		super.onIterationEnd();
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		TopologyDrawer drawer = new TopologyDrawer();
		drawer.setNetwork(this.trainer.getNetwork());
		drawer.draw( g, 0, 0, g.getClipBounds().width, g.getClipBounds().height );
	}
}
