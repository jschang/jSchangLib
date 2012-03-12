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
import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * The core GeneticAlgorithm implementation.
 * 
 * Requires properties:
 * 	- GenerationEvaluator generationEvaluator
 *  - GeneticAlgFactory geneFactory 
 * @author schang
 */
public class GeneticAlgorithm<P extends Phenotype<G>,G extends Gene> {
	
	private GeneticAlgFactory<P,G> geneticAlgFactory = null;
	private Float mutationRate = 0.00f;
	private Boolean removeDuplicates = true;
	private Short phenotypeSize = 10;
	private Boolean maintainSize = false;
	private Integer lowNOC = 10;
	private Integer highNOC = 10;
	private GenerationEvaluator<P,G> generationEvaluator = null;
	private boolean done=false;
	private List<GeneticAlgorithmObserver<P,G>> observers = new ArrayList<GeneticAlgorithmObserver<P,G>>();
	
	/**
	 * A running list of the entire tested population
	 */
	private List<P> population = new ArrayList<P>();
	
	/**
	 * @param eval The delegate for evaluating a particular generation.
	 */
	public void setGenerationEvaluator(GenerationEvaluator<P,G> eval) {
		generationEvaluator = eval;
	}
	
	/**
	 * @param low The lowest number of offspring each pairing should produce.
	 * @param high The highest number of offspring each pairing should produce.
	 */
	public void setNumberOfOffspring(Integer low, Integer high) {
		lowNOC = low;
		highNOC = high;
	}
	
	/**
	 * @param factory The factory the fitness function, genes and phenotype prototypes is pulled from.
	 */
	public void setGeneFactory(GeneticAlgFactory<P,G> factory) {
		geneticAlgFactory = factory;
	}
	
	/**
	 * @param rate The rate at which a random gene will appear at any chromosome.
	 */
	public void setMutationRate(Float rate) {
		mutationRate = rate;
	}
	
	/**
	 * Whether or not to remove duplicate genes from the offspring
	 * @param remove
	 */
	public void setRemoveDuplicates(Boolean remove) {
		removeDuplicates = remove;
	}
	
	/**
	 * Set the number of genes allowed in a phenotype
	 * @param size
	 */
	public void setPhenotypeSize(Short size) {
		phenotypeSize = size;
	}
	
	/**
	 * 
	 */
	public void setMaintainSize(Boolean maintainSize) {
		this.maintainSize = maintainSize;
	}
	
	/**
	 * @return The entire history of phenotypes tested.
	 */
	public List<P> getPopulation() {
		return population;
	}
	
	public void addObserver(GeneticAlgorithmObserver<P,G> observer) {
		this.observers.add(observer);
	}
	public void removeObserver(GeneticAlgorithmObserver<P,G> observer) {
		this.observers.remove(observer);
	}
	
	public void run() throws GeneticAlgException {
		
		synchronized(this) {
			done=false;
		}
		
		// create two random and different parents
		P parent1 = random();
		population.add(parent1);
		P parent2 = random();
		while( parent2.equals(parent1) )
			parent2 = random();
		population.add(parent2);
		
		P child = null;
		
		List<P> thisGeneration = new ArrayList<P>();
		thisGeneration.add(parent1);
		thisGeneration.add(parent2);
		generationEvaluator.evaluate(thisGeneration);
		thisGeneration.clear();
		
		Integer numChildren;
		while( ! isDone() ) {
			
			Logger.getLogger(this.getClass()).info("parents: "+parent1+" + "+parent2);
			
			numChildren = lowNOC + ( (Double)( (highNOC.doubleValue()-lowNOC.doubleValue()) * Math.random() ) ).intValue();
			Logger.getLogger(this.getClass()).trace("creating "+numChildren+" children");
			
			for( int i = 0; i < numChildren; i++ ) {
				child = procreate(parent1,parent2);
				if( ! population.contains(child) ) {
					Logger.getLogger(this.getClass()).trace("created child "+child);
					thisGeneration.add(child);
					maintainAncestory(parent1,parent2,child);
					population.add(child);
				} //else Logger.getLogger(this.getClass()).trace("rejecting "+child+" as is already a tested canidate");
			}

			for( GeneticAlgorithmObserver<P,G> observer : observers )
				observer.onGenerationEvaluation(thisGeneration);
			
			generationEvaluator.evaluate(thisGeneration);
			
			// well, it's not advisable in nature,
			// but we want the best among parents and offspring
			// so give the parents the opportunity to procreate
			// in the next round
			if( ! thisGeneration.contains(parent1) )
				thisGeneration.add(parent1);
			if( ! thisGeneration.contains(parent2) )
				thisGeneration.add(parent2);
			
			// sorts the generation from the least fit to the most fit
			Collections.sort(thisGeneration);
			
			for( GeneticAlgorithmObserver<P,G> observer : observers )
				observer.onGenerationEvaluationComplete(thisGeneration);
			
			// select the two most fit phenotypes from the generation
			// to produce the next generation
			parent1 = thisGeneration.get( thisGeneration.size()-1 );
			parent2 = thisGeneration.get( thisGeneration.size()-2 );
			
			if( thisGeneration.size()==2 ) {
				synchronized(this) {
					done=true;
				}
			} else thisGeneration.clear();
		}
		
		for( GeneticAlgorithmObserver<P,G> observer : observers )
			observer.onComplete(parent1);
	}
	
	public boolean isDone() {
		synchronized(this) {
			if( done ) return true;
		}
		return false;
	}
	
	/**
	 * Creates a Phenotype of the correct number of Genes randomly from the GeneticAlgFactory
	 * @return P An adam or eve
	 */
	private P random() throws GeneticAlgException {
		P p = (P)geneticAlgFactory.newPhenotype();
		List<G> genes = new ArrayList<G>();
		Boolean done = false;
		int i = 0;
		while( !done ) {
			genes.add( geneticAlgFactory.newGene() );
			if( removeDuplicates )
				removeDuplicateGenes(genes);
			if( maintainSize && genes.size()==phenotypeSize )
				done=true;
			else if( !maintainSize && i==phenotypeSize-1 )
				done=true;
			i++;
		}
		p.setGenes(genes);
		return p;
	}
	
	/**
	 * Creates an offspring Phenotype from two parent Phenotypes
	 * @param donor1
	 * @param donor2
	 * @return P The resulting offspring from the pairing
	 */
	private P procreate(P donor1, P donor2) throws GeneticAlgException {
		Iterator<G> donor1Iter = donor1.getGenes().iterator();
		Iterator<G> donor2Iter = donor2.getGenes().iterator();
		List<G> childGenes = new ArrayList<G>();
		Double rand = null;
		Float tmp;
		G gene1,gene2;
		P childPhen = geneticAlgFactory.newPhenotype();
		
		// iterate through the genes of each parent
		// randomly selecting the gene from one
		// and periodically mutating
		while( donor1Iter.hasNext() && donor2Iter.hasNext() ) {
			gene1 = mutate();
			if( gene1 != null ) {
				childGenes.add(gene1);
				gene1 = donor1Iter.next();
				gene2 = donor2Iter.next();
				continue;
			}
			rand = Math.random();
			gene1 = donor1Iter.next();
			gene2 = donor2Iter.next();
			tmp = gene1.getExpressiveness()+gene2.getExpressiveness();
			tmp *= rand.floatValue();
			if( gene1.getExpressiveness()<tmp )
				childGenes.add( geneticAlgFactory.newGene( gene1.getName() ) );
			else childGenes.add( geneticAlgFactory.newGene( gene2.getName() ) );
		}
		
		// either parent may have more genes than the other
		// so finish up iterating here
		finishIterating( donor1Iter, childGenes );
		finishIterating( donor2Iter, childGenes );
		
		if( removeDuplicates ) 
			removeDuplicateGenes(childGenes);
		
		if( maintainSize && childGenes.size()<phenotypeSize )
			while( childGenes.size()<phenotypeSize ) {
				childGenes.add( geneticAlgFactory.newGene() );
				removeDuplicateGenes(childGenes);
			}
		
		childPhen.setGenes(childGenes);
		
		return childPhen;
	}
	
	/**
	 * Method completes iteration over Genes of a parent Phenotype.
	 * Two parents may have a different number of Genes, so this method
	 * is provided to finish creating the offspring Phenotype
	 * @param iter
	 * @param genes
	 */
	private void finishIterating( Iterator<G> iter, List<G> genes ) throws GeneticAlgException {
		G gene = null;
		while( iter.hasNext() ) {
			gene = mutate();
			if( gene != null ) {
				genes.add(gene);
				continue;
			}
			genes.add( geneticAlgFactory.newGene( iter.next().getName() ) );
		}
	}
	
	/**
	 * Creates a random Gene, if a random number is inside the mutation rate
	 * @return
	 */
	private G mutate() throws GeneticAlgException {
		Double rand = Math.random();
		if( rand < mutationRate ) {
			return geneticAlgFactory.newGene();
		}
		return null;
	}
	
	/**
	 * Removes the duplicate Genes from a Phenotype
	 * @param genes
	 */
	private void removeDuplicateGenes(List<G> genes) {
		Map<String,G> heldGenes = new HashMap<String,G>();
		List<Integer> removeIdx = new ArrayList<Integer>();
		Integer i=0;
		for( G gene : genes ) {
			if( heldGenes.get(gene.getName())!=null )
				removeIdx.add(i);
			else heldGenes.put(gene.getName(),gene);
			i++;
		}
		Collections.sort(removeIdx);
		for( i = removeIdx.size()-1; i>=0; i-- ) {
			genes.remove((int)removeIdx.get(i));
		}
	}
	
	static <A extends Phenotype<G>,G extends Gene> void maintainAncestory(A donor1, A donor2, A childPhen) {
		
		if( childPhen.getParents()==null ) {
			childPhen.setParents(new ArrayList<Phenotype<G>>());
		}
		childPhen.getParents().add(donor1);
		childPhen.getParents().add(donor2);
		
		if( donor1.getChildren()==null ) {
			donor1.setChildren(new ArrayList<Phenotype<G>>());
		}
		donor1.getChildren().add(childPhen);
		
		if( donor2.getChildren()==null ) {
			donor2.setChildren(new ArrayList<Phenotype<G>>());
		}
		donor2.getChildren().add(childPhen);
	}
}
