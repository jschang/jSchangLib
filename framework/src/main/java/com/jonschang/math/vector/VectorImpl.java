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

public class VectorImpl implements MathVector {

	static private String format = "%.3f";
	
	private ArrayList<Double> data = new ArrayList<Double>();
	
	public VectorImpl() {}
	
	public VectorImpl(int size)
	{
		for( int i=0; i<size; i++ )
			this.data.add(0.0);
	}
	
	/*public VectorImpl(double... data)
	{
		for(int i=0;i<data.length;i++)
			this.data.add(data[i]);
	}*/
	
	public VectorImpl(Double... data)
	{
		for(int i=0;i<data.length;i++)
			this.data.add(data[i]);
	}
	
	public VectorImpl slice(int start, int end) {
		return new VectorImpl(this.data.subList(start, end).toArray(new Double[]{}));
	}
	
	public Double valueOf(int index){
		return (Double)this.data.toArray()[index];
	}
	
	public void setValue(int index, Double value) {
		this.data.set(index, value);
	}
	
	public void setValue(Double... value) {
		for( int i=0; i<value.length; i++ ) {
			setValue(i,value[i]);
		}
	}
	
	public MathVector randomize(double lowerBound, double upperBound) {
		if( data!=null  )
			for( int i=0; i<data.size(); i++ )
				data.set(i, lowerBound + ( (upperBound-lowerBound) * Math.random() ) );
		return this;
	}
	
	public ArrayList<Double> getData() {
		return this.data;
	}

	public Double getSquaredDistance(MathVector destination) {
		return null;
	}
	
	public void setData(ArrayList<Double> data) {
		this.data = data;
	}
	
	public boolean equals(MathVector otherVector){
		return false;
	}
	
	public MathVector minus(MathVector secondValue)
	{
		MathVector res = new VectorImpl();
		for( int i = 0; i < this.data.size(); i++ )
		{
			res.getData().add(this.valueOf(i)-secondValue.valueOf(i));
		}
		return res;
	}

	public MathVector squared() {
		return this.pow(2.0);
	}
	
	public MathVector pow(Double power) {
		MathVector res = new VectorImpl();
		for(int i=0;i<this.data.size();i++)
			res.getData().add(Math.pow(this.data.get(i),power));
		return res;
	}

	public Double sum() {
		double sum=0;
		for(int i=0;i<this.data.size();i++)
			sum += this.data.get(i);
		return sum;
	}
	
	public Integer size() {
		return this.data.size();
	}
	
	public String toString() {
		if( data!=null ) {
			if( data.size()==0 )
				return "[empty]";
			StringBuilder sb = new StringBuilder();
			for( int i=0; i<data.size(); i++ ) {
				if( i>0 )
					sb.append(',');
				sb.append(String.format(VectorImpl.format, data.get(i)));
			}
			return "["+sb.toString()+"]";
		} else return "[null]";
	}
	
	public static void setFormat(String format) {
		VectorImpl.format = format;
	}
	
	public Double magnitude() {
		return Math.sqrt(this.squared().sum());
	}
}

