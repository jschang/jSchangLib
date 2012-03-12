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

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clEnqueueWriteBuffer;
import static org.jocl.CL.clSetKernelArg;

import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.*;

import com.jonschang.math.vector.*;
import com.jonschang.ai.network.*;
import com.jonschang.opencl.*;
import com.jonschang.utils.FileUtils;

import org.jocl.*;

// not public by intention.  this is only used by GABPInputOptimizer
class GABPFeedForwards {
	
	private List<float[]> error = new ArrayList<float[]>();
	private List<float[]> activation = new ArrayList<float[]>();
	private List<float[]> input = new ArrayList<float[]>();
	private List<float[]> threshold = new ArrayList<float[]>();
	private List<float[]> synapse = new ArrayList<float[]>();
	private float[] inputMasks = null;
	private float[] learningRate = null;
	
	private List<cl_mem> clError = new ArrayList<cl_mem>();
	private List<cl_mem> clActivation = new ArrayList<cl_mem>();
	private List<cl_mem> clInput = new ArrayList<cl_mem>();
	private List<cl_mem> clThreshold = new ArrayList<cl_mem>();
	private List<cl_mem> clSynapse = new ArrayList<cl_mem>();
	private cl_mem clInputMasks;
	private cl_mem clLearningRate;
	private cl_mem clInputTrainingData;
	private cl_mem clOutputTrainingData;
	private Map<String,cl_kernel> kernels = new HashMap<String,cl_kernel>();
	private boolean buffersCreated=false;
	
	private float[] inputTrainingData = null;
	private float[] outputTrainingData = null;
	
	private int inputs = 0;
	private int outputs = 0;
	private int copies = 0;
	
	private FeedForward prototype = null;
	private TrainingSetSource trainingSetSource = null;
	private List<MathVector> inputMaskVectors = null;
	
	private OCLContext context = null;
	private OCLCommandQueue commandQueue = null;
	private cl_program program = null;
	
	private long[] globalWorkSize = new long[2]; 
	
	public List<float[]> getError()        { return error; }
	public List<float[]> getActivation()   { return activation; }
	public List<float[]> getThreshold()    { return threshold; }
	public List<float[]> getSynapse()      { return synapse; }
	public float[]       getInputMasks()   { return inputMasks; }
	public float[]       getLearningRate() { return learningRate; }
	public float[]       getInputData()    { return inputTrainingData; }
	public float[]       getOutputData()   { return outputTrainingData; }
	public int           getCopies()       { return copies; }
	
	public void computeInputLayer(int dataRow) {
		cl_kernel kernel = kernels.get("computeInputLayer");
		
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clInputTrainingData));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(clInputMasks));
        clSetKernelArg(kernel, 2, Sizeof.cl_uint, Pointer.to(new int[]{inputs}));
        clSetKernelArg(kernel, 3, Sizeof.cl_uint, Pointer.to(new int[]{dataRow}));
        
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(clInput.get(1)));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(clActivation.get(1)));
        clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to(clThreshold.get(1)));
        clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(clSynapse.get(0)));
        clSetKernelArg(kernel, 8, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(1).size()}));

        globalWorkSize[0]=prototype.getAllLayers().get(1).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	public void computeNextLayer(int backwardLayer, int forwardLayer) {
		cl_kernel kernel = kernels.get("computeNextLayer");
		
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clActivation.get(backwardLayer)));
        clSetKernelArg(kernel, 1, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(backwardLayer).size()}));
        
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(clInput.get(forwardLayer)));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(clActivation.get(forwardLayer)));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(clThreshold.get(forwardLayer)));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(clSynapse.get(forwardLayer-1)));
        clSetKernelArg(kernel, 6, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(forwardLayer).size()}));

        globalWorkSize[0]=prototype.getAllLayers().get(forwardLayer).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	public void computeOutputError(int dataRow) {
		cl_kernel kernel = kernels.get("computeOutputError");
		
		int lastLayer = clInput.size()-1;
		
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clOutputTrainingData));
        clSetKernelArg(kernel, 1, Sizeof.cl_uint, Pointer.to(new int[]{outputs}));
        clSetKernelArg(kernel, 2, Sizeof.cl_uint, Pointer.to(new int[]{dataRow}));
        
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(clInput.get(lastLayer)));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(clActivation.get(lastLayer)));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(clError.get(lastLayer)));
        clSetKernelArg(kernel, 6, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(lastLayer).size()}));

        globalWorkSize[0]=prototype.getAllLayers().get(lastLayer).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	public void computePrevLayerError(int backwardLayer, int forwardLayer) {
		cl_kernel kernel = kernels.get("computePrevLayerError");
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clInput.get(backwardLayer)));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(clError.get(backwardLayer)));
        clSetKernelArg(kernel, 2, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(backwardLayer).size()}));
		
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(clError.get(forwardLayer)));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(clSynapse.get(forwardLayer-1)));
        clSetKernelArg(kernel, 5, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(forwardLayer).size()}));

        globalWorkSize[0]=prototype.getAllLayers().get(backwardLayer).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	/**
	 * Updates the synapses between the backward-layer and the layer specified
	 * @param layer The forward layer the synapses feed into
	 */
	public void updateSynapses(int layer) {
		cl_kernel kernel = kernels.get("updateSynapses");
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clError.get(layer)));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(clActivation.get(layer)));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(clSynapse.get(layer-1)));
        clSetKernelArg(kernel, 3, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(layer-1).size()}));
        clSetKernelArg(kernel, 4, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(layer).size()}));
        clSetKernelArg(kernel, 5, Sizeof.cl_mem, Pointer.to(this.clLearningRate));

        globalWorkSize[0]=prototype.getAllLayers().get(layer).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	/**
	 * Updates the thresholds of the neurons for a layer
	 * @param layer The layer of neurons to update
	 */
	public void updateThresholds(int layer) {
		cl_kernel kernel = kernels.get("updateThresholds");
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(clError.get(layer)));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(clActivation.get(layer)));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(clThreshold.get(layer)));
        clSetKernelArg(kernel, 3, Sizeof.cl_uint, Pointer.to(new int[]{prototype.getAllLayers().get(layer).size()}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(this.clLearningRate));

        globalWorkSize[0]=prototype.getAllLayers().get(layer).size();
        globalWorkSize[1]=copies;
        
        clEnqueueNDRangeKernel(commandQueue.getCLCommandQueue(), kernel, 2, null, globalWorkSize, null, 0, null, null);
	}
	public void createProgram() throws NetworkTrainingException {
		String str = null;
		try {
			str = FileUtils.readResourceFile(this.getClass(), "GABPFeedForwards.cl");
		} catch (IOException ioe) {
			throw new NetworkTrainingException(ioe);
		}
		program = context.createProgram(new String[]{str});
		kernels.put("computeInputLayer",context.createKernel(program,"computeInputLayer"));
		kernels.put("computeNextLayer",context.createKernel(program,"computeNextLayer"));
		kernels.put("computeOutputError",context.createKernel(program,"computeOutputError"));
		kernels.put("computePrevLayerError",context.createKernel(program,"computePrevLayerError"));
		kernels.put("updateSynapses",context.createKernel(program,"updateSynapses"));
		kernels.put("updateThresholds",context.createKernel(program,"updateThresholds"));
	}
	public void releaseProgram() {
		for( Map.Entry<String, cl_kernel> ent : kernels.entrySet() )
			context.releaseKernel(ent.getValue());
		kernels.clear();
		context.releaseProgram(program);
	}
	public void createBuffers() {
		if( buffersCreated )
			return;
		writeMemList(clActivation,activation,CL.CL_MEM_READ_WRITE);
		writeMemList(clInput,input,CL.CL_MEM_READ_WRITE);
		writeMemList(clThreshold,threshold,CL.CL_MEM_READ_WRITE);
		writeMemList(clSynapse,synapse,CL.CL_MEM_READ_WRITE);
		writeMemList(clError,error,CL.CL_MEM_READ_WRITE);
		clInputMasks = writeMem(inputMasks,CL.CL_MEM_READ_ONLY);
		clLearningRate = writeMem(learningRate,CL.CL_MEM_READ_ONLY);
		clInputTrainingData = writeMem(inputTrainingData,CL.CL_MEM_READ_ONLY);
		clOutputTrainingData = writeMem(outputTrainingData,CL.CL_MEM_READ_ONLY);
		buffersCreated=true;
	}
	public void readResults() {
		readMemList(clActivation,activation);
		readMemList(clInput,input);
		readMemList(clThreshold,threshold);
		readMemList(clSynapse,synapse);
		readMemList(clError,error);
	}
	private void readMemList(List<cl_mem> mems, List<float[]> datas) {
		int i=0;
		for( float[] data : datas ) {
			readMem(mems.get(i++),data);
		}
	}
	private void readMem(cl_mem mem, float[] data) {
		clEnqueueReadBuffer(commandQueue.getCLCommandQueue(), mem, CL_TRUE, 0, Sizeof.cl_float * data.length, Pointer.to(data), 0, null, null);
	}
	private cl_mem writeMem(float[] floats, long flags) {
		cl_mem mem = context.createBuffer(floats,flags);
		clEnqueueWriteBuffer(commandQueue.getCLCommandQueue(), mem, true, 0, floats.length * Sizeof.cl_float, Pointer.to(floats), 0, null, null);
		return mem;
	}
	private void writeMemList(List<cl_mem> clMems, List<float[]> floats, long flags) {
		for( float[] theseDs : floats ) {
			clMems.add( writeMem(theseDs, flags) );
		}
	}
	public void releaseBuffers() {
		for( cl_mem mem : clError ) 
			context.releaseBuffer(mem);
		clError.clear();
		for( cl_mem mem : clActivation )
			context.releaseBuffer(mem);
		clActivation.clear();
		for( cl_mem mem : clInput )
			context.releaseBuffer(mem);
		clInput.clear();
		for( cl_mem mem : clThreshold )
			context.releaseBuffer(mem);
		clThreshold.clear();
		for( cl_mem mem : clSynapse )
			context.releaseBuffer(mem);
		clSynapse.clear();
		context.releaseBuffer(clInputMasks);
		context.releaseBuffer(clInputTrainingData);
		context.releaseBuffer(clOutputTrainingData);
		context.releaseBuffer(clLearningRate);
		buffersCreated=false;
	}
	
	public GABPFeedForwards(
		OCLContext        context, 
		OCLCommandQueue   commandQueue, 
		FeedForward       prototype,
		List<MathVector>  inputMaskVectors, 
		TrainingSetSource trainingData
	) {
		
		this.context = context;
		this.commandQueue = commandQueue;
		this.copies = inputMaskVectors.size();
		this.prototype = prototype;
		this.trainingSetSource = trainingData;
		this.inputMaskVectors = inputMaskVectors; 
		
		loadPrototype();
		loadTrainingData();
	}
	
	public void loadTrainingData() {
		// TODO: figure out a good way to intelligently, dynamically page the data based on available resources 
		int count = 0;
		for( TrainingSetSource.Pair pair : trainingSetSource ) {
			count++;
		}
		inputTrainingData = new float[count*inputs];
		outputTrainingData = new float[count*outputs];
		int inputOffset = 0, outputOffset = 0;
		for( TrainingSetSource.Pair pair : trainingSetSource ) {
			for( Double d : pair.getInput().getData() )
				inputTrainingData[inputOffset++] = d.floatValue();
			for( Double d : pair.getOutput().getData() )
				outputTrainingData[outputOffset++] = d.floatValue();
		}
	}
	
	public void loadPrototype() {
		int i = 0;
		float[] temp = null;
		
		learningRate = new float[copies];
		for(i=0;i<copies;i++)
			learningRate[i]=0.02f;
		i=0;
		
		inputs = prototype.getInputNeurons().size();
		outputs = prototype.getOutputNeurons().size();
		
		globalWorkSize[1] = copies;
		
		List<List<Neuron>> neurons = prototype.getAllLayers();
		int neuronsCount, synapsesCount;
		Integer lastLayerNs = null;
		
		inputMasks = new float[neurons.get(0).size()*copies];
		for(MathVector mv : inputMaskVectors) {
			for(double d:mv.getData()) {
				inputMasks[i] = (float)d;
				i++;
			}
		}		
		
		// iterate over each neuron in the layer
		for( List<Neuron> layerNeurons : neurons ) {
			
			// create a copy of the synapses for each layer
			// for each network
			if( lastLayerNs != null ) {
				synapsesCount = lastLayerNs * layerNeurons.size() * copies;
				temp = new float[synapsesCount];
				i = 0;
				// TODO: this is shit...come on now...fucking do this better.
				for( int j=0; j<copies; j++ ) {
					for( Neuron neuron : layerNeurons )
						for( Synapse synapse : neuron.getInputSynapses() )
							temp[i++] = (float)synapse.getWeight();
				}
				synapse.add( temp );
			}
			
			neuronsCount = layerNeurons.size() * copies;
			
			// create a copy of the thresholds for each layer
			// for each network
			temp = new float[neuronsCount];
			i=0;
			for( int j=0; j<copies; j++ ) {
				for( Neuron neuron : layerNeurons )
					temp[i++] = (float)neuron.getThreshold();
			}
			threshold.add( temp );
			
			temp = new float[neuronsCount];
			activation.add( temp );
			temp = new float[neuronsCount];
			input.add( temp );
			temp = new float[neuronsCount];
			error.add( temp );
			
			lastLayerNs = layerNeurons.size();				
		}
	}
}
