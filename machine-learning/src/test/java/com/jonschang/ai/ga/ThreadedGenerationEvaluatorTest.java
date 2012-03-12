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

import org.apache.log4j.*;
import org.junit.*;
import java.util.*;
import org.dom4j.*;
import com.jonschang.ai.ga.MockGeneticAlgFactory.*;

public class ThreadedGenerationEvaluatorTest {
	@Test public void testGeneticAlgorithm() throws Exception {
		
		com.jonschang.utils.LoggingUtils.configureLogger();
		
		GeneticAlgorithm<MockPhenotype,MockGene> ga = new GeneticAlgorithm<MockPhenotype,MockGene>();
		GeneticAlgFactory<MockPhenotype,MockGene> gaf = MockGeneticAlgFactory.newFactory();
		
		ThreadedGenerationEvaluator<MockPhenotype,MockGene> tge = new ThreadedGenerationEvaluator<MockPhenotype,MockGene>();
		tge.setGeneticAlgFactory( gaf );
		
		ga.setGeneFactory( gaf );
		ga.setGenerationEvaluator( tge );
		ga.setNumberOfOffspring(30, 30);
		ga.setPhenotypeSize(((Integer)6).shortValue());
		ga.setRemoveDuplicates(true);
		ga.setMaintainSize(true);
		ga.setMutationRate(0.20f);
		
		ga.run();
		
		Collections.sort(ga.getPopulation());
		Logger.getLogger(this.getClass()).trace("winner is "+ga.getPopulation().get( ga.getPopulation().size()-1 ));
	}
}
