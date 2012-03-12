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

import org.junit.Test;

public class Testing {
	@Test public void test()
	{
		Tester test = new Tester();
		test.getBase();
		test.getMid();
		test.getTop();
		test.setTop((ITestBase) test);
	}
	public interface ITestBase {
		public ITestBase getBase();
	}
	public interface ITestMid extends ITestBase {
		public ITestMid getMid();
	}
	public interface ITestTop extends ITestMid {
		public ITestTop getTop();
	}
	public class Tester implements ITestTop
	{
		public ITestBase getBase()
			{ return (ITestBase)this; }
		public ITestMid getMid()
			{ return (ITestMid)this; }
		public ITestTop getTop()
			{ return (ITestTop)this; }
		public void setTop(ITestBase top)
			{ } 
	}
}
