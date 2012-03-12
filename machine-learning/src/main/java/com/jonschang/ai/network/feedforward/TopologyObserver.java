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

import javax.swing.*;
import java.awt.*;
import com.jonschang.ai.network.*;

public class TopologyObserver extends JPanel implements JPanelNetworkObserver<FeedForward> {

	public FeedForward network;
	
	@SuppressWarnings("unchecked")
	public <T extends FeedForward> T getNetwork() {
		return (T) this.network;
	}
	
	public <T extends FeedForward> void setNetwork(T network) {
		this.network=network;
	}

	public void onCalculate() {
		this.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		TopologyDrawer drawer = new TopologyDrawer();
		drawer.setNetwork(this.network);
		drawer.draw( g, 0, 0, g.getClipBounds().width, g.getClipBounds().height );
	}
}
