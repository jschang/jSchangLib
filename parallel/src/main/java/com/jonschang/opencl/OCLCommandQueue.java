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

package com.jonschang.opencl;

import static org.jocl.CL.*;

import org.jocl.*;

public class OCLCommandQueue {
	
	private OCLContext context = null;
	private cl_command_queue clCommandQueue = null;
	
	private OCLCommandQueue(OCLContext context, cl_command_queue queue) {
		this.context = context;
		this.clCommandQueue = queue;
	}
	
	public cl_command_queue getCLCommandQueue() {
		return clCommandQueue;
	}
	
	public void release() {
		clReleaseCommandQueue(clCommandQueue);
	}
	
	public static OCLCommandQueue create(OCLContext context) {
		
		long[] numBytes = new long[2];
		cl_context clContext = context.getCLContext();
		
        // Get the list of GPU devices associated with the context
        clGetContextInfo(clContext, CL_CONTEXT_DEVICES, 0, null, numBytes); 
        
        // Obtain the cl_device_id for the first device
        int numDevices = (int) numBytes[0] / Sizeof.cl_device_id;
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetContextInfo(clContext, CL_CONTEXT_DEVICES, numBytes[0], Pointer.to(devices), null);

        // Create a command-queue
        return new OCLCommandQueue(context, clCreateCommandQueue(clContext, devices[0], 0, null));
	}
}
