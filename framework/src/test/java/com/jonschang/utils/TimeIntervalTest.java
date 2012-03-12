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

import java.util.*;
import java.text.*;
import org.junit.*;

public class TimeIntervalTest {
	@Test public void testTimeIntervalAdd() {
		try {
			Date date = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("01/08/2009 16:00");
			TimeInterval.DAY.add(date);
			Assert.assertTrue(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("02/08/2009 16:00").compareTo(date)==0);
			TimeInterval.DAY.add(date,2);
			Assert.assertTrue(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("04/08/2009 16:00").compareTo(date)==0);
			TimeInterval.DAY.add(date,-3);
			Assert.assertTrue(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse("01/08/2009 16:00").compareTo(date)==0);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
}
