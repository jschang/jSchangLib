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
package com.jonschang.investing.trading;

import java.util.*;
import org.apache.log4j.Logger;
import com.jonschang.utils.*;

@SuppressWarnings(value="unchecked")
public class GenericAgentManager implements AgentManager {
	
	private List<Agent> agents;

	public List<Agent> getAgents() {
		return this.agents;
	}
	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Agent> void run() throws AgentManagerException {
		for( T agent : (List<T>)this.agents ) {
			try {
				agent.run();
			} catch(AgentException ae) {
				// in this instance, if an agent fails...we want to continue with the others
				// so it should be enough to log the exception
				Logger.getLogger(this.getClass()).error(com.jonschang.utils.StringUtils.stackTraceToString(ae));
			}
		}
	}
}
