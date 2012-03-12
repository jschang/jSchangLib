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

import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import com.jonschang.editor.AbstractEditorView;

public class FeedForwardView extends AbstractEditorView<FeedForwardDocument> {
	
	public TopologyObserver topology;
	public FeedForwardContextMenu contextMenu;
	
	public FeedForwardView() {
		super();
		this.addFocusListener(this);
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.topology = new TopologyObserver();
		this.panel = this.topology;
		this.panel.setVisible(true);
		this.add(this.panel);
		this.setVisible(true);
		this.validate();
		this.panel.setOpaque(true);
		this.panel.repaint();
		this.contextMenu = new FeedForwardContextMenu();
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		if( this.getDocument()!=null )
		{
			this.topology.setNetwork( this.getDocument().getNetwork() );
			this.panel.setVisible(true);
			this.panel.validate();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if( e.isPopupTrigger() )
			this.contextMenu.show(e.getComponent(),e.getX(), e.getY());
	}
}
