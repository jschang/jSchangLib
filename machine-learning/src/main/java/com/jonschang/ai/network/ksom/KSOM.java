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

package com.jonschang.ai.network.ksom;

import com.jonschang.ai.network.AbstractNetwork;
import com.jonschang.math.vector.MathVector;
import com.jonschang.math.vector.VectorImpl;
import java.util.List;
import java.util.ArrayList;

/**
 * Kohonen Self-Organizing Map
 * 
 * A 2-dimensional Kohonen SOM
 * limited to 2 dimensions
 * 
 * @author schang
 */
public class KSOM extends AbstractNetwork {

	protected int width;
	protected int height;
	protected int numberOfVectorComponents;
	protected List<List<MathVector>> map;
	
	/**
	 * Setup a Kohonen SOM of a certain number of dimensions 
	 * @param components The number of components in the input vectors
	 * @param width The width of the output map
	 * @param height The height of the output map
	 */
	public void setup(int components, int width, int height) {
		
		this.numberOfVectorComponents=components;
		this.width=width;
		this.height=height;
		
		// create map
		map = new ArrayList<List<MathVector>>();
		for(int i=0; i<height; i++) {
			map.add( new ArrayList<MathVector>() );
			for( int j=0; j<width; j++) {
				map.get(i).add(new VectorImpl(components).randomize(0.0,0.4));
			}
		}
	}
	
	/**
	 * Determine which coordinate on the KSOM has weights closest to the input vector
	 * 
	 * @param input A vector with the number of components this KSOM was setup with
	 * @return A 2d Vector where the components are x,y on the KSOM
	 */
	@Override
	public MathVector calculateResponse(MathVector input)
	{
		if( map==null )
			return null;
		
		int lastI=0, lastJ=0;
		
		double closestLast=-1, dist;
		for(int i=0; i<height; i++) {
			map.add( new ArrayList<MathVector>() );
			for( int j=0; j<width; j++) {
				dist = map.get(i).get(j).getSquaredDistance(input);
				if( closestLast==-1 )
					closestLast=dist;
				else if( closestLast>=dist ) {
					lastI=i; lastJ=j; closestLast=dist;
				}
			}
		}
		
		super.calculateResponse(input);
		
		MathVector response = new VectorImpl(2);
		response.getData().set(0, (double)lastJ);
		response.getData().set(1, (double)lastI);
		
		return response;
	}	
}
