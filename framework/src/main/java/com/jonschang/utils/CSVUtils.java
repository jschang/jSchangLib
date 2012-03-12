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

package com.jonschang.utils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class CSVUtils 
{
	/**
	 * takes a String filled with CSV content that has a heading row
	 * and returns a Map of each column where the map key is the heading of the column
	 * @param itemSeparator
	 * @param csvData
	 * @return
	 */
	static public Map<String,List<String>> digestCSVWithHeadings(String itemSeparator, String csvData)
	{
		Map<String,List<String>> toRet = new HashMap<String,List<String>>();
		
		// split up the incoming csvData lines into an array of strings
		String[] csvLines = csvData.split("\n");
		if( csvLines==null || csvLines.length==0 )
			return null;
		List<String> arrCSVLines = new ArrayList<String>( java.util.Arrays.asList(csvLines) );
		
		// extract the header row and create the map keys
		String[] headerRow = arrCSVLines.get(0).split(itemSeparator);
		if( headerRow.length==0 )
			return null;
		else for( String heading: headerRow )
			toRet.put(heading, new ArrayList<String>() );
		
		// iterate over the remainder of the lines	
		for( String line : arrCSVLines.subList(1,arrCSVLines.size()) )
		{
			String[] columnEntries = line.split(itemSeparator);
			// iterate over the header entries
			// and add each column entry into the appropriate map array list
			for( int i=0; i<headerRow.length; i++ )
				toRet.get( headerRow[i] ).add( columnEntries[i] );
		}
		
		// return the map of arrays, keyed off the heading name
		return toRet;
	}
}
