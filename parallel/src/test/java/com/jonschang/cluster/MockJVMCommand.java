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
import com.jonschang.cluster.event.*;

public class MockJVMCommand implements ClusterNodeWSServerAware {
	private ClusterNodeWSServer service;
	private Boolean triggerAnException=false;
	private Boolean endThread=false;

	static private MockJVMCommand instance = new MockJVMCommand();
	
	static synchronized public MockJVMCommand instance() {
		Logger.getLogger(MockJVMCommand.class).info("In instance()");
		return instance;
	}
	
	public void setClusterNodeWSServer(ClusterNodeWSServer service) {
		this.service = service;
	}
	
	public void endThread(Boolean end) {
		endThread=true;
	}
	
	public void triggerException(Boolean triggerAnException) {
		this.triggerAnException=triggerAnException;
	}
	
	public void run() throws Exception {
		try {
			synchronized(this) {
				while(! endThread)
					wait();
			}
			if( triggerAnException )
				throw new Exception("This is a test exception.");
		} catch( Exception e ) {
			ErrorClusterEvent ece = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_COMMAND_FAILED);
			ece.setException(e);
			service.triggerEvent(ece); 
			throw e;
		} finally {
			triggerAnException = false;
			endThread = false;
		}
	}
}
