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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CommandInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommandInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://model.cluster.jonschang.com/Cluster}CommandId"/>
 *         &lt;element name="request" type="{http://model.cluster.jonschang.com/Cluster}CommandRequest"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="result" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="runTime" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="startedTime" type="{http://www.w3.org/2001/XMLSchema}date" />
 *       &lt;attribute name="status">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="running"/>
 *             &lt;enumeration value="complete"/>
 *             &lt;enumeration value="error"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommandInfo", propOrder = {
    "id",
    "request",
    "error",
    "result"
})
public class CommandInfo {

    @XmlElement(required = true)
    protected CommandId id;
    @XmlElement(required = true)
    protected CommandRequest request;
    protected String error;
    protected String result;
    @XmlAttribute
    protected Long runTime;
    @XmlAttribute
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar startedTime;
    @XmlAttribute
    protected String status;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link CommandId }
     *     
     */
    public CommandId getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandId }
     *     
     */
    public void setId(CommandId value) {
        this.id = value;
    }

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link CommandRequest }
     *     
     */
    public CommandRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandRequest }
     *     
     */
    public void setRequest(CommandRequest value) {
        this.request = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
    }

    /**
     * Gets the value of the runTime property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRunTime() {
        return runTime;
    }

    /**
     * Sets the value of the runTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRunTime(Long value) {
        this.runTime = value;
    }

    /**
     * Gets the value of the startedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartedTime() {
        return startedTime;
    }

    /**
     * Sets the value of the startedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartedTime(XMLGregorianCalendar value) {
        this.startedTime = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

}
