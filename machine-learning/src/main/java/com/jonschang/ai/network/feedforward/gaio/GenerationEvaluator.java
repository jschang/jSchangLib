package com.jonschang.ai.network.feedforward.gaio;

import java.util.ArrayList;
import java.util.List;

import com.jonschang.ai.ga.GeneticAlgException;
import com.jonschang.ai.ga.GeneticAlgFactory;
import com.jonschang.ai.network.NetworkTrainingException;
import com.jonschang.math.vector.MathVector;

public class GenerationEvaluator implements
		com.jonschang.ai.ga.GenerationEvaluator<Phenotype, Gene> {

	private GeneticAlgFactory<Phenotype,Gene> geneticAlgFactory;
	private GABPInputOptimizer optimizer;
	
	public GenerationEvaluator() {
		optimizer = new GABPInputOptimizer();
	}
	
	@Override
	public void setGeneticAlgFactory(GeneticAlgFactory<Phenotype, Gene> gaf) {
		geneticAlgFactory = gaf;
	}
	@Override
	public GeneticAlgFactory<Phenotype, Gene> getGeneticAlgFactory() {
		return geneticAlgFactory;
	}

	@Override
	public void evaluate(List<Phenotype> phenotypes) throws GeneticAlgException {
		try {
			List<MathVector> inputMasks = new ArrayList<MathVector>();
			for(Phenotype phenotype:phenotypes) {
				MathVector vector = phenotype.getInputMask();
				inputMasks.add(vector);
			}
			optimizer.setInputMasks(inputMasks);
			optimizer.train();
		} catch (NetworkTrainingException e) {
			throw new GeneticAlgException(e);
		}
	}

}
