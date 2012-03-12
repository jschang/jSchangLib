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
 * 
 * @author schang
 */
public enum ClusterEventType {
	
	/**
	 * Triggered in terminate()
	 */
	NODE_TERMINATE_REQUEST,
	
	/**
	 * Triggered in notifyTermination() called by a child node after recording the termination.
	 */
	NODE_TERMINATED,
	
	/**
	 * Triggered in executeCommand() prior to even evaluating capacity.
	 */
	COMMAND_REQUESTED,
	
	/**
	 * Triggered in executeCommand() on the accepting node prior to doing anything at all.
	 */
	COMMAND_ACCEPTED,
	
	/**
	 * Triggered in executeCommand() immediately after a successful pass-off to the least busy child node.
	 */
	COMMAND_PROXIED,
	
	/**
	 * Triggered in notifyCommandExecuted() before doing anything else.
	 */
	COMMAND_COMPLETED,
	
	/**
	 * Triggered in getCommandInfo(), prior to gathering process/thread information.
	 */
	COMMAND_INFO_REQUESTED,
	
	/**
	 * Triggered when the status monitoring script dies
	 */
	ERROR_MONITOR_DIED,
	
	ERROR_AGGREGATE_MONITOR_DIED,
	
	ERROR_SHUTDOWN_MONITOR,
	
	/**
	 * Triggered when a method that calls all children fails on a child
	 */
	ERROR_DISSEMINATION,
	
	/**
	 * Triggered when a node provisioning fails
	 */
	ERROR_PROVISION_FAILED,
	
	/**
	 * Triggered when we fail to decommission this node
	 */
	ERROR_DECOMMISSION_FAILED,
	
	ERROR_COMMAND_FAILED,
	
	ERROR_EVENT_PROCESSING,
	
	/**
	 * Triggered when the cpu of this node and descendents drops below threshold
	 */
	THRESHOLD_CPU_LOW,
	
	/**
	 * Triggered when the cpu usage of this node and descendents rises above threshold
	 */
	THRESHOLD_CPU_HIGH,
	
	/**
	 * Triggered when the aggregate memory usage of this node rises above threshold
	 */
	THRESHOLD_MEM_HIGH,
	
	/**
	 * Triggered when the aggregate swapping of this node rises above threshold
	 */
	THRESHOLD_SWAP_HIGH
}
