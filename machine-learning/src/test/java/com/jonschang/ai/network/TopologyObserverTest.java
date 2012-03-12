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

package com.jonschang.ai.network;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.jonschang.ai.network.feedforward.*;
import com.jonschang.math.vector.VectorImpl;

public class TopologyObserverTest extends WindowAdapter {

	@org.junit.Test public void nullMethod() {
		// TODO: figure out a way to make this a valid test class
	}
	
	public static void main(String[] args) throws Exception {

		try {
			
			com.jonschang.utils.LoggingUtils.configureLogger();
			
			SingleNetworkTrainer<FeedForward> trainer = createTrainer();
			TopologyObserverTest listener = new TopologyObserverTest();
			TopologyObserver observer = new TopologyObserver();		
			trainer.getNetwork().attach(observer);
			
			TrainingRunner runner = new TrainingRunner(trainer);
			runner.start();
			
			JFrame frame = new JFrame();
			frame.add( observer );
			frame.addWindowListener(listener);
			frame.setBounds(0,0,600,400);
			frame.setVisible(true);
			frame.validate();
		} catch( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
	
	static public class TrainingRunner extends Thread
	{
		SingleNetworkTrainer<FeedForward> trainer;
		public TrainingRunner(SingleNetworkTrainer<FeedForward> trainer)
		{
			this.trainer = trainer;
		}
		public void run() {
			//System.out.print("in trainer\n");
			try {
				this.trainer.train();
				if( this.trainer instanceof FeedForwardNetworkTrainer )
					System.out.print("convergence at "+((FeedForwardNetworkTrainer)this.trainer).getIterations());
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	
	I took this test out, because the Stock stuff is part of another module now
	
	static public SingleNetworkTrainer<FeedForward> createTrainer2() throws Exception {
		
		NetworkBuilder<FeedForward,StockQuote,Stock> t = new NetworkBuilder_0_0_1a( new GenericDatePublisher() );
		
		t.setRunEnd( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2009 16:00") );
		t.setTrainingStart( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2006 16:00") );
		t.setTrainingEnd( new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("10/05/2008 16:00") );
		
		SingleNetworkTrainer<FeedForward> trainer = t.getTrainer();
		
		List<Stock> quotables = new ArrayList<Stock>();
		quotables.add( (Stock)Investing.instance().getQuotableServiceFactory().get(Stock.class).get("MSFT") );
		t.getQuoteVSTrainingSetSource().setQuotables(quotables);
		
		return trainer;
	}
	*/
	
	static public FeedForwardNetworkTrainer createTrainer()
	{
		FeedForward net = new FeedForward();
		BackPropagation trainer = new BackPropagation();
		SigmoidActivator activator = new SigmoidActivator();
		net.addLayer(3, activator);
		net.addLayer(6, activator);
		net.addLayer(6, activator);
		net.addLayer(6, activator);
		net.addLayer(3, activator);
		
		BufferedTrainingSetSource trainingData = new BufferedTrainingSetSource();
		Double d[][][] =
		{
			{{1.0,   2.0,   3.0  } ,{1.0,0.0,0.0}},
			{{0.1, 0.2, 0.3} ,{1.0,0.0,0.0}},
			{{10.0,  100.0, 1000.0},{1.0,0.0,0.0}},
			{{3.0,   2.0,   1.0}   ,{0.0,1.0,0.0}},
			{{0.3, 0.2, 0.1} ,{0.0,1.0,0.0}},
			{{1000.0,100.0, 10.0}  ,{0.0,1.0,0.0}},
			{{100.0, 100.0, 100.0} ,{0.0,0.0,1.0}},
			{{0.1, 0.1, 0.1} ,{0.0,0.0,1.0}},
			{{10.0,  10.0,  10.0}  ,{0.0,0.0,1.0}},
		};
		for( int i=0; i<d.length; i++ )
			trainingData.addPair( new VectorImpl(d[i][0]), new VectorImpl(d[i][1]));
		
		trainer.setNetwork(net);
		trainer.setTrainingSetSource(trainingData);
		
		return trainer;
	}
	
	public void windowClosing(WindowEvent e)
	{
		System.exit(0);
	}
}
