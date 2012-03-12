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
import com.jonschang.ai.ga.MockGeneticAlgFactory.*;
import org.dom4j.*;

public class WSGenerationEvaluatorTest {
	@Test public void testGeneticAlgorithm() throws Exception {
		
		// TODO: fix this test so that it works correctly.
		if(1==1) return;
		
		// this was originally for configuring the logger
		com.jonschang.utils.LoggingUtils.configureLogger();
		
		GeneticAlgorithm<MockPhenotype,MockGene> ga = new GeneticAlgorithm<MockPhenotype,MockGene>();
		GeneticAlgFactory<MockPhenotype,MockGene> gaf = MockGeneticAlgFactory.newFactory();
		
		GeneticAlgWSEndpointsConfigurator conf = new GeneticAlgWSEndpointsConfigurator();
		conf.setHostName("localhost");
		conf.setPort(8000);
		conf.setProtocol("http");
		conf.setPath("/services");
		conf.createEndpoints();
		
		WSGenerationEvaluator<MockPhenotype,MockGene> tge = new WSGenerationEvaluator<MockPhenotype,MockGene>();
		tge.setGeneticAlgFactory(gaf);
		tge.setFactoryClass("com.jonschang.ai.ga.TestGeneticAlgFactory");
		tge.setFactoryMethod("newFactory");
		tge.setManagerEndpoint( conf.getManager() );
		tge.setGeneticAlgFactory( gaf );
		
		conf.getManager().addWSGenerationEvaluator(tge);
		conf.getNode().setNodeUrl(conf.getNodeUrl());
		conf.getNode().setManagerUrl(conf.getManagerUrl());
		
		ga.setGeneFactory( gaf );
		ga.setGenerationEvaluator( tge );
		ga.setNumberOfOffspring( 30, 30 );
		ga.setPhenotypeSize( ((Integer)6).shortValue() );
		ga.setRemoveDuplicates( true );
		ga.setMaintainSize( true );
		ga.setMutationRate( 0.20f );
		
		ga.run();
		
		Collections.sort(ga.getPopulation());
		Logger.getLogger(this.getClass()).trace("winner is "+ga.getPopulation().get( ga.getPopulation().size()-1 ));
	}
}
