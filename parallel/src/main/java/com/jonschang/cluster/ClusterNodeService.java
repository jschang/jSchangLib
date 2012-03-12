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

package com.jonschang.cluster;

import com.jonschang.cluster.model.*;

/**
 * An abstraction to loosely-couple ClusterNodeWS to a source of nodes and node information.
 * @author schang
 */
public interface ClusterNodeService {
	
	/**
	 * Provision a new node for use in the cluster.
	 * 
	 * Blocks till the node is provisioned.  
	 * Best to run in a separate thread.
	 * 
	 * @return
	 * @throws ClusterException
	 */
	NodeInfo provision() throws TimeoutClusterException, ClusterException;
	
	/**
	 * Decommission a node.
	 * 
	 * Blocks till the node is shutdown.
	 * Best to run in a separate thread.
	 * 
	 * @param node
	 * @throws ClusterException
	 */
	void decommission(NodeInfo node) throws TimeoutClusterException, ClusterException;
	
	/**
	 * Fills in the ServiceInfo object of the NodeInfo object
	 * with the ClusterNodeService info of this node.
	 * 
	 * @param node
	 * @throws ClusterException
	 */
	void serviceInfo(NodeInfo node) throws TimeoutClusterException, ClusterException;
}
