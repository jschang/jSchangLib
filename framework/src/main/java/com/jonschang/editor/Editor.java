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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.awt.Component;

import javax.swing.*;

@SuppressWarnings(value="unchecked")
public class Editor extends WindowAdapter {
	
	private JFrame mainFrame;
	private JMenuBar mainMenuBar;
	private JComponent focusContainer;
	
	private List<EditorDocument> openDocuments = new ArrayList<EditorDocument>();
	private EditorDocument activeDocument;
	
	private Component mouseDownOnTabAtParent;
	private Component mouseDownOnTab;
	private Component mouseEntered;

	public Editor()
	{
		this.mainMenuBar = new JMenuBar();
		createMainMenu();
		this.mainMenuBar.setVisible(true);
		
		this.mainFrame = new JFrame();		
		this.mainFrame.setVisible(true);
		this.mainFrame.addWindowListener(this);
		this.mainFrame.setBounds(0,0,600,400);
		this.mainFrame.setJMenuBar(mainMenuBar);
		this.mainFrame.validate();
	}
	
	public void addDocument(EditorDocument doc) {
		if( ! this.openDocuments.contains(doc) )
			this.openDocuments.add(doc);
		this.setActiveDocument(doc); 
		refreshDocumentMenu();
	}
	
	public void mouseDownOnTabAt(Component parent,Component component) {
		this.mouseDownOnTabAtParent=parent;
		this.mouseDownOnTab=component;
	}
	
	public void mouseReleased(MouseEvent e) {
		if( this.mouseEntered!=null
			&& this.mouseEntered!=this.mouseDownOnTabAtParent 
			&& this.mouseEntered instanceof ContainerTabbedPane 
			&& this.mouseDownOnTabAtParent instanceof ContainerTabbedPane ) {
			((ContainerTabbedPane)this.mouseDownOnTabAtParent).remove(this.mouseDownOnTab);
			((ContainerTabbedPane)this.mouseEntered).add(this.mouseDownOnTab,this.mouseDownOnTab.getClass().getSimpleName());
			this.mouseDownOnTabAtParent.invalidate();
			this.mouseEntered.invalidate();
			this.mainFrame.validate();
			this.mouseDownOnTabAtParent.validate();
		}
		this.mouseDownOnTabAtParent=null;
		this.mouseDownOnTab=null;
		this.mouseEntered=null;
	}
	
	public void mouseEntered(Component p) {
		this.mouseEntered = p;
	}
	
	public void refreshDocumentMenu() {
		
	}
	
	public void setFocusContainer(JComponent focus) {
		this.focusContainer=focus;
	}
	
	public JComponent getFocusContainer() {
		return this.focusContainer;
	}
	
	public JComponent newContainerPanel(EditorDocument doc) {
		JComponent panel = new ContainerTabbedPane();
		ContainerListener listener = new ContainerListener(doc,panel);
		panel.addFocusListener(listener);
		panel.addMouseListener(listener);
		return panel;
	}
	
	public List<EditorDocument> getDocuments() {
		return this.openDocuments;
	}
	
	public void setActiveDocument(EditorDocument doc)
	{
		if( this.activeDocument!=null && this.mainFrame.isAncestorOf(this.activeDocument.getRootComponent()) )
			this.mainFrame.remove(this.activeDocument.getRootComponent());
		this.activeDocument=doc;
		this.mainFrame.add(this.activeDocument.getRootComponent());
		this.mainFrame.validate();
	}
	
	public EditorDocument getActiveDocument() {
		return this.activeDocument;
	}
	
	public void createMainMenu()
	{			
		this.mainMenuBar.add( new DefaultFileMenu(this,null) );
		this.mainMenuBar.add( new DefaultViewMenu(this,null) );
		this.mainMenuBar.add( new DefaultDocumentsMenu(this,null) );
	}
		
	@Override
	public void windowClosing(WindowEvent e)
	{
		System.exit(0);
	}
	
	public static void main(String[] args)
	{
		new Editor();
	}
}
