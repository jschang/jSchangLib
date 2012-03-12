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

import java.util.GregorianCalendar;

import javax.xml.datatype.*;

import org.apache.cxf.jaxws.*;
import com.jonschang.cluster.event.*;
import com.jonschang.cluster.model.*;

public class ClusterFactory {
	/**
	 * Create a new ClusterEvent based on type
	 * @param type The event type to create a concrete ClusterEvent class for
	 * @return The appropriate implementor of ClusterEvent, otherwise a GenericClusterEvent holding the type from the ClusterEventType enum.
	 */
	public static ClusterEvent newEvent(ClusterEventType type) {
		if( type.toString().startsWith("THRESHOLD_") )
			return new UsageClusterEvent(type); 
		
		if( type.toString().startsWith("COMMAND_") )
			return new CommandClusterEvent(type);
		
		if( type.toString().startsWith("ERROR_") )
			return new ErrorClusterEvent(type);
		
		return new GenericClusterEvent(type);
	}

	/**
	 * Creates a new client for this concrete ClusterNodeWS
	 * 
	 * @param url The url of the host to create the client for
	 * @return a ClusterNodeWS client proxy bean
	 * @throws ClusterException
	 */
	public static ClusterNodeWS newClient(String url) throws ClusterException { 
		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		factoryBean.setServiceClass(ClusterNodeWS.class);
		factoryBean.setAddress(url);
		return (ClusterNodeWS)factoryBean.create();
	}

	/**
	 * Creates a new ClusterNodeWS end-point backed by ImplClusterNodeWS
	 * 
	 * If the url passed in is at localhost, then the string "localhost"
	 * will be replaced by whatever the ClusterNodeService thinks the
	 * correct ip address should be.  Subsequent calls to {@link ClusterNodeWS::getNodeInfo()}
	 * will return the url with the ip address replaced.
	 * 
	 * @uses JaxWSServerFactoryBean
	 * @param url The url to publish the endpoint at.  Ignored after the initial call.
	 * @return The instance of ImplClusterNodeWS.
	 * @throws ClusterException
	 */
	public static ClusterNodeWSServer newEndpoint(String url) throws ClusterException {
		ClusterNodeWSServer ws = new ImplClusterNodeWS(url);
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
		sf.setServiceClass(ClusterNodeWS.class);
		sf.setServiceBean(ws);
		sf.setAddress(url);
		sf.create();
		return ws;
	}

	/**
	 * Create a 
	 * @return A new NodeInfo object from the JAXB generated set, with all the non-primitives initialized
	 * @throws ClusterException
	 */
	public static NodeInfo newNodeInfo() throws ClusterException {
		return ClusterModelTools.initializeNodeInfo(new NodeInfo());
	}
	
	public static CommandRequest newJVMCommandRequest() {
		CommandRequest toRet = new CommandRequest();
		FactoryMethodCommand fmc = new FactoryMethodCommand();
		toRet.setFactoryMethodCommand( fmc );
		MethodCall mc = new MethodCall();
		fmc.setExecutionMethod(mc);
		mc = new MethodCall();
		fmc.setFactoryMethod(mc);
		return toRet;
	}
	
	public static CommandRequest newShellCommandRequest() {
		CommandRequest toRet = new CommandRequest();
		ShellCommand fmc = new ShellCommand();
		toRet.setShellCommand( fmc );
		return toRet;
	}
	
	public static XMLGregorianCalendar newXMLGregorianCalendar() throws ClusterException {
		try {
			XMLGregorianCalendar newCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			return newCal;
		} catch( DatatypeConfigurationException dce ) {
			throw new ClusterException(dce);
		}
	}
}
