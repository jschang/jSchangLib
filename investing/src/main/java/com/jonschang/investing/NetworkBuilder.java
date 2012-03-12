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
package com.jonschang.investing;

import java.util.*;
import com.jonschang.ai.network.*;
import com.jonschang.investing.valuesource.*;
import com.jonschang.investing.model.*;

public abstract class NetworkBuilder<N extends Network, Q extends Quote<S>, S extends Quotable> {
	abstract public void setTrainingStart(Date date);
	abstract public void setTrainingEnd(Date date);
	abstract public void setRunEnd(Date date);
	abstract public List<QuotePublisher<Q,S>> getPublishers();
	abstract public SingleNetworkTrainer<N> getTrainer() throws NetworkBuilderException;
	abstract public QuoteVSTrainingSetSource<Q,S> getQuoteVSTrainingSetSource();
}

