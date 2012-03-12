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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.*;

/**
 * A GeneticAlgFactory using Spring-bean prototypes.
 * 
 * @author schang
 *
 * @param <P>
 * @param <G>
 */
public class SpringGeneticAlgFactory<P extends Phenotype<G>,G extends Gene> 
	implements GeneticAlgFactory<P,G>, BeanFactoryAware {

	List<String> genes                     = null;
	private String phenotypeBeanName       = "phenotype";
	private String fitnessFunctionBeanName = "fitness-function";
	
	private BeanFactory beanFactory = null;
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		this.beanFactory = factory;
	}
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	public void setGeneBeanNames(List<String> beanNames) {
		genes = beanNames;
	}
	public List<String> getGeneBeanNames() {
		return genes;
	}
	
	public void setPhenotypeBeanName(String beanName) {
		phenotypeBeanName = beanName;
	}
	public String getPhenotypeBeanName() {
		return phenotypeBeanName;
	}
	
	public void setFitnessFunctionBeanName(String beanName) {
		fitnessFunctionBeanName=beanName;
	}
	public String getFitnessFunctionBeanName() {
		return fitnessFunctionBeanName;
	}
	
	@Override
	public G newGene(String name) throws GeneticAlgException {
		Object o = beanFactory.getBean(name);
		if( o instanceof Gene )
			return (G)o;
		else throw new GeneticAlgException(name+" was unusable.  A Gene must implement the Gene interface.");
	}

	@Override
	public G newGene() throws GeneticAlgException {
		Collections.shuffle(genes);
		return newGene( genes.get(0) );
	}
	
	@Override
	public P newPhenotype() {
		Object o = beanFactory.getBean(phenotypeBeanName);
		if( o instanceof GenericPhenotype ) {
			((GenericPhenotype) o).setFactory(this);
			return (P)o;
		} return null;
	}
	
	@Override
	public FitnessFunction<P,G> newFitnessFunction() {
		return (FitnessFunction<P,G>)beanFactory.getBean(fitnessFunctionBeanName);
	}

}
