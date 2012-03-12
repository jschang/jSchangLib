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
 * Executes any of the command-types accepted by the Cluster
 * @author schang
 */
public interface CommandExecutor extends Runnable {
	/**
	 * Set the client of the command's originating node.
	 * @param client
	 */
	void setCommandRequest(CommandRequest request);
	CommandRequest getCommandRequest();
	
	void setCommandId(CommandId id);
	CommandId getCommandId();
	
	/**
	 * @return The thread this CommandExecutor is running in.
	 */
	Thread getThread();
	
	/**
	 * @return The amount of cpu time in milliseconds the executor used
	 */
	Long getCpuTime();
	
	/**
	 * @return The amount of time in milliseconds the executor has been running (or ran)
	 */
	Long getDuration();
	
	/**
	 * Get information about the running/run command.
	 * 
	 * @return Relevant information packed into a CommandInfo object.
	 * @throws ClusterException
	 */
	CommandInfo getCommandInfo() throws ClusterException;
	
	void setClusterNodeWSServer( ClusterNodeWSServer service );
	
	ClusterNodeWSServer getClusterNodeWSServer();
	
	/**
	 * Determine if the command is still executing.
	 * 
	 * It's probably best to use the thread of the CommandExecutor to do the
	 * major work of the command.  If the command spawns another thread so
	 * that the CommandExecutor may return, then the UsageClusterEventHandler
	 * may prematurely shut-down the machine.
	 * 
	 * For some types of commands, this may be desirable.  For instance,
	 * if the command is intended to bring up another service.
	 * 
	 * @return true if the command is still executing, else false.
	 */
	boolean isRunning();
}
