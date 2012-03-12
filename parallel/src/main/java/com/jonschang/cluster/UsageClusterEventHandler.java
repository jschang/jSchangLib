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

import com.jonschang.utils.*;
import com.jonschang.cluster.model.*;
import com.jonschang.cluster.event.*;
import org.apache.log4j.*;

/**
 * Either provisions or decommissions the node, depending on the event passed in.
 */
public class UsageClusterEventHandler implements ClusterEventHandler {

	private ClusterNodeWSServer service;
	private ThreadSafeValue<Boolean> provisioning = new ThreadSafeValue<Boolean>(false);
	private ThreadSafeValue<Boolean> decommissioning = new ThreadSafeValue<Boolean>(false);
	
	@Override
	public void setClusterNodeWSServer(ClusterNodeWSServer webService) {
		service = webService;
	}
	
	/**
	 * Either provisions or decommissions the node, depending on the event passed in.
	 * 
	 * Must be synchronized so that we only process one of these types of events at a time.
	 */
	public synchronized void process(ClusterEvent event) throws ClusterException {
		NodeInfo nodeInfo = service.getNodeInfo();
		Thread.currentThread().setName(nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
		NodeConfiguration configuration = service.getConfiguration();
		try {
			synchronized(configuration) { synchronized(event) {
				if( ( event.getType() == ClusterEventType.THRESHOLD_CPU_HIGH 
					|| event.getType() == ClusterEventType.THRESHOLD_MEM_HIGH 
					|| event.getType() == ClusterEventType.THRESHOLD_SWAP_HIGH )
					// there's no point in provisioning if we're shutting down
					&& service.getNodeStatus() != NodeStatus.SHUTTING_DOWN
					&& ! decommissioning.value()
					// we only provision one new node at a time
					// if there's really a need after the new node is
					// provisioned, then the event will be thrown again
					&& ! provisioning.value()
				    && configuration.getMaxNodeDepth() > nodeInfo.getDepth().shortValue()
					&& configuration.getMaxChildren() > service.childCount()
				) {
					// so that we don't try to provision more than
					// one new node at a time, flip this flag
					provisioning.value(true);
					service.execute(new NodeProvisioner());
				}
			} }
		} catch( Exception e ) {
			Logger.getLogger(this.getClass()).error("Threw an exception provisioning the node. "+StringUtils.stackTraceToString(e));
			ErrorClusterEvent ce = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_PROVISION_FAILED);
			ce.setException(e);
			service.triggerEvent( ce );
		} 
		
		try {
			synchronized(configuration) { synchronized(event) {
				
				if( nodeInfo.getParentNodeId()==null || nodeInfo.getParentNodeId().getUrl()==null || nodeInfo.getParentNodeId().getUrl().length()==0 ) {
					Logger.getLogger(this.getClass()).warn("NOT shutting down the root node");
				} else
				
				// we'll allow the decommissioning to continue while provisioning
				if( ( event.getType() == ClusterEventType.THRESHOLD_CPU_LOW
						|| event.getType() == ClusterEventType.NODE_TERMINATE_REQUEST )
						&& ! decommissioning.value() 
				) {
					// so that we don't try to decommission the node
					// more than once, flip this flag till exit.
					decommissioning.value(true);
					service.execute(new NodeDecommissioner());
				}
			} }
		} catch( Exception e ) {
			Logger.getLogger(this.getClass()).error("Threw an exception decommissioning the node. "+StringUtils.stackTraceToString(e));
			ErrorClusterEvent ce = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_DECOMMISSION_FAILED);
			ce.setException(e);
			service.triggerEvent( ce );
		} finally {
			Thread.currentThread().setName("terminated - "+nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
		}
	}

	private class NodeProvisioner implements Runnable {
		public void run() {
			NodeInfo nodeInfo=null; 
			try {			
				nodeInfo = service.getNodeInfo();
				Thread.currentThread().setName(nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
				if( service.getClusterNodeService()!=null ) {
					NodeInfo newNode = service.getClusterNodeService().provision();
					if( newNode.getNodeId()==null || newNode.getNodeId().getUrl()==null || newNode.getNodeId().getUrl().length()==0 )
						throw new ClusterException("ClusterNodeService did not return a url for a newly provisioned node.");
					service.addChild(service.newAuthorization(),newNode.getNodeId());
				} else Logger.getLogger(this.getClass()).warn("Would have used the ClusterNodeService to provision another node at this point, except one isn't set.");
			} catch( Exception e ) {
				Logger.getLogger(this.getClass()).error(StringUtils.stackTraceToString(e));
				ErrorClusterEvent ce = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_PROVISION_FAILED);
				ce.setException(e);
				service.triggerEvent( ce );
			} finally {
				provisioning.value(false);
				Thread.currentThread().setName("terminated - "+nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
			}
		}
	}
	
	private class NodeDecommissioner implements Runnable {
		public void run() {
			NodeInfo nodeInfo=null;
			String threadName=null;
			try {		
				nodeInfo = service.getNodeInfo();
				threadName = nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName();
				Thread.currentThread().setName(threadName);
				
				// i'm nervous about letting any other class effect the node status
				// via any means other than triggering events...
				// but i need the NodeStatus.SHUTTING_DOWN at this point
				service.setNodeStatus( NodeStatus.SHUTTING_DOWN );
				
				// if we allow the decommissioning to continue,
				// in the midst of a provisioning, 
				// then we won't be aware of the new child
				// when we terminate...and an orphan will be created
				while(provisioning.value())
					Thread.sleep(100);
				
				// if the node hasn't reached the minimum lifetime, then don't bother to do anything
				NodeConfiguration config = service.getConfiguration(service.newAuthorization());
				Long startTime = nodeInfo.getStarted().toGregorianCalendar().getTimeInMillis();
				Long now = new java.util.Date().getTime();
				if( !( startTime + (config.getMinLifetime()*1000) < now ) ) {
					Logger.getLogger(this.getClass()).info("Not decommissioning the node yet, it hasn't really lived long enough.");
					decommissioning.value(false);
					return;
				}
				
				// notify the parent, if there is one, that we're  intending to terminate
				NodeId parent = nodeInfo.getParentNodeId();
				if( parent!=null && parent.getUrl()!=null && parent.getUrl().length()>0 ) {
					ClusterNodeWS parentService = ClusterFactory.newClient(parent.getUrl());
					TerminationNotification response = new TerminationNotification();
					response.setNodeId(nodeInfo.getNodeId());
					parentService.notifyTermination(service.newAuthorization(), response);
				}
				
				// wait for children to terminate
				// in theory we should be aware of all children at this point
				// because we waiting for any pending provisioning to complete
				service.terminateAllChildren();
				Thread.currentThread().setName(threadName+" - terminating children");
				Logger.getLogger(this.getClass()).debug("Waiting for children to terminate");
				while( service.childCount() > 0 ) {
					Thread.sleep(100);
				}
				
				// wait for executing commands to complete
				Thread.currentThread().setName(threadName+" - completing commands");
				Logger.getLogger(this.getClass()).debug("Waiting for running commands to complete");
				while( service.isExecutingCommands() ) {
					// TODO: add a timeout here to trigger something
					Thread.sleep(100);
				}
				
				if( service.getClusterNodeService()!=null ) {
					// use the cloud service to "self-destruct"
					Logger.getLogger(this.getClass()).info("Calling the decommission() method of the ClusterNodeService...this MachineInstance will be shut down.");
					service.getClusterNodeService().decommission(nodeInfo);
				} else Logger.getLogger(this.getClass()).warn("Would have used the ClusterNodeService to terminate at this point, except it's not setup in the ClusterNodeWS.");
				
			} catch( Exception e ) {
				Logger.getLogger(this.getClass()).error("Threw an exception decommissioning the node. "+StringUtils.stackTraceToString(e));
				ErrorClusterEvent ce = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_DECOMMISSION_FAILED);
				ce.setException(e);
				service.triggerEvent( ce );
			} finally {
				Thread.currentThread().setName("terminated - "+threadName);
				// we only decommission a node once, so...we can leave that flag false
			}
		}
	}
}
