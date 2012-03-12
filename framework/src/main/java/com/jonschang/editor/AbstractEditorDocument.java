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

import java.util.List;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.io.File;

public class AbstractEditorDocument<T extends EditorView> implements EditorDocument<T> {
	private Editor editor;
	private List<Class<T>> validViewClasses;
	private JComponent rootPanel;
	private String name;
	private File file;
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		if( name==null )
			name=this.getClass().getSimpleName();
		return name;
	}
	
	public Editor getEditor() {
		return editor;
	}
	public void setEditor(Editor editor) {
		this.editor = editor;		
	}
	
	public List<Class<T>> getValidViews() {
		if( this.validViewClasses == null )
			this.validViewClasses = new ArrayList<Class<T>>();
		return this.validViewClasses;
	}

	public void setValidViews(List<Class<T>> viewClasses) {
		this.validViewClasses = viewClasses;		
	}
	
	public void setRootComponent(JComponent component) {
		this.rootPanel = component;
		this.editor.setActiveDocument(this);
	}
	
	public JComponent getRootComponent() {
		if( this.rootPanel==null )
		{
			this.rootPanel = this.editor.newContainerPanel(this);
			this.editor.setFocusContainer(this.rootPanel);
		}
		return this.rootPanel;
	}
}
