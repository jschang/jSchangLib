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

public class GenericsTest {
	@Test public void test()
	{
	}
	
	public interface AInter<B extends BInter> {
		public void testA(B test);
	}
	
	public interface BInter<A extends AInter> {
		public void testB(A test);
	}
	
	public class AImpl<B extends BInter> implements AInter<B> {
		public void testA(B test) {
			test.testB(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public class BImpl<A extends AInter> implements BInter<A> {
		
		public void testB(A test) {
			test.testA(this);
		}
	}
}
