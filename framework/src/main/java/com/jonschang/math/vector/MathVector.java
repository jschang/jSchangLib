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

package com.jonschang.math.vector;
import java.util.ArrayList;

public interface MathVector {
	/**
	 * @param index the component index within the vector
	 * @return the value at the component index
	 */
	public Double valueOf(int index);
	
	public void setValue(int index, Double value);
	
	public void setValue(Double... value);
	
	/**
	 * @return the component values of the vector
	 */
	public ArrayList<Double> getData();
	
	/**
	 * @param data the component values
	 */
	public void setData(ArrayList<Double> data);
	
	/**
	 * @return the distance from this vector to another
	 */
	public Double getSquaredDistance(MathVector destination);
	
	/**
	 * @param otherVector
	 * @return true if components are the same
	 */
	public boolean equals(MathVector otherVector);
	
	/**
	 * subtract input from this and return the result
	 * @param input the second value of the subtraction operation
	 * @return a vector of difference
	 */
	public MathVector minus(MathVector secondValue);
	
	public MathVector squared();
	
	public MathVector pow(Double power);
	
	public Double magnitude();
	
	public Double sum();
	
	public Integer size();
}
