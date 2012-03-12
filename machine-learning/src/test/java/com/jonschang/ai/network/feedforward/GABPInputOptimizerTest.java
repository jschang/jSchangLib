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

package com.jonschang.ai.network.feedforward;

import org.junit.*;
import java.util.*;
import com.jonschang.ai.network.*;
import com.jonschang.math.vector.*;
import com.jonschang.opencl.*;

public class GABPInputOptimizerTest {
	@Test public void testTraining() throws NetworkTrainingException {
		
		GABPInputOptimizer mbp = new GABPInputOptimizer();
		
		List<MathVector> mvl = new ArrayList<MathVector>();
		mvl.add( new VectorImpl( 1.0, 1.0, 1.0, 0.0, 1.0 ) );
		mvl.add( new VectorImpl( 1.0, 1.0, 0.0, 0.0, 0.0 ) );
		mvl.add( new VectorImpl( 1.0, 1.0, 0.0, 1.0, 0.0 ) );
		mbp.setInputMasks(mvl);
		
		OCLContext context = OCLContext.create();
		OCLCommandQueue queue = OCLCommandQueue.create(context);
		mbp.setOCLContext(context);
		mbp.setOCLCommandQueue(queue);
		
		// create a TrainingSetSource with maybe 6 ValueSources
		TrainingSetSource tss = createTrainingSetSource();
		mbp.setTrainingSetSource(tss);
		mbp.setPrototype(FeedForward.create( new SigmoidActivator(), 5, 3, 4, 2 ));
		
		// train
		mbp.train();
		
		// test
	}
	
	public TrainingSetSource createTrainingSetSource() {
		GenericTrainingSetSource tss = new GenericTrainingSetSource();
		for( double x=0; x<100; x++ )
			tss.addPair(
				// input vector with 4 strongly correlated channels and 1 noise channel
				new VectorImpl(
					Math.pow(x,2),
					Math.pow(x,3),
					Math.sqrt(x),
					x,
					Math.random()
				), 
				// output vector with 2 strongly correlated channels
				new VectorImpl(
					Math.sin((Math.PI/100))*x,
					x*7.0
				) 
			);
		return tss;
	}
}
