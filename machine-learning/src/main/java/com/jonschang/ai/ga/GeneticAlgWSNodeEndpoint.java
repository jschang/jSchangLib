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

@WebService public interface GeneticAlgWSNodeEndpoint {

	/**
	 * @param hostUrl The url of this host...must be called prior to setManagerUrl()
	 */
	@WebMethod(exclude=true) public void setNodeUrl(String hostUrl);
	
	/**
	 * Sets the manager url for this node.  
	 * Calls notifyAvailable(nodeUrl) on the manager.
	 * @param url The url of the GeneticAlgWSManagerEndpoint to report back to
	 */
	@Oneway void setManagerUrl(@WebParam(name = "url") String url);

	/**
	 * @return The number of exceptions experienced during Phenotype evaluation
	 */
	Integer getThrownExceptions();

	/**
	 * @return The number of evaluations submitted to this service
	 */
	Integer getSubmittedEvaluations();
	
	/**
	 * @return The number of evaluations completed by this service
	 */
	Integer getCompletedEvaluations();
	
	/**
	 * @return The number of currently running evaluations
	 */
	Integer getRunningEvaluations();

	/**
	 * @return The amount of memory available to the Java Virtual Machine
	 */
	long getFreeMemory();

	/**
	 * Evaluates the Phenotype described by the Phenotype Xml in a separate thread
	 * 
	 * @param uuid the uuid to refer to the Phenotype by when replying to the GeneticAlgWSManagerEndpoint
	 * @param factoryClass a GeneticAlgFactory implementing Class
	 * @param factoryMethod the static factory method to call on the factoryClass constructing the GeneticAlgFactory
	 * @param phenotypeXml a string of Xml describing the Phenotype
	 * @return "ok" or a stack trace if an Exception is thrown before the evaluation callable is scheduled
	 */
	String evaluatePhenotype(
			@WebParam(name = "uuid") String uuid,
			@WebParam(name = "factoryClass") String factoryClass,
			@WebParam(name = "factoryMethod") String factoryMethod,
			@WebParam(name = "phenotypeXml") String phenotypeXml);

}