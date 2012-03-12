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

import org.dom4j.io.*;
import org.dom4j.*;

public class FeedForwardXmlUnmarshaller implements XmlUnmarshaller<FeedForward> {
	public void unmarshal(FeedForward obj, Reader xmlReader) throws XmlException {
		SAXReader reader = new SAXReader();
		Document doc = null;
		
		try {
			doc = reader.read(xmlReader);
		} catch(Exception e) {
			throw new XmlException("Could not read the Xml document",e);
		}
		
		Element root = doc.getRootElement();
		Element current = null;
		
		Map<Integer,Neuron> intToNeuronMap = new HashMap<Integer,Neuron>();
		Map<Integer,Activator> intToActivatorMap = new HashMap<Integer,Activator>();
		int neuronIndex=0, activators=0;
		Attribute attr = null;
		Neuron neuron = null;
		Neuron outputNeuron = null;
		
		obj.getAllLayers().clear();
		
		// build all the neurons and cache them in an
		// index-to-neuron map
		for( Object e : root.elements() ) if( e instanceof Element ) {
			current = (Element)e;
			
			if( current.getName().compareTo("layers")==0 ) {
				for( Object layerObject : current.elements() ) if( ((Element)layerObject).getName().compareTo("layer")==0 ){
					List<Neuron> thisLayer = new ArrayList<Neuron>();
					for( Object neuronObject : ((Element)layerObject).elements() ) if( ((Element)neuronObject).getName().compareTo("neuron")==0 ){
						neuron = new GenericNeuron();
						
						intToNeuronMap.put(neuronIndex,neuron);
						
						attr = ((Element)neuronObject).attribute("threshold");
						neuron.setThreshold(Double.valueOf(attr.getValue()));
						
						thisLayer.add(neuron);
						
						neuronIndex++;
					}
					obj.getAllLayers().add(thisLayer);
				}
			} else if( current.getName().compareTo("activators")==0 && current.elements().size()>0 ) {
				for( Object a : current.elements() ) if( a instanceof Element ){
					Element activator = (Element)a;
					ActivatorXmlFactory axf = new ActivatorXmlFactory();
					Activator activatorObject = null;
					String clazz = activator.attributeValue("type");
					try {
						activatorObject = (Activator)Class.forName(clazz).newInstance();
						@SuppressWarnings(value="unchecked")
						XmlUnmarshaller<Activator> m = (XmlUnmarshaller<Activator>)axf.getUnmarshaller( activatorObject );
						m.unmarshal( activatorObject, new StringReader(activator.asXML()) );
					} catch( Exception cnfe ) {
						throw new XmlException(cnfe);
					} 
					intToActivatorMap.put(activators,activatorObject);
					activators++;
				}
				
			}
		}
		
		// now that we've built a cross-reference of index-to-neuron
		// we can process the synapses and easily reconstruct
		// the connections between neurons
		Integer inputIndex=0, outputIndex, activatorIndex=0;
		Double weight;
		for( Object e : root.elements() ) if( ((Element)e).getName().compareTo("layers")==0 ) {
			for( Object layerObject : current.elements() ) if( ((Element)layerObject).getName().compareTo("layer")==0 ){
				for( Object neuronObject : ((Element)layerObject).elements() ) if( ((Element)neuronObject).getName().compareTo("neuron")==0 ){
					current = (Element)neuronObject;
		
					neuron = intToNeuronMap.get(inputIndex);
					
					// set the activator 
					attr = current.attribute("activator-index");
					activatorIndex = Integer.valueOf(attr.getValue());
					neuron.setActivator(intToActivatorMap.get(activatorIndex));
		
					// process the out-going synapses of the neuron
					if( current.element("synapses")!=null && current.element("synapses").element("synapse")!=null ) {
						for( Object e2 : current.element("synapses").elements() ) if( e2 instanceof Element ) {
							current = (Element)e2;
							
							// get the synapses output neuron
							attr = current.attribute("output");
							outputIndex = Integer.valueOf(attr.getValue());
							outputNeuron = intToNeuronMap.get(outputIndex);
							
							// set the weight of the synapse
							attr = current.attribute("weight");
							weight = Double.valueOf(attr.getValue());
							Synapse s = new GenericSynapse();					
							neuron.setSynapseTo(s,outputNeuron);
							s.setWeight(weight);
						}
					}
					inputIndex++;
				}
			}
		}
		
		obj.getInputNeurons().clear();
		obj.getOutputNeurons().clear();
		obj.getInputNeurons().addAll(obj.getAllLayers().get(0));
		obj.getOutputNeurons().addAll(obj.getAllLayers().get(obj.getAllLayers().size()-1));
	}
}
