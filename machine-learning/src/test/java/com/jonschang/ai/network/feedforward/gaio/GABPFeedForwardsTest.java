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

package com.jonschang.ai.network.feedforward.gaio;

import org.junit.*;
import org.apache.log4j.*;
import java.util.*;

import com.jonschang.ai.network.*;
import com.jonschang.ai.network.feedforward.FeedForward;
import com.jonschang.ai.network.feedforward.gaio.GABPFeedForwards;
import com.jonschang.math.vector.*;
import com.jonschang.opencl.OCLCommandQueue;
import com.jonschang.opencl.OCLContext;

public class GABPFeedForwardsTest {
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Validates that the prototype loads into arrays correctly.
	 * @throws Exception
	 */
	@Test public void testLoadPrototypes() throws Exception {
		
		FeedForward ff = new FeedForward( new SigmoidActivator(), 2, 3, 2 );
		
		List<MathVector> lmv = new ArrayList<MathVector>();
		lmv.add( new VectorImpl(1.0,0.0) );
		lmv.add( new VectorImpl(0.0,1.0) );
		lmv.add( new VectorImpl(1.0,1.0) );
		
		int copies = lmv.size();
		GABPFeedForwards ffs = new GABPFeedForwards(null,null,ff,lmv,createTrainingSetSource());
		
		Assert.assertTrue( ffs.getThreshold().size()==ff.getAllLayers().size() );
		Assert.assertTrue( ffs.getActivation().size()==ff.getAllLayers().size() );
		Assert.assertTrue( ffs.getError().size()==ff.getAllLayers().size() );
		Assert.assertTrue( ffs.getSynapse().size()==ff.getAllLayers().size()-1 );
		Assert.assertTrue( ffs.getCopies()==copies );
		
		int layerId = 0;
		List<Neuron> lastLayerNS = null;
		int synapsesId = 0;
		for( List<Neuron> layerNeurons : ff.getAllLayers() ) {
			
			int layerSize = layerNeurons.size();
			
			Assert.assertTrue( ffs.getActivation().get(layerId).length == layerSize*copies );
			Assert.assertTrue( ffs.getError().get(layerId).length      == layerSize*copies );
			Assert.assertTrue( ffs.getThreshold().get(layerId).length  == layerSize*copies );
			
			int neuronId = 0;
			for( Neuron neuron : layerNeurons ) {
				for( int copy=0; copy<copies; copy++ ) {
					logger.trace( ffs.getThreshold().get(layerId)[copy*layerSize+neuronId] +"=="+ neuron.getThreshold() );
					Assert.assertTrue( ffs.getThreshold().get(layerId)[copy*layerSize+neuronId] == (float)neuron.getThreshold() );
				}
				neuronId ++;
			}
			
			if( lastLayerNS != null ) {
				int synapsesCount = lastLayerNS.size() * layerNeurons.size() * copies;
				int synapsesSize = lastLayerNS.size() * layerNeurons.size();
				Assert.assertTrue( ffs.getSynapse().get(synapsesId).length == synapsesCount );
				int synapseId = 0;
				for( Neuron neuron : layerNeurons ) {
					for( Synapse synapse : neuron.getInputSynapses() ) {
						for( int copy=0; copy<copies; copy++ ) {
							//logger.trace( ffs.synapse.get(synapsesId)[copy*synapsesSize+synapseId] +"=="+ synapse.getWeight() );
							Assert.assertTrue( ffs.getSynapse().get(synapsesId)[copy*synapsesSize+synapseId] == (float)synapse.getWeight() );
						}
						synapseId++;
					}
				}
				synapsesId++;
			}

			lastLayerNS = layerNeurons;
			layerId++;
		}
	}
	
	/**
	 * Validates that the expected result of a single iteration is correct.
	 * @throws Exception
	 */
	@Test public void testComputeAndBackPropagate() throws Exception {
		
		// This is inefficient in traffic between the GPU and system memory
		// because I'd rather be assured that one operation has not tampered
		// with data from another operation.  
		//
		// For future debugging, I currently believe it's better to test after each
		// operation, than test the entire result after all operations.
		
		float tolerance = 1E-7f;
		
		GABPFeedForwards ffs = createGABPFeedForwards();
		
		ffs.createBuffers();
		ffs.createProgram();
		
		ffs.computeInputLayer(0);
		ffs.readResults();
		// mathematically validated 11/14/2010
		Assert.assertArrayEquals( 
			new float[]{
				0.5f, 0.5f, 0.5f, 
				0.5249792f, 0.5249792f, 0.5249792f, 
				0.54983395f, 0.54983395f, 0.54983395f
			}, 
			ffs.getActivation().get(1),
			tolerance
		);
		logger.trace("compute input : "+Arrays.toString(ffs.getActivation().get(1)));

		ffs.computeNextLayer(1,2);
		ffs.readResults();
		// mathematically validated 11/14/2010
		Assert.assertArrayEquals(
			new float[]{
				0.5124974f, 0.5124974f, 
				0.5143695f, 0.5143695f, 
				0.51623183f, 0.51623183f
			}, 
			ffs.getActivation().get(2),
			tolerance
		);
		logger.trace("compute middle : "+Arrays.toString(ffs.getActivation().get(2)));
		
		ffs.computeOutputError(0);
		ffs.readResults();
		// mathematically validated 11/14/2010
		Assert.assertArrayEquals(
			new float[]{
				0.09681512f, -0.10305993f, 
				0.096328f, -0.10350681f, 
				0.09584092f, -0.103948295f
			}, 
			ffs.getError().get(2),
			tolerance
		);
		logger.trace("output error : "+Arrays.toString(ffs.getError().get(2)));
		
		ffs.computePrevLayerError(1,2);
		ffs.readResults();
		// mathematically validated 11/14/2010
		logger.trace("error out->mid : "+Arrays.toString(ffs.getError().get(1)));
		Assert.assertArrayEquals(
			new float[]{
				-1.561204E-4f, -1.561204E-4f, -1.561204E-4f, 
				-1.7902236E-4f, -1.7902236E-4f, -1.7902236E-4f, 
				-2.006708E-4f, -2.006708E-4f, -2.006708E-4f
			}, 
			ffs.getError().get(1),
			tolerance
		);		
		
		ffs.computePrevLayerError(0,1);
		ffs.readResults();
		logger.trace("error mid->in : "+Arrays.toString(ffs.getError().get(0)));
		Assert.assertArrayEquals(
			new float[]{
				-1.1709029E-5f, -1.1709029E-5f, 
				-1.3426677E-5f, -1.3426677E-5f, 
				-1.5050309E-5f, -1.5050309E-5f
			}, 
			ffs.getError().get(0),
			tolerance
		);
		
		ffs.updateSynapses(2);
		ffs.readResults();
		logger.trace("new synapses mid->out : "+Arrays.toString(ffs.getSynapse().get(1)));
		Assert.assertArrayEquals(
			new float[]{
				0.10099235f, 0.10099235f, 0.10099235f, 0.09894364f, 0.09894364f, 0.09894364f, 
				0.100990966f, 0.100990966f, 0.100990966f, 0.09893519f, 0.09893519f, 0.09893519f, 
				0.10098952f, 0.10098952f, 0.10098952f, 0.098926775f, 0.098926775f, 0.098926775f
			}, 
			ffs.getSynapse().get(1),
			tolerance
		);
		
		ffs.updateThresholds(2);
		ffs.readResults();
		logger.trace("new thresholds 2 : "+Arrays.toString(ffs.getThreshold().get(2)));
		Assert.assertArrayEquals(
			new float[]{
				0.09900765f, 0.10105636f, 
				0.09900904f, 0.101064816f, 
				0.09901048f, 0.10107323f
			}, 
			ffs.getThreshold().get(2),
			tolerance
		);
		
		ffs.releaseBuffers();
		ffs.releaseProgram();
	}
	
	private GABPFeedForwards createGABPFeedForwards() {
		OCLContext context = OCLContext.create();
		OCLCommandQueue queue = OCLCommandQueue.create(context);
		
		FeedForward ff = new FeedForward( new SigmoidActivator(), 2, 3, 2 );
		
		List<MathVector> lmv = new ArrayList<MathVector>();
		lmv.add( new VectorImpl(1.0,0.0) );
		lmv.add( new VectorImpl(0.0,1.0) );
		lmv.add( new VectorImpl(1.0,1.0) );
		
		GABPFeedForwards ffs = new GABPFeedForwards(context,queue,ff,lmv,createTrainingSetSource());
		
		for( float[] f : ffs.getSynapse() ) {
			for( int i=0; i<f.length; i++ )
				f[i]=.1f;
		}
		for( float[] f : ffs.getThreshold() ) {
			for( int i=0; i<f.length; i++ )
				f[i]=.1f;
		}
		return ffs;
	}
	
	public TrainingSetSource createTrainingSetSource() {
		GenericTrainingSetSource tss = new GenericTrainingSetSource();
		for( double x=0; x<10; x++ ) 
			tss.addPair( new VectorImpl(1.0,2.0), new VectorImpl(0.9,0.1) );
		return tss;
	}
}
