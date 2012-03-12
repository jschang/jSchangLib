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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.JPanel;

import com.jonschang.imaging.V4L4JImageSource;
import com.jonschang.imaging.ContrastConverter;

public class V4L4JTest extends WindowAdapter {
    private Component l;
    private Component l2;
    private JFrame f;
    private Thread captureThread;
    private Thread captureThread2;
    private boolean stop;
    private V4L4JImageSource frameSource;
    private V4L4JImageSource frameSource2;
    
    private int width=320;
    private int height=240;
        
    @org.junit.Test public void nullMethod() {
		// TODO: figure out a way to make this a valid test class
	}
    
    /**
     * Builds a WebcamViewer object
     * @param dev the video device file to capture from
     * @param w the desired capture width
     * @param h the desired capture height
     * @param std the capture standard
     * @param channel the capture channel
     * @param qty the JPEG compression quality
     * @throws V4L4JException if any parameter if invalid
     */
    public void init(String dev) throws Exception {
        initGUI();
        
        // left image source
        this.frameSource = new V4L4JImageSource();
        this.frameSource.setDevice("/dev/video0");
        this.frameSource.setDimension(new Rectangle(0,0,width,height));
        this.frameSource.initialize();
        
        // right image source
        this.frameSource2 = new V4L4JImageSource();
        this.frameSource2.setDevice("/dev/video2");
        this.frameSource2.setDimension(new Rectangle(0,0,width,height));
        this.frameSource2.initialize();
        
        stop = false;
        captureThread = new Thread(new CaptureThread(this.frameSource,((JLabel)this.l)), "Capture Thread");
        captureThread.start();
        captureThread2 = new Thread(new CaptureThread(this.frameSource2,((JLabel)this.l2)), "Capture Thread");
        captureThread2.start();
    }
    
    /** 
     * Creates the graphical interface components and initialises them
     */
    private void initGUI() {
        f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.addWindowListener(this);
        
        // our two labels, side by side
        JPanel p = new JPanel();
        java.awt.LayoutManager lf = new BoxLayout(p,BoxLayout.X_AXIS);
        p.setLayout(lf);
        l = new JLabel();
        l2 = new JLabel();
        p.add(l);p.add(l2);
        
        // vertical container for everything
        JPanel p2 = new JPanel();
        lf = new BoxLayout(p2,BoxLayout.Y_AXIS);
        p2.setLayout(lf);
        p2.add(p);f.add(p2);
        
        f.setSize(width*2,height);
        f.setVisible(true);        
    }
    
    
    private class CaptureThread implements Runnable {
    	private V4L4JImageSource fg;
    	private JLabel l;
    	
    	public CaptureThread(V4L4JImageSource fg, JLabel l) {
    		this.fg = fg;
    		this.l = l;
    	}
    	
	    /**
	     * Implements the capture thread: get a frame from the FrameGrabber, and display it
	     */
	    public void run() {
	        try {                   
		        while(!stop) {
	               	ImageIcon icon = new ImageIcon(this.fg.getImage());
	               	((JLabel)this.l).setIcon(icon);
		        }
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("Failed to capture image");
	        }
	    }
	}

    /**
     * Catch window closing event s V4L4JException, IOo we can free up resources before exiting
     * @param e
     */
	public void windowClosing(WindowEvent e) {
        if(captureThread.isAlive()){
            stop = true;
            try {
            	captureThread.join();
            } catch (InterruptedException e1) {}
        }
        if( captureThread2.isAlive() )
        	try {
        		captureThread2.join();
        	} catch (InterruptedException e1) {}
        
        this.frameSource.dispose();
        this.frameSource2.dispose();
        f.dispose();            
    }

    public static void main(String[] args) throws Exception {
        String dev = "/dev/video0";
        new V4L4JTest().init(dev);
    }
}