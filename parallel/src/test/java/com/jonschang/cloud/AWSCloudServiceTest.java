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

import org.apache.log4j.*;
import org.junit.*;

/**
 * Provisions and decommissions a small instance in AmazonAWS EC2
 * using the AWSCloudService and associated classes.
 *  
 * Note: this test costs the price of a small instance for a single hour.
 * EC2_SECRET_KEY and EC2_ACCESS_KEY must be set in order to run this test. 
 */
public class AWSCloudServiceTest {

	@Test public void testDateParsing() {
		String time = "2009-11-28T16:32:56.000Z";
		String [] dateParts = time.split("T");
		String launchTime = "";
		if( dateParts.length==2 )
			launchTime = dateParts[0]+" "+dateParts[1].substring( 0 , dateParts[1].length() - 1 );
		Logger.getLogger(this.getClass()).info(launchTime);
	}
	
	@Test public void testFull() throws Exception {
		
		// NOTE: uncomment this if you actually want to spend money on the test.
		if( 1==1 ) return;
		
		AWSCloudService service = AWSCloudService.instance();
		service.setDefaultKeyPairName("gsg-keypair");
		
		AWSMachineInstance instance = new AWSMachineInstance();
		instance.setImageId("ami-1515f67c");
		instance.setInstanceType(AWSMachineInstance.Type.M1_SMALL);
		
		instance = service.provision(instance);
		Logger.getLogger(this.getClass()).info("Provisioned a new instance: "+instance.getInstanceId()+","+instance.getStatus()+" starting at "+instance.getStartTime());
		
		instance = service.status(instance);
		Assert.assertTrue( instance.getStatus()==MachineStatus.PROVISIONING );
		
		while( instance.getStatus()==MachineStatus.PROVISIONING ) {
			Thread.sleep(2500);
			instance = service.status(instance);
			Logger.getLogger(this.getClass()).info("instance "+instance.getInstanceId()+" has status "+instance.getStatus());
		}
		Assert.assertNotNull(instance);
		Assert.assertTrue(instance.getImageId().compareTo("ami-1515f67c")==0);
		Assert.assertTrue(instance.getStartTime()!=null);
		Assert.assertTrue(instance.getStatus()==MachineStatus.RUNNING);
		Assert.assertTrue(instance.getPrivateIp()!=null && instance.getPrivateIp().matches("\\d{0,3}(\\.\\d{0,3}){3}") );
		Assert.assertTrue(instance.getPrivateDns()!=null && instance.getPrivateDns().length()>0);
		Assert.assertTrue(instance.getPublicDns()!=null && instance.getPublicDns().length()>0);
		Assert.assertTrue(instance.getInstanceType()==AWSMachineInstance.Type.M1_SMALL);
		
		service.decommission(instance);
		instance = service.status(instance);
		Assert.assertNotNull(instance);
		Assert.assertTrue(instance.getImageId().compareTo("ami-1515f67c")==0);
		Assert.assertTrue(instance.getStartTime()!=null);
		Assert.assertTrue(instance.getStatus()==MachineStatus.DECOMMISSIONING);
		Assert.assertTrue(instance.getPrivateIp()!=null && instance.getPrivateIp().matches("\\d{0,3}(\\.\\d{0,3}){3}") );
		Assert.assertTrue(instance.getPrivateDns()!=null && instance.getPrivateDns().length()>0);
		Assert.assertTrue(instance.getPublicDns()!=null && instance.getPublicDns().length()>0);
		Assert.assertTrue(instance.getInstanceType()==AWSMachineInstance.Type.M1_SMALL);
		
		
		Logger.getLogger(this.getClass()).info("instance decommissioning has status "+instance.getStatus());
		while( instance.getStatus()==MachineStatus.DECOMMISSIONING ) {
			instance = service.status(instance);
			Logger.getLogger(this.getClass()).info("instance "+instance.getInstanceId()+" has status "+instance.getStatus());
			Thread.sleep(2500);
		}
		
		Assert.assertNotNull(instance);
		Assert.assertTrue(instance.getImageId().compareTo("ami-1515f67c")==0);
		Assert.assertTrue(instance.getStartTime()!=null);
		Assert.assertTrue(instance.getStatus()==MachineStatus.DECOMMISSIONED);
		Assert.assertTrue(instance.getPrivateIp()!=null && instance.getPrivateIp().matches("\\d{0,3}(\\.\\d{0,3}){3}") );
		Assert.assertTrue(instance.getPrivateDns()!=null && instance.getPrivateDns().length()>0);
		Assert.assertTrue(instance.getPublicDns()!=null && instance.getPublicDns().length()>0);
		Assert.assertTrue(instance.getInstanceType()==AWSMachineInstance.Type.M1_SMALL);
	}
}
