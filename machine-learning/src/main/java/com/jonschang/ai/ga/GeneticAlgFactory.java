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


/**
 * An abstract factory for different concrete implementations 
 * to create custom Phenotypes and Genes
 * 
 * Implementors should be self-contained and aware of what
 * resources they need and able to fetch them.
 * 
 * @author schang
 */
public interface GeneticAlgFactory<P extends Phenotype<G>,G extends Gene> {
	
	/**
	 * A new instance of the Gene identified by the name passed in.
	 * 
	 * @param name The unique name of the gene within the genome.
	 * @return A new instance of the unique gene in the genome.
	 */
	G newGene(String name) throws GeneticAlgException;
	
	/**
	 * Provides for mutation by returning a random new Gene from the genome.
	 * 
	 * @return A random Gene within the Genome.
	 */
	G newGene() throws GeneticAlgException;
	
	/**
	 * @return A new Phenotype
	 */
	P newPhenotype() throws GeneticAlgException;
	
	/**
	 * The only means of creating a FitnessFunction,
	 * for only the GeneticAlgFactory has awareness of how it should be
	 * created.
	 * 
	 * @return A FitnessFunction to evaluate a Phenotype with.
	 */
	FitnessFunction<P,G> newFitnessFunction() throws GeneticAlgException;
}
