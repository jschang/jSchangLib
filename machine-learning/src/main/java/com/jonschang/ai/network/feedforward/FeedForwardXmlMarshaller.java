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

package com.jonschang.ai.network.feedforward;

import java.io.*;
import java.util.*;
import com.jonschang.ai.network.*;
import com.jonschang.utils.*;

public class FeedForwardXmlMarshaller implements XmlMarshaller<FeedForward> {
	public void marshal(FeedForward obj, Writer out) throws XmlException {
		
		if( obj.getAllLayers()==null )
			return;
		try {
			out.append("<feedforward xmlns=\"com.jonschang.ai.network.feedforward\">\n");
			
			Map<Neuron,Integer> neuronToIntMap = new HashMap<Neuron,Integer>();
			Map<Activator,Integer> activatorToIntMap = new HashMap<Activator,Integer>();
			int i=0, j=0;
			boolean activatorsStarted=false;
			for(  List<Neuron> neuronList : obj.getAllLayers() )
				for( Neuron neuron : neuronList ) {
					if( !neuronToIntMap.containsKey(neuron) ) {
						neuronToIntMap.put(neuron, i);
						if( !activatorToIntMap.containsKey(neuron.getActivator()) ) {
							if( neuron.getActivator()!=null ) {
								if( !activatorsStarted ) {
									out.append("\t<activators>\n");
									activatorsStarted=true;
								}
								out.append("\t");
								ActivatorXmlFactory axf = new ActivatorXmlFactory();
								@SuppressWarnings(value="unchecked")
								XmlMarshaller<Activator> m = (XmlMarshaller<Activator>)axf.getMarshaller(neuron.getActivator());
								m.marshal(neuron.getActivator(),out);
								out.append("\n");
								
							} 
							activatorToIntMap.put(neuron.getActivator(), j);
							j++;
						}
						i++;
					}
				}
			if( activatorsStarted )
				out.append("\t</activators>\n");
		
			out.append("<layers>\n");
			for(  List<Neuron> neuronList : obj.getAllLayers() ) {
				out.append("\t<layer>\n");
				for( Neuron neuron : neuronList ) {
					out.append("\t\t<neuron threshold=\"");
					out.append(new Double(neuron.getThreshold()).toString());
					out.append("\" activator-index=\"");
					out.append(activatorToIntMap.get(neuron.getActivator()).toString());
					out.append("\"");
					
					if( neuron.getOutputSynapses()!=null && neuron.getOutputSynapses().size()>0 ) {
						out.append(">\n");
						out.append("\t\t\t<synapses>\n");
						for( Synapse synapse : neuron.getOutputSynapses() ) {
							out.append("\t\t\t\t<synapse output=\"");
							out.append(neuronToIntMap.get(synapse.getOutput()).toString());
							out.append("\" weight=\"");
							out.append(new Double(synapse.getWeight()).toString());
							out.append("\"/>\n");
						}
						out.append("\t\t\t</synapses>\n");
						out.append("\t\t</neuron>\n");
					} else out.append("/>\n");
				}
				out.append("\t</layer>\n");
			}
			out.append("</layers>\n");
			
			out.append("</feedforward>");
		} catch( IOException ioe ) {
			throw new XmlException("An IOException occurred marshalling the Xml",ioe);
		}
	}
}
