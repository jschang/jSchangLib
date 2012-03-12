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
import java.io.*;
import java.lang.Runtime;
import javax.jws.*;
import org.dom4j.io.*;
import org.dom4j.*;
import org.apache.cxf.jaxws.*;
import org.apache.log4j.Logger;

import com.jonschang.utils.*;

/**
 * A WebService Endpoint specialized to enable the distributed evaluation of Phenotypes.
 * 
 * @author schang
 */
@WebService(name="GeneticAlgWSNodeEndpoint") 
public class ImplGeneticAlgWSNodeEndpoint implements GeneticAlgWSNodeEndpoint {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	// TODO: Determine whether I need to extract this or not.  Really, the entire application can share a thread pool and it should be accessible via a global singleton
	private ExecutorService executor = Executors.newCachedThreadPool();

	private ThreadSafeValue<Integer> maxRunningEvaluations = new ThreadSafeValue<Integer>(10);
	private Counter runningEvaluations = new Counter();
	private Counter submittedEvaluations = new Counter();
	private Counter completedEvaluations = new Counter();
	private Counter thrownExceptions = new Counter();
	private ThreadSafeValue<String> nodeUrl = new ThreadSafeValue<String>("unknown");
	private ThreadSafeValue<String> managerUrl = new ThreadSafeValue<String>();
	
	/**
	 * Sets the url this class instance is available at.
	 * 
	 * @param hostUrl The url this class instance is exposed as an endpoint at.
	 */
	@WebMethod(exclude=true) public void setNodeUrl(String hostUrl) {
		this.nodeUrl.value(hostUrl);
	}
	
	@Override @Oneway public void setManagerUrl(@WebParam(name="url") String url) {
		managerUrl.value(url);
		try {
			newClient().notifyAvailable(nodeUrl.value(),runningEvaluations.count().intValue());
		} catch(Exception e) {
			// TODO: NO! BAD! FIX!
		}
	}
	
	@Override public Integer getThrownExceptions() {
		return thrownExceptions.count().intValue();
	}
	
	@Override public Integer getSubmittedEvaluations() {
		return submittedEvaluations.count().intValue();
	}
	
	@Override public Integer getCompletedEvaluations() {
		return completedEvaluations.count().intValue();
	}
	
	@Override public Integer getRunningEvaluations() {
		return runningEvaluations.count().intValue();
	}
	
	@Override public long getFreeMemory() {
		Runtime.getRuntime().gc();
		return Runtime.getRuntime().freeMemory();
	}
	
	@Override public String evaluatePhenotype(
		@WebParam(name="uuid") String uuid,
		@WebParam(name="factoryClass") String factoryClass,
		@WebParam(name="factoryMethod") String factoryMethod,
		@WebParam(name="phenotypeXml") String phenotypeXml  
	) {	
		submittedEvaluations.increment();
		try {
			logger.trace(uuid+" submitting a request using "+factoryClass+"::"+factoryMethod);
			logger.trace(uuid+" phenotype xml is "+phenotypeXml);
			FitnessFunctionCallable callable = new FitnessFunctionCallable(uuid,factoryClass,factoryMethod,phenotypeXml);
			executor.submit( callable );
		} catch( Exception e ) {
			String exceptionStr = "EXCEPTION - "+uuid+" "+StringUtils.stackTraceToString(e);
			logger.error(exceptionStr);
			return exceptionStr;
		}
		return "ok - "+uuid;
	}
	
	/**
	 * Creates and returns a client for the manager endpoint at managerUrl.
	 * 
	 * The _only_ way this class knows at what url the manager endpoint 
	 * lives at is through a submission to setManagerUrl()
	 * 
	 * @return GeneticAlgWSManagerEndpoint client proxy created by JaxWsProxyFactoryBean
	 * @throws Exception
	 */
	@WebMethod(exclude=true) private GeneticAlgWSManagerEndpoint newClient() throws Exception {
		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		factoryBean.setServiceClass(GeneticAlgWSManagerEndpoint.class);
		factoryBean.setAddress(managerUrl.toString());
		return (GeneticAlgWSManagerEndpoint)factoryBean.create();
	}
	
	@WebMethod(exclude=true) private GeneticAlgFactory newGeneticAlgFactory(String factoryClass, String factoryMethod) throws Exception {
		Class clazz = Class.forName(factoryClass);
		Object o = null;
		if( factoryMethod != null && factoryMethod.length()!=0 ) {
			java.lang.reflect.Method m = clazz.getMethod(factoryMethod);
			o = m.invoke(null);
		} else o = clazz.newInstance();
		return (GeneticAlgFactory)o;
	}
	
	@SuppressWarnings(value="unchecked")
	private class FitnessFunctionCallable implements Callable<Double> {
		private String factoryClass;
		private String factoryMethod;
		private String phenotypeXml;
		private String uuid;
		private Logger logger = Logger.getLogger(this.getClass());
		private GeneticAlgWSManagerEndpoint client=null;
		
		public FitnessFunctionCallable(String uuid, String factoryClass, String factoryMethod, String phenotypeXml) throws Exception {
			this.factoryClass = factoryClass;
			this.factoryMethod = factoryMethod;
			this.phenotypeXml = phenotypeXml;
			this.uuid = uuid;
			this.client = newClient();
		}
		
		public Double call() throws Exception {
			Double ret=0.0;
			
			try {
				runningEvaluations.increment();
				if( runningEvaluations.count()>maxRunningEvaluations.value() )
					client.notifyUnavailable(nodeUrl.toString(),runningEvaluations.count().intValue());
				
				GeneticAlgFactory gaf = newGeneticAlgFactory(factoryClass,factoryMethod);
				logger.trace(uuid+" GeneticAlgFactory successfully created");
				
				Phenotype p = gaf.newPhenotype();
				SAXReader sr = new SAXReader();
				Document d = sr.read(new StringReader(phenotypeXml));
				p.setXml( d.getRootElement() );
				logger.trace(uuid+" reconstructed phenotype as "+p);
				
				FitnessFunction ff = gaf.newFitnessFunction();
				ret = ff.evaluate(p);

				logger.trace(uuid+" passing back phenotype xml as "+p.getXml().asXML());
				client.notifyFitnessComplete(nodeUrl.value(),runningEvaluations.count().intValue()-1,uuid,p.getXml().asXML());
				completedEvaluations.increment();
			} catch(Exception e) {
				thrownExceptions.increment();
				String stackTrace = "EXCEPTION - "+uuid+" "+StringUtils.stackTraceToString(e);
				logger.error(stackTrace);
				try {
					client.notifyFitnessComplete(nodeUrl.value(),runningEvaluations.count().intValue()-1,uuid,stackTrace);
				} catch(Exception e2) {
					stackTrace = "EXCEPTION - "+uuid+" "+StringUtils.stackTraceToString(e2);
					logger.error(stackTrace);
				}
				throw e;
			} finally {
				runningEvaluations.decrement();
				if( runningEvaluations.count()<maxRunningEvaluations.value() )
					client.notifyAvailable(nodeUrl.value(),runningEvaluations.count().intValue());
			}
			return ret;
		}
	}
}
