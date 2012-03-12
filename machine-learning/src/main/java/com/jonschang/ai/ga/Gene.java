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

import com.jonschang.utils.*;

/**
 * Gene
 * 
 * Comparable must be implemented.  The easiest way
 * would just be to return name.compareTo(gene.getName()),
 * as Gene's of the same name _must_ be identical.
 * The gene's name is effectively an type id of the gene
 * within the genome.
 * 
 * @author schang
 */
public interface Gene extends HasXml, Comparable<Gene> {
	void setExpressiveness(Float expressiveness);
	Float getExpressiveness();
	String getName();
	void setName(String name);
	boolean equals(Object o);
}
