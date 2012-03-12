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

/**
 * Runs the FitnessFunction on a Phenotype List.
 * 
 * Implementors should be aware of what resources they
 * need and able to fetch them.
 * 
 * Any heavy weight lifting, including verifying that resources
 * are available and fetched should be done in the evaluate()
 * method.
 * 
 * Implemetors should also be thread-safe and stateless,
 * so that they are safe to run within the ThreadedGenerationEvaluator.
 * 
 * @author schang
 */
public interface GenerationEvaluator<P extends Phenotype<G>,G extends Gene> {
	
	void setGeneticAlgFactory(GeneticAlgFactory<P,G> gaf);
	GeneticAlgFactory<P,G> getGeneticAlgFactory();
	
	void evaluate(List<P> phenotypes) throws GeneticAlgException;
}
