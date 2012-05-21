package com.jonschang.ai.network.feedforward.gaio;

import java.util.List;

import com.jonschang.ai.ga.GenericPhenotype;
import com.jonschang.math.vector.MathVector;
import com.jonschang.math.vector.VectorImpl;

public class Phenotype extends GenericPhenotype<Gene> {

	private List<Gene> allGenes;
	
	/**
	 * The list of genes that are possible for a phenotype.
	 * @param genes A comprehensive sequence-significant list of genes used in phenotype generation.
	 */
	public void setAllGenes(List<Gene> genes) {
		this.allGenes = genes;
	}
	public List<Gene> getAllGenes() {
		return this.allGenes;
	}
	
	/**
	 * @return The input mask represented by the genes of the phenotype.
	 */
	public MathVector getInputMask() {
		MathVector inputMask = new VectorImpl();
		for(Gene gene : getAllGenes()) {
			if(getGenes().contains(gene)) {
				inputMask.getData().add(1.0);
			} else {
				inputMask.getData().add(0.0);
			}
		}
		return inputMask;
	}

	public int compareTo(Phenotype o) {
		return (int)(this.getScore()-((Phenotype)o).getScore());
	}	
}
