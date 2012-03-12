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

package com.jonschang.imaging;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import java.nio.ByteBuffer;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.FrameGrabber;

public class V4L4JImageSource implements ImageSource {

	private String device = null;
	private Rectangle bounds = null;
	private VideoDevice videoDevice = null;
	private FrameGrabber frameGrabber = null;
	private int captureStandard = V4L4JConstants.STANDARD_WEBCAM;
	private int quality = 60;
	
	public void initialize() throws Exception {
		this.videoDevice = new VideoDevice(this.device);
		this.frameGrabber = this.videoDevice.getJPEGFrameGrabber(bounds.width, bounds.height, 0, this.captureStandard, this.quality);
		this.frameGrabber.startCapture();
	}
	
	public void dispose() {
		this.frameGrabber.stopCapture();
        this.videoDevice.releaseFrameGrabber();
 
	}
	
	public void setDimension(Rectangle bounds) {
		this.bounds = (Rectangle)bounds.clone();
	}
	
	public void setJPEGQuality(int quality) {
		this.quality = quality;
	}
	
	public void setCaptureStandard(int standard) {
		this.captureStandard = standard;
	}
	
	public void setDevice(String device) {
		this.device = device;
	}

	public BufferedImage getImage() throws Exception {
		if( this.frameGrabber!=null )
		{
			ByteBuffer bytes = this.frameGrabber.getFrame();
			byte[] b = new byte[bytes.limit()];
        	bytes.get(b);
        	ImageIcon imageIcon = new ImageIcon(b);
        	return ImageUtils.toBufferedImage(imageIcon.getImage());
		} else return null;
	}

}
