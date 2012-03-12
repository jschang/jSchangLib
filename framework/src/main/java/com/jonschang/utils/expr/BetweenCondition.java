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

package com.jonschang.utils.expr;

import com.jonschang.utils.valuesource.ValueSource;

public class BetweenCondition implements ICondition {
	
	/**
	 * @param valueSource the value to evaluate against
	 */
	public void setValueSource(ValueSource valueSource)
		{ m_value = valueSource; }
	public ValueSource getValueSource()
		{ return m_value; }
	ValueSource m_value = null;
	
	/**
	 * @param upperLimit an IValueSource that returns the upper limit
	 */
	public void setUpperLimit(ValueSource upperLimit)
		{ m_upperLimit = upperLimit; }
	public ValueSource getUpperLimit()
		{ return m_upperLimit; }
	ValueSource m_upperLimit;
	
	/**
	 * @param lowerLimit an IValueSource that returns the lower limit
	 */
	public void setLowerLimit(ValueSource lowerLimit)
		{ m_lowerLimit = lowerLimit; }
	public ValueSource getLowerLimit()
		{ return m_lowerLimit; }
	ValueSource m_lowerLimit;

	public Boolean evaluate(Object context) 
	{
			return false;
//		if( m_value.getValue() > m_upperLimit.getValue() )
//			return true;
//		return null;
	}
}
