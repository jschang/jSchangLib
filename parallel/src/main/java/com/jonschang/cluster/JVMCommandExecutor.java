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

import com.jonschang.cluster.model.*;

import java.lang.reflect.*;

/**
 * Executes a JVM class method for an object that may be supplied by 
 * a static factory method.
 * 
 * A class may implement ClusterNodeWSAware, and the ClusterNodeWS service
 * will be injected via the setter method defined in the interface.
 * 
 * @author schang
 */
public class JVMCommandExecutor extends AbstractCommandExecutor {

	String result=null;
	
	public JVMCommandExecutor(CommandRequest request) {
		super(request);
	}
	
	@Override protected void actuallyRunCommand() throws Exception {
		
		Class methodClass=null, factoryMethodClass;
		Method factoryMethod=null, methodMethod;
		
		// determine what we need from the commandRequest,
		// then release it.
		synchronized(commandRequest) {
			FactoryMethodCommand fmc = commandRequest.getFactoryMethodCommand();
			if( fmc == null )
				throw new ClusterException("The FactoryMethodCommand was null.");
			MethodCall em = fmc.getExecutionMethod();
			MethodCall fm = fmc.getFactoryMethod();
			Object object=null;
			methodClass = Class.forName(em.getType());
			methodMethod = methodClass.getMethod(em.getValue());
			if( fm != null && fm.getType()!=null && fm.getValue()!=null ) {
				factoryMethodClass = Class.forName(fm.getType());
				factoryMethod = factoryMethodClass.getMethod(fm.getValue());
			}
		}
		
		Object object=null;
		if( factoryMethod!=null )
			object = factoryMethod.invoke(null);
		else object = methodClass.newInstance();

		// inject the ClusterNodeWS so that the object
		// may coordinate further actions within the cluster.
		if( object instanceof ClusterNodeWSServerAware ) {
			((ClusterNodeWSServerAware)object).setClusterNodeWSServer(this.service);
		}
		
		if( methodMethod.getReturnType() == String.class )
			this.result = (String)methodMethod.invoke(object);
		else if( methodMethod.getReturnType() == Number.class )
			this.result = ((Number)methodMethod.invoke(object)).toString();
		else methodMethod.invoke(object);
	}
	
	@Override public CommandInfo getCommandInfo() throws ClusterException {
		CommandInfo toRet = super.getCommandInfo();
		
		if( exception!=null )
			toRet.setStatus( CommandStatus.error.toString() );
		else if( isRunning() )
			toRet.setStatus( CommandStatus.running.toString() );
		else toRet.setStatus( CommandStatus.complete.toString() );
		
		if( result!=null )
			toRet.setResult(result);

		return toRet;
	}
}
