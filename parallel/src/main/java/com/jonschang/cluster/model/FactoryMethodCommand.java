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
 * <p>Java class for FactoryMethodCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FactoryMethodCommand">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="factoryMethod" type="{http://model.cluster.jonschang.com/Cluster}MethodCall"/>
 *         &lt;element name="executionMethod" type="{http://model.cluster.jonschang.com/Cluster}MethodCall"/>
 *       &lt;/all>
 *       &lt;attribute name="factoryClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FactoryMethodCommand", propOrder = {

})
public class FactoryMethodCommand {

    @XmlElement(required = true)
    protected MethodCall factoryMethod;
    @XmlElement(required = true)
    protected MethodCall executionMethod;
    @XmlAttribute
    protected String factoryClass;

    /**
     * Gets the value of the factoryMethod property.
     * 
     * @return
     *     possible object is
     *     {@link MethodCall }
     *     
     */
    public MethodCall getFactoryMethod() {
        return factoryMethod;
    }

    /**
     * Sets the value of the factoryMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodCall }
     *     
     */
    public void setFactoryMethod(MethodCall value) {
        this.factoryMethod = value;
    }

    /**
     * Gets the value of the executionMethod property.
     * 
     * @return
     *     possible object is
     *     {@link MethodCall }
     *     
     */
    public MethodCall getExecutionMethod() {
        return executionMethod;
    }

    /**
     * Sets the value of the executionMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodCall }
     *     
     */
    public void setExecutionMethod(MethodCall value) {
        this.executionMethod = value;
    }

    /**
     * Gets the value of the factoryClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFactoryClass() {
        return factoryClass;
    }

    /**
     * Sets the value of the factoryClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFactoryClass(String value) {
        this.factoryClass = value;
    }

}