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

package com.jonschang.math.vector;

import java.util.List;
import java.util.ArrayList;

/**
 * a vector tree node, where each node may branch off into multiple vectors
 * and each branching vector uses the current vector as origin 
 * @author schang
 */
public class VectorTreeNode extends VectorImpl {
	private List<VectorTreeNode> branches = null; 
	
	public void addBranch(VectorTreeNode node) {
		if( this.branches==null )
			this.branches = new ArrayList<VectorTreeNode>();
		if( !this.branches.contains(node) )
			this.branches.add(node);
	}
	
	public void removeBranch(VectorTreeNode node) {
		if( this.branches==null )
			return;
		if( this.branches.contains(node) )
			this.branches.remove(node);
	}
	
	public List<VectorTreeNode> getBranches() {
		return this.branches;
	}
}
