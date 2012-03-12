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

package com.jonschang.cluster;

import com.jonschang.cluster.model.CommandRequest;
import org.apache.log4j.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;

public class SpringCommandExecutor extends AbstractCommandExecutor {

	public SpringCommandExecutor(CommandRequest request) {
		super(request);
		Element element = (Element)request.getSpringCommand().getConfiguration().getAny();
		Document document = element.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		String str = serializer.writeToString(element);
		Logger.getLogger(this.getClass()).info(str);
	}

	@Override
	protected void actuallyRunCommand() throws Exception {
		// TODO Auto-generated method stub

	}

}
