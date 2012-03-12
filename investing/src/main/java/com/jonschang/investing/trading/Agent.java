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
package com.jonschang.investing.trading;

import java.util.*;
import com.jonschang.investing.model.*;
import com.jonschang.utils.*;

public interface Agent<S extends Quotable<Q,E>, Q extends Quote<S>, E extends Exchange<S>> 
	extends HasDatePublisher {
	
	<T extends Platform<S,Q,E>> T getPlatform();
	<T extends Platform<S,Q,E>> void setPlatform(T tradingPlatform);
	
	<T extends Account<S,Q,E>> T getAccount();
	<T extends Account<S,Q,E>> void setAccount(T account);
	
	<T extends Quotable<Q,E>> void setQuotables(List<T> quotables);
	<T extends Quotable<Q,E>> List<T> getQuotables();
	
	<T extends Transaction<S,Q,E>> List<T> run() throws AgentException;
}
