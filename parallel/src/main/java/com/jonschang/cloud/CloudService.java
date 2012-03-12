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

package com.jonschang.cloud;

/**
 * Provides a simple interface to a cloud service provider.
 * 
 * The interface caters to the lowest common denominator.
 * 
 * @author schang
 * @param <M>
 */
public interface CloudService<M extends MachineInstance<?>> {
	/**
	 * Provisions a single new machine instance from the concrete cloud service.
	 * 
	 * @param instance The instance with only provisioning required parameters filled in.
	 * @return The instance with any unique identifiers filled in by the cloud service.
	 * @throws CloudException
	 */
	M provision(M instance) throws CloudException;
	
	/**
	 * Checks on the status of a single machine instance known to be running in the cloud service.
	 * 
	 * @param instance The instance provisioned to check status on.
	 * @return An instance filled in, complete with status
	 * @throws CloudException
	 */
	M status(M instance) throws CloudException;
	
	/**
	 * Terminates an instance in the cloud service
	 * 
	 * @param instance The instance provisioned to decommission.
	 * @throws CloudException
	 */
	void decommission(M instance) throws CloudException;
	
	/**
	 * Get the MachineInstance object for the server this JVM is running on.
	 * 
	 * @return The MachineInstance object containing information about this server.
	 * @throws CloudException
	 */
	M getMyInstance() throws CloudException;
}
