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
import java.awt.image.WritableRaster;

public class ContrastConverter implements BufferedImageConverter {

	private int gridSize = 5;
	private int threshold = 180; 
	
	public void setGridSize(int size) {
		this.gridSize = size;
	}
	
	public BufferedImage convert(BufferedImage img) {
		int halfGrid = gridSize/2;
		BufferedImage bf = ImageUtils.createScreenCompatibleBufferedImage(img.getWidth(null),img.getHeight(null),false);
		
		int iStart, iEnd, jStart, jEnd, i, j, x, y;
		int lumin=0, luminStart=0, luminEnd=5000, luminAvg=0;

		WritableRaster r = img.getRaster();
		x = r.getMinX();
		y = r.getMinY();
		int width = r.getWidth();
		int height = r.getHeight();
		int[] pels=new int[width*height+1];
		
		for( y=0; y<height; y++ )
		{
			pels = r.getPixels( 0, y, width-1, 1, pels );
			bf.setRGB( 0, y, width-1, 1, pels, 0, width );
			/*
			iStart = x-halfGrid;
			iStart = iStart>=0?iStart:0;
			
			iEnd = x+halfGrid;
			iEnd = iEnd<width?iEnd:width;
			
			jStart = y-halfGrid;				
			jStart = jStart>=0?jStart:0;

			jEnd = y+halfGrid;
			jEnd = jEnd<height?jEnd:height;
			
			luminAvg=0;
			
			luminStart = ImageUtils.getLuminousity( new Color(img.getRGB(iStart,y)) );
			luminEnd = ImageUtils.getLuminousity( new Color(img.getRGB(iEnd-1,y)) );
			lumin = Math.abs(luminEnd-luminStart)>threshold ? 255 : 0;
			
			luminStart = ImageUtils.getLuminousity( new Color(img.getRGB(x,jStart)) );
			luminEnd = ImageUtils.getLuminousity( new Color(img.getRGB(x,jEnd-1)) );
			lumin = lumin<180
				? (Math.abs(luminEnd-luminStart)>threshold ? 255 : 0)
				: lumin;
				
			for( i=iStart; i<iEnd; i++ )
				luminAvg += ImageUtils.getLuminousity( new Color(img.getRGB(x,i)) );
			luminAvg = luminAvg/((iEnd-iStart)*(jEnd-jStart));
			*/
			//lumin = luminHigh-luminLow>threshold?255:0;
		}
		
		return bf;
	}

}
