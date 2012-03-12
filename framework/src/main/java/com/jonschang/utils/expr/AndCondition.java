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

import java.util.List;

public class AndCondition implements ICondition 
{
	public void setExpressions(List<ICondition> conditions)
		{ m_andList = conditions; }
	private List<ICondition> m_andList = null;
	
	public Boolean evaluate(Object context) 
	{
		if( m_andList == null )
			return true;	
		for( ICondition cond : m_andList )
			if( ! cond.evaluate(context) )
				return false;
		return true;
	}

}
