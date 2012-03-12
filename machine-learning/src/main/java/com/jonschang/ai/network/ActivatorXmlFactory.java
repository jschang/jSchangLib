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

package com.jonschang.ai.network;

import com.jonschang.utils.*;

public class ActivatorXmlFactory implements XmlFactory {

	public XmlMarshaller<?> getMarshaller(Object obj) throws XmlException {
		if( obj.getClass()==SigmoidActivator.class )
			return new SigmoidActivatorXmlMarshaller();
		throw new XmlException("Coult not find the XmlMarshaller for "+obj.getClass());
	}

	public XmlUnmarshaller<?> getUnmarshaller(Object obj) throws XmlException {
		if( obj.getClass()==SigmoidActivator.class )
			return new SigmoidActivatorXmlUnmarshaller();
		throw new XmlException("Count not find the XmlUnmarshaller for "+obj.getClass());
	}

}
