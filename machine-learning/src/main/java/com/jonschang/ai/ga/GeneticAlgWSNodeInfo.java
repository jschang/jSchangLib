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

import javax.jws.WebMethod;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * A Node information holder class.
 * 
 * Only used here, so all data members are public (for the moment)
 * 
 * @author schang
 */
public class GeneticAlgWSNodeInfo implements Comparable<GeneticAlgWSNodeInfo>{
	private String url = null;
	private Long freeMemory = null;
	private Integer completedEvaluations = null;
	private Integer runningEvaluations = null;
	private Integer submittedEvaluations = null;
	private Integer thrownExceptions = null;
	private String lastException = null;
	private Boolean available = null;
	public GeneticAlgWSNodeEndpoint client = null;

	public GeneticAlgWSNodeInfo(String url) {
		this.url = url;
	}
	
	public synchronized String url() { return url; }
	public synchronized void url(String val) { url=val; }
	
	public synchronized Long freeMemory() { return freeMemory; }
	public synchronized void freeMemory(Long val) { freeMemory=val; }
	
	public synchronized Integer completedEvaluations() { return completedEvaluations; }
	public synchronized void completedEvaluations(Integer val) { completedEvaluations=val; }
	
	public synchronized Integer runningEvaluations() { return runningEvaluations; }
	public synchronized void runningEvaluations(Integer val) { runningEvaluations=val; }
	
	public synchronized Integer submittedEvaluations() { return submittedEvaluations; }
	public synchronized void submittedEvaluations(Integer val) { submittedEvaluations=val; }
	
	public synchronized Integer thrownExceptions() { return thrownExceptions; }
	public synchronized void thrownExceptions(Integer val) { thrownExceptions=val; }
	
	public synchronized String lastException() { return lastException; }
	public synchronized void lastException(String val) { lastException=val; }
	
	public synchronized Boolean available() { return available; }
	public synchronized void available(Boolean val) { available=val; }
	
	@Override public int compareTo(GeneticAlgWSNodeInfo o) {
		return o.runningEvaluations.compareTo(runningEvaluations);
	}

	public synchronized GeneticAlgWSNodeInfo clone() {
		GeneticAlgWSNodeInfo cloned = new GeneticAlgWSNodeInfo(url());
		cloned.freeMemory=freeMemory();
		cloned.completedEvaluations=completedEvaluations();
		cloned.runningEvaluations=runningEvaluations();
		cloned.submittedEvaluations=submittedEvaluations();
		cloned.thrownExceptions=thrownExceptions();
		cloned.lastException=lastException();
		cloned.client = newClient(url());
		return cloned;
	}
	public synchronized GeneticAlgWSNodeEndpoint newClient() {
		return newClient(url);
	}
	public synchronized void updateNodeStatus() {
		freeMemory = client.getFreeMemory();
		submittedEvaluations = client.getCompletedEvaluations();
		thrownExceptions = client.getThrownExceptions();
		runningEvaluations = client.getRunningEvaluations();
	}
	static public GeneticAlgWSNodeEndpoint newClient(String url) {
		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		factoryBean.setServiceClass(GeneticAlgWSNodeEndpoint.class);
		factoryBean.setAddress(url);
		return (GeneticAlgWSNodeEndpoint)factoryBean.create();
	}
}