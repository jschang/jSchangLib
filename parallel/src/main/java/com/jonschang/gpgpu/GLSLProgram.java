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

import java.io.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;

public class GLSLProgram implements Program {

	private GLSLEventListener listener = new GLSLEventListener();
	private short pixelWidth = 4;

	public void run() {
		GLPbuffer pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(new GLCapabilities(), null, listener.width , listener.height, null);
		pbuffer.addGLEventListener(listener);
		pbuffer.display();
		pbuffer.getContext().destroy();
	}
	
	protected void onCreateProgram() {}
	protected void onLoadData() {}
	protected void onRun() {}
	
	protected void execute() {
		listener.render();
	}
	
	protected void addSource(InputStream source) {
		listener.programSource.add(source);
	}

	protected void addSource(List<InputStream> sources) {
		for( InputStream source : sources )
			listener.programSource.add(source);
	}
	
	protected void addData(String name, float[] data) {
		listener.addData(name,data);
	}
	
	protected void getData(String name, float[] data) {
		listener.getData(name,data);
	}
	
	protected void attachData(String name) {
		listener.attachData(name);
	}
	
	protected void detachData(String name) {
		listener.detachData(name);
	}
	
	protected void setTargetData(String name) {
		listener.setTargetData(name);
	}

	protected void destroy() {
	}
	
	protected void createCompileAndLoad() {
		listener.createCompileAndLoad();
	}

	protected void addParam(String name) {
		listener.params.put(name,CgGL.cgGetNamedParameter(listener.fragmentProgram, name));
	}
	
	protected void removeParam(String name) {
		CgGL.cgGLDisableTextureParameter(CgGL.cgGetNamedParameter(listener.fragmentProgram, name));
		listener.params.remove(name);
	}
	
	protected void setDataParam(String name, String dataName) {
		listener.setDataParam(name,dataName);
	}
	
	protected void setParam(String name, int value) {
	}

	protected void setParam(String name, float value) {
	}

	protected void setParam(String name, float[] values) {
	}

	protected void setParam(String name, int[] values) {
	}

	protected void setParams(Map<String, Object> values) {
	}

	protected void unload() {
	}

	private class GLSLEventListener implements GLEventListener
	{
		public CGcontext context;
		public CGprogram fragmentProgram;
		
		public List<InputStream> programSource = new ArrayList<InputStream>();
		public Map<String,CGparameter> params = new HashMap<String,CGparameter>();
		
		public Map<String,Integer> textures = new HashMap<String,Integer>();
		public Map<String,Integer> textureHeights = new HashMap<String,Integer>();
		
		public String targetTexture = null;

		public GLU glu = new GLU();
		public GL gl = null;
		
		public Map<Integer,String> textureAttachPoints = new HashMap<Integer,String>();		
		public int attachmentPoints[] = { 
				GL.GL_COLOR_ATTACHMENT0_EXT, 
				GL.GL_COLOR_ATTACHMENT1_EXT,
				GL.GL_COLOR_ATTACHMENT2_EXT,
				GL.GL_COLOR_ATTACHMENT3_EXT,
				GL.GL_COLOR_ATTACHMENT4_EXT,
				GL.GL_COLOR_ATTACHMENT5_EXT,
				GL.GL_COLOR_ATTACHMENT6_EXT,
				GL.GL_COLOR_ATTACHMENT7_EXT,
				GL.GL_COLOR_ATTACHMENT8_EXT,
				GL.GL_COLOR_ATTACHMENT9_EXT,
				GL.GL_COLOR_ATTACHMENT10_EXT,
				GL.GL_COLOR_ATTACHMENT11_EXT,
				GL.GL_COLOR_ATTACHMENT12_EXT,
				GL.GL_COLOR_ATTACHMENT13_EXT,
				GL.GL_COLOR_ATTACHMENT14_EXT,
				GL.GL_COLOR_ATTACHMENT15_EXT
			};

		public GLSLEventListener() {
			for( int val : attachmentPoints )
				textureAttachPoints.put(val,null);
		}
		
		public int fragmentProfile;
		
		public int width = 2048;
		public int height = 2048;
		
		public void checkCgError() {
			int err = CgGL.cgGetError();

			if (err != CgGL.CG_NO_ERROR) {
				throw new RuntimeException("CG error: " + CgGL.cgGetErrorString(err));
			}
		}
		
		public void addData(String name, float[] data) {
			
			// determine how many pels (float RGBA) the data will consume
			int dataLen = (int)Math.ceil((double)data.length/(double)pixelWidth);
			
			// determine how many pels high the data is (at least 1)
			int height = (int)Math.ceil((double)dataLen/(double)width);
			
			// create a new texture at least 1 full row,
			// copy the data into that texture
			float[] textureAlignedData = new float[height*pixelWidth*width];
			Arrays.fill(textureAlignedData,0);
			System.arraycopy(data, 0, textureAlignedData, 0, data.length);
			
			int[] texID = {0};
			
			gl.glGenTextures(1, texID, 0);
			gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_ARB, texID[0]);
			
			gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
			
			gl.glTexImage2D(
				GL.GL_TEXTURE_RECTANGLE_ARB, 
				0, GL.GL_RGBA32F_ARB, 
				width, height, 
				0, GL.GL_RGBA, 
				GL.GL_FLOAT, FloatBuffer.wrap(textureAlignedData)
			);
			
			textureHeights.put(name,height);
			textures.put(name,texID[0]);
		}
		
		public Integer setTextureAttachPoint(String name) {
			
			if( textures.get(name) == null )
				throw new RuntimeException("The texture with name \""+name+"\" was not found.");
			
			// attach the texture associated to the name to the first
			// free attachment point
			for( Map.Entry<Integer, String> ent : textureAttachPoints.entrySet() ) {
				if( ent.getValue()==null ) {
					textureAttachPoints.put(ent.getKey(), name);
					return ent.getKey();
				}
			}
			return null;
		}
		
		public Integer getTextureAttachPoint(String name) {
			
			if( textures.get(name) == null )
				throw new RuntimeException("The texture with name \""+name+"\" was not found.");
			
			// get the attachment point associated to a particular texture name
			for( Map.Entry<Integer,String> ent : textureAttachPoints.entrySet() ) {
				if( ent.getValue().compareTo(name)== 0 )
					return ent.getKey();
			}
			return null;
		}
		
		public void getData(String name, float[] data) {
			
			Boolean removeAfter = false;
			
			if( textureAttachPoints.get(name)==null ) {
				attachData(name);
				removeAfter=true;
			}
			
			Integer attachmentPoint = getTextureAttachPoint(name);
			if( attachmentPoint == null )
				throw new RuntimeException("\""+name+"\" is not currently assigned an attachment point.  call attachData().");
			
			int height = textureHeights.get(name);
			float[] textureAlignedData = new float[height*width*pixelWidth];
			
			gl.glReadBuffer(attachmentPoint);   
			gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_FLOAT, FloatBuffer.wrap(textureAlignedData));
			
			System.arraycopy(textureAlignedData, 0, data, 0, data.length);
			
			if( removeAfter )
				detachData(name);
		}
		
		/**
		 * Determines the first free attachment point
		 * and attaches to texture associated with the name passed in to it.
		 * 
		 * Throws a runtime exception if no empty attachment points are found.
		 * 
		 * @param name
		 */
		public void attachData(String name) {
			
			Integer attachmentPoint = setTextureAttachPoint(name);
			if( attachmentPoint == null )
				throw new RuntimeException("Could not find an empty attachment point for \""+name+"\"");
			
			gl.glFramebufferTexture2DEXT(
				GL.GL_FRAMEBUFFER_EXT, 
				attachmentPoint, 
				GL.GL_TEXTURE_RECTANGLE_ARB, 
				textures.get(name), 
				0
			);				
		}
		
		public void detachData(String name) {
			Integer point = getTextureAttachPoint(name);
			if( point == null )
				return;
			textureAttachPoints.put(point,null);
			gl.glFramebufferTexture2DEXT(
				GL.GL_FRAMEBUFFER_EXT, 
				point, 
				GL.GL_TEXTURE_RECTANGLE_ARB, 
				0, 
				0
			);
		}
		
		/**
		 * Sets the texture that we intend to write to using the currently loaded program.
		 * @param name
		 */
		public void setTargetData(String name) {
			targetTexture=name;
			Integer attachmentPoint = getTextureAttachPoint(name);
			if( attachmentPoint == null ) {			
				attachmentPoint = setTextureAttachPoint(name);
				if( attachmentPoint == null ) {
					throw new RuntimeException("\""+name+"\" is could not be attached...call attachData()");
				}
			}
			gl.glDrawBuffer(attachmentPoint);
		}
		
		/**
		 * Associates a loaded texture to a named texture parameter
		 * @param name
		 * @param dataName
		 */
		public void setDataParam(String name, String dataName) {
			
			if( textures.get(dataName)==null )
				throw new RuntimeException("\""+dataName+"\" not found.  You must call addData() before you can setDataParam()");
			
			if( params.get(name)==null )
				throw new RuntimeException("\""+name+"\" not found.  You must call addDataParam() before you can setDataParam()");
			
			if( targetTexture==null )
				throw new RuntimeException("The target texture is null.  You must call setTargetData() before calling setDataParam()");
			
			CgGL.cgGLSetTextureParameter(
				CgGL.cgGetNamedParameter(fragmentProgram, name), 
				textures.get(dataName)
			);
			
			CgGL.cgGLEnableTextureParameter( params.get(name) );
		}
		
		public void createCompileAndLoad() {
			try {
				// here, if i recall correctly,
				// i was trying to concatenate all source
				// for a shader together into a buffered stream...
				// then create the program from that stream
				CgGL.cgGLEnableProfile(fragmentProfile);
				InputStream is = null;
				for( InputStream stream : programSource ) {
					if( is==null )
						is = stream;
					else is = new SequenceInputStream(is,stream);
				}
				fragmentProgram = CgGL.cgCreateProgramFromStream(
					context, CgGL.CG_SOURCE, new BufferedInputStream(is), fragmentProfile, null, null);
				if (!CgGL.cgIsProgramCompiled(fragmentProgram)) {
					CgGL.cgCompileProgram(fragmentProgram);
				}
				CgGL.cgGLLoadProgram(fragmentProgram);
			} catch (IOException e) {
				throw new RuntimeException("Error loading Cg fragment program", e);
			}
		}
		
		private void chooseProfile() {
			if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP40)) {
				fragmentProfile = CgGL.CG_PROFILE_FP40;
			}
			else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP30)) {
				fragmentProfile = CgGL.CG_PROFILE_FP30;
			}
			else {
				if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_ARBFP1)) {
					fragmentProfile = CgGL.CG_PROFILE_ARBFP1;
				}
				else {
					System.out.println("Neither arbfp1 or fp30 fragment profiles supported on this system.\n");
					System.exit(1);
				}
			}
		}
		
		@Override
		public void init(GLAutoDrawable drawable) {
			
			// map 1 texel to 1 pixel to 4 floats
			gl = drawable.getGL();
			if(gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT) == 0) {
				throw new RuntimeException("GL_FRAMEBUFFER_EXT not supported!");
			}
			
			gl = drawable.getGL();
			gl.glMatrixMode( GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluOrtho2D( 0.0, width, 0.0, height);
			gl.glMatrixMode( GL.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glViewport( 0, 0, width, height);

			checkCgError();
			
			context = CgGL.cgCreateContext();
			
			chooseProfile();
			checkCgError();

			onCreateProgram();
			checkCgError();
			
			createCompileAndLoad();
			checkCgError();
		
			onLoadData();
			checkCgError();
			
			// generate and bind framebuffer
			int[] fb = new int[1];
			gl.glGenFramebuffersEXT(1, fb, 0);
			gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fb[0]);
			checkCgError();
			
			onRun();
			
			// destroy texture and framebuffer
			CgGL.cgDestroyProgram(fragmentProgram);
			CgGL.cgDestroyContext(context);
			
			for( Integer id : textures.values() ) {
				int[] vals = new int[1];
				vals[0] = id;
				gl.glDeleteTextures(1, vals, 0);
			}
			gl.glDeleteFramebuffersEXT(1, fb, 0);
		}
		
		public void render() {
			int targetHeight = textureHeights.get(targetTexture);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f);          gl.glVertex2f(0.0f, 0.0f);
			gl.glTexCoord2f(width, 0.0f);         gl.glVertex2f(width, 0.0f);
			gl.glTexCoord2f(width, targetHeight); gl.glVertex2f(width, targetHeight);
			gl.glTexCoord2f(0.0f, targetHeight);  gl.glVertex2f(0.0f, targetHeight);
			gl.glEnd();
		}
		
		@Override public void display(GLAutoDrawable arg0) {}
		@Override public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {}
		@Override public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}
	}
}
