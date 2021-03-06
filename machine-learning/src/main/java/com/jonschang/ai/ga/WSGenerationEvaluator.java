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

import java.io.StringReader;
import java.util.*;
import org.apache.log4j.*;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.jonschang.utils.*;
/**
 * Uses the WSGeneticAlgorithmEndpoint to evaluate Phenotypes
 * in parallel across a network
 * 
 * @author schang
 */
public class WSGenerationEvaluator<P extends Phenotype<G>,G extends Gene> implements GenerationEvaluator<P,G> {
	
	/**
	 * A uuid/Phenotype<G> map of the current generation.
	 */
	Map<String,P> phenotypes = new Hashtable<String,P>();
	ThreadSafeValue<Boolean> waiting = new ThreadSafeValue<Boolean>(false);
	
	/**
	 * Evaluate a generation of phenotypes.
	 * 
	 * Waits until all phenotypes in the generation have been evaluated before returning.
	 * 
	 * This class is stateful and this is the primary functionallity,
	 * so this method is synchronized to prevent state corruption.
	 */
	public synchronized void evaluate(List<P> phenotypes) throws GeneticAlgException {
		if(phenotypes==null || phenotypes.size()==0 )
			return;
		this.phenotypes.clear();
		waiting.value(false);
		String uuid = null;
		for( P p : phenotypes ) {
			uuid = UUID.randomUUID().toString();
			this.phenotypes.put(uuid, p);
			GeneticAlgWSNodeInfo node = null;
			while( node == null ) {
				node = endpoint.findNode();
				if( node==null ) {
					try {
						Logger.getLogger(this.getClass()).trace("SLEEPING");
						Thread.sleep(1000);
					} catch(InterruptedException e) {
						// I don't care about an interrupted exception here
					}
				}
			}
			Logger.getLogger(this.getClass()).info("submitting to "+node.url()+" the phenotype "+p);
			try {
				node.client.evaluatePhenotype(uuid, factoryClass, factoryMethod, p.getXml().asXML());
			} catch( XmlException xe ) {
				throw new GeneticAlgException(xe);
			}
		}
		try {
			Logger.getLogger(this.getClass()).trace("waiting for phenotypes to complete evaluation");
			waiting.value(true);
			while( waiting.value() )
				Thread.sleep(1000);
		} catch( Exception e ) {
			throw new GeneticAlgException(e);
		}
	}

	public void setFactoryClass(String clazz) {
		factoryClass = clazz;
	}
	private String factoryClass;
	
	public void setFactoryMethod(String method) {
		factoryMethod = method;
	}
	private String factoryMethod;
	
	@Override public void setGeneticAlgFactory(GeneticAlgFactory<P, G> gaf) {
		factory = gaf;
	}
	@Override public GeneticAlgFactory<P, G> getGeneticAlgFactory() {
		return factory;
	}
	private GeneticAlgFactory<P,G> factory = null;
	
	/**
	 * Set the manager endpoint that the evaluator is to use for scheduling distributed fitness evaluations.
	 * @param endpoint The manager endpoint to use for fitness evaluation scheduling.
	 */
	public void setManagerEndpoint(GeneticAlgWSManagerEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	public GeneticAlgWSManagerEndpoint getManagerEndpoint() {
		return endpoint;
	}
	private GeneticAlgWSManagerEndpoint endpoint;
	
	/**
	 * Called by the GeneticAlgWSManagerEndpoint when a Phenotype has completed evaluation.
	 * 
	 * @param uuid The uuid generated by this class to identify the Phenotype
	 * @param phenotypeXml The Xml to use reconstructing the Phenotype, or a Stack Trace
	 * @return true if this evaluator handles the phenotype, else false
	 */
	public boolean notifyComplete(String uuid, String phenotypeXml) {
		if( phenotypes.get(uuid)==null )
			return false;
		if( 1==0 ) {
			// if the phenotype failed, 
		} else {
			SAXReader sr = new SAXReader();
			try {
				Document d = sr.read(new StringReader(phenotypeXml));
				phenotypes.get(uuid).setXml( d.getRootElement() );
			} catch( Exception e ) {
				Logger.getLogger(uuid+" An exception occured reading phenotypexml "+this.getClass()).error(StringUtils.stackTraceToString(e));
			} finally {
				synchronized(phenotypes) {
					phenotypes.remove(uuid);
					if( phenotypes.size()==0 && waiting.value()==true ) {
						Logger.getLogger(this.getClass()).trace("notifying that phenotypes are all evaluated");
						waiting.value(false);
					}
				}
			}
		}
		return true;
	}
}
