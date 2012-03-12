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

package com.jonschang.utils.valuesource;

import java.util.*;
import com.jonschang.math.vector.*;
import com.jonschang.math.vector.MathVector;

/**
 * extends ArrayList<ValueSource> to ensure the list is a unique set of ValueSource's
 * @author schang
 */
public class ValueSourceList<V extends ValueSource> 
	extends ArrayList<V> 
	implements IValueSourceList<V> { 
	
	protected void onChange() {
	}
	
	@Override
	public void clear() {
		this.onChange();
		super.clear();
	}
	
	@Override
	public boolean add(V valueSource) {
		if( !this.contains(valueSource) ) {
			super.add(valueSource);
			this.onChange();
		} else return false;
		return true;
	}
	
	public void remove(V valueSource) {
		if( this.contains(valueSource) ) {
			super.remove(valueSource);
			this.onChange();
		}
	}

	public MathVector getVector() throws Exception {
		com.jonschang.math.vector.VectorImpl res = new com.jonschang.math.vector.VectorImpl();
		for( V valueSource : this )
			res.getData().add( valueSource.getValue() );
		return res;
	}
}
