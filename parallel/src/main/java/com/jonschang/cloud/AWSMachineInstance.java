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

package com.jonschang.cloud;

import java.text.*;

import com.amazonaws.ec2.model.*;

public class AWSMachineInstance extends MachineInstanceAdapter<AWSCloudService> {
	private String instanceId;
	private String imageId;
	private Type instanceType;
	private String keyName;
	
	public void setKeyName( String name ) {
		keyName = name;
	}
	public String getKeyName() {
		return keyName;
	}
	
	// m1.small | m1.large | m1.xlarge | c1.medium | c1.xlarge | m2.2xlarge | m2.4xlarge
	public enum Type {
		M1_SMALL("m1.small"),
		M1_LARGE("m1.large"),
		M1_XLARGE("m1.xlarge"),
		C1_MEDIUM("c1.medium"),
		C1_XLARGE("c1.xlarge"),
		M2_2XLARGE("m2.2xlarge"),
		M2_4XLARGE("m2.4xlarge");
		private String actualName;
		Type(String name) {
			actualName = name;
		}
		public String toString() {
			return actualName;
		}
		static public Type fromString(String name) {
			String actualName = name.replace('.', '_').toUpperCase();
			return Enum.valueOf(AWSMachineInstance.Type.class, actualName);
		}
	}
	
	public void setInstanceType(Type type) {
		instanceType = type;
	}
	public Type getInstanceType() {
		return instanceType;
	}
	
	public void setInstanceId(String id) {
		instanceId = id;
	}
	public String getInstanceId() {
		return instanceId;
	}
	
	public void setImageId(String id) {
		imageId = id;
	}
	public String getImageId() {
		return imageId;
	}
	
	/**
	 * Must be able to clone AWSMachineInstance's to help avoid concurrency issues.
	 */
	@Override @SuppressWarnings("unchecked")
	public AWSMachineInstance clone() {
		AWSMachineInstance toRet = new AWSMachineInstance();
		
		toRet.setPrivateDns( getPrivateDns() );
		toRet.setPrivateIp( getPrivateIp() );
		toRet.setPublicDns( getPublicDns() );
		toRet.setStatus( getStatus() );
		toRet.setStartTime( (java.util.Date)getStartTime().clone() );
		
		toRet.setInstanceId( getInstanceId() );
		toRet.setImageId( getImageId() );
		toRet.setInstanceType( getInstanceType() );
		toRet.setKeyName( getKeyName() );
		
		return toRet;
	}
	
	/**
	 * Interpret the status code returned by AWS. 
	 * @param code The AWS instance status code.
	 * @return The MachineStatus enum value corresponding to the AWS status code.
	 * @throws UnrecognizedMachineStatusException
	 */
	static public MachineStatus interpretStatusCode(int code) throws UnrecognizedMachineStatusException {
		// "pending"
		if( code == 0 ) 
			return MachineStatus.PROVISIONING;
		
		// "running"
		else if( code == 16 ) 
			return MachineStatus.RUNNING;
		
		// "shutting-down"
		else if( code==32 ) 
			return MachineStatus.DECOMMISSIONING;
		
		// "terminated"
		else if( code==48 ) 
			return MachineStatus.DECOMMISSIONED;
		
		throw new UnrecognizedMachineStatusException("The AWS status code "+code+" is not recognized as a valid MachineStatus.");
	}
	
	/**
	 * Copy data from the AWS RunningInstance model element into the AWSMachineInstance object.
	 * @param targetInstance The AWSMachineInstance to overwrite properties in.
	 * @param instance The RunningInstance object to copy from.
	 * @throws CloudException
	 */
	static public void copyFromRunningInstance(AWSMachineInstance targetInstance, RunningInstance instance) throws CloudException {
		
		// first the aws specific properties 
		
		targetInstance.setKeyName(instance.getKeyName());
		targetInstance.setInstanceId(instance.getInstanceId());
		targetInstance.setImageId(instance.getImageId());
		targetInstance.setInstanceType(Type.fromString(instance.getInstanceType()));

		// now take care of all the common properties
		
		// the following three properties will be empty after calling decommission()
		// we want to make sure that we don't overwrite what they were
		if( instance.getPrivateDnsName()!=null && instance.getPrivateDnsName().length()>0 )
			targetInstance.setPrivateDns(instance.getPrivateDnsName());
		if( instance.getPrivateIpAddress()!=null && instance.getPrivateIpAddress().length()>0 )
			targetInstance.setPrivateIp(instance.getPrivateIpAddress());
		if( instance.getPublicDnsName()!=null && instance.getPublicDnsName().length()>0 )
			targetInstance.setPublicDns(instance.getPublicDnsName());
		
		targetInstance.setStatus(interpretStatusCode(instance.getInstanceState().getCode()));

		String [] dateParts = instance.getLaunchTime().split("T");
		String launchTime = "";
		if( dateParts.length==2 )
			launchTime = dateParts[0]+" "+dateParts[1].substring( 0 , dateParts[1].length() - 1 );
		try {
			targetInstance.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(launchTime));
		} catch( ParseException pe ) {
			throw new CloudException("Parsing "+instance.getLaunchTime()+" cause the DateFormat class to throw a ParseException",pe);
		}
	}
}
