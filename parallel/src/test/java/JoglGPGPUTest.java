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

/*
 * @author amehio
 */
import java.io.IOException;
import java.nio.FloatBuffer;

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

public class JoglGPGPUTest {
	@org.junit.Test public void nullMethod() {
		// TODO: make this a runnable test
	}
	public static void main(String[] args) {
		int width = 100, height = 100;
		GLPbuffer pbuffer = GLDrawableFactory.getFactory().createGLPbuffer(new GLCapabilities(), null, width , height, null);
		JoglEventListener listener = new JoglEventListener( width, height);
		pbuffer.addGLEventListener(listener);
		pbuffer.display();
		pbuffer.getContext().destroy();
	}
}
class JoglEventListener implements GLEventListener {
	private CGcontext context;
	private CGprogram fragmentProgram;

	private GLU glu = new GLU();
	private int attachmentpoints[] = { GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT};

	private int fragmentProfile;
	private int[] texID = new int[2];
	private int width;
	private int height;

	public JoglEventListener(int width, int height) {
		this.width = width;
		this.height = height;
	}
	public void init(GLAutoDrawable drawable) {
		int read = 0, write = 1;
		int numIterations = 1000;

		float data[] = new float[width*height*4];
		float result[] = new float[width*height*4];

		for(int i = 0; i < data.length; i++) {
			data[i] = i + 1.0f;
		}

		// map 1 texel to 1 pixel to 4 floats
		GL gl = drawable.getGL();
		if(gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT) == 0) {
			return;
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
		loadCgPixelShader();
		loadTextures(gl, data);

		// generate and bind framebuffer
		int[] fb = new int[1];
		gl.glGenFramebuffersEXT(1, fb, 0);
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fb[0]);
		// attach textures to framebuffer
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, attachmentpoints[read], GL.GL_TEXTURE_RECTANGLE_ARB, texID[read], 0);
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, attachmentpoints[write], GL.GL_TEXTURE_RECTANGLE_ARB, texID[write], 0);

		CGparameter param = CgGL.cgGetNamedParameter(fragmentProgram, "texture");
		CgGL.cgGLBindProgram(fragmentProgram);

		for(int i = 0; i < numIterations; i++) {
			gl.glDrawBuffer(attachmentpoints[write]);
			CgGL.cgGLSetTextureParameter(CgGL.cgGetNamedParameter(fragmentProgram, "texture"), texID[read]);
			CgGL.cgGLEnableTextureParameter(param);
			renderQuad(gl);
			int temp = write;
			write = read;
			read = temp;
		}

		CgGL.cgGLDisableTextureParameter(CgGL.cgGetNamedParameter(fragmentProgram, "texture"));

		if(numIterations % 2 == 0) {
			gl.glReadBuffer(attachmentpoints[write]);
		}
		else {
			gl.glReadBuffer(attachmentpoints[read]);   
		}
		gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_FLOAT, FloatBuffer.wrap(result));

		System.out.println("Data after roundtrip");
		for(int i = 0; i < result.length; i++) {
			System.out.println(result[i]);
		}

		// destroy texture and framebuffer
		CgGL.cgDestroyProgram(fragmentProgram);
		CgGL.cgDestroyContext(context);
		gl.glDeleteTextures(2, texID, 0);
		gl.glDeleteFramebuffersEXT(1, fb, 0);
	}

	public void display(GLAutoDrawable arg0) {}
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {}
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}

	private void checkCgError() {
		int err = CgGL.cgGetError();

		if (err != CgGL.CG_NO_ERROR) {
			throw new RuntimeException("CG error: " + CgGL.cgGetErrorString(err));
		}
	}
	public void chooseProfile() {
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
	private void loadCgPixelShader() {
		try {
			fragmentProgram = CgGL.cgCreateProgramFromStream(context, CgGL.CG_SOURCE,
					getClass().getResourceAsStream("pixelShader.cg"),
					fragmentProfile, null, null);
		} catch (IOException e) {
			throw new RuntimeException("Error loading Cg fragment program", e);
		}
		if (!CgGL.cgIsProgramCompiled(fragmentProgram)) {
			CgGL.cgCompileProgram(fragmentProgram);
		}

		CgGL.cgGLEnableProfile(fragmentProfile);
		CgGL.cgGLLoadProgram(fragmentProgram);
	}
	private void loadTextures(GL gl, float[] data) {
		gl.glGenTextures(2, texID, 0);
		gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_ARB, texID[0]);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_ARB, 0, GL.GL_RGBA32F_ARB, width, height, 0, GL.GL_RGBA, GL.GL_FLOAT, FloatBuffer.wrap(data));

		gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_ARB, texID[1]);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_ARB, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_ARB, 0, GL.GL_RGBA32F_ARB, width, height, 0, GL.GL_RGBA, GL.GL_FLOAT, FloatBuffer.wrap(data));
	}
	private void renderQuad(GL gl)
	{
		// make quad filled to hit every pixel/texel
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);

		// and render quad
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0.0f, 0.0f);      gl.glVertex2f(0.0f, 0.0f);
		gl.glTexCoord2f(width, 0.0f);      gl.glVertex2f(width, 0.0f);
		gl.glTexCoord2f(width, height);   gl.glVertex2f(width, height);
		gl.glTexCoord2f(0.0f, height);   gl.glVertex2f(0.0f, height);
		gl.glEnd();
	}
} 