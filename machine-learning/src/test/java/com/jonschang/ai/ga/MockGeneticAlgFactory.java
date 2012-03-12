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

public class MockGeneticAlgFactory implements GeneticAlgFactory<MockGeneticAlgFactory.MockPhenotype,MockGeneticAlgFactory.MockGene> {
	List<String> genes = new ArrayList<String>();
	private MockGeneticAlgFactory() {
		genes.add("alf");
		genes.add("ruv");
		genes.add("yama");
		genes.add("waters");
		genes.add("soprano");
		genes.add("another");
		genes.add("black&mild");
		genes.add("jesus she is hot");
		genes.add("man i really wish");
	}
	
	static public MockGeneticAlgFactory newFactory() {
		return new MockGeneticAlgFactory();
	}
	
	@Override public MockGene newGene(String name) {
		//Logger.getLogger(this.getClass()).trace("in newGene(name)");
		MockGene gene = new MockGene();
		gene.setName(name);
		gene.setExpressiveness(1.0f);
		return gene;
	}
	@Override public MockGene newGene() {
		//Logger.getLogger(this.getClass()).trace("in newGene()");
		Collections.shuffle(genes);
		return newGene( genes.get(0) );
	}
	@Override public MockPhenotype newPhenotype() {
		//Logger.getLogger(this.getClass()).trace("in newPhenotype()");
		MockPhenotype phen = new MockPhenotype();
		phen.setFactory(this);
		return phen;
	}
	@Override public MockFitnessFunction newFitnessFunction() {
		MockFitnessFunction tff = new MockFitnessFunction();
		return tff;
	}	
	
	public class MockPhenotype extends GenericPhenotype<MockGene> {}
	public class MockGene extends GenericGene {}
	public class MockFitnessFunction implements FitnessFunction<MockPhenotype,MockGene> {
		public Double evaluate(MockPhenotype individual) {
			//Logger.getLogger(this.getClass()).trace("in evaluate()");
			individual.setScore(((Integer)individual.toString().length()).doubleValue());
			return individual.getScore();
		}
	}
}