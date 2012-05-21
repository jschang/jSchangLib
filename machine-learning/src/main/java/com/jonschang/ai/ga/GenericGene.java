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

package com.jonschang.ai.ga;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class GenericGene implements Gene {

	protected Float expressiveness = 1.0f;
	protected String name = null;
	
	@Override public Float getExpressiveness() {
		return expressiveness;
	}

	@Override public String getName() {
		return name;
	}

	@Override public void setExpressiveness(Float expressiveness) {
		this.expressiveness = expressiveness;
	}

	@Override public void setName(String name) {
		this.name = name;
	}

	@Override
	public Element getXml() {
		DocumentFactory f = DocumentFactory.getInstance(); 
		Element toRet = f.createElement("gene");
		toRet.addAttribute("name", this.getName());
		toRet.addAttribute("expressiveness", this.getExpressiveness().toString() );
		return toRet;
	}

	@Override
	public void setXml(Element xml) {
		this.setExpressiveness(Float.valueOf(xml.attributeValue("expressiveness")));
		this.setName(xml.attributeValue("name"));
	}

	@Override public int compareTo(Gene gene) {
		return gene.getName().compareTo(name);
	}
	
	public boolean equals(Object o) {
		if( ! (o instanceof Gene) )
			return false;
		Gene gene = (Gene)o;
		return gene.getName().compareTo(name)==0;
	}

}
