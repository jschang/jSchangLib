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

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import java.util.Map;
import java.util.HashMap;

public class DefaultDocumentsMenu  extends JMenu {
	
	protected Editor editor;
	protected JComponent panel;
	protected Map<JCheckBoxMenuItem,EditorDocument> docs;
	
	public DefaultDocumentsMenu(Editor editor,JComponent panel)
	{
		super("Documents");
		this.panel = panel;
		this.editor = editor;
		docs = new HashMap<JCheckBoxMenuItem,EditorDocument>();
	}
	
	public void add(EditorDocument d) {
		// add
	}
	
	public void remove(EditorDocument d) {
		// remove
	}
	
	public void refresh()
	{
		for( EditorDocument d : this.editor.getDocuments() )
		{
			if( !docs.containsValue(d) )
			{
				// add a new menu item
				add(d);
			} 
		}
		for( Map.Entry<JCheckBoxMenuItem, EditorDocument> ent : docs.entrySet() )
		{
			if( ! this.editor.getDocuments().contains(ent.getValue()) )
			{
				// remove item from documents map
			}
		}
		this.invalidate();
	}
	
	private void uncheckAll()
	{
		for( JCheckBoxMenuItem menu : docs.keySet() )
			menu.setState(false);
	}
	
}