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

import org.apache.log4j.Logger;
import org.jocl.*;

public class OCLContext {
	
	private cl_context context = null;
	
	private OCLContext(cl_context context) {
		this.context = context;
	}
	
	public cl_context getCLContext() {
		return context;
	}
	
	/**
	 * Params are like:
	 *  1) CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR
	 *  2) CL_MEM_WRITE_ONLY
	 *  3) CL_MEM_READ_WRITE
	 * @param buffer
	 * @param params
	 * @return
	 */
	public cl_mem createBuffer(float[] buffer, long params) {
		return clCreateBuffer(context, params, Sizeof.cl_float * buffer.length, Pointer.to(buffer), null);
	}
	
	public cl_mem createBuffer(double[] buffer, long params) {
		return clCreateBuffer(context, params, Sizeof.cl_double * buffer.length, Pointer.to(buffer), null);
	}
	
	public void releaseBuffer(cl_mem memObject) {
		clReleaseMemObject(memObject);
	}
	
	public cl_program createProgram(String[] sources) {
		
        // Create the program from the source code
		int[] errRet = new int[sources.length];
        cl_program program = clCreateProgramWithSource(context, sources.length, sources, null, errRet);
        
        // Build the program
        clBuildProgram(program, 0, null, "-cl-mad-enable", null, null);
        //clBuildProgram(program, 0, null, null, null, null);
        
        return program;
	}
	
	public void releaseProgram(cl_program program) {
		clReleaseProgram(program);
	}
	
	protected void release() {
        clReleaseContext(context);
	}
	public cl_kernel createKernel(cl_program program, String name) {
		return clCreateKernel(program, name, null); 
	}
	public int releaseKernel(cl_kernel kernel) {
		return clReleaseKernel(kernel);
	}
	public static OCLContext create() {
		
		cl_context clContext;
		
        // Obtain the platform IDs and initialize the context properties
		Logger.getLogger(OCLContext.class).info("Obtaining OpenCL platform...");
        cl_platform_id platforms[] = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platforms[0]);
        
        // Create an OpenCL context on a GPU device
        clContext = clCreateContextFromType(contextProperties, CL_DEVICE_TYPE_GPU, null, null, null);
        if (clContext == null)
        {
        	Logger.getLogger(OCLContext.class).warn("Unable to create a GPU context.  Falling back on the CPU.");
        	
            // If no context for a GPU device could be created,
            // try to create one for a CPU device.
            clContext = clCreateContextFromType(contextProperties, CL_DEVICE_TYPE_CPU, null, null, null);
            
            if (clContext == null)
            {
                Logger.getLogger(OCLContext.class).error("Unable to create a context.");
                return null;
            } else Logger.getLogger(OCLContext.class).info("CPU OpenCL context obtained");
        } else Logger.getLogger(OCLContext.class).info("GPU OpenCL context obtained");

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);
        
        return new OCLContext(clContext);
	}

}
