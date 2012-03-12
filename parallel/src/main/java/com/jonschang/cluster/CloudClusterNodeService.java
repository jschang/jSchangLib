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

import org.apache.log4j.*;
import java.util.*;
import com.jonschang.cloud.CloudException;
import com.jonschang.cloud.CloudService;
import com.jonschang.cloud.MachineInstance;
import com.jonschang.cloud.MachineStatus;
import com.jonschang.cluster.model.*;
import com.jonschang.utils.StringUtils;

public class CloudClusterNodeService implements ClusterNodeService {
	
	/**
	 * @return The number of seconds signaling an error in provisioning
	 */
	public Long getProvisionTimeout() {
		return provisionTimeout;
	}
	public void setProvisionTimeout(Long millis) {
		provisionTimeout = millis;
	}
	/**
	 * Default provision timeout is 5 minutes
	 */
	Long provisionTimeout = 300000L;
	
	
	/**
	 * @return The ClusterNodeWS of this node.
	 */
	public ClusterNodeWSServer getClusterNodeWSServer() {
		return clusterNodeWS;
	}
	public void setClusterNodeWSServer(ClusterNodeWSServer ws) {
		clusterNodeWS = ws;
	}
	private ClusterNodeWSServer clusterNodeWS = null;
	
	/**
	 * @return The CloudService used to provision new MachineInstances
	 */
	public CloudService<?> getCloudService() {
		return cloudService;
	}
	public void setCloudService(CloudService<?> service) throws ClusterException {
		cloudService = service;
	}
	private CloudService cloudService = null;
	
	/**
	 * @return The MachineInstance prototype to clone() for provisioning new nodes.
	 */
	public MachineInstance getMachineInstancePrototype() {
		return machineInstancePrototype;
	}
	public void setMachineInstancePrototype(MachineInstance prototype) {
		machineInstancePrototype = prototype;
	}
	private MachineInstance machineInstancePrototype = null;
	
	public NodeInfo provision() throws ClusterException, TimeoutClusterException {
		try {
			Date start = new Date();
			
			// provision a new instance and block till it is RUNNING
			MachineInstance newInstance = cloudService.provision(machineInstancePrototype);
			while( newInstance.getStatus() != MachineStatus.RUNNING ) { 
				newInstance = cloudService.status(newInstance);
				Thread.sleep(5000);
				if( new Date().getTime()-start.getTime() > provisionTimeout ) 
					throw new TimeoutClusterException("Provisioning a new node exceeded the allowed timeout.");
			}
			
			// TODO: Determine if I need to assume the path information is the same; there's got to be a cleaner way to do this.
			String origUrl = this.clusterNodeWS.getNodeInfo().getNodeId().getUrl();
			java.net.URL url = new java.net.URL(origUrl);
			String strUrl = origUrl.replace(url.getHost(), newInstance.getPrivateIp());
			
			// though the instance may have an Ip, the ClusterNodeWS may not be up yet
			// so block until we can get the NodeInfo from the horses mouth.
			ClusterNodeWS client = ClusterFactory.newClient(strUrl);
			NodeInfo newNode = null;
			while( newNode == null ) {
				try {
					newNode = client.getNodeInfo(clusterNodeWS.newAuthorization());
				} catch (Exception e) {
					Thread.sleep(5000);
					if( new Date().getTime()-start.getTime() > provisionTimeout ) 
						throw new TimeoutClusterException("Provisioning a new node exceeded the allowed timeout.",e);
				}
			}
			
			return newNode;
		} catch( Exception e ) {
			throw new ClusterException(e);
		}
	}
	
	public void decommission(NodeInfo node) throws ClusterException {
		try {
		} catch( Exception e ) {
			throw new ClusterException(e);
		}
	}
	
	public void serviceInfo(NodeInfo node) throws ClusterException {
		try {
			MachineInstance myInst = cloudService.getMyInstance();
			node.getServiceInfo().setAny( myInst );
			node.getServiceInfo().setService(cloudService.getClass().getName());
		} catch( CloudException e ) {
			throw new ClusterException("A CloudException was thrown determining the MachineInstance for this server in the Cloud.  Running this outside of the service?  "+StringUtils.stackTraceToString(e));
		}
	}
}
