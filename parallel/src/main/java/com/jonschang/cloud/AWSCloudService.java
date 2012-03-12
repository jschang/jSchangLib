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

import java.util.*;
import java.net.*;

import org.apache.log4j.*;
import com.amazonaws.ec2.*;
import com.amazonaws.ec2.model.*;
import com.jonschang.utils.*;

/**
 * The Amazon WebServices EC2 Cloud Service.
 * 
 * Looks at the EC2_SECRET_KEY and EC2_ACCESS_KEY environment variables.
 * You can also use the accessor methods to set these, though.
 * 
 * @author schang
 *
 */
public class AWSCloudService implements CloudService<AWSMachineInstance> {
	
	/**
	 * Provides access to the AWSCloudService singleton
	 * @return
	 * @throws CloudException
	 */
	synchronized static public AWSCloudService instance() throws CloudException {
		if( instance==null ) {
			instance = new AWSCloudService();
		}
		return instance;
	}
	
	/**
	 * Set the EC2 access-key.  Required.
	 * @param key
	 */
	public void setAccessKey(String key) {
		ec2AccessKey = key;
	}
	public String getAccessKey() {
		return ec2AccessKey;
	}
	private String ec2AccessKey=null;
	
	/**
	 * Set the EC2 secret-key.  Required.
	 * @param key
	 */
	public void setSecretKey(String key) {
		ec2SecretKey = key;
	}
	public String getSecretKey() {
		return ec2SecretKey;
	}
	private String ec2SecretKey=null;
	
	/**
	 * Sets the default key-pair name to provision instances with
	 * @param keyPairName
	 */
	public void setDefaultKeyPairName(String keyPairName) {
		ec2KeyPairName = keyPairName;
	}
	public String getDefaultKeyPairName() {
		return ec2KeyPairName;
	}
	private String ec2KeyPairName=null;
	
	/**
	 * Add a machine instance to the decommissioning-watch list.
	 * A machine that does not change to a DECOMISSIONED status
	 * after a certain period will trigger an error-handler
	 * @param instance
	 */
	public void watchForDecommissioning(AWSMachineInstance instance) {
		synchronized(watchForDecommissionedStatus) {
			this.watchForDecommissionedStatus.add(instance);
		}
	}
	private List<AWSMachineInstance> watchForDecommissionedStatus = new ArrayList<AWSMachineInstance>();
	
	/* ******************** *
	 * OVERRIDES ONLY BELOW *
	 * ******************** */
	
	/**
	 * Determined by private Ip at instantiation
	 */
	private AWSMachineInstance myInstance = null;
	@Override public synchronized AWSMachineInstance getMyInstance() throws CloudException {
		
		// pull this machine instance's status and information from the cloud service
		myInstance = new AWSMachineInstance();
		try {
	        InetAddress addr = InetAddress.getLocalHost();
	        myInstance.setPrivateIp( addr.getHostAddress() );
	    } catch (UnknownHostException e) {
	    	throw new CloudException("Unable to determine local host ip address.", e);
	    }
		myInstance = status(myInstance);
		
		return myInstance;
	}
	
	@Override public synchronized void decommission(AWSMachineInstance instance) throws CloudException {
		verifyRequirements();
		AmazonEC2Client client = new AmazonEC2Client( ec2AccessKey, ec2SecretKey );
		TerminateInstancesRequest request = new TerminateInstancesRequest();
		request.getInstanceId().add(instance.getInstanceId());
		try {
			client.terminateInstances(request);
		} catch( AmazonEC2Exception e ) {
			throw new CloudException("An exception was throw terminating the instance "+instance.getInstanceId(),e);
		}
		// TODO: validate the response
		// TODO: add the instance to the watchForDecommissionedStatus list
	}

	@Override public synchronized AWSMachineInstance provision(AWSMachineInstance instance) throws CloudException {
	
		// validate that the access and secret keys have been set
		verifyRequirements();
		
		// we require an image id and instance type
		if( instance.getImageId()==null || instance.getInstanceType()==null )
			throw new CloudException("You must at least fill in the imageId and instanceType properties of the instance.");
		
		AmazonEC2Client client = new AmazonEC2Client( ec2AccessKey, ec2SecretKey );
		
		try {
			RunInstancesRequest request = new RunInstancesRequest();
			
			request.setImageId( instance.getImageId() );
			
			if( instance.getInstanceType()!=null )
				request.setInstanceType( instance.getInstanceType().toString() );
			else request.setInstanceType( AWSMachineInstance.Type.M1_SMALL.toString() );
			
			if( instance.getKeyName()!=null )
				request.setKeyName( instance.getKeyName() );
			else if( ec2KeyPairName!=null )
				request.setKeyName( ec2KeyPairName );
			
			// provision is only currently setup to provision a single instance
			// at a time
			request.setMinCount(1);
			request.setMaxCount(1);
			
			RunInstancesResponse response = client.runInstances( request );
			if( 
				response.getRunInstancesResult()==null
				|| response.getRunInstancesResult().getReservation()==null
				|| response.getRunInstancesResult().getReservation().getRunningInstance()==null
				|| response.getRunInstancesResult().getReservation().getRunningInstance().size()!=1 
			) {
				throw new CloudException("The RunInstancesResponse object did not contain the expected properties.");
			}
			
			RunningInstance inst = response.getRunInstancesResult().getReservation().getRunningInstance().get(0);
			AWSMachineInstance newInstance = new AWSMachineInstance();
			AWSMachineInstance.copyFromRunningInstance(newInstance,inst);
			return newInstance;
		} catch( Exception e ) {
			throw new ProvisioningException("An exception occurred provisioning a MachineInstance from Amazon EC2",e);
		} 
	}

	@Override public synchronized AWSMachineInstance status(AWSMachineInstance instance) throws CloudException {
		List<AWSMachineInstance> inst = new ArrayList<AWSMachineInstance>();
		inst.add(instance);
		AWSMachineInstance toRet = null;
		try {
			toRet = status(inst).get(0);
		} catch( IndexOutOfBoundsException ioobe ) {
			throw new InstanceNotFoundException("It appears Amazon isn't aware of the instance id "+instance.getInstanceId(),ioobe);
		}
		return toRet;
	}

	/* **************** *
	 * PRIVATE METHODS  *
	 * **************** */
	
	static private AWSCloudService instance;
	private AWSCloudService() throws CloudException {
		ec2AccessKey = System.getenv("EC2_ACCESS_KEY");
		ec2SecretKey = System.getenv("EC2_SECRET_KEY");
	}
	
	/**
	 * Just checks to make sure the EC2 Access and Secret keys have been set
	 * @throws CloudException
	 */
	private void verifyRequirements() throws CloudException {
		List<String> chastize = new ArrayList<String>();
		
		if( ec2AccessKey == null )
			chastize.add("AWSCloudService needs your EC2 Access key.");
		if( ec2SecretKey == null )
			chastize.add("AWSCloudService needs your EC2 Secret Key.");
		if( chastize.size()>0 ) {
			Logger.getLogger(this.getClass()).error(chastize.toArray());
			throw new CloudException(""+chastize.toArray());
		}
		
		if( ec2KeyPairName == null )
			Logger.getLogger(this.getClass()).warn("Use setDefaultKeyPairName() to assign a default key-pair name to the instances provisioned.");
	}

	/**
	 * Pulls the status information for a list of AWSMachineInstances
	 * 
	 * I'm deciding whether to pull this down to the interface contract.
	 * It doesn't verify that it actually get's information for each 
	 * instance requested.  Insuring that each item in the return list
	 * is in the passed in list is up to the caller.  The list returned
	 * shares no objects with the list passed in.
	 * 
	 * @param instances
	 * @return
	 * @throws CloudException
	 */
	private List<AWSMachineInstance> status(List<AWSMachineInstance> instances) throws CloudException {
		
		verifyRequirements();
		
		List<AWSMachineInstance> toRet = new ArrayList<AWSMachineInstance>();
		
		// each of the instances to check must have it's instance id filled in
		// unless there is only one instance to check and it's ip is filled in
		for( AWSMachineInstance instance : instances )
			if( instance.getInstanceId()==null ) {
				if( instance.getPrivateIp()!=null && instances.size()==1 )
					toRet.add(statusByIp(instance));
				throw new CloudException("You must at least fill in the instanceId property of the instance to query.");
			}
		
		AmazonEC2Client client = new AmazonEC2Client( ec2AccessKey, ec2SecretKey );
		
		try {
			List<String> instanceIds = new ArrayList<String>();
			Map<String,AWSMachineInstance> idToInstMap = new HashMap<String,AWSMachineInstance>(); 
			for( AWSMachineInstance instance : instances ) {
				idToInstMap.put( instance.getInstanceId(), instance );
				instanceIds.add( instance.getInstanceId() );
			}
			
			DescribeInstancesRequest req = new DescribeInstancesRequest();
			req.setInstanceId(instanceIds);
			
			DescribeInstancesResponse resp = client.describeInstances( req );

			// verify that we got the information we wanted
			if( resp.getDescribeInstancesResult()==null	|| resp.getDescribeInstancesResult().getReservation()==null	)
				throw new CloudException("The DescribeInstancesResult object did not contain the expected properties.");
			
			for( Reservation reservation : resp.getDescribeInstancesResult().getReservation() ) {
				for( RunningInstance instance : reservation.getRunningInstance() ) {
					AWSMachineInstance thisOne = idToInstMap.get(instance.getInstanceId()).clone();
					AWSMachineInstance.copyFromRunningInstance(thisOne,instance);
					toRet.add(thisOne);
				}
			}
			
			return toRet; 
		} catch( AmazonEC2Exception e ) {
			throw new CloudException("Was unable to determine status of the machine instance "+instance,e);
		}
	}
	
	/**
	 * If the instanceId is not set, but the privateIp address is, then the status() method falls back to this
	 * @param inst
	 * @return
	 * @throws CloudException, InstanceNotFoundException
	 */
	private AWSMachineInstance statusByIp(AWSMachineInstance inst) throws CloudException, InstanceNotFoundException {
		AmazonEC2Client client = new AmazonEC2Client( ec2AccessKey, ec2SecretKey );
		try {
			DescribeInstancesResponse resp = client.describeInstances(null);
			DescribeInstancesResult res = resp.getDescribeInstancesResult();
			if( res!=null && res.getReservation()!=null && res.getReservation().size()>0 ) {
				for( Reservation reservation : res.getReservation() ) {
					if( reservation.getRunningInstance()!=null && reservation.getRunningInstance().size()>0 )
						for( RunningInstance runningInst : reservation.getRunningInstance() ) {
							if( runningInst.getPrivateIpAddress().compareTo(inst.getPrivateIp()) ==0 ) {
								AWSMachineInstance targetInstance = inst.clone();
								AWSMachineInstance.copyFromRunningInstance(targetInstance, runningInst);
								return targetInstance;
							}
						}
				}
			}
		} catch( AmazonEC2Exception aee ) {
			throw new CloudException(aee);
		}
		throw new InstanceNotFoundException("The instance with a private ip address "+inst.getPrivateIp()+" was not found.");
	}

	/**
	 * Continually checks to make sure that instances don't get hung in decommissioning.
	 * 
	 * Currently is not implemented and I'm not sure if I will make it functional or not.
	 * @author schang
	 */
	private class DecommissionedChecker implements Runnable {
		public void run() {
			while(true) {
				
				// it's possible someone could add an instance to the watch list
				// during iteration...we want to avoid that, but we don't 
				// want to prevent additions during status updates.  That would
				// take too long, so we'll just make a copy of the array first
				// and then allow additions to the original list
				List<AWSMachineInstance> watchList = new ArrayList<AWSMachineInstance>();
				synchronized(watchForDecommissionedStatus) {
					for( AWSMachineInstance node : watchForDecommissionedStatus ) 
						watchList.add(node);
				}
					
				if( watchList.size()>0 ) {
					Map<AWSMachineInstance,Exception> exceptions = new HashMap<AWSMachineInstance,Exception>();
					List<AWSMachineInstance> instancesStatus = new ArrayList<AWSMachineInstance>();
					try {
						 instancesStatus = status(watchList);
					} catch( CloudException ce ) {
						Logger.getLogger(this.getClass()).error( StringUtils.stackTraceToString(ce) );
					}
					
					// evaluate each instance to watch for decomissioned status
					for( AWSMachineInstance instance : instancesStatus ) {
						// if the instance was scheduled for decommissioning a longer time ago than the threshold
						// then kick-off an error handler call-back thread
					}
				}

				// we don't want to piss aws off with too many checks
				// 1 time every 15 seconds is adequate.
				// TODO: this interval should be configurable
				try { Thread.sleep(15000); } 
				catch( InterruptedException e) {}
			}
		}
	}
}
