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

package com.jonschang.gpgpu;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.junit.*;

public class GLSLProgramTest {

	@Test public void test() {
		new TestProgram().run();
	}
	
	public class TestProgram extends GLSLProgram
	{
		public int size = 2048*4;
		
		@Override protected void onCreateProgram() {
			addSource( getClass().getResourceAsStream("pixelShader.cg") );
		}
		
		@Override protected void onLoadData() {
			
			float[] data = new float[size];
			for(int i = 0; i < data.length; i++) {
				data[i] = i + 1.0f;
			}
			
			addData("texture1",data);
			addData("texture2",data);
		}
		
		@Override protected void onRun() {
			
			String[] dataNames = {"texture1","texture2"};
			
			Integer currentData = 1; // start out writing to texture2
			
			attachData("texture1"); // should end up in attachment point 1
			attachData("texture2"); // should end up in attachment point 2
			addParam("texture");    // setup a named parameter to be recognized 
			
			for( int i=0; i<1000; i++ ) {
				
				// set the current data as the target
				setTargetData(dataNames[currentData]);
				
				// use the other texture as the data source
				setDataParam("texture", dataNames[ currentData==1 ? 0 : 1 ]);
				
				// render the quad using the currently compiled program
				execute();
				
				// flip-flop the current target texture,
				// the first time texture1 will replace texture2 as the target
				currentData = currentData == 1 ? 0 : 1;
			}
			
			// get data from the last texture we wrote to
			// if only 1 iteration is performed, then this is texture2
			float[] result = new float[size];
			getData(dataNames[ currentData == 0 ? 1 : 0 ],result);

			System.out.println("Data after roundtrip");
			for(int i = 0; i < result.length; i++) {
				System.out.println( i +"\t:" + result[i] );
			}
		}
	}
}
