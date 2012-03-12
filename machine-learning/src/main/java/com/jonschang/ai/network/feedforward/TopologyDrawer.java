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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jonschang.ai.network.GraphicDrawer;
import com.jonschang.ai.network.Neuron;
import com.jonschang.ai.network.Synapse;

/**
 * Draws the topology of a FeedForward Network at a position and within a given dimension.
 * 
 * The terminology may be incorrect.  This actually draws a representation
 * of the Network, where Synapses between Neurons are lines of a thickness
 * proportional to weight and the Neurons are circles of a radius proportional
 * to threshold.  Each weight and threshold is scaled to the maximum and minimum
 * currently in the network.
 * 
 * Green circles are positive thresholds, while red are negative.
 * 
 * @author schang
 */
public class TopologyDrawer implements GraphicDrawer {

	/**
	 * padding within the drawn rectangular area
	 * if it's less than 1, then interpreted as a percent
	 * if it's greater than 1, then interpreted as a pixel value
	 */
	public double edgePadding = 52;
	
	/**
	 * modulate the radius of neurons displayed
	 * proportional to the maximum threshold
	 * up to the maximum radius allowed
	 */
	public boolean modulateNeuronRadius = true;
	public int defaultNeuronRadius = 10;
	public int maximumNeuronRadius = 25;
	
	public boolean modulateSynapseWidth = true;
	public int defaultSynapseWidth = 1;
	public int maximumSynapseWidth = 25;
	
	public Color colorThresholdPositive = Color.green;
	public Color colorThresholdNegative = Color.red;
	public Color colorWeightPositive = Color.black;
	public Color colorWeightNegative = Color.red;
	
	private double highestThreshold = 0;
	private double lowestThreshold = 0;
	private double highestWeight = 0;
	private double lowestWeight = 0;
	private int left, top, width, height;
	
	private FeedForward network;
	private Map<Neuron,Point> coordinateMap = new HashMap<Neuron,Point>();
	
	public void draw(Graphics g, int x, int y, int width, int height) {
		
		this.top=y;
		this.left=x;
		this.width=width;
		this.height=height;
		
		// we don't want anyone changing the network while we're in the middle of painting it
		if( this.network!=null )
			synchronized (this.network)
			{
				List<List<Neuron>> layers = createNeuronCoordinateMap(width, height);
				g.clearRect(x,y,width,height);
				drawNeuralNetwork(g, layers);
			}
	}
	
	public <T extends FeedForward > void setNetwork(T network) {
		this.network = network;
	}
	
	private List<List<Neuron>> createNeuronCoordinateMap(int width, int height)
	{
		List<Neuron> neurons = this.network.getInputNeurons();
		List<List<Neuron>> layers = new ArrayList<List<Neuron>>();
		layers.add(neurons);
		buildLayers(neurons,layers,0);
		
		// assign each distinct neuron a grid position
		int i=0;
		int numLayers = layers.size();
		for( List<Neuron> layer : layers )
		{
			int j = 0;
			int layerCols = layer.size();
			int x = itemToScreen(width,numLayers,i)+this.left;
			for( Neuron n : layer )
			{
				int y = itemToScreen(height,layerCols,j)+this.top;
				Point p = new Point(x,y);
				this.coordinateMap.put(n,p);	
				j++;
			}
			i++;
		}
		
		return layers;
	}
	
	private void drawNeuralNetwork(Graphics g, List<List<Neuron>> layers)
	{
		// assign each distinct neuron a grid position
		for( List<Neuron> layer : layers )
		{
			for( Neuron n : layer )
			{
				Point p = this.coordinateMap.get(n);
				
				for( Synapse sj : n.getOutputSynapses() )
				{
					Neuron nj = sj.getOutput();
					Point pj = this.coordinateMap.get(nj);
					g.setColor(sj.getWeight()>0?this.colorWeightPositive:this.colorWeightNegative);
					g.drawLine(p.x, p.y, pj.x, pj.y);
				}
				
				int radius = this.defaultNeuronRadius;
				if( this.modulateNeuronRadius )
				{
					if( n.getThreshold()>0 )
						radius = (int)(this.maximumNeuronRadius*Math.abs(n.getThreshold()/this.highestThreshold));
					else radius = (int)(this.maximumNeuronRadius*Math.abs(n.getThreshold()/this.lowestThreshold));
				}
				
				int diameter = 2*radius;
				
				g.setColor(n.getThreshold()>0?this.colorThresholdPositive:this.colorThresholdNegative);
				g.fillArc(p.x-radius, p.y-radius, diameter, diameter, 0, 360);
				
				g.setColor(Color.BLACK);
				g.drawArc(p.x-radius, p.y-radius, diameter, diameter, 0, 360);	
			}
		}
	}
	
	private void buildLayers(List<Neuron> neurons, List<List<Neuron>> layers, int thisLayer)
	{
		List<Neuron> nextLayer;
		if( layers.size()>(thisLayer+1) )
			nextLayer = layers.get(thisLayer+1);
		else nextLayer = new ArrayList<Neuron>();
		
		// iterate over the neurons in this layer
		for( Neuron n : neurons )
		{
			this.highestThreshold = n.getThreshold() > this.highestThreshold ? n.getThreshold() : this.highestThreshold; 
			this.lowestThreshold = n.getThreshold() < this.lowestThreshold ? n.getThreshold() : this.lowestThreshold;
			
			// extract the neurons connected to that we haven't already added to a layer
			for( Synapse s : n.getOutputSynapses() )
			{
				this.highestWeight = s.getWeight() > this.highestWeight ? s.getWeight() : this.highestWeight; 
				this.lowestWeight = s.getWeight() < this.lowestWeight ? s.getWeight() : this.lowestWeight;
				if( !nextLayer.contains(s.getOutput()) && !multiLevelContains(layers,s.getOutput()) )
					nextLayer.add(s.getOutput());
			}
		}
		
		if( nextLayer.size()!=0 )
		{
			if( ! layers.contains(nextLayer) )
				layers.add(nextLayer);
			buildLayers(nextLayer,layers,thisLayer+1);
		} 
	}
	
	private <T extends Object> boolean multiLevelContains(List<List<T>> list,T o)
	{
		for( List<T> l : list )
			if( l.contains(o) )
				return true;
		return false;
	}
	
	/**
	 * map the item to the screen coordinates
	 * @param extent the extent of the dimension (x or y)
	 * @param elements the number of elements in the row or column
	 * @param index the index of the element along the dimension (x or y)
	 * @return the coordinate of the the item along the dimension
	 */
	private int itemToScreen(int extent, int elements, int index)
	{
		int edgePadding = 0;
		if( this.edgePadding<1 )
			edgePadding = (int)( extent*this.edgePadding );
		else edgePadding = (int)this.edgePadding;
		
		return ((extent-2*edgePadding)/(elements>1?elements-1:1))*(index)+edgePadding;
	}
	
}
