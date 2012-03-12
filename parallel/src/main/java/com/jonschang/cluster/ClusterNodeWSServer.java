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

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.jonschang.cluster.model.Authorization;
import com.jonschang.cluster.model.NodeConfiguration;
import com.jonschang.cluster.model.NodeInfo;

@WebService(name="ClusterNodeWS")
public interface ClusterNodeWSServer extends ClusterNodeWS {
	@WebMethod(exclude=true) NodeConfiguration getConfiguration();
	
	/**
	 * A convenience method to return a clone of the aggregateNodeInfo of this node.
	 * @return
	 * @throws ClusterException
	 */
	@WebMethod(exclude=true) NodeInfo getNodeInfo() throws ClusterException;
	
	/**
	 * Fires off a ClusterEvent.  
	 * 
	 * All ClusterEventHandler's subscribing to an event are run, in sequence,
	 * in a separate thread, the EventRunner.
	 * 
	 * @param event
	 */
	@WebMethod(exclude=true) void triggerEvent(ClusterEvent event);
	
	/**
	 * Subscribes a ClusterEventHandler to a ClusterEvent type.
	 * @param event
	 * @param handler
	 */
	@WebMethod(exclude=true) void subscribe(ClusterEventType event, ClusterEventHandler handler);
	
	/**
	 * Unsubscribes a ClusterEventHandler from a ClusterEvent type.
	 * @param event
	 * @param handler
	 */
	@WebMethod(exclude=true) void unsubscribe(ClusterEventType event, ClusterEventHandler handler);
	
	/**
	 * Set the ClusterNodeService with which to provision new Nodes
	 * 
	 * @param service
	 */
	@WebMethod(exclude=true) void setClusterNodeService(ClusterNodeService service) throws ClusterException;
	
	/**
	 * @return The ClusterNodeService this ClusterNodeWS uses to provision and decommission nodes.
	 * @throws ClusterException
	 */
	@WebMethod(exclude=true) ClusterNodeService getClusterNodeService() throws ClusterException;
	
	/**
	 * @param key The secret key that Authorization requests coming from the cluster will generate hashes with.
	 */
	@WebMethod(exclude=true) void setNodeSecretKey(String key);
	
	/**
	 * @param key The secret key that Authorization requests coming from the user will generate hashes with.
	 */
	@WebMethod(exclude=true) void setUserSecretKey(String key);
	
	/**
	 * Throws an AuthenticationException if the Authorization object
	 * fails to pass authentication.
	 * 
	 * @param auth The hash and value to authenticate.
	 * @return true if the hash is from another node in the cluster, else false
	 * @throws ClusterException, AuthenticationException
	 */
	@WebMethod(exclude=true) boolean authenticate(Authorization auth) throws AuthenticationException, ClusterException;
	
	/**
	 * A means of getting the current status of the node.
	 * 
	 * No setter is provided because the concrete implementation alone changes 
	 * this based on other method calls.
	 * 
	 * @return The current status of this node. 
	 */
	@WebMethod(exclude=true) NodeStatus getNodeStatus();
	
	@WebMethod(exclude=true) public void setNodeStatus(NodeStatus status);
	
	/**
	 * Generates a valid hash/value pair using the node secret key of the cluster.
	 * @return An Authorization object containing a randomly generated hash/value pair.
	 * @throws ClusterException
	 */
	@WebMethod(exclude=true) Authorization newAuthorization() throws ClusterException;
	
	/**
	 * A pass through method to the thread pool used by the web-service.
	 * @param cmd The Runnable to execute
	 */
	@WebMethod(exclude=true) void execute(Runnable cmd);
	
	/**
	 * Tells all child nodes to complete their current commands and then exit.
	 * 
	 * Provides event handlers a means of terminated downstream nodes.
	 * 
	 * @throws ClusterException
	 */
	@WebMethod(exclude=true) void terminateAllChildren() throws NodeBusyException, NodeTerminatingException, ClusterException;;
	
	/**
	 * @return The number of immediate children this node proxys commands to.
	 */
	@WebMethod(exclude=true) int childCount();
	
	/**
	 * @return true if this node is currently executing any commands, else false.
	 */
	@WebMethod(exclude=true) boolean isExecutingCommands();
}
