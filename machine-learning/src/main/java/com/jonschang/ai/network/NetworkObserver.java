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


/**
 * A common-base to extend other, more specific, network observers from
 */
public interface NetworkObserver<N extends Network> 
{
	/**
	 * Called by Network::attach()
	 * @param <T>
	 * @param network
	 */
	public <T extends N> void setNetwork(T network);
	
	/**
	 * Returns the Network the NetworkObserver is attached to
	 * @param <T>
	 * @return
	 */
	public <T extends N> T getNetwork();
	
	/**
	 * Called at the end of Network::calculateResponse() 
	 */
	public void onCalculate();
}
