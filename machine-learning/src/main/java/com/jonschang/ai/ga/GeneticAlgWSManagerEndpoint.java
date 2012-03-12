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

import java.util.*;
import javax.jws.*;

@WebService public interface GeneticAlgWSManagerEndpoint {
	
	@WebMethod(exclude=true) void addWSGenerationEvaluator(WSGenerationEvaluator evaluator);
	
	/**
	 * Finds the node running the fewest evaluations and returns a clone of the status object.
	 * 
	 * If a node running fewer than the maxRunningPerNode cannot be found,
	 * then this method sleeps for 10 seconds and then tries again.
	 *  
	 * @return A clone (with functional client) of the status info object OR null, if no Node endpoints are known
	 */
	@WebMethod(exclude = true) public GeneticAlgWSNodeInfo findNode();
	
	/**
	 * Called by the GeneticAlgWSNodeEndpoint upon publishing
	 * 
	 * @param url The url of the newly available GeneticAlgWSNodeEndpoint
	 */
	void notifyAvailable(
		@WebParam(name = "nodeUrl") String nodeUrl,
		@WebParam(name = "runningEvaluations") Integer runningEvaluations
	) throws Exception;
	
	/**
	 * Called by a node when it is too occupied to handle more evaluation requests.
	 * 
	 * @param url
	 * @throws Exception
	 */
	void notifyUnavailable(
		@WebParam(name = "nodeUrl") String nodeUrl,
		@WebParam(name = "runningEvaluations") Integer runningEvaluations 
	) throws Exception;

	/**
	 * Called by the GeneticAlgWSNodeEndpoint upon the completion of evaluating a Phenotype
	 * 
	 * @param uuid The uuid the Phenotype is associated with
	 * @param phenotypeXml The resulting Phenotype Xml, or a Stack Trace in the event of an Exception during fitness evaluation 
	 */
	void notifyFitnessComplete(
		@WebParam(name = "nodeUrl") String nodeUrl,
		@WebParam(name = "runningEvaluations") Integer runningEvaluations,
		@WebParam(name = "uuid") String uuid,
		@WebParam(name = "phenotypeXml") String phenotypeXml
	);
}