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

import org.junit.*;
import org.apache.log4j.*;

import com.jonschang.cluster.model.*;
import com.jonschang.utils.*;

public class ImplClusterNodeWSTest {
	static public ClusterNodeWSServer nodeWS = null;
	static public ClusterNodeWS client = null;
	
	public ImplClusterNodeWSTest() throws Exception {

		if( nodeWS==null ) {
			
			Logger.getLogger("com.jonschang").setLevel(Level.TRACE);
			
			nodeWS = ClusterFactory.newEndpoint("http://localhost:"+MockClusterNodeService.startPort+"/node");
			
			MockClusterNodeService mcns = new MockClusterNodeService();
			mcns.setClusterNodeWSServer(nodeWS);
			nodeWS.setClusterNodeService( mcns );
			
			UsageClusterEventHandler uceh = new UsageClusterEventHandler();
			uceh.setClusterNodeWSServer(nodeWS);
			nodeWS.subscribe(ClusterEventType.THRESHOLD_CPU_HIGH, uceh);
			nodeWS.subscribe(ClusterEventType.THRESHOLD_CPU_LOW, uceh);
			
			NodeConfiguration conf = nodeWS.getConfiguration();
			conf.getCpuHigh().setOne(1000);
			conf.getCpuHigh().setFive(1000);
			conf.getCpuHigh().setFifteen(1000);
			conf.getCpuLow().setOne(0);
			conf.getCpuLow().setFive(0);
			conf.getCpuLow().setFifteen(0);
			conf.setMinLifetime((short)10);
			conf.setMaxChildren((short)1);
			conf.setMaxNodeDepth((short)2);
		}
		if( client==null ) {
			client = ClusterFactory.newClient("http://localhost:"+MockClusterNodeService.startPort+"/node");
		}
		
		Authorization newAuth = nodeWS.newAuthorization();
		Logger.getLogger(this.getClass()).info("value=\""+newAuth.getValue()+"\" hash=\""+newAuth.getHash()+"\"");
		
		//startWaiting();
	}
	@Test 
	public void testAuthorization() throws Exception {		
		// Validate that an Authorization exception is thrown if auth is bad
		Exception exceptionThrown = null;
		try {
			Authorization auth = new Authorization();
			auth.setHash("crap");
			auth.setValue("crap");
			NodeInfo nodeInfo = client.getNodeInfo(auth);
		} catch( Exception e ) {
			exceptionThrown = e;
			Logger.getLogger(this.getClass()).error( StringUtils.stackTraceToString(e) );
		}
		Assert.assertTrue( exceptionThrown instanceof AuthenticationException );
	}
	@Test 
	public void testShellCommandRequest() throws Exception {
	}
	@Test 
	public void testJVMCommandRequest() throws Exception {		
		CommandRequest cr = ClusterFactory.newJVMCommandRequest();
		
		MethodCall em = new MethodCall();
		em.setType("com.jonschang.cluster.MockJVMCommand");
		em.setValue("run");
		
		MethodCall fm = new MethodCall();
		fm.setType("com.jonschang.cluster.MockJVMCommand");
		fm.setValue("instance");
		
		cr.getFactoryMethodCommand().setFactoryMethod(fm);
		cr.getFactoryMethodCommand().setExecutionMethod(em);
		cr.setMode("async");
		
		// validate that the status is initially "running"}
		CommandInfo info = client.executeCommand(nodeWS.newAuthorization(), cr);
		info = client.getCommandInfo(nodeWS.newAuthorization(), info.getId());
		Logger.getLogger(this.getClass()).info(info.getStatus());
		Assert.assertTrue( info.getStatus().compareTo("running")==0 );
		Assert.assertTrue( info.getError()==null );
		synchronized(MockJVMCommand.instance()) {
			MockJVMCommand.instance().endThread(true);
			MockJVMCommand.instance().notifyAll();
		}
		
		// validate that the status is "complete" once the command terminates
		info = client.getCommandInfo(nodeWS.newAuthorization(), info.getId());
		Logger.getLogger(this.getClass()).info(info.getStatus());
		Assert.assertTrue( info.getStatus().compareTo("complete")==0 );
		Assert.assertTrue( info.getError()==null );
		
		// validate that a CommandNotFoundException is returned when checking
		// on a completed command we've already checked on once
		Boolean commandNotFoundThrown = false;
		try {
			info = client.getCommandInfo(nodeWS.newAuthorization(), info.getId());
		} catch( Exception e ) {
			if( e instanceof CommandNotFoundException )
				commandNotFoundThrown = true;
		}
		Assert.assertTrue(commandNotFoundThrown);
		
		info = client.executeCommand(nodeWS.newAuthorization(), cr);
		Logger.getLogger(this.getClass()).info(info.getStatus());
		synchronized(MockJVMCommand.instance()) {
			MockJVMCommand.instance().triggerException(true);
			MockJVMCommand.instance().endThread(true);
			MockJVMCommand.instance().notifyAll();
		}
		
		// Verify that an "error" status is set and an Exception stack trace returned 
		// if an exception is thrown from the command.
		info = client.getCommandInfo(nodeWS.newAuthorization(), info.getId());
		Logger.getLogger(this.getClass()).info(info.getStatus());
		Assert.assertTrue( info.getStatus().compareTo("error")==0 );
		Assert.assertTrue( info.getError()!=null && info.getError().contains("Exception") );
		Logger.getLogger(this.getClass()).info(info.getError());
	}
	public void testExpansion() throws Exception {
		NodeConfiguration conf = nodeWS.getConfiguration();
		conf.getCpuLow().setOne(0);
		conf.getCpuLow().setFive(0);
		conf.getCpuLow().setFifteen(0);
		startWaiting();
	}
	@Test 
	public void testContraction() throws Exception {
	}
	public void startWaiting() throws Exception {
		Boolean done=false;
		synchronized(this) {
			while(!done) 
				wait();
		}
	}
}
