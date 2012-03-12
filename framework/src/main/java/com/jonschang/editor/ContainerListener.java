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
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public class ContainerListener 
	implements FocusListener, MouseListener {

	private EditorDocument document;
	private JComponent panel;
	private JPopupMenu contextMenu;
	
	public ContainerListener(EditorDocument doc, JComponent panel) {
		super();
		this.document=doc;
		this.panel=panel;
		this.contextMenu=new DefaultContainerContextMenu(doc.getEditor(),panel);
	}
	
	public void focusGained(FocusEvent e) {
		this.document.getEditor().setFocusContainer(panel);
	}

	public void focusLost(FocusEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		this.document.getEditor().mouseEntered(e.getComponent());
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		
		if( this.panel instanceof ContainerTabbedPane ) {
			int atTab = ((ContainerTabbedPane)this.panel).indexAtLocation(e.getPoint().x,e.getPoint().y);
			if( atTab!=-1 ) {
				this.document.getEditor().mouseDownOnTabAt(
					this.panel,
					((ContainerTabbedPane)this.panel).getComponentAt(atTab));
			}
    	}
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
    	
   		this.document.getEditor().mouseReleased(e);
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.contextMenu.show(e.getComponent(),e.getX(), e.getY());
        }
    }

}
