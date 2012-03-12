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

import org.apache.cxf.*;
import org.apache.cxf.endpoint.*;

import com.jonschang.cluster.model.*;
import com.jonschang.utils.StringUtils;

import org.apache.log4j.*;

import java.util.regex.*;
import javax.xml.ws.*;

public class MockClusterNodeService implements ClusterNodeService {

	static private int current=1;
	static public int startPort=10001;
	private ClusterNodeWSServer service = null;
	public void setClusterNodeWSServer(ClusterNodeWSServer service) {
		this.service = service;
	}
	
	@Override
	public void decommission(NodeInfo node) throws TimeoutClusterException,
			ClusterException {
		Bus bus = BusFactory.getDefaultBus();
		ServerRegistry serverRegistry = bus.getExtension(ServerRegistry.class);
		synchronized(serverRegistry) {
			for( Server server : serverRegistry.getServers() ) {
				String address = server.getEndpoint().getEndpointInfo().getAddress();
				Logger.getLogger(this.getClass()).info("server endpoint: "+address);
				Pattern p = Pattern.compile(".*/node(\\d+)$");
				Matcher m = p.matcher(address);
				Matcher m2 = p.matcher(node.getNodeId().getUrl());
				if( m.matches() && m2.matches() && m.group(1).compareTo(m2.group(1))==0 ) {
					server.stop();
					break;
				} 
			}
		}
	}

	@Override
	synchronized public NodeInfo provision() throws TimeoutClusterException,
			ClusterException {
		current++;
		String url = "http://localhost:"+MockClusterNodeService.startPort+"/node"+current;
		ClusterNodeWSServer cnws = ClusterFactory.newEndpoint(url);
		UsageClusterEventHandler uceh = new UsageClusterEventHandler();
		uceh.setClusterNodeWSServer(cnws);
		cnws.subscribe(ClusterEventType.THRESHOLD_CPU_HIGH, uceh);
		cnws.subscribe(ClusterEventType.THRESHOLD_CPU_LOW, uceh);
		cnws.subscribe(ClusterEventType.NODE_TERMINATE_REQUEST, uceh);
		cnws.setClusterNodeService(this);
		while(true) {
			try {
				return ClusterFactory.newClient(url).getNodeInfo(service.newAuthorization());
			} catch(AuthenticationException e) {
				throw new ClusterException(e);
			} catch(WebServiceException e) {
				// really, this shouldn't happen
				Logger.getLogger(this.getClass()).error("Threw a WebServiceException: "+StringUtils.stackTraceToString(e));
			}
		}
	}

	@Override
	public void serviceInfo(NodeInfo node) throws TimeoutClusterException,
			ClusterException {
		// doesn't need to do anything
	}

}
