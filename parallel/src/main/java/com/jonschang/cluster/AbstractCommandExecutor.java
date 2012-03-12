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

import java.lang.management.*;
import java.util.*;

import javax.xml.datatype.*;

import com.jonschang.cluster.event.*;
import com.jonschang.cluster.model.*;
import com.jonschang.utils.*;

/**
 * An AbstractCommandExecutor to simplify descendent class implementation
 * @author schang
 */
abstract public class AbstractCommandExecutor implements CommandExecutor {
	
	protected Long cpuTime = 0L;
	protected Date start = new Date();
	protected Date end = null;
	protected Thread myThread = null;
	protected ClusterNodeWSServer service = null;
	protected ThreadSafeValue<Boolean> isRunning = new ThreadSafeValue<Boolean>(false);
	protected CommandRequest commandRequest = null;
	protected CommandId commandId = null;
	protected Exception exception = null;
	
	public AbstractCommandExecutor(CommandRequest request) {
		setCommandRequest(request);
	}

	@Override public void setCommandRequest(CommandRequest request) {
		commandRequest = request;
	}
	
	@Override public CommandRequest getCommandRequest() {
		return commandRequest;
	}
	
	@Override public void setCommandId(CommandId id) {
		commandId = id;
	}
	
	@Override public CommandId getCommandId() {
		return commandId;
	}
	
	@Override public void setClusterNodeWSServer(ClusterNodeWSServer service) {
		this.service = service;
	}
	
	@Override public ClusterNodeWSServer getClusterNodeWSServer() {
		return service;
	}
	
	@Override public void run() {
		try {
			myThread = Thread.currentThread();
			isRunning.value(true);
			actuallyRunCommand();
		} catch( Exception e ) {
			exception = e;
			ErrorClusterEvent event = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_COMMAND_FAILED);
			event.setException(e);
			service.triggerEvent(event);
		} finally {
			end = new Date();
			cpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000L;
			isRunning.value(false);
		}
	}

	@Override public boolean isRunning() {
		return isRunning.value();
	}
	
	@Override public Thread getThread() {
		return myThread;
	}

	@Override public Long getCpuTime() {
		if( cpuTime>0 )
			return cpuTime;
		else return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000L;
	}

	@Override public Long getDuration() {
		if( end == null )
			return new Date().getTime() - start.getTime();
		else return end.getTime() - start.getTime();
	}
	
	/**
	 * Fills in most of the CommandInfo.
	 */
	@Override public CommandInfo getCommandInfo() throws ClusterException {
		CommandInfo toRet = new CommandInfo();
		toRet.setRunTime(getDuration());
		toRet.setRequest(ClusterModelTools.cloneCommandRequest(commandRequest));
		toRet.setId(ClusterModelTools.cloneCommandId(commandId));
		if( exception!=null )
			toRet.setError( StringUtils.stackTraceToString(exception) );
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(start.getTime());
			toRet.setStartedTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
		} catch( DatatypeConfigurationException dtce ) {
			throw new ClusterException(dtce);
		}
		return toRet;
	}
	
	abstract protected void actuallyRunCommand() throws Exception;
}
