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
package com.jonschang.investing.model;

import java.util.Date;

import com.jonschang.utils.TimeInterval;
import javax.persistence.*;

@MappedSuperclass
public class GenericQuote<Q extends Quotable> implements Quote<Q>
{
	@Transient
	public double getField(int fieldNum) {
		if( fieldNum == Quote.HIGH )
			return getPriceHigh();
		if( fieldNum == Quote.LOW )
			return getPriceLow();
		if( fieldNum == Quote.CLOSE )
			return getPriceClose();
		if( fieldNum == Quote.OPEN )
			return getPriceOpen();
		return 0.0f;
	}
	
	@Transient
	public Q getQuotable()
	{ return this.quotable; }
	public void setQuotable(Q quotable)
	{ this.quotable=quotable; }
	private Q quotable;
	
	@Column(name="date")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDate()
		{ return m_date; }
	public void setDate(Date date)
		{ m_date = date; }
	private Date m_date = null;

	@Column(name="open")
	public Float getPriceOpen()
		{ return m_openPrice; }
	public void setPriceOpen(Float price)
		{ m_openPrice = price; }
	private Float m_openPrice;
	
	@Column(name="close")
	public Float getPriceClose()
		{ return m_closePrice; }
	public void setPriceClose(Float price)
		{ m_closePrice = price; }
	private Float m_closePrice;
	
	@Column(name="high")
	public Float getPriceHigh()
		{ return m_highPrice; }
	public void setPriceHigh(Float price)
		{ m_highPrice = price; }
	private Float m_highPrice;
	
	@Column(name="low")
	public Float getPriceLow()
		{ return m_lowPrice; }
	public void setPriceLow(Float price)
		{ m_lowPrice = price; }
	private Float m_lowPrice;
	
	@Transient
	public TimeInterval getTimeInterval() {
		return TimeInterval.get(getInterval());
	}
	
	@Column(name="intrvl")
	public Integer getInterval()
		{ return m_interval; }
	public void setInterval(Integer interval)
		{ m_interval = interval; }
	private Integer m_interval;
	
	public int compareTo(Quote<Q> o) 
	{
		return this.m_date.compareTo(o.getDate());
	}
}
