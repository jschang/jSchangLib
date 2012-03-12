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

import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import com.jonschang.editor.ContainerTabbedPane;
import com.jonschang.editor.EditorAction;

public class DefaultViewMenu extends JMenu {
	
	protected Editor editor;
	protected JComponent panel;
	
	public DefaultViewMenu(Editor editor, JComponent panel)
	{
		super("View");
		
		this.panel = panel;
		this.editor = editor;
		
		// add the different variety of network documents we can create
		// this may become more complicated, but this is currently all we
		// need
		if( panel!=null )
		{
			this.createSplitMenu();
			if( this.editor.getActiveDocument()!=null )
				this.createNewViewMenu();
		}
	}
	
	private void createNewViewMenu() {
		JMenu newViewOpts = new JMenu("New View");
		for( Object o : this.editor.getActiveDocument().getValidViews() )
		{
			Class c = (Class)o;
			EditorAction action = new EditorAction(c.getSimpleName(),this.editor,this.panel) {
				public void actionPerformed(ActionEvent e) {
					if( this.getEditor()!=null && this.getEditor().getFocusContainer()!=null )
					{
						try
						{
							Class c = (Class)this.getData();
							EditorView view = (EditorView)c.newInstance();
							view.setDocument( this.getEditor().getActiveDocument() );
							view.setEditor( this.getEditor() );
							this.getPanel().add( c.getSimpleName(), (JComponent)view );
							this.getPanel().validate();
						} catch(Exception exc) {
							com.jonschang.utils.StringUtils.stackTraceToString(exc);
						}						
					}
				}
			};
			action.setData(c);
			newViewOpts.add(action);
		}
		this.add(newViewOpts);
	}
	
	private void createSplitMenu() {
		JMenu splitOpts = new JMenu("Split/Join");
		
		splitOpts.add( 
				new EditorAction("into left and right",this.editor,this.panel) {
					public void actionPerformed(ActionEvent e) {
						if( this.getEditor()!=null && this.getEditor().getFocusContainer()!=null )
						{
							JComponent currentPanel = this.getPanel();
							JSplitPane newPane = new JSplitPane();
							java.awt.Container parent = currentPanel.getParent();
							
							int i=0;
							for( Component component : parent.getComponents() )
								if( component==currentPanel )
									break;
								else i++;
							
							parent.remove(currentPanel);
							parent.add(newPane,i);
							newPane.setLeftComponent(currentPanel);
							newPane.setRightComponent(
									this.getEditor().newContainerPanel(
										this.getEditor().getActiveDocument()));
							newPane.setOrientation(VERTICAL);
							newPane.getParent().validate();
						}
					}
				}
			);
		
		splitOpts.add( 
				new EditorAction("into top and bottom",this.editor,this.panel) {
					public void actionPerformed(ActionEvent e) {
						if( this.getEditor()!=null && this.getEditor().getFocusContainer()!=null )
						{
							JComponent currentPanel = this.getPanel();
							JSplitPane newPane = new JSplitPane();
							java.awt.Container parent = currentPanel.getParent();
							
							replacePanel(parent,currentPanel,newPane);
							
							newPane.setTopComponent(currentPanel);
							newPane.setBottomComponent(
									this.getEditor().newContainerPanel(
										this.getEditor().getActiveDocument()));
							newPane.setOrientation(HORIZONTAL);
							newPane.getParent().validate();
						}
					}
				}
			);
		
		splitOpts.add( 
				new EditorAction("join all under into one",this.editor,this.panel) {
					public void actionPerformed(ActionEvent e) {
						if( this.getEditor()!=null && this.getEditor().getFocusContainer()!=null )
						{
							JComponent currentPanel = this.getPanel();
							java.awt.Container parent = currentPanel.getParent();
							if( parent instanceof JSplitPane )
							{
								JComponent newPanel = this.getEditor().newContainerPanel(this.getEditor().getActiveDocument());
								collapseInto(parent,newPanel);
								replacePanel(parent.getParent(),parent,newPanel);
								newPanel.getParent().validate();
							}
						}
					}
				}
			);
		
		this.add(splitOpts);
	}
	
	private void collapseInto(java.awt.Container parent, java.awt.Container newPanel)
	{
		for( Component c : parent.getComponents() )
		{
			if( c instanceof ContainerTabbedPane )
			{
				for( Component b : ((ContainerTabbedPane)c).getComponents() )
					newPanel.add(b,b.getClass().getSimpleName());
			} else if( c instanceof JSplitPane) 
				collapseInto((JSplitPane)c,newPanel);
		}
	}
	
	private void replacePanel(java.awt.Container parent, java.awt.Container currentPanel, java.awt.Container newPane)
	{
		int i=0;
		for( Component component : parent.getComponents() )
			if( component==currentPanel )
				break;
			else i++;
		parent.remove(currentPanel);
		parent.add(newPane,i);
	}
	
}
