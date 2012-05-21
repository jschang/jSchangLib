//#pragma OPENCL EXTENSION cl_khr_fp65: enable

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

/**
 * @brief Calculate the offset of a set of synapses.
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @brief calculates the starting offset of synapses feeding forward into a neuron
 * @param networkId The id of the network
 * @param neuronId The id of the target neuron in the output layer
 * @param inNeurons the number of neurons in the input layer
 * @param outNeurons the number of neurons in the output layer
 */
inline uint offsetSynapses(
	uint networkId,		
	uint neuronId,
	uint inNeurons,
	uint outNeurons	
) {
	return (networkId*inNeurons*outNeurons)+(neuronId*inNeurons);
}

/**
 * @brief calculates the offset of a neuron within the target layer
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @param networkId The id of the network
 * @param neuronId The id of the target neuron in the output layer
 * @param neurons The number of neurons in the output network layer
 */
inline uint offsetNeurons(
	uint networkId,
	uint neuronId,
	uint neurons
) {
	return neurons*networkId+neuronId;
}

inline float valueAt(float value) {
	return 1.0/(1.0+exp(-1.0*value));
}

inline float slopeAt(float value) {
	float pos = valueAt(value);
	return pos*(1.0-pos);
}

/**
 * @brief Calculates the first layer activation values from inputs
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @param dataSet          the input dataSet we're computing 
 * @param inputMasks       though each network is topographically the same, not all inputs are necessarily active
 * @param dataRowWidth     the width of each row in the dataSet
 * @param dataRow          the row of the dataSet to pull from
 * @param layerInput       the next layer "input" values to feed forward
 * @param layerActivation  the activation of the next layer
 * @param layerThresholds  the synapses between the insynapsesOffset(put and next layer
 * @param layerSynapses    the synapses between the input and next layer 
 * @param numberOfNeurons  the number of neurons in the next layer
 */
__kernel void computeInputLayer(
	// dataset variables
	__global const float *dataSet,
	__global const float *inputMasks,
	const uint dataRowWidth,
	const uint dataRow,
	
	// next layer variables
	__global float *layerInput,
	__global float *layerActivation,
	__global const float *layerThresholds,
	__global const float *layerSynapses,
	const uint numberOfNeurons
) {
	uint neuronId = get_global_id(0);  // neuron of the output layer
	uint networkId = get_global_id(1); // the specific network
	
	// calculate the offset of the target neuron
	uint neuronOffset = offsetNeurons(networkId,neuronId,numberOfNeurons);
	
	// calculate the starting offset of the data items
	// it will be the same data for all neurons in the input row
	uint dataSetOffset = dataRowWidth*dataRow;
	
	// calculate the starting offset of the synapses for this network neuron
	uint synapsesOffset = offsetSynapses(networkId,neuronId,dataRowWidth,numberOfNeurons);
	
	uint i=0;
	float input = 0.0f;
	for( i=0; i < dataRowWidth; i++ ) {
		input = input + dataSet[dataSetOffset+i] * layerSynapses[synapsesOffset+i] * inputMasks[networkId*dataRowWidth+i];
	}
	input = input - layerThresholds[neuronOffset];
	layerInput[neuronOffset] = input;
	layerActivation[neuronOffset] = valueAt(input);
}

/**
 * @brief feeds values from a previous layer forward
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @param prevLayerActivation  the activation of the prev layer
 * @param prevNumberOfNeurons  the number of neurons in the prev layer
 * @param layerInput           the next layer "input" values to feed forward
 * @param layerActivation      the activation of the next layer
 * @param layerThresholds      the thresholds between the input and next layer
 * @param layerSynapses        the synapses between the input and next layer 
 * @param numberOfNeurons      the number of neurons in the next layer
 */
__kernel void computeNextLayer(
	// prev layer variables
	__global const float *prevLayerActivation,
	uint prevNumberOfNeurons,
	
	// next layer variables
	__global float *layerInput,
	__global float *layerActivation,
	__global const float *layerThresholds,
	__global const float *layerSynapses,
	uint numberOfNeurons
) {
	uint neuronId = get_global_id(0);   // neuron of the output layer
	uint networkId = get_global_id(1);

	// the start of the prev layers set of neurons
	uint prevNeuronOffset = offsetNeurons(networkId,0,prevNumberOfNeurons);
	// the target neuron offset
	uint neuronOffset = offsetNeurons(networkId,neuronId,numberOfNeurons);
	// the start of this neurons synapses from the prev layer
	uint synapsesOffset = offsetSynapses(networkId,neuronId,prevNumberOfNeurons,numberOfNeurons);
		
	int i=0;
	float input = 0.0f;
	for( i=0; i < prevNumberOfNeurons; i++ ) {
		input = input + prevLayerActivation[prevNeuronOffset+i] * layerSynapses[synapsesOffset+i];
	}
	input = input - layerThresholds[neuronOffset];
	layerInput[neuronOffset] = input;
	layerActivation[neuronOffset] = valueAt(input);	
}

/**
 * @brief Calculate the error from the output layer to the next backward layer.
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @param dataSet         the output dataSet we're computing 
 * @param dataRowWidth    the width of each row in the dataSet
 * @param dataRow         the row of the dataSet to pull from
 * @param layerInput      the prev layer "input" values to feed forward
 * @param layerActivation the activation of the prev layer
 * @param layerError      the resulting error of the output layer 
 * @param numberOfNeurons the number of neurons in the output layer
 */
__kernel void computeOutputError(

	// dataset variables
	__global float *dataSet,
	uint dataRowWidth,
	uint dataRow,
	
	// output layer variables
	__global float *layerInput,
	__global float *layerActivation,
	__global float *layerError,
	uint numberOfNeurons
) {
	uint neuronId = get_global_id(0);  // neuron of the output layer
	uint networkId = get_global_id(1);
	
	uint neuronOffset = offsetNeurons(networkId,neuronId,numberOfNeurons);
	uint dataSetOffset = dataRowWidth*dataRow+neuronId;
	
	layerError[neuronOffset] = slopeAt(layerInput[neuronOffset]) * (dataSet[dataSetOffset] - layerActivation[neuronOffset]);
}

__kernel void accumulateError(
	__global float *lastLayerNeurons,
	const uint lastLayerNeuronCount
)
{
	uint networkId = get_global_id(0);
}

/**
 * Propagate the error from a forward to backward layer.
 *
 * Output has been mathematically validated 11/14/2010
 *
 * @param prevLayerInput      the prev layer "input" values to feed forward
 * @param prevLayerError      the resulting error  
 * @param prevNumberOfNeurons the number of neurons in the input layer
 * @param layerError          the resulting error of the output layer 
 * @param layerSynapses       the synapses between the input and next layer
 * @param numberOfNeurons     the number of neurons in the output layer
 */
__kernel void computePrevLayerError(
	// previous layer variables
	__global const float *prevLayerInput,
	__global float *prevLayerError,
	uint prevNumberOfNeurons,
	
	// output layer variables
	__global const float *layerError,
	__global const float *layerSynapses,
	uint numberOfNeurons
) {
	uint neuronId = get_global_id(0);  // neuron of the prev layer in the network
	uint networkId = get_global_id(1);
	
	// the neuron we're targetting in the previous layer
	uint prevNeuronOffset = offsetNeurons(networkId,neuronId,prevNumberOfNeurons);
	// the starting offset of neurons in the output layer
	uint neuronOffset = offsetNeurons(networkId,0,numberOfNeurons);
	uint synapseOffset;
	
	int i=0;
	float input = 0;
	for( i=0; i < numberOfNeurons; i++ ) {
		// determine the specific synapse offset in the synapses of the forward layer
		synapseOffset = offsetSynapses(networkId,i,prevNumberOfNeurons,numberOfNeurons)+neuronId;
		input = input + layerError[neuronOffset+i] * layerSynapses[synapseOffset];
	}
	prevLayerError[prevNeuronOffset] = input * slopeAt(prevLayerInput[prevNeuronOffset]);
}

/**
 * @brief update the synapses feeding into the current layer
 * @todo each synapse should be run rather than iterating over the layer neurons
 */
__kernel void updateSynapses(
	__global const float *layerError,
	__global const float *layerActivation,
	__global float *layerSynapses,
	uint prevNumberOfNeurons,
	uint numberOfNeurons,
	__global const float *learningRates
) {
	uint neuronId = get_global_id(0);
	uint networkId = get_global_id(1);
	
	uint synapseOffset = offsetSynapses(networkId,neuronId,prevNumberOfNeurons,numberOfNeurons);
	uint neuronOffset = offsetNeurons(networkId,neuronId,numberOfNeurons);
	
	for( uint i = 0; i<prevNumberOfNeurons; i++ ) {
		layerSynapses[synapseOffset+i] = layerSynapses[synapseOffset+i] + layerActivation[neuronOffset] * layerError[neuronOffset] * learningRates[networkId];
	}
}

__kernel void updateThresholds(
	__global const float *layerError,
	__global const float *layerActivation,
	__global float *layerThresholds,
	uint numberOfNeurons,
	__global const float *learningRates
) {
	uint neuronId = get_global_id(0);
	uint networkId = get_global_id(1);
	uint neuronOffset = offsetNeurons(networkId,neuronId,numberOfNeurons);
	layerThresholds[neuronOffset] = layerThresholds[neuronOffset] - layerActivation[neuronOffset] * layerError[neuronOffset] * learningRates[networkId];
}
