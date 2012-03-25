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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for NodeInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NodeInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="nodeId" type="{http://model.cluster.jonschang.com/Cluster}NodeId"/>
 *         &lt;element name="rootNodeId" type="{http://model.cluster.jonschang.com/Cluster}NodeId"/>
 *         &lt;element name="parentNodeId" type="{http://model.cluster.jonschang.com/Cluster}NodeId"/>
 *         &lt;element name="childNodes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="nodeId" type="{http://model.cluster.jonschang.com/Cluster}NodeId" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="serviceInfo">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;any/>
 *                 &lt;/choice>
 *                 &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="cpuUsage" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="memUsage" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="swapUsage" type="{http://model.cluster.jonschang.com/Cluster}ThreeFloatUsageInfo"/>
 *         &lt;element name="commands">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="command" type="{http://model.cluster.jonschang.com/Cluster}CommandInfo" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *       &lt;attribute name="started" use="required" type="{http://www.w3.org/2001/XMLSchema}date" />
 *       &lt;attribute name="nodeCount" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="depth" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeInfo", propOrder = {

})
public class NodeInfo {

    @XmlElement(required = true)
    protected NodeId nodeId;
    @XmlElement(required = true)
    protected NodeId rootNodeId;
    @XmlElement(required = true)
    protected NodeId parentNodeId;
    @XmlElement(required = true)
    protected NodeInfo.ChildNodes childNodes;
    @XmlElement(required = true)
    protected NodeInfo.ServiceInfo serviceInfo;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo cpuUsage;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo memUsage;
    @XmlElement(required = true)
    protected ThreeFloatUsageInfo swapUsage;
    @XmlElement(required = true)
    protected NodeInfo.Commands commands;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar started;
    @XmlAttribute(required = true)
    protected long nodeCount;
    @XmlAttribute(required = true)
    protected BigInteger depth;

    /**
     * Gets the value of the nodeId property.
     * 
     * @return
     *     possible object is
     *     {@link NodeId }
     *     
     */
    public NodeId getNodeId() {
        return nodeId;
    }

    /**
     * Sets the value of the nodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeId }
     *     
     */
    public void setNodeId(NodeId value) {
        this.nodeId = value;
    }

    /**
     * Gets the value of the rootNodeId property.
     * 
     * @return
     *     possible object is
     *     {@link NodeId }
     *     
     */
    public NodeId getRootNodeId() {
        return rootNodeId;
    }

    /**
     * Sets the value of the rootNodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeId }
     *     
     */
    public void setRootNodeId(NodeId value) {
        this.rootNodeId = value;
    }

    /**
     * Gets the value of the parentNodeId property.
     * 
     * @return
     *     possible object is
     *     {@link NodeId }
     *     
     */
    public NodeId getParentNodeId() {
        return parentNodeId;
    }

    /**
     * Sets the value of the parentNodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeId }
     *     
     */
    public void setParentNodeId(NodeId value) {
        this.parentNodeId = value;
    }

    /**
     * Gets the value of the childNodes property.
     * 
     * @return
     *     possible object is
     *     {@link NodeInfo.ChildNodes }
     *     
     */
    public NodeInfo.ChildNodes getChildNodes() {
        return childNodes;
    }

    /**
     * Sets the value of the childNodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeInfo.ChildNodes }
     *     
     */
    public void setChildNodes(NodeInfo.ChildNodes value) {
        this.childNodes = value;
    }

    /**
     * Gets the value of the serviceInfo property.
     * 
     * @return
     *     possible object is
     *     {@link NodeInfo.ServiceInfo }
     *     
     */
    public NodeInfo.ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    /**
     * Sets the value of the serviceInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeInfo.ServiceInfo }
     *     
     */
    public void setServiceInfo(NodeInfo.ServiceInfo value) {
        this.serviceInfo = value;
    }

    /**
     * Gets the value of the cpuUsage property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Sets the value of the cpuUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setCpuUsage(ThreeFloatUsageInfo value) {
        this.cpuUsage = value;
    }

    /**
     * Gets the value of the memUsage property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getMemUsage() {
        return memUsage;
    }

    /**
     * Sets the value of the memUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setMemUsage(ThreeFloatUsageInfo value) {
        this.memUsage = value;
    }

    /**
     * Gets the value of the swapUsage property.
     * 
     * @return
     *     possible object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public ThreeFloatUsageInfo getSwapUsage() {
        return swapUsage;
    }

    /**
     * Sets the value of the swapUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreeFloatUsageInfo }
     *     
     */
    public void setSwapUsage(ThreeFloatUsageInfo value) {
        this.swapUsage = value;
    }

    /**
     * Gets the value of the commands property.
     * 
     * @return
     *     possible object is
     *     {@link NodeInfo.Commands }
     *     
     */
    public NodeInfo.Commands getCommands() {
        return commands;
    }

    /**
     * Sets the value of the commands property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeInfo.Commands }
     *     
     */
    public void setCommands(NodeInfo.Commands value) {
        this.commands = value;
    }

    /**
     * Gets the value of the started property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStarted() {
        return started;
    }

    /**
     * Sets the value of the started property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStarted(XMLGregorianCalendar value) {
        this.started = value;
    }

    /**
     * Gets the value of the nodeCount property.
     * 
     */
    public long getNodeCount() {
        return nodeCount;
    }

    /**
     * Sets the value of the nodeCount property.
     * 
     */
    public void setNodeCount(long value) {
        this.nodeCount = value;
    }

    /**
     * Gets the value of the depth property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDepth() {
        return depth;
    }

    /**
     * Sets the value of the depth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDepth(BigInteger value) {
        this.depth = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="nodeId" type="{http://model.cluster.jonschang.com/Cluster}NodeId" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "nodeId"
    })
    public static class ChildNodes {

        @XmlElement(required = true)
        protected List<NodeId> nodeId;

        /**
         * Gets the value of the nodeId property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the nodeId property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNodeId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link NodeId }
         * 
         * 
         */
        public List<NodeId> getNodeId() {
            if (nodeId == null) {
                nodeId = new ArrayList<NodeId>();
            }
            return this.nodeId;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="command" type="{http://model.cluster.jonschang.com/Cluster}CommandInfo" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *       &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "command"
    })
    public static class Commands {

        @XmlElement(required = true)
        protected List<CommandInfo> command;
        @XmlAttribute(required = true)
        protected BigInteger count;

        /**
         * Gets the value of the command property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the command property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCommand().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CommandInfo }
         * 
         * 
         */
        public List<CommandInfo> getCommand() {
            if (command == null) {
                command = new ArrayList<CommandInfo>();
            }
            return this.command;
        }

        /**
         * Gets the value of the count property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getCount() {
            return count;
        }

        /**
         * Sets the value of the count property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setCount(BigInteger value) {
            this.count = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;any/>
     *       &lt;/choice>
     *       &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class ServiceInfo {

        @XmlAnyElement(lax = true)
        protected Object any;
        @XmlAttribute(required = true)
        protected String service;

        /**
         * Gets the value of the any property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getAny() {
            return any;
        }

        /**
         * Sets the value of the any property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setAny(Object value) {
            this.any = value;
        }

        /**
         * Gets the value of the service property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getService() {
            return service;
        }

        /**
         * Sets the value of the service property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setService(String value) {
            this.service = value;
        }

    }

}