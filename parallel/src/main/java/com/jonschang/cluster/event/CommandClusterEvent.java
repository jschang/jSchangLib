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

package com.jonschang.cluster.event;

import com.jonschang.cluster.*;
import com.jonschang.cluster.model.*;

public class CommandClusterEvent extends GenericClusterEvent {

	private NodeId acceptingNode;
	private NodeId nodePassedTo;
	private CommandRequest commandRequest;
	private CommandExecutor commandExecutor;
	
	public void setAcceptingNode(NodeId node) {
		acceptingNode = node;
	}
	public NodeId getAcceptingNode() {
		return ClusterModelTools.cloneNodeId(acceptingNode);
	}
	
	public void setNodePassedTo(NodeId node) {
		nodePassedTo = node;
	}
	public NodeId getNodePassedTo() {
		return ClusterModelTools.cloneNodeId(nodePassedTo);
	}
	
	public void setCommandRequest(CommandRequest id) {
		commandRequest = id;
	}
	public CommandRequest getCommandRequest() {
		return ClusterModelTools.cloneCommandRequest(commandRequest);
	}
	
	public void setCommandExecutor(CommandExecutor id) {
		commandExecutor = id;
	}
	public CommandExecutor getCommandExecutor() {
		return commandExecutor;
	}
	
	public CommandClusterEvent(ClusterEventType type) {
		super(type);
		setType(type);
	}

}
