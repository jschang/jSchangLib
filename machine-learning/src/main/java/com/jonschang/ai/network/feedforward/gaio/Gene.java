package com.jonschang.ai.network.feedforward.gaio;

import com.jonschang.ai.ga.GenericGene;
import com.jonschang.utils.valuesource.IValueSourceList;

/**
 * Represents a set of value-sources that may be masked in the GABPInputOptimizer;
 * 
 * @author schang
 */
public class Gene extends GenericGene {
	private IValueSourceList valueSources;
	public void setValueSourceList(IValueSourceList list) {
		valueSources = list;
	}
	public IValueSourceList getValueSourceList() {
		return valueSources;
	}
}
