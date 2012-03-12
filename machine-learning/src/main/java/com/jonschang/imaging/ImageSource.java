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

import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public interface ImageSource {
	
	/**
	 * @param bounds the desired dimension of the resulting frame
	 */
	public void setDimension(Rectangle bounds) throws Exception;
	
	/**
	 * mainly to abstract away from either v4l4j or jmf, depending on platform
	 * @return the most recent frame from the underlying source
	 */
	public BufferedImage getImage() throws Exception;
	
	/**
	 * frees up resources associated with the FrameSource
	 */
	public void dispose() throws Exception;
	
	public void initialize() throws Exception;
}
