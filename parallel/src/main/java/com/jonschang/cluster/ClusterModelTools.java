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

import java.util.*;

import javax.xml.datatype.*;

import com.jonschang.cluster.model.*;
import com.jonschang.cluster.model.NodeInfo.*;

public class ClusterModelTools {
	static public ServiceInfo cloneServiceInfo(ServiceInfo svc) {
		ServiceInfo newSvcInf = new ServiceInfo();
		newSvcInf.setService(svc.getService());
		newSvcInf.setAny(svc.getAny());
		return newSvcInf;
	}
	
	static public boolean usageGreaterThan(ThreeFloatUsageInfo info, ThreeFloatUsageInfo bar) {
		if( info.getOne() > bar.getOne()
			&& info.getOne() > bar.getOne()
			&& info.getOne() > bar.getOne() )
			return true;
		return false;
	}
	
	static public boolean usageLessThan(ThreeFloatUsageInfo info, ThreeFloatUsageInfo bar) {
		if( info.getOne() < bar.getOne()
			&& info.getOne() < bar.getOne()
			&& info.getOne() < bar.getOne() )
			return true;
		return false;
	}
	
	static public Double usageSquared(ThreeFloatUsageInfo info) {
		return Math.pow(info.getOne(),2)
			+ Math.pow(info.getFive(),2)
			+ Math.pow(info.getFifteen(),2); 
	}
	
	static public Double nodeInfoUsageSquared(NodeInfo info) {
		return usageSquared(info.getCpuUsage())
			+ usageSquared(info.getMemUsage())
			+ usageSquared(info.getSwapUsage());
	}
	
	static public NodeId cloneNodeId(NodeId nodeId) {
		NodeId newNodeId = new NodeId();
		newNodeId.setUrl( nodeId.getUrl() );
		newNodeId.setUuid( nodeId.getUuid() );
		return newNodeId;
	}
	
	static public ThreeFloatUsageInfo cloneThreeFloatUsageInfo(ThreeFloatUsageInfo info) {
		ThreeFloatUsageInfo d = new ThreeFloatUsageInfo();
		d.setOne( info.getOne() );
		d.setFive( info.getFive() );
		d.setFifteen( info.getFifteen() );
		return d;
	}
	
	static public NodeInfo cloneNodeInfo(NodeInfo src) throws ClusterException {
		NodeInfo dest = ClusterFactory.newNodeInfo();
		copyNodeInfo(src,dest);
		return dest;
	}
	
	static public NodeInfo copyNodeInfo(NodeInfo src, NodeInfo dest) {
		List<NodeId> sNodes = null, dNodes = null;
		
		dest.setServiceInfo( cloneServiceInfo(src.getServiceInfo()) );
		dest.setStarted( (XMLGregorianCalendar)src.getStarted().clone() );
		dest.setNodeCount( src.getNodeCount() );
		dest.setDepth( src.getDepth() );
		
		dest.setNodeId( cloneNodeId(src.getNodeId()) );
		dest.setParentNodeId( cloneNodeId(src.getParentNodeId()) );
		dest.setRootNodeId( cloneNodeId(src.getRootNodeId()) );
		
		sNodes = src.getChildNodes().getNodeId();
		dNodes = dest.getChildNodes().getNodeId();
		dNodes.clear();
		for( NodeId n : sNodes ) {
			dNodes.add(cloneNodeId(n));
		}
		
		dest.setCpuUsage( cloneThreeFloatUsageInfo( src.getCpuUsage() ) );
		dest.setMemUsage( cloneThreeFloatUsageInfo( src.getMemUsage() ) );
		dest.setSwapUsage( cloneThreeFloatUsageInfo( src.getSwapUsage() ) );
		
		return dest;
	}
	
	static public void updateAccum(NodeInfo accum, NodeInfo info) {
		ThreeFloatUsageInfo t;
		
		t = accum.getCpuUsage();
		t.setOne( info.getCpuUsage().getOne()+t.getOne() );
		t.setFive( info.getCpuUsage().getFive()+t.getFive() );
		t.setFifteen( info.getCpuUsage().getFifteen()+t.getFifteen() );
		
		t = accum.getMemUsage();
		t.setOne( info.getMemUsage().getOne()+t.getOne() );
		t.setFive( info.getMemUsage().getFive()+t.getFive() );
		t.setFifteen( info.getMemUsage().getFifteen()+t.getFifteen() );
		
		t = accum.getSwapUsage();
		t.setOne( info.getSwapUsage().getOne()+t.getOne() );
		t.setFive( info.getSwapUsage().getFive()+t.getFive() );
		t.setFifteen( info.getSwapUsage().getFifteen()+t.getFifteen() );
	}
	
	static public NodeInfo initializeNodeInfo(NodeInfo nodeInfo) throws ClusterException {
		
		if( nodeInfo.getCpuUsage()==null )
			nodeInfo.setCpuUsage(new ThreeFloatUsageInfo());
		
		if( nodeInfo.getSwapUsage()==null )
			nodeInfo.setSwapUsage(new ThreeFloatUsageInfo());
		
		if( nodeInfo.getMemUsage()==null )
			nodeInfo.setMemUsage(new ThreeFloatUsageInfo());
		
		nodeInfo.setParentNodeId( new NodeId() );
		nodeInfo.setRootNodeId( new NodeId() );
		nodeInfo.setNodeId( new NodeId() );
		nodeInfo.setServiceInfo( new ServiceInfo() );
		nodeInfo.setChildNodes( new ChildNodes() );
		try {
			nodeInfo.setStarted( DatatypeFactory.newInstance().newXMLGregorianCalendar() );
		} catch(DatatypeConfigurationException dce) {
			throw new ClusterException(dce);
		}
		
		return nodeInfo;
	}
	
	static public NodeConfiguration copyConfiguration(NodeConfiguration target, NodeConfiguration src) {
		if( src.getSwapHigh()!=null )
			target.setSwapHigh( cloneThreeFloatUsageInfo(src.getSwapHigh()) );
		else target.setSwapHigh( new ThreeFloatUsageInfo() );
		if( src.getMemHigh()!=null )
			target.setMemHigh( cloneThreeFloatUsageInfo(src.getMemHigh()) );
		else target.setMemHigh( new ThreeFloatUsageInfo() );
		if( src.getCpuHigh()!=null )
			target.setCpuHigh( cloneThreeFloatUsageInfo(src.getCpuHigh()) );
		else target.setCpuHigh( new ThreeFloatUsageInfo() );
		if( src.getCpuLow()!=null )
			target.setCpuLow( cloneThreeFloatUsageInfo(src.getCpuLow()) );
		else target.setCpuLow( new ThreeFloatUsageInfo() );
		if( src.getMinLifetime()!=null )
			target.setMinLifetime( src.getMinLifetime() );
		if( src.getMaxNodeDepth()!=null )
			target.setMaxNodeDepth( src.getMaxNodeDepth() );
		if( src.getStatusUpdateInterval()!=null )
			target.setStatusUpdateInterval( src.getStatusUpdateInterval() );
		if( src.getMaxChildren()!=null )
			target.setMaxChildren( src.getMaxChildren() );
		return target;
	}
	
	static public MethodCall cloneMethodCall(MethodCall mc) {
		MethodCall toRet = new MethodCall();
		toRet.setType(mc.getType());
		toRet.setValue(mc.getValue());
		return toRet;
	}
	
	static public CommandRequest cloneCommandRequest(CommandRequest req) {
		
		CommandRequest newReq = new CommandRequest();
		newReq.setMode(req.getMode());
		
		if( req.getOriginNodeId()!=null )
			newReq.setOriginNodeId( cloneNodeId(req.getOriginNodeId()) );
		
		FactoryMethodCommand fmc = req.getFactoryMethodCommand();
		if( fmc!=null ) {
			FactoryMethodCommand fmc2 = new FactoryMethodCommand();
			if( fmc.getExecutionMethod()!=null )
				fmc2.setExecutionMethod( cloneMethodCall(fmc.getExecutionMethod()) );
			if( fmc.getFactoryMethod()!=null )
				fmc2.setFactoryMethod( cloneMethodCall(fmc.getFactoryMethod()) );
			newReq.setFactoryMethodCommand( fmc2 );
		}
		
		ShellCommand sc = req.getShellCommand();
		if( sc!=null ) {
			newReq.setShellCommand( new ShellCommand() );
			newReq.getShellCommand().setValue( req.getShellCommand().getValue() );
		}
		
		return newReq;
	}
	
	static public CommandId cloneCommandId(CommandId id) {
		CommandId toRet = new CommandId();
		if( id.getAcceptingnodeId()!=null )
			toRet.setAcceptingnodeId( cloneNodeId(id.getAcceptingnodeId()) );
		if( id.getOriginNodeId()!=null )
			toRet.setOriginNodeId( cloneNodeId(id.getOriginNodeId()) );
		toRet.setUuid( id.getUuid() );
		return toRet;
	}
	
	static public List<NodeInfo> copyNodeInfoCollection(Collection<NodeInfo> nodes) throws ClusterException {
		List<NodeInfo> toRet = new ArrayList<NodeInfo>();
		for( NodeInfo node : nodes )
			toRet.add( copyNodeInfo(node, ClusterFactory.newNodeInfo() ) );
		return toRet;
	}
}
