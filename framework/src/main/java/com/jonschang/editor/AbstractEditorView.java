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

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

abstract public class AbstractEditorView<T extends EditorDocument> 
	extends JPanel implements EditorView<T> {

	protected T document;
	protected Editor editor;
	protected JPanel panel;
	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {}

	public <C extends T> void setDocument(C document) {
		this.document = document;
	}
	public <C extends T> C getDocument() {
		return (C)this.document;
	}
	
	public Editor getEditor() {
		return this.editor;
	}
	public void setEditor(Editor editor) {
		this.editor = editor;
	}
	
	public JPanel getPanel() {
		return this.panel;
	}
}
