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

import java.util.concurrent.*;
import java.util.*;
import com.jonschang.utils.*;
import org.apache.log4j.*;

/**
 * Provides the concurrent evaluation of the phenotypes of a generation.
 *  
 * @author schang
 *
 * @param <P>
 * @param <G>
 */
public class ThreadedGenerationEvaluator<P extends Phenotype<G>,G extends Gene> implements GenerationEvaluator<P, G> {
	
	private GeneticAlgFactory<P,G> geneticAlgFactory = null;
	public void setGeneticAlgFactory(GeneticAlgFactory<P,G> gaf) {
		geneticAlgFactory = gaf;
	}
	public GeneticAlgFactory<P,G> getGeneticAlgFactory() {
		return geneticAlgFactory;
	}
	
	private ExecutorService executorService = null;
	public void setExecutorService(ExecutorService service) {
		this.executorService = service;
	}
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	List<Exception> exceptions = new ArrayList<Exception>();
	public synchronized void addException(Exception e) {
		exceptions.add(e);
	}
	
	public void evaluate(List<P> phenotypes) throws GeneticAlgException {
		if( executorService == null )
			executorService = Executors.newCachedThreadPool();
		for( P phen : phenotypes ) {
			executorService.execute( 
				new Runnable() {
					P phen = null;
					ThreadedGenerationEvaluator<P,G> genEval = null;
					Runnable setParams(P phen,ThreadedGenerationEvaluator<P,G> tge) {
						this.phen = phen;
						this.genEval = tge;
						return this;
					}
					public void run() {
						try {			
							FitnessFunction<P,G> fitnessFunction = geneticAlgFactory.newFitnessFunction();
							fitnessFunction.evaluate(phen);
						} catch( GeneticAlgException e ) {
							genEval.addException(e);
						}
					}
				}.setParams(phen,this)
			);
		}
		try {
			executorService.awaitTermination(0, TimeUnit.SECONDS);
			for(Exception e : exceptions) {
				Logger.getLogger(this.getClass()).error(StringUtils.stackTraceToString(e));
			}
		} catch( Exception e ) {
			throw new GeneticAlgException(e);
		}
	}
}
