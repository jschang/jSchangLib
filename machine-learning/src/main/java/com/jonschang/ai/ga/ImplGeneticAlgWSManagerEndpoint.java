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
import java.util.concurrent.*;
import javax.jws.*;

import org.apache.log4j.*;

import com.jonschang.utils.StringUtils;

/**
 * The Genetic Algorithm Manager Web-Service Endpoint
 * 
 * Node's will advertise availability to this web-service,
 * where another class will handle provisioning nodes
 * to the WSGenerationEvaluator
 * 
 * @author schang
 */
@WebService(name="GeneticAlgWSManagerEndpoint")
public class ImplGeneticAlgWSManagerEndpoint implements GeneticAlgWSManagerEndpoint {

	private Logger logger = Logger.getLogger(this.getClass());
	private List<WSGenerationEvaluator> evaluators = new Vector<WSGenerationEvaluator>();
	private Map<String,GeneticAlgWSNodeInfo> nodeStatusInfo = new Hashtable<String,GeneticAlgWSNodeInfo>();
	
	@WebMethod(exclude=true) private void updateNodeStatus() {
		for( Map.Entry<String,GeneticAlgWSNodeInfo> entry : nodeStatusInfo.entrySet() ) {
			GeneticAlgWSNodeInfo status = entry.getValue();
			status.updateNodeStatus();
		}
	}
	
	@Override @WebMethod(exclude=true) public GeneticAlgWSNodeInfo findNode() {
		Boolean failed=false;
		GeneticAlgWSNodeInfo leastRequestsNode=null;
		if( nodeStatusInfo.size()==0 )
			return null;
		for( GeneticAlgWSNodeInfo node : nodeStatusInfo.values() ) {
			if( node.available() == false )
				continue;
			if( leastRequestsNode==null )
				leastRequestsNode=node;
			else if( node.runningEvaluations()<leastRequestsNode.runningEvaluations() )
				leastRequestsNode = node;
		}
		if( leastRequestsNode==null )
			return null;
		leastRequestsNode.updateNodeStatus();
		return leastRequestsNode.clone();
	}
	
	@WebMethod(exclude=true) public void addWSGenerationEvaluator(WSGenerationEvaluator evaluator) { 
		logger.trace("adWSGenerationEvaluator() adding the evaluator "+evaluator);
		evaluators.add(evaluator);
	}
	
	@Override public void notifyAvailable(
		@WebParam(name="nodeUrl") String url,
		@WebParam(name = "runningEvaluations") Integer runningEvaluations) 
	throws Exception {
		if( nodeStatusInfo.get(url)==null ) {
			logger.trace("notifyAvailable() url passed in "+url);
			GeneticAlgWSNodeInfo status = new GeneticAlgWSNodeInfo(url);
			status.client = GeneticAlgWSNodeInfo.newClient(url);
			status.available(true);
			status.updateNodeStatus();
			this.nodeStatusInfo.put(url,status);	
		} else {
			GeneticAlgWSNodeInfo node = nodeStatusInfo.get(url);
			node.available(true);
			node.runningEvaluations(runningEvaluations);
		}
	}
	
	@Override public void notifyUnavailable(
			@WebParam(name = "nodeUrl") String nodeUrl,
			@WebParam(name = "runningEvaluations") Integer runningEvaluations 
	) throws Exception {
		GeneticAlgWSNodeInfo node = nodeStatusInfo.get(nodeUrl);
		if( node!=null ) {
			node.runningEvaluations(runningEvaluations);
			node.available(false);
		}
	}
	
	@Override public void notifyFitnessComplete(
		@WebParam(name="nodeUrl") String hostUrl,
		@WebParam(name="runningEvaluations") Integer running,
		@WebParam(name="uuid") String uuid, 
		@WebParam(name="phenotypeXml") String phenotypeXml
	) {
		GeneticAlgWSNodeInfo node = nodeStatusInfo.get(hostUrl);
		node.runningEvaluations(running);
		
		// if an exception was thrown, i would rather log it earlier than later
		String sub = (String)phenotypeXml.subSequence(0,9);
		if( sub.compareTo("EXCEPTION")==0 ) {
			logger.error(phenotypeXml);
			node.lastException(phenotypeXml);
		}
		
		// safely iterate over the evaluators
		// calling notify on each until the phenotype
		// is handled
		for( WSGenerationEvaluator eval : evaluators ) {
			if( eval.notifyComplete(uuid, phenotypeXml) )
				break;
		}
	}
}
