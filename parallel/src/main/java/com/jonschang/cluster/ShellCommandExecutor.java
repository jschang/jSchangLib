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

import java.io.*;
import com.jonschang.cluster.model.*;

/**
 * Handles the execution of a Shell Command
 * @author schang
 */
public class ShellCommandExecutor extends AbstractCommandExecutor {
	public ShellCommandExecutor(CommandRequest request) {
		super(request);
	}
	
	private StringBuilder standardOut = new StringBuilder();
	private StringBuilder errorOut = new StringBuilder();
	private int exitValue = (-1);
	
	@Override protected void actuallyRunCommand() throws Exception {
		ShellCommand command = this.commandRequest.getShellCommand();
		Process process = Runtime.getRuntime().exec(command.getValue());

		BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		Boolean notDone = true;

		int exitValue = (-1);
		while(notDone) {
			String line = inputStream.readLine();
			if( line!=null ) {
				synchronized(standardOut) {
					standardOut.append(line);
				}
			}
			line = errorStream.readLine();
			if( line!=null ) {
				synchronized(errorOut) {
					errorOut.append(line);
				}
			}
			try {
				exitValue = process.exitValue();
				notDone=false;
			} catch( IllegalThreadStateException itse ) {
				// we want to loop till the process is done
				// so here we'll do nothing whatsoever
				Thread.sleep(50);
			}
		}
		this.exitValue = exitValue;  
	}
	
	@Override public CommandInfo getCommandInfo() throws ClusterException {
		CommandInfo toRet = super.getCommandInfo();
		if( exception!=null )
			toRet.setStatus( CommandStatus.error.toString() );
		else toRet.setStatus( 
			exitValue==(-1) ? CommandStatus.running.toString()
				: exitValue>0 ? CommandStatus.error.toString()
					: CommandStatus.complete.toString()
		);
		synchronized(errorOut) {
			if( toRet.getError()==null && errorOut.length()!=0 )	
				toRet.setError(errorOut.toString());
		}
		synchronized(standardOut) {
			toRet.setResult(standardOut.toString());
		}
		return toRet;
	}
}
