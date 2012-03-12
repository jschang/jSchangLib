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

import javax.jws.*;

import com.jonschang.cluster.model.*;
import com.jonschang.cloud.*;

/**
 * The service or client of a single node within the cluster composite
 * 
 * All nodes in the cluster perform work.  At a certain point,
 * the number of requests being proxied through a node will
 * keep it busy to the point where it cannot handle actual
 * command processing.  It then can be considered more of an arterial node,
 * rather than a worker node.
 * 
 * The transition is transparent to clients and triggered
 * by the node capacity-remaining dropping below a pre-specified 
 * threshold.
 * 
 * From the client perspective there is a single cluster node.
 * 
 * This interface is intended to encompass the web-service operations only.
 * {@link ClusterNodeWSServer} contains the remainder of methods
 * that are callable on the server.
 * 
 * @author schang
 */
@WebService(name="ClusterNodeWS")
public interface ClusterNodeWS {
	
	/**
	 * Gets aggregate information about the node and it's children.  
	 * 
	 * Response is immediate and aggregated from statistics continually 
	 * monitored by the parent.
	 * 
	 * Events:
	 * 
	 * getNodeInfo() triggers a NODE_INFO_REQUESTED event
	 * 
	 * @param request The auth container for the node info request.
	 * @return The response container for the NodeInfo result.
	 */
	@WebResult(name="nodeInfo") NodeInfo getNodeInfo(
		@WebParam(name="auth") Authorization auth
	)  throws AuthenticationException, ClusterException;
	
	/**
	 * Set the configuration parameters of this node.
	 * @param config The NodeConfiguration of nodes within the cluster.
	 */
	@Oneway void setConfiguration(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="configuration") NodeConfiguration config
	)  throws AuthenticationException, ClusterException;
	
	/**
	 * @return The NodeConfiguration of this ClusterNodeWS.
	 */
	@WebResult(name="configuration") NodeConfiguration getConfiguration(
		@WebParam(name="auth") Authorization auth
	) throws AuthenticationException, ClusterException;
	
	/**
	 * Add a child to this ClusterNodeWS.  
	 * 
	 * Calls the setParentNode() method on the node passed in.
	 *  
	 * @param auth
	 * @param node
	 */
	@Oneway void addChild(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="nodeId") NodeId node
	) throws AuthenticationException, ClusterException;
	
	/**
	 * Set the parent node of this node.
	 * 
	 * Called by the parent once the parent obtains a WSDL response from the provisioned node.
	 * 
	 * @param rootNode The NodeId of the root node.
	 * @param parentNode The NodeId of the parent node.
	 * @param depth The depth of this node within the cluster.
	 */
	@Oneway void setParentNode(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="parentNodeId") NodeId parentNode
	) throws AuthenticationException, ClusterException;
	
	/**
	 * Executes a command on some node within the cluster.
	 * 
	 * If this node does not accept the command, then it is passed on to the child node
	 * that is lowest below capacity threshold.  Recall that NodeInfo is an aggregate of
	 * the usage of all descendents in the node hierarchy.  When the NodeCommandRequest
	 * is passed toward leaf-level, it is done so asynchronously.  A ClusterNodeWS service
	 * can distinguish between a client request and a request from another node by the
	 * private-key the Authorization value validates against.  The first private-key tested
	 * should be the cluster private-key, to save performance.
	 * 
	 * Once a command has reached an accepting node, the node calls notifyCommandAccepted()
	 * on the node the command was originally submitted to.
	 * 
	 * notifyCommandAccepted() populates the CommandId node url and the thread
	 * running executeCommand() then returns the completed CommandId object back
	 * to the requesting client.
	 * 
	 * After a NodeCommandRequest has been accepted by one of the nodes in the cluster,
	 * it is then the responsibility for the code of the command executed to
	 * communicate completion status to the client requesting the command.
	 * 
	 * Events:
	 * 
	 * executeCommand() triggers a COMMAND_REQUESTED event and either a COMMAND_ACCEPTED or a COMMAND_PROXIED event.
	 * In the event of an exception in the thread execution is passed to, a COMMAND_EXCEPTION event is triggered.
	 * If the command is executed in the shell, then a non-zero return triggers a COMMAND_FAILED event.
	 * 
	 * @return The url to the node that finally handled the request, so that further mediation by the parent is not required.
	 */
	@WebResult(name="commandInfo") CommandInfo executeCommand(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="commandRequest") CommandRequest command
	) throws AuthenticationException, NodeBusyException, NodeTerminatingException, ClusterException;
	
	/**
	 * Obtains the information relevant to an executed command.
	 * 
	 * Each node retains information about the commands it is running.  
	 * Once command information has been requested on a completed task, 
	 * the node forgets entirely about the command.
	 * 
	 * Clients may continuously poll the accepting node for CommandInfo.
	 * 
	 * Events:
	 * 
	 * getCommandInfo() triggers a COMMAND_INFO_REQUESTED event after command-info is acquired, but prior to return.
	 * 
	 * @param commandId The CommandId object returned by executeCommand() 
	 * @return Information relevant to the command.  CommandInfo content varies depending on the content of the NodeCommandRequest.
	 */
	@WebResult(name="commandInfo") CommandInfo getCommandInfo(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="commandId") CommandId commandId
	) throws AuthenticationException, CommandNotFoundException, ClusterException;
	
	/**
	 * Notifies the node to shut itself down.
	 * 
	 * When terminate() is called, the node stops accepting and proxying requests.
	 * Subsequence calls to executeCommand() will result in a NodeTerminatingException
	 * 
	 * After terminating running processes and 
	 * immediately prior to submission to the service provider, 
	 * the node calls notifyTermination() on the parent.
	 * 
	 * Events: 
	 * 
	 * terminate() triggers a NODE_TERMINATE_REQUEST event prior to the termination request being fulfilled.
	 */
	@Oneway void terminate(
		@WebParam(name="auth") Authorization auth
	)  throws AuthenticationException, NodeTerminatingException, ClusterException;
	
	/**
	 * Notify a parent node that a child node is shutting down and intends to have itself decommissioned.
	 * 
	 * If terminate() has been called on the child node and
	 * the managing node does not get this within a pre-specified amount of time,
	 * then an ERROR_NODE_HUNG event is triggered and ClusterNodeEvent s subscribing to
	 * the event are called.
	 * 
	 * notifyTerminate() triggers a NODE_TERMINATED event, after the termination has been logged.
	 * 
	 * @param response
	 */
	@Oneway void notifyTermination(
		@WebParam(name="auth") Authorization auth,
		@WebParam(name="terminationResponse") TerminationNotification response
	)  throws AuthenticationException, NodeTerminatingException, ClusterException;
}
