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

package com.jonschang.ai.ga;

import javax.jws.*;
import javax.xml.ws.Endpoint;
import java.util.*;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.log4j.Logger;

public class GeneticAlgWSEndpointsConfigurator {
	private String hostName = "localhost";
	private Integer port = 8000;
	private String path = "/";
	private String protocol = "http";
	private GeneticAlgWSManagerEndpoint manager = null;
	private GeneticAlgWSNodeEndpoint node = null;
	
	public GeneticAlgWSManagerEndpoint getManager() {
		return manager;
	}
	public GeneticAlgWSNodeEndpoint getNode() {
		return node;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	/**
	 * Set the relative path of the service
	 * 
	 * Must end AND begin with a '/'.
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getNodeUrl() {
		return protocol+"://"+hostName+":"+port+path+"node";
	}
	public String getManagerUrl() {
		return protocol+"://"+hostName+":"+port+path+"manager";
	}
	
	public void createEndpoints() {
		Logger.getLogger(this.getClass()).info("standing up Genetic Algorithm endpoints");

		manager = new ImplGeneticAlgWSManagerEndpoint();
		Endpoint.publish(getManagerUrl(), manager);
		Logger.getLogger(this.getClass()).info("manager endpoint published at "+getManagerUrl());
		
		node = new ImplGeneticAlgWSNodeEndpoint();
		Endpoint.publish(getNodeUrl(), node);
		Logger.getLogger(this.getClass()).info("node endpoint published at "+getNodeUrl());
	}
	
	public static void main(String[] argv) throws Exception {
		
		// this was originally for configuring the logger
		//com.jonschang.investing.Investing.instance();
		
		GeneticAlgWSEndpointsConfigurator c = new GeneticAlgWSEndpointsConfigurator();
		c.createEndpoints();
		Logger.getLogger(GeneticAlgWSEndpointsConfigurator.class).info("GeneticAlgWS*Endpoints are published");
		
		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		factoryBean.setServiceClass(GeneticAlgWSNodeEndpoint.class);
		factoryBean.setAddress(c.getNodeUrl());
		GeneticAlgWSNodeEndpoint client = (GeneticAlgWSNodeEndpoint)factoryBean.create();
		
		Logger.getLogger(GeneticAlgWSEndpointsConfigurator.class).info("client has "+client.getFreeMemory()+" bytes free");
	}
}
