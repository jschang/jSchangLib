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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.08 at 09:03:52 PM EST 
//


package com.jonschang.cluster.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NodeConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NodeConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="cpuLow" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="cpuHigh" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="memHigh" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="swapHigh" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *       &lt;/all>
 *       &lt;attribute name="minLifetime" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="maxNodeDepth" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="maxChildren" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="statusUpdateInterval" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeConfiguration", propOrder = {

})
public class NodeConfiguration {

    @XmlElement(required = true)
    protected ThreeFloatUsageInfo cpuLow;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo cpuHigh;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo memHigh;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo swapHigh;
    @XmlAttribute
    protected Short minLifetime;
    @XmlAttribute
    protected Short maxNodeDepth;
    @XmlAttribute
    protected Short maxChildren;
    @XmlAttribute
    protected Long statusUpdateInterval;

    /**
     * Gets the value of the cpuLow property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getCpuLow() {
        return cpuLow;
    }

    /**
     * Sets the value of the cpuLow property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setCpuLow(ThreeFloatUsageInfo value) {
        this.cpuLow = value;
    }

    /**
     * Gets the value of the cpuHigh property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getCpuHigh() {
        return cpuHigh;
    }

    /**
     * Sets the value of the cpuHigh property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setCpuHigh(ThreeFloatUsageInfo value) {
        this.cpuHigh = value;
    }

    /**
     * Gets the value of the memHigh property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getMemHigh() {
        return memHigh;
    }

    /**
     * Sets the value of the memHigh property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setMemHigh(ThreeFloatUsageInfo value) {
        this.memHigh = value;
    }

    /**
     * Gets the value of the swapHigh property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getSwapHigh() {
        return swapHigh;
    }

    /**
     * Sets the value of the swapHigh property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setSwapHigh(ThreeFloatUsageInfo value) {
        this.swapHigh = value;
    }

    /**
     * Gets the value of the minLifetime property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMinLifetime() {
        return minLifetime;
    }

    /**
     * Sets the value of the minLifetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMinLifetime(Short value) {
        this.minLifetime = value;
    }

    /**
     * Gets the value of the maxNodeDepth property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMaxNodeDepth() {
        return maxNodeDepth;
    }

    /**
     * Sets the value of the maxNodeDepth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMaxNodeDepth(Short value) {
        this.maxNodeDepth = value;
    }

    /**
     * Gets the value of the maxChildren property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getMaxChildren() {
        return maxChildren;
    }

    /**
     * Sets the value of the maxChildren property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMaxChildren(Short value) {
        this.maxChildren = value;
    }

    /**
     * Gets the value of the statusUpdateInterval property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getStatusUpdateInterval() {
        return statusUpdateInterval;
    }

    /**
     * Sets the value of the statusUpdateInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStatusUpdateInterval(Long value) {
        this.statusUpdateInterval = value;
    }

}
