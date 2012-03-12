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

import java.util.List;

import org.dom4j.Node;

import com.jonschang.utils.HasXml;

/**
 * An individual within a generation
 * 
 * setXml() and getXml() should be able to completely reconstruct
 * the Phenotype and any additional data used by the Phenotype, 
 * with the explicit exception of children and parents. 
 *  
 * @author schang
 * @param <G>
 */
public interface Phenotype<G extends Gene> extends Comparable<Phenotype<G>>, HasXml {

	/**
	 * A brief representation of the Phenotype.
	 * @return a brief representation of the Phenotype suitable for logging entries.
	 */
	String toString();
	
	void setGenes(List<G> genes);
	List<G> getGenes();

	void setScore(Double score);
	Double getScore();
	
	void setFactory(GeneticAlgFactory<? extends Phenotype<G>,G> factory);
	GeneticAlgFactory<? extends Phenotype<G>,G> getFactory();

	void setParents(List<Phenotype<G>> parents);
	List<Phenotype<G>> getParents();

	List<Phenotype<G>> getChildren();
	void setChildren(List<Phenotype<G>> children);
}