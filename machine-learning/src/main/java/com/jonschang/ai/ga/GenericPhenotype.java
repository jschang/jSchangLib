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

import java.util.*;
import org.apache.log4j.*;
import com.jonschang.utils.*;
import org.dom4j.*;

/**
 * An individual of the entire population.
 * 
 * It's important that the constructor do as little as possible
 * because the Phenotype may be transmitted by Gene name only
 * across a network.
 * 
 * Any heavy weight processing should be done in a method added
 * in a descendent class.
 * 
 * @author schang
 */
public class GenericPhenotype<G extends Gene>
	implements Phenotype<G> {
	private List<G> genes;
	public void setGenes(List<G> genes) {
		this.genes = genes; 
	}
	public List<G> getGenes() {
		return genes;
	}
	
	private Double score;
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score=score;
	}
	
	List<Phenotype<G>> parents = null;
	public List<Phenotype<G>> getParents() {
		return parents;
	}
	public void setParents(List<Phenotype<G>> parents) {
		this.parents = parents;
	}
	
	List<Phenotype<G>> children = null;
	public List<Phenotype<G>> getChildren() {
		return children;
	}
	public void setChildren(List<Phenotype<G>> children) {
		this.children = children;
	}
	
	final public int compareTo(Phenotype<G> other) {
		if( score!=null && other.getScore()!=null ) {
			if( score==other.getScore() )
				return 0;
			else if( score>other.getScore() ) {
				return 1;
			} else {
				return (-1);
			}
		}
		if( score==null && other.getScore()!=null )
			return (-1);
		return 1;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Boolean firstRun = true;
		sb.append("<Phenotype score:"+score+" - ");
		for( Gene gene : genes ) {
			if( !firstRun )
				sb.append(",");
			sb.append( gene.getName() );
			firstRun = false;
		}
		sb.append(">");
		return sb.toString();
	}
		
	public boolean equals(Object o) {
		if( ! ( o instanceof Phenotype ) )
			return false;
		Phenotype<G> phen = (Phenotype<G>)o;
		if( phen.getGenes()==null && genes!=null )
			return false;
		if( phen.getGenes().size() != genes.size() )
			return false;
		for( int i = 0; i<genes.size(); i++ )
			if( ! phen.getGenes().contains( genes.get(i) ) ) 
				return false;
		return true;
	}
	
	private GeneticAlgFactory<? extends Phenotype<G>,G> factory;
	public void setFactory(GeneticAlgFactory<? extends Phenotype<G>,G> factory) {
		this.factory = factory;
	}
	public GeneticAlgFactory<? extends Phenotype<G>,G> getFactory() {
		return factory;
	}
	
	public Element getXml() throws XmlException {
		DocumentFactory f = DocumentFactory.getInstance(); 
		Element toRet = f.createElement("phenotype");
		toRet.addAttribute("class","com.jonschang.ai.ga.GenericPhenotype");
		toRet.addAttribute("score",((Double)(score!=null?score:0.0)).toString());
		Element geneEle=null;
		for( Gene gene : this.genes ) {
			toRet.add(gene.getXml());
		}
		return toRet;
	}
	public void setXml(Element xml) throws XmlException {
		if( xml.getName().compareTo("phenotype")==0 ) {
			Logger.getLogger(this.getClass()).trace("populating Phenotype with xml: "+xml.asXML());
			this.setScore( Double.valueOf(xml.attributeValue("score")) );
			List<Element> xmlGenes = (List<Element>)xml.elements("gene");
			if( genes == null )
				genes = new ArrayList<G>();
			else genes.clear();
			for( Element geneEle : xmlGenes ) {
				try {
					G newGene = factory.newGene( geneEle.attributeValue("name") );
					newGene.setXml(geneEle);
					genes.add(newGene);
				} catch( GeneticAlgException gae ) {
					throw new XmlException("Could not create a new gene "+geneEle.attributeValue("name"),gae);
				}
			}
		}
	}
}
