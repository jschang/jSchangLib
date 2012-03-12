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
package com.jonschang.investing.stocks;

import java.util.*;

import com.jonschang.ai.network.*;
import com.jonschang.ai.network.feedforward.*;
import com.jonschang.investing.*;
import com.jonschang.investing.stocks.*;
import com.jonschang.investing.stocks.model.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.utils.*;
import com.jonschang.utils.valuesource.*;

public class NetworkBuilder_0_0_1a extends NetworkBuilder<FeedForward,StockQuote,Stock> {

	private QuoteVSTrainingSetSource<StockQuote,Stock> qvsTSS = null;
	private Date trainingStart = null;
	private Date trainingEnd = null;
	private Date runEnd = null;
	private DatePublisher datePublisher = null;
	private FeedForwardNetworkTrainer trainer = null;
	private List<QuotePublisher<StockQuote,Stock>> publishers = new ArrayList<QuotePublisher<StockQuote,Stock>>();
	
	public NetworkBuilder_0_0_1a(DatePublisher dp) throws NetworkBuilderException {
		datePublisher = dp;
	}
	
	@Override
	public void setRunEnd(Date date) {
		this.runEnd = (Date)date.clone();
	}

	@Override
	public void setTrainingEnd(Date date) {
		this.trainingEnd = (Date)date.clone();
	}

	@Override
	public void setTrainingStart(Date date) {
		this.trainingStart = (Date)date.clone();
	}

	@Override
	public List<QuotePublisher<StockQuote, Stock>> getPublishers() {
		return publishers;
	}
	
	public SingleNetworkTrainer<FeedForward> getTrainer() throws NetworkBuilderException {		
		TrainingSetSource qvsTss = null;
		try {
			qvsTss = createTrainingSetSource();
			trainer = createNetworkAndTrainer();
			trainer.attachObserver( new SimulatedAnnealingTrainerObserver<FeedForward>() );
		} catch(Exception e) {
			throw new NetworkBuilderException(e);
		}
		trainer.setTrainingSetSource(qvsTss);
		
		QuoteValueSourceList<StockQuote,Stock> inputs = 
			(QuoteValueSourceList<StockQuote,Stock>)
			((QuoteVSTrainingSetSource<StockQuote,Stock>)
			((BufferedTrainingSetSource)trainer
			.getTrainingSetSource())
			.getTrainingSetSource()).getInputs();
		trainer.getNetwork().setInputs((ValueSourceList)inputs);
		datePublisher.subscribe(inputs);
		return trainer;
	}
	
	public QuoteVSTrainingSetSource getQuoteVSTrainingSetSource() {
		return qvsTSS;
	}

	private FeedForwardNetworkTrainer createNetworkAndTrainer()
	{
		FeedForward net = new FeedForward();
		BackPropagation trainer = new BackPropagation();
		SigmoidActivator activator = new SigmoidActivator();
		net.addLayer(7, activator);
		net.addLayer(5, activator);
		net.addLayer(10, activator);
		net.addLayer(20, activator);
		net.addLayer(3, activator);
		trainer.setNetwork(net);
		trainer.setLearningRate(1.0);
		return trainer;
	}
	
	private TrainingSetSource createTrainingSetSource() throws Exception {
		
		QuoteVSTrainingSetSource<StockQuote,Stock> TSS = new QuoteVSTrainingSetSource<StockQuote,Stock>();
		qvsTSS = TSS;
		TSS.setDatePublisher(datePublisher);
		
		AggregateQuoteValueSourceList<StockQuote,Stock> inputs = new AggregateQuoteValueSourceList<StockQuote,Stock>();
		AggregateQuoteValueSourceList<StockQuote,Stock> outputs = new AggregateQuoteValueSourceList<StockQuote,Stock>();
		datePublisher.subscribe(inputs);
		datePublisher.subscribe(outputs);
		
		QuoteValueSourceList<StockQuote,Stock> qvsl1 = createQVSL(null);
		QuoteValueSourceList<StockQuote,Stock> qvsl2 = createQVSL("^GSPC");
		QuoteValueSourceList<StockQuote,Stock> qvsl1Out = createQVSL(null);
		
		TSS.getQuotePublishersToUpdate().add(qvsl1.getQuotePublisher());
		TSS.getQuotePublishersToUpdate().add(qvsl1Out.getQuotePublisher());
		
		datePublisher.subscribe(qvsl1);
		datePublisher.subscribe(qvsl2);
		datePublisher.subscribe(qvsl1Out);
		
		inputs.add( qvsl1 );
		inputs.add( qvsl2 );
		outputs.add( qvsl1Out );
		
		createSymbolInput( qvsl1 );
		createIndexInput( qvsl2 );
		createSymbolOutput( qvsl1Out );
		
		TSS.setInputs(inputs);
		TSS.setOutputs(outputs);
		TSS.setDateRange(
			new DateRange(
				(Date)trainingStart.clone(), 
				(Date)trainingEnd.clone()
			)	
		);
		TSS.setInterval(TimeInterval.DAY);
		return new BufferedTrainingSetSource().setTrainingSetSource(TSS);
	}
	
	private QuoteValueSourceList<StockQuote,Stock> createQVSL(String stock) throws Exception {
		StockService stockService = (StockService)Investing.instance().getQuotableServiceFactory().get(Stock.class);
		PrefetchQuotePublisher<StockQuote,Stock> pub = new PrefetchQuotePublisher<StockQuote,Stock>();
		StockEventSplitAdjuster adj = new StockEventSplitAdjuster();
		pub.addQuoteAdjuster(adj);
		datePublisher.subscribe(pub);
		if( stock!=null ) {
			Stock stockQuotable = stockService.get(stock);
			pub.setQuotable(stockQuotable);
		}
		pub.setQuoteClass(StockQuote.class);
		pub.setTimeInterval(TimeInterval.DAY);
		pub.setDateRange( new DateRange(trainingStart,runEnd) );
		publishers.add(pub);
		QuoteValueSourceList<StockQuote,Stock> qvsl = new QuoteValueSourceList<StockQuote,Stock>();
		qvsl.setQuotePublisher(pub);		
		datePublisher.subscribe(qvsl);
		return qvsl;
	}
	
	private void createSymbolOutput(QuoteValueSourceList<StockQuote,Stock> qvsl) throws Exception {
		
		SingleQuoteValueSource<StockQuote,Stock> ssqvs;
		OffsetPeriodsValueSource<StockQuote,Stock> current;
		
		ssqvs = new SingleQuoteValueSource<StockQuote,Stock>();
		ssqvs.setReturnType(StockQuote.LOW);
		
		/***
		 * NOW CREATE THE OUTPUT SET
		 */
		
		current = new OffsetPeriodsValueSource<StockQuote,Stock>();
		current.setPeriods(15);
		current.setValueSource(ssqvs);
		
		// 15 after current are less than current
		LessThanValueSource<StockQuote,Stock> ltvs = new LessThanValueSource<StockQuote,Stock>();
		ltvs.setValueSource(ssqvs);
		ltvs.setThreshold(current);
		ltvs.setPeriods(15);
		qvsl.add(ltvs);
		
		// all 15 after current are greater than current
		GreaterThanValueSource<StockQuote,Stock> gtvs = new GreaterThanValueSource<StockQuote,Stock>();
		gtvs.setValueSource(ssqvs);
		gtvs.setThreshold(current);
		gtvs.setPeriods(15);
		qvsl.add(gtvs);
		
		PercentageValueSource<StockQuote,Stock> pvs = new PercentageValueSource<StockQuote,Stock>();
		pvs.setBasis(current);
		pvs.setMeasure(ssqvs);
		qvsl.add(pvs);
	}
	
	private void createSymbolInput(QuoteValueSourceList<StockQuote,Stock> qvsl) throws Exception {
		
		@SuppressWarnings(value="unchecked")
		QuoteService<StockQuote,Stock> stockQuoteService 
			= (QuoteService<StockQuote,Stock>)Investing
				.instance()
				.getQuoteServiceFactory()
				.getQuoteService(StockQuote.class);
		
		SingleQuoteValueSource<StockQuote,Stock> ssqvs;
		StochasticOscillatorValueSource<StockQuote,Stock> sovs;
		DifferenceValueSource<StockQuote,Stock> dvs;
		OffsetPeriodsValueSource<StockQuote,Stock> idvs;
		DirectionalIndexValueSource<StockQuote,Stock> divs;
		DirectionalMovementValueSource<StockQuote,Stock> dmvs;
		
		/***
		 * CREATE THE INPUT SET
		 */
		
		ssqvs = new SingleQuoteValueSource<StockQuote,Stock>();
		ssqvs.setReturnType(StockQuote.LOW);
		
		//+++++++++++++++++++++++++++
		
		// create an ema(10)-ema(30) value source
		dvs = new DifferenceValueSource<StockQuote,Stock>();
		dvs.setLeft(createEMAValueSource(ssqvs,10));
		dvs.setRight(createEMAValueSource(ssqvs,30));
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(dvs);
		idvs.setPeriods(15);
		
		qvsl.add(idvs);
		
		//+++++++++++++++++++++++++++
		
		// create an ema %K-%D difference stochastic oscillator
		dvs = new DifferenceValueSource<StockQuote,Stock>();
		sovs = new StochasticOscillatorValueSource<StockQuote,Stock>();
		sovs.setPeriods(10);
		sovs.setValueSource(ssqvs);
		dvs.setRight(createEMAValueSource(sovs,15));
		sovs = new StochasticOscillatorValueSource<StockQuote,Stock>();
		sovs.setPeriods(30);
		sovs.setValueSource(ssqvs);
		dvs.setLeft(createEMAValueSource(sovs,15));	
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(createEMAValueSource(dvs,15));
		idvs.setPeriods(15);
		
		qvsl.add(idvs);	
		
		//+++++++++++++++++++++++++++
		
		// create a ema(15) directional index
		divs = new DirectionalIndexValueSource<StockQuote,Stock>();
		divs.setPeriods(15);
		divs.setQuoteService(stockQuoteService);	
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(createEMAValueSource(divs,15));
		idvs.setPeriods(15);
		
		qvsl.add(idvs);
		
		//+++++++++++++++++++++++++++
		
		// create a ema positive directional movement indicator
		dmvs = new DirectionalMovementValueSource<StockQuote,Stock>();
		dmvs.setPeriods(15);
		dmvs.setDirection(DirectionalMovementValueSource.Direction.Positive);
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(createEMAValueSource(dmvs,15));
		idvs.setPeriods(15);
		
		qvsl.add(idvs);		
	}
	
	private void createIndexInput(QuoteValueSourceList<StockQuote,Stock> qvsl) throws Exception {
		
		@SuppressWarnings(value="unchecked")
		QuoteService<StockQuote,Stock> stockQuoteService 
			= (QuoteService<StockQuote,Stock>)Investing
				.instance()
				.getQuoteServiceFactory()
				.getQuoteService(StockQuote.class);
		
		SingleQuoteValueSource<StockQuote,Stock> ssqvs = new SingleQuoteValueSource<StockQuote,Stock>();	
		ssqvs.setReturnType(StockQuote.LOW);
		
		DifferenceValueSource<StockQuote,Stock> dvs;
		OffsetPeriodsValueSource<StockQuote,Stock> idvs;
		DirectionalIndexValueSource<StockQuote,Stock> divs;
		DirectionalMovementValueSource<StockQuote,Stock> dmvs;
		
		//+++++++++++++++++++++++++++
		
		// create an ema(10)-ema(30) value source
		dvs = new DifferenceValueSource<StockQuote,Stock>();
		dvs.setLeft(createEMAValueSource(ssqvs,10));
		dvs.setRight(createEMAValueSource(ssqvs,30));
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(dvs);
		idvs.setPeriods(15);
		
		qvsl.add(idvs);
		
		//+++++++++++++++++++++++++++
		
		// create a ema adx
		divs = new DirectionalIndexValueSource<StockQuote,Stock>();
		divs.setPeriods(15);
		divs.setQuoteService(stockQuoteService);
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(createEMAValueSource(divs,15));
		idvs.setPeriods(15);
		
		qvsl.add(idvs);
		
		//+++++++++++++++++++++++++++
		
		// create a ema positive directional movement indicator
		dmvs = new DirectionalMovementValueSource<StockQuote,Stock>();
		dmvs.setPeriods(15);
		dmvs.setDirection(DirectionalMovementValueSource.Direction.Positive);
		idvs = new OffsetPeriodsValueSource<StockQuote,Stock>();
		idvs.setValueSource(createEMAValueSource(dmvs,15));
		idvs.setPeriods(15);
		
		qvsl.add(idvs);		
	}
	
	private EMAValueSource<StockQuote,Stock> createEMAValueSource(QuoteValueSource<StockQuote,Stock> source, int periods) {
		EMAValueSource<StockQuote,Stock> evs = new EMAValueSource<StockQuote,Stock>();
		evs.setPeriods(periods);
		evs.setValueSource(source);
		return evs;
	}
}
