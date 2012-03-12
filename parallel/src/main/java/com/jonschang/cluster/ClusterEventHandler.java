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

/**
 * Respond to an event on a node within the Cluster.
 * 
 * ClusterEventHandle objects are configured for the entire Cluster
 * and each event handler should be programmed to be thread-safe.
 * In a Cluster where nodes A, B, and C have been provisioned,
 * each will have a single unique instance of each ClusterEventHandler
 * registered to the Cluster.
 * 
 * ClusterEvent registration is done at the creation of the node
 * and prior to the publishing of the ClusterNodeWS.
 * 
 * @author schang
 */
public interface ClusterEventHandler {
	void setClusterNodeWSServer(ClusterNodeWSServer webService);
	void process(ClusterEvent event) throws ClusterException;
}
