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

package com.jonschang.editor;

import java.awt.event.ActionEvent;

import javax.swing.JMenu;
import javax.swing.JComponent;

//import com.jonschang.ai.network.feedforward.FeedForwardDocument;

public class DefaultFileMenu extends JMenu {
	
	protected Editor editor;
	protected JComponent panel;
	
	public DefaultFileMenu(Editor editor,JComponent panel)
	{
		super("File");
		
		this.panel = panel;
		this.editor = editor;
		
		// add the different variety of network documents we can create
		// this may become more complicated, but this is currently all we
		// need
		JMenu newOpts = new JMenu("New Network...");
		
		newOpts.add( 
				new EditorAction("Feed Forward",this.editor,this.panel) {
					public void actionPerformed(ActionEvent e) {
						// create the composite network document
						/*FeedForwardDocument doc = new FeedForwardDocument();
						doc.setEditor(this.getEditor());
						this.getEditor().addDocument(doc);*/
					}
				}
			);		
		
		newOpts.add( 
				new EditorAction("Composite",this.editor,this.panel) {
					public void actionPerformed(ActionEvent e) {
						// create the composite network document
						// create a view in the largest available tabbed view panel
					}
				}
			);
		
		// add the "File" menu items under the file menu item
		this.add( newOpts );
	}
}
