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

package com.jonschang.cluster;

import java.util.*;
import java.util.concurrent.*;
import java.math.*;

import javax.xml.ws.*;

import org.apache.log4j.Logger;

import com.jonschang.cluster.model.*;
import com.jonschang.cluster.event.*;
import com.jonschang.utils.*;

/**
 * The concrete implementation of ClusterNodeWS
 * 
 * This class demands that the following environment variables be set:
 * 
 *  - JONSCHANG_ROOT : the system-path where the application lives.
 *  This is used to locate the continuous monitoring scripts.
 * 
 * @author schang
 */
public class ImplClusterNodeWS implements ClusterNodeWSServer {
	
	private String nodeSecretKey = "DEFAULT_NODE_SECRET_KEY";
	private String userSecretKey = "DEFAULT_USER_SECRET_KEY";
	
	/**
	 * Contains the node information about this node.
	 * 
	 * Used mostly for determining whether this instance is
	 * too busy to perform non-node-coordinating work.
	 * 
	 * Remember to copy non-usage information to aggregateNodeInfo when changing this.
	 */
	private NodeInfo nodeInfo = ClusterFactory.newNodeInfo();
	
	/**
	 * Contains the average usage statistics for this node and descendents.
	 * Be careful to copy non-usage information to this when updating nodeInfo.
	 */
	private NodeInfo aggregateNodeInfo = null;
	
	/**
	 * A map of Url/NodeInfo pairs.
	 * 
	 * Most of these will have been provisioned by this instance.
	 */
	private Map<String,NodeInfo> childNodes = new HashMap<String,NodeInfo>();
	
	/**
	 * Configuration for this node.  Will at least be populated by defaults.
	 */
	private NodeConfiguration configuration = null;
	
	/**
	 * An interface to the means of node provision() and decommission() services
	 */
	private ClusterNodeService clusterNodeService = null;
	
	/**
	 * Runs pretty much any runnable we create in this class
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * A map of Uuid/CommandExecutor pairs
	 */
	private Map<String,CommandExecutor> commandsRunning = new HashMap<String,CommandExecutor>();
	
	/**
	 * The current NodeStatus of the ClusterNodeWS
	 */
	private ThreadSafeValue<NodeStatus> myStatus = new ThreadSafeValue<NodeStatus>();
	
	/**
	 * Maps ClusterEventType's to the ClusterEventHandler's that take care of them
	 */
	private Map<ClusterEventType,List<ClusterEventHandler>> handlers = new HashMap<ClusterEventType,List<ClusterEventHandler>>();

	/**
	 * Create a new instance of ImplClusterNodeWS
	 * 
	 * Kicks off the aggregate and instance node monitoring threads.
	 * Records what time the web-service object was created
	 * 
	 * Sets up the NodeId for this Node to have the url passed into
	 * the constructor, except the string "localhost" is replaced
	 * with the return from InetAddress.getLocalHost().getHostAddress().
	 * 
	 * @throws ClusterException
	 */
	public ImplClusterNodeWS(String url) throws ClusterException {
		myStatus.value(NodeStatus.AVAILABLE);
		try {
			nodeInfo.setStarted( ClusterFactory.newXMLGregorianCalendar() );
			nodeInfo.getNodeId().setUuid( java.util.UUID.randomUUID().toString() );
			nodeInfo.setDepth(BigInteger.valueOf(0L));
			
			if( clusterNodeService!=null ) {
				clusterNodeService.serviceInfo(nodeInfo); 
			} else Logger.getLogger(this.getClass()).warn("No ClusterNodeService is set.  Running this as a single node?  That's sort of weird. =|");
			
			java.net.InetAddress addy = java.net.InetAddress.getLocalHost();
			nodeInfo.getNodeId().setUrl( url.replace("localhost", addy.getHostAddress() ) );
		} catch( Exception e ) {
			Logger.getLogger(this.getClass()).error(StringUtils.stackTraceToString(e));
			throw new ClusterException(e);
		}
	
		aggregateNodeInfo = ClusterModelTools.cloneNodeInfo(nodeInfo);
		setupConfigurationDefaults();
		executor.execute(new NodeStatsMonitor());
		executor.execute(new AggregateNodeStatsMonitor());
	}
	
	@Override public void terminateAllChildren() throws NodeBusyException, NodeTerminatingException, ClusterException {
		List<NodeInfo> childsCopy = null;
		synchronized(childNodes) {
			childsCopy = ClusterModelTools.copyNodeInfoCollection(childNodes.values());
		}
		for( NodeInfo node : childsCopy ) {
			try {
				ClusterNodeWS client = ClusterFactory.newClient(node.getNodeId().getUrl());
				client.terminate(newAuthorization());
			} catch( AuthenticationException ae ) {
				throw new ClusterException(ae);
			} catch( WebServiceException e ) {
				// no response?  cool.  the service may have already shut itself down.
				// TODO: check to make sure the node removed itself from the childNodes...else trigger an ERROR_ type event
			}
		}
	}
	@Override public int childCount() {
		synchronized(childNodes) {
			return childNodes.size();
		}
	}
	@Override public boolean isExecutingCommands() {
		synchronized(commandsRunning) {
			for( CommandExecutor exec : commandsRunning.values() )
				if( exec.isRunning() )
					return true;
		}
		return false;
	}
	@Override public void execute(Runnable cmd) {
		executor.execute(cmd);
	}
	@Override public void setUserSecretKey(String key) {
		userSecretKey = key;
	}
	@Override public void setNodeSecretKey(String key) {
		nodeSecretKey = key;
	}
	@Override public NodeStatus getNodeStatus() {
		return myStatus.value();
	}
	@Override public void setNodeStatus(NodeStatus status) {
		myStatus.value(status);
	}
	@Override public void setClusterNodeService(ClusterNodeService service) {
		clusterNodeService = service;
	}
	@Override public ClusterNodeService getClusterNodeService() {
		return clusterNodeService;
	}
	@Override public NodeInfo getNodeInfo() throws ClusterException {
		synchronized(aggregateNodeInfo) {
			return ClusterModelTools.cloneNodeInfo(aggregateNodeInfo);
		} 
	}
	@Override public NodeConfiguration getConfiguration() {
		return configuration;
	}
	@Override public Authorization newAuthorization() throws ClusterException {
		Authorization auth = new Authorization();
		Hasher hasher = new HMACSha1Hasher();
		String uuid = UUID.randomUUID().toString();
		try {
			synchronized(nodeSecretKey) {
				auth.setHash( hasher.calculate( uuid, nodeSecretKey ) );
			}
		} catch( Exception e ) {
			throw new ClusterException(e);
		}
		auth.setValue( uuid );
		return auth;
	}
	@Override public boolean authenticate(Authorization auth) throws ClusterException, AuthenticationException {
		Hasher hasher = new HMACSha1Hasher();
		try {
			synchronized(nodeSecretKey) {
				if( hasher.calculate(auth.getValue(), nodeSecretKey).compareTo(auth.getHash())==0 )
					return true;
			}
			synchronized(userSecretKey) {
				if( hasher.calculate(auth.getValue(), userSecretKey).compareTo(auth.getHash())==0 )
					return false;
			}
		} catch(Exception e) {
			throw new ClusterException(e);
		}
		throw new AuthenticationException("Authentication values passed in did not pass evaluation.");
	}
	@Override public synchronized void triggerEvent(ClusterEvent event) {
		Logger.getLogger(this.getClass()).trace(event.getType().toString()+" ClusterEvent has been triggered.");
		if( handlers.get(event.getType())!=null ) {
			for( ClusterEventHandler handler : handlers.get(event.getType()) ) {
				EventRunner runner = new EventRunner();
				runner.setHandlers(handler);
				runner.setEvent(event);
				executor.execute(runner);
			}
		} else Logger.getLogger(this.getClass()).warn("There are no handlers setup for the ClusterEvent "+event.getType().toString());
	}
	public synchronized void subscribe(List<ClusterEventType> events, ClusterEventHandler handler) {
		for( ClusterEventType event : events )
			subscribe(event,handler);
	}
	@Override public synchronized void subscribe(ClusterEventType event, ClusterEventHandler handler) {
		if( handlers.get(event)==null )
			handlers.put(event,new ArrayList<ClusterEventHandler>());
		handlers.get(event).add(handler);
	}
	@Override public synchronized void unsubscribe(ClusterEventType event, ClusterEventHandler handler) {
		if( handlers.get(event)==null )
			return;
		handlers.get(event).remove(handler);
	}	
	
	/*
	 * WEB METHOD OVERRIDES 
	 */
	
	@Override public NodeConfiguration getConfiguration(Authorization auth) throws ClusterException, AuthenticationException {
		authenticate(auth);
		synchronized(configuration) {
			return ClusterModelTools.copyConfiguration(new NodeConfiguration(), configuration);
		}
	}
	@Override public void setConfiguration(Authorization auth, NodeConfiguration config) throws ClusterException, AuthenticationException {
		authenticate(auth);
		synchronized(nodeInfo) {
			Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" Updating configuration and dissemminating to children.");
		}
		synchronized(configuration) {
			configuration = config;
		}
	}
	@Override public NodeInfo getNodeInfo(Authorization auth) throws ClusterException, AuthenticationException {
		authenticate(auth);
		NodeInfo n = null;
		synchronized(aggregateNodeInfo) {
			n = ClusterModelTools.cloneNodeInfo(aggregateNodeInfo);
		}
		return n;
	}
	@Override public void terminate(Authorization auth) throws ClusterException, AuthenticationException {
		authenticate(auth);
		
		// don't bother doing anything if we're already shutting down
		if( myStatus.value() == NodeStatus.SHUTTING_DOWN )
			return;
		
		// trigger the event and let the appropriate event handler manage the termination process
		triggerEvent( ClusterFactory.newEvent(ClusterEventType.NODE_TERMINATE_REQUEST) );
	}
	@Override public void notifyTermination(Authorization auth, TerminationNotification response) throws ClusterException, AuthenticationException {
		authenticate(auth);
		removeChildNode(response.getNodeId().getUrl());
		
		// kick off a method to watch for the node actually having been shut-down
		executor.execute(new Runnable() {
			private TerminationNotification response = null;
			public void run() {
				synchronized(nodeInfo) {
					Thread.currentThread().setName( nodeInfo.getNodeId().getUrl()+" ShutdownMonitor on "+response.getNodeId().getUrl() );
				}
				try {
					ClusterNodeWS client = ClusterFactory.newClient(response.getNodeId().getUrl());
					Boolean waiting = true;
					while( waiting ) {
						try {
							Logger.getLogger(this.getClass()).trace("waiting for node info to fail on "+response.getNodeId().getUrl());
							client.getNodeInfo(newAuthorization());
							Thread.sleep(1000);
							// TODO: add a triggerEvent for a ERROR_CHILD_TERMINATE_TIMEOUT
						} catch(WebServiceException e) {
							Logger.getLogger(this.getClass()).info("getting node info threw an EXPECTED exception in shutdown monitor "+StringUtils.stackTraceToString(e));
							waiting = false;
						}
					}
					Logger.getLogger(this.getClass()).info("Shutdown thread ending");
				} catch( Exception e ) {
					Logger.getLogger(this.getClass()).error("Monitoring a child service for shutdown, an exception was thrown: "+StringUtils.stackTraceToString(e));
					ErrorClusterEvent ece = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_SHUTDOWN_MONITOR);
					ece.setException(e);
					triggerEvent( ece );
				} finally {
					synchronized(nodeInfo) {
						Thread.currentThread().setName( "terminated - "+nodeInfo.getNodeId().getUrl()+" ShutdownMonitor on "+response.getNodeId().getUrl() );
					}
				}
			}
			public Runnable setParams(TerminationNotification response) { 
				this.response = response; 
				return this;
			}
		}.setParams(response));
	}
	@Override public CommandInfo executeCommand(Authorization auth, CommandRequest command) throws NodeTerminatingException, NodeBusyException, AuthenticationException, ClusterException {
		authenticate(auth);
		
		if( myStatus.value()==NodeStatus.BUSY )
			throw new NodeBusyException("This cluster node and it's children are too busy to accept new commands.");
		
		if( myStatus.value()==NodeStatus.SHUTTING_DOWN ) {
			throw new NodeTerminatingException("This cluster node is shutting down and not accepting new commands right now."); 
		}
		
		// throw a COMMAND_REQUESTED event
		this.triggerEvent(ClusterFactory.newEvent(ClusterEventType.COMMAND_REQUESTED));
		
		// get the least busy node among this node and immediate descendents
		NodeInfo leastBusy = getLeastBusyAvailableNode();
		Boolean notBusy = false;
		
		// determine if the least busy node is too busy to accept a request
		if( leastBusy!=null ) {
			List<ClusterEvent> events = evaluateNodeInfo(leastBusy);
			if( events.size()>0 )
				for( ClusterEvent event : events ) {
					if( event.getType() == ClusterEventType.THRESHOLD_CPU_LOW ) {
						notBusy = true;
						break;
					}
				}
			else notBusy = true;
			
			// if the least busy node is too busy to do work,
			// then set our node status to busy so and let the AggregateNodeStatsMonitor
			// handle flipping it back to AVAILABLE
			// ALSO, attempt to provision a new child, if we are allowed.
			if( notBusy == false ) {
				myStatus.value(NodeStatus.BUSY);
				throw new NodeBusyException("This node and descendents are all too busy to accept any new commands");
			}
		}

		if( notBusy ) {
			NodeId myNodeId = null;
			synchronized(nodeInfo) {
				myNodeId = ClusterModelTools.cloneNodeId( nodeInfo.getNodeId() );
			}
			
			// if the uuid is this node's uuid, then we'll be running the command locally
			if( leastBusy.getNodeId().getUuid().compareTo( myNodeId.getUuid() ) == 0 ) {
				
				CommandId toRet = new CommandId();
				toRet.setAcceptingnodeId( myNodeId );
				if( command.getOriginNodeId()!=null ) 
					toRet.setOriginNodeId( command.getOriginNodeId() );
				else toRet.setOriginNodeId( myNodeId );
				toRet.setUuid( java.util.UUID.randomUUID().toString() );
				
				CommandExecutor commandExec = null;
				if( command.getShellCommand()!=null ) {
					commandExec = new ShellCommandExecutor(command);
					commandExec.setClusterNodeWSServer(this);
					commandExec.setCommandId(toRet);
					executor.execute(commandExec);
				} else if( command.getFactoryMethodCommand()!=null ) {
					commandExec = new JVMCommandExecutor(command);
					commandExec.setClusterNodeWSServer(this);
					commandExec.setCommandId(toRet);
					executor.execute(commandExec);
				} else if( command.getSpringCommand()!=null ) {
					commandExec = new SpringCommandExecutor(command);
					commandExec.setClusterNodeWSServer(this);
					commandExec.setCommandId(toRet);
					executor.execute(commandExec);
				}
				
				synchronized(commandsRunning) {
					this.commandsRunning.put( toRet.getUuid(), commandExec );
				}
				
				CommandClusterEvent event = (CommandClusterEvent)ClusterFactory.newEvent(ClusterEventType.COMMAND_ACCEPTED);
				event.setAcceptingNode(getNodeInfo().getNodeId());
				event.setCommandExecutor(commandExec);
				event.setCommandRequest(command);
				triggerEvent(event);
				
				return commandExec.getCommandInfo();
			}
			
			// otherwise, we'll be proxying the command to a downstream node,
			// and only need to pass back the command id returned by the node
			// that eventually accepts the command
			else {
				if( command.getOriginNodeId()==null )
					synchronized( nodeInfo ) {
						command.setOriginNodeId( myNodeId );
					}
				ClusterNodeWS client = ClusterFactory.newClient( leastBusy.getNodeId().getUrl() );
				
				CommandClusterEvent event = (CommandClusterEvent)ClusterFactory.newEvent(ClusterEventType.COMMAND_PROXIED);
				event.setNodePassedTo(getNodeInfo().getNodeId());
				event.setCommandRequest(command);
				triggerEvent(event);
				
				// if the least busy node is too busy to accept the command,
				// then the exception will be passed upward from the most
				// leaf-level node the command reaches.
				return client.executeCommand(newAuthorization(),command);
			}
		}
		return null;
	}
	@Override public CommandInfo getCommandInfo(Authorization auth, CommandId commandId) throws ClusterException, AuthenticationException, CommandNotFoundException {
		authenticate(auth);
		CommandExecutor runningCommand;
		synchronized(commandsRunning) {
			runningCommand = commandsRunning.get(commandId.getUuid());
		}
		if( runningCommand!=null ) {
			synchronized(runningCommand) {
				CommandInfo toRet = runningCommand.getCommandInfo();
				// if the command is no longer running,
				// then the calling application should handle the return
				// and we don't need to keep it around.
				// any logging of the command should have been
				// performed either by the application requesting it
				// or the event handlers associated to the ERROR_COMMAND_FAILED
				// and COMMAND_ events.
				if( Enum.valueOf(CommandStatus.class, toRet.getStatus()) != CommandStatus.running ) {
					synchronized(commandsRunning) {
						commandsRunning.remove(commandId.getUuid());
					}
				}
				return toRet;
			}
		} else throw new CommandNotFoundException("The cluster command "+commandId.getUuid()+" was not found.");
	}
	@Override public void addChild(Authorization auth, NodeId nodeId) throws ClusterException, AuthenticationException {
		
		synchronized( nodeInfo ) {
			Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" adding a new child node "+nodeId.getUrl());
		}

		ClusterNodeWS client = ClusterFactory.newClient(nodeId.getUrl());
		
		// give the new node our configuration
		synchronized(configuration) {
			client.setConfiguration(newAuthorization(), configuration);
		}
		
		// set this node as the parent
		synchronized( nodeInfo ) {
			client.setParentNode(newAuthorization(),nodeInfo.getNodeId());
		}
		
		// get the node info from the node, and add the node as a child
		NodeInfo info = client.getNodeInfo(newAuthorization());
		addChildNode(info);
	}
	@Override public void setParentNode(Authorization auth, NodeId parentNode) throws ClusterException, AuthenticationException {
		authenticate(auth);		
		ClusterNodeWS client = ClusterFactory.newClient(parentNode.getUrl());
		NodeInfo parentInfo = client.getNodeInfo(newAuthorization());
		if( parentInfo.getNodeId()==null || parentInfo.getNodeId().getUrl()==null || parentInfo.getNodeId().getUrl().length()==0 )
			throw new ClusterException("The NodeInfo returned from the parent node did not contain a url.");
		synchronized(nodeInfo) {
			Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" setting parent node as "+parentInfo.getNodeId().getUrl());
			nodeInfo.setParentNodeId( ClusterModelTools.cloneNodeId(parentInfo.getNodeId()) );
			nodeInfo.setRootNodeId( ClusterModelTools.cloneNodeId(parentInfo.getRootNodeId()) );
			nodeInfo.setDepth( parentInfo.getDepth().add(BigInteger.valueOf(1L)) );
		}
	}
	
	/* *************** *
	 * PRIVATE METHODS *
	 * *************** */
	
	/**
	 * Initializes the default NodeConfiguration.  Values will be overridden by the parent node.
	 */
	private void setupConfigurationDefaults() {
		configuration = new NodeConfiguration();
		configuration.setCpuLow( new ThreeFloatUsageInfo() );
		configuration.getCpuLow().setOne(5);
		configuration.getCpuLow().setFive(5);
		configuration.getCpuLow().setFifteen(5);
		configuration.setCpuHigh( new ThreeFloatUsageInfo() );
		configuration.getCpuHigh().setOne(95);
		configuration.getCpuHigh().setFive(95);
		configuration.getCpuHigh().setFifteen(95);
		configuration.setMemHigh( new ThreeFloatUsageInfo() );
		configuration.getMemHigh().setOne(95);
		configuration.getMemHigh().setFive(95);
		configuration.getMemHigh().setFifteen(95);
		configuration.setSwapHigh( new ThreeFloatUsageInfo() );
		configuration.getSwapHigh().setOne(3000);
		configuration.getSwapHigh().setFive(3000);
		configuration.getSwapHigh().setFifteen(3000);
		configuration.setMaxChildren( (short)0 );
		configuration.setMaxNodeDepth( (short)0 );
		configuration.setMinLifetime( (short)3600 );
		configuration.setStatusUpdateInterval( 15000L );
	}
	
	/**
	 * Determine and return the NodeInfo object corresponding to the least 
	 * busy among this node and immediate children.
	 * 
	 * @return The NodeInfo object corresponding to the least busy among this node and immediate children.
	 * @throws ClusterException
	 */
	private NodeInfo getLeastBusyAvailableNode() throws ClusterException {
		List<NodeInfo> nodeInfoCopy;
		synchronized(childNodes) {
			nodeInfoCopy = ClusterModelTools.copyNodeInfoCollection(childNodes.values());
		}
		synchronized(nodeInfo) {
			nodeInfoCopy.add( ClusterModelTools.cloneNodeInfo(nodeInfo) );
		}
		Double thisSquared, minUsageSquared=null;
		NodeInfo candidate = null;
		for( NodeInfo node : nodeInfoCopy ) {
			thisSquared = ClusterModelTools.nodeInfoUsageSquared(node);
			if( minUsageSquared==null || thisSquared < minUsageSquared ) {
				candidate = node;
				minUsageSquared = thisSquared;
			}
		}
		return candidate;
	}
	
	/**
	 * Called by the AggregateNodeStatsMonitor to determine if any action need be taken. 
	 * 
	 * Will trigger the SWAP_HIGH_THRESHOLD, MEM_HIGH_THRESHOLD, CPU_HIGH_THRESHOLD, and CPU_LOW_THRESHOLD
	 * events if the usage values in aggregateNodeInfo exceed or fall below those thresholds.
	 */
	private List<ClusterEvent> evaluateNodeInfo(NodeInfo info) throws ClusterException {	
		List<ClusterEvent> toRet = new ArrayList<ClusterEvent>();
		synchronized(configuration) {
			// TODO: decide whether there is an order of precedence here, or they should flip flags or something
			if( ClusterModelTools.usageGreaterThan( info.getSwapUsage(), configuration.getSwapHigh() ) ) {
				toRet.add(ClusterFactory.newEvent(ClusterEventType.THRESHOLD_SWAP_HIGH));
			}
			if( ClusterModelTools.usageGreaterThan( info.getMemUsage(), configuration.getMemHigh() ) ) {
				toRet.add(ClusterFactory.newEvent(ClusterEventType.THRESHOLD_MEM_HIGH));
			}
			if( ClusterModelTools.usageGreaterThan( info.getCpuUsage(), configuration.getCpuHigh() ) ) {
				toRet.add(ClusterFactory.newEvent(ClusterEventType.THRESHOLD_CPU_HIGH));
			}
			if( ClusterModelTools.usageLessThan( info.getCpuUsage(), configuration.getCpuLow() ) ) {
				toRet.add(ClusterFactory.newEvent(ClusterEventType.THRESHOLD_CPU_LOW));
			}
		}
		return toRet;
	}
	
	private void removeChildNode(String Url) {
		synchronized(childNodes) {
			childNodes.remove(Url);
		}
		List<Integer> toRem = new ArrayList<Integer>();
		synchronized(nodeInfo) {
			int i = 0;
			for( NodeId id : nodeInfo.getChildNodes().getNodeId() ) {
				if( id.getUrl().compareTo( Url )==0 )
					toRem.add(i);
				i++;
			}
			for( Integer idx : toRem )
				nodeInfo.getChildNodes().getNodeId().remove((int)idx);
		} 
	}
	
	private void addChildNode(NodeInfo info) {
		synchronized(childNodes) {
			childNodes.put(info.getNodeId().getUrl(),info);
		}
		synchronized(nodeInfo) {
			nodeInfo.setNodeCount( nodeInfo.getNodeCount()+1 );
			nodeInfo.getChildNodes().getNodeId().add(info.getNodeId());
		}
	}
	
	/* ****************************** *
	 * ALL BELOW ARE INTERNAL CLASSES *
	 * ****************************** */
	
	/**
	 * Updates aggregateNodeInfo each second.  
	 * 
	 * aggregateNodeInfo contains the aggregate data for all children as well as this node.
	 * The variable is what is passed back on a call to getNodeInfo()
	 * 
	 * @author schang
	 */
	private class AggregateNodeStatsMonitor implements Runnable {
		public void run() {
			synchronized(nodeInfo) {
				Thread.currentThread().setName(nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
				Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" starting up the AggregateNodeStatusMonitor thread");
			}
			try {
				while(myStatus.value()!=NodeStatus.SHUTTING_DOWN) {
					
					synchronized(aggregateNodeInfo) {
						synchronized(nodeInfo) {
							aggregateNodeInfo = ClusterModelTools.cloneNodeInfo(nodeInfo);
						}
					}
					
					// make sure we have a client instantiated for each of the children
					// while pulling the node info from each immediate child node
					List<NodeInfo> nodesCopy;
					synchronized(childNodes) {
						nodesCopy = ClusterModelTools.copyNodeInfoCollection(childNodes.values());
					}
					
					// TODO: these should be done each in separate threads in case the user has allowed some ridiculously large number of nodes per level
					List<NodeInfo> nodesUpdated = new ArrayList<NodeInfo>();
					for( NodeInfo info : nodesCopy ) {
						
						try {
							ClusterNodeWS thisClient = ClusterFactory.newClient(info.getNodeId().getUrl());
							NodeInfo updatedInfo = thisClient.getNodeInfo( newAuthorization() );
							synchronized(configuration) {
								thisClient.setConfiguration( newAuthorization(), configuration);
							}
							nodesUpdated.add(updatedInfo);
						} catch ( WebServiceException e ) {
							
							// TODO: create an event type and remove the child from the node.  make sure the unresponsive node doesn't receive any events till the issue is resolved.
						}
						
						// I want to make sure we keep the childNodes map nodeInfo
						// up-to-date, so that we can minimize the need for fetching
						// from the node, 
						// but I am not sure how to implement hashCode() and equals()
						// in a JAXB generated object.  If I modify the code manually
						// then I won't be able to conveniently regenerate on schema
						// updates.  For now, this is the best solution...						
					}
					
					// TODO: do this better, for fucks sake
					synchronized(childNodes) {
						for( NodeInfo info : nodesUpdated )
							for( NodeInfo node : childNodes.values() ) {
								if( node.getNodeId().getUuid().compareTo( info.getNodeId().getUuid() )==0 ) {
									ClusterModelTools.copyNodeInfo(info, node);
									break;
								}
							}
					}
					
					// the aggregate of all child nodes is accumulated in this object
					NodeInfo accum = ClusterFactory.newNodeInfo();
					
					// sum all the child values into the node info accumulator object
					ThreeFloatUsageInfo t = new ThreeFloatUsageInfo();
					for( NodeInfo info : nodesUpdated ) {
						ClusterModelTools.updateAccum(accum,info);
					}
					
					// we also need to add in this nodes information
					synchronized(nodeInfo) {
						ClusterModelTools.updateAccum(accum,nodeInfo);
					}

					// now just divide by count to get the average.
					Integer count = nodesUpdated.size()+1;
					t = accum.getCpuUsage();
					t.setOne( t.getOne()/count );
					t.setFive( t.getFive()/count );
					t.setFifteen( t.getFifteen()/count );
					
					t = accum.getMemUsage();
					t.setOne( t.getOne()/count );
					t.setFive( t.getFive()/count );
					t.setFifteen( t.getFifteen()/count );
					
					t = accum.getSwapUsage();
					t.setOne( t.getOne()/count );
					t.setFive( t.getFive()/count );
					t.setFifteen( t.getFifteen()/count );
					
					// and finally copy the information into the object instances aggregateNodeInfo object
					synchronized(aggregateNodeInfo) {
						aggregateNodeInfo.setCpuUsage( ClusterModelTools.cloneThreeFloatUsageInfo(accum.getCpuUsage()) );
						aggregateNodeInfo.setSwapUsage( ClusterModelTools.cloneThreeFloatUsageInfo(accum.getSwapUsage()) );
						aggregateNodeInfo.setMemUsage( ClusterModelTools.cloneThreeFloatUsageInfo(accum.getMemUsage()) );
					}
					
					// evaluate the resulting aggregation 
					NodeInfo info = null;
					synchronized(aggregateNodeInfo) {
						info = ClusterModelTools.cloneNodeInfo(aggregateNodeInfo);
					}
					List<ClusterEvent> events = evaluateNodeInfo(info);
					Boolean busy = false;
					for( ClusterEvent event : events ) {
						
						if( event.getType() == ClusterEventType.THRESHOLD_MEM_HIGH
							|| event.getType() == ClusterEventType.THRESHOLD_CPU_HIGH
							|| event.getType() == ClusterEventType.THRESHOLD_SWAP_HIGH
						) busy = true;
						
						if( event instanceof UsageClusterEvent ) {
							((UsageClusterEvent)event).setNodeInfo( ClusterModelTools.cloneNodeInfo(info) );
						}
						
						triggerEvent( event );
					}
					
					if( myStatus.value()!=NodeStatus.SHUTTING_DOWN ) {
						if( !busy  )
							myStatus.value(NodeStatus.AVAILABLE);
						else myStatus.value(NodeStatus.BUSY);
					}
					
					// no need to do this but each second,
					// as the node info itself is only updated each second
					Long interval;
					synchronized(configuration) {
						interval = configuration.getStatusUpdateInterval();
					}
					Thread.sleep(interval);
				}
			} catch( Exception e ) {
				Logger.getLogger(this.getClass()).error(StringUtils.stackTraceToString(e));
				
				ErrorClusterEvent event = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_AGGREGATE_MONITOR_DIED);
				event.setException(e);
				triggerEvent(event);
			} finally {
				synchronized(nodeInfo) {
					Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" Aggregate Monitor thread shuttingdown");
					Thread.currentThread().setName("terminated - "+nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
				}
			}
		}
		
	}
	
	/**
	 * Monitors various usage statistics for this MachineInstance.
	 * 
	 * If the statistics for this machine become too high,
	 * and no child nodes are provisioned, then a new child,
	 * up to the number of children allowed, is provisioned
	 * 
	 * @author schang
	 */
	private class NodeStatsMonitor implements Runnable {
		public void run() {
			synchronized(nodeInfo) {
				Thread.currentThread().setName(nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
				Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" starting up the NodeStatusMonitor thread");
			}
			List<Float> cpuUsage = new ArrayList<Float>();
			List<Float> swapUsage = new ArrayList<Float>();
			List<Float> memUsage = new ArrayList<Float>();
			
			// TODO: this should be specified in the NodeConfiguration
			String scriptCmd=null;
			if( System.getProperty("os.name").startsWith("Windows") )
				scriptCmd = "cscript "+System.getenv("JONSCHANG_ROOT")+"/bin/script/node_stats.vbs";
			else {
				String file = System.getenv("JONSCHANG_ROOT")+"/bin/script/node_stats.php";
				scriptCmd = "php -n "+file;
			}
			
			Process scriptProc=null;
			try {
				Logger.getLogger(this.getClass()).debug("kicking off the NodeStatsMonitor shell script: "+scriptCmd);
				scriptProc = Runtime.getRuntime().exec(scriptCmd);
				
				java.io.InputStreamReader isr = new java.io.InputStreamReader(scriptProc.getInputStream());
				java.io.BufferedReader br = new java.io.BufferedReader(isr);
				java.io.InputStreamReader eisr = new java.io.InputStreamReader(scriptProc.getErrorStream());
				java.io.BufferedReader ebr = new java.io.BufferedReader(eisr);
				
				String thisLine = br.readLine();
				
				while( myStatus.value()!=NodeStatus.SHUTTING_DOWN ) {
					try {
						if( scriptProc.exitValue()>0 ) {
							StringBuilder sb = new StringBuilder();
							sb.append("The monitor script quit abnormally.\n");
							StringBuilder sb2 = new StringBuilder();
							thisLine = ebr.readLine();
							while(thisLine!=null) {
								sb2.append(thisLine);
								thisLine = ebr.readLine();
							}
							sb.append("Result to STDERR was:\n" + sb);
							throw new ClusterException(sb.toString());
						}
					} catch( IllegalThreadStateException e ) {
						// we do nothing here...it's good that the monitor hasn't exitted
					}
					
					// if the line starts with the correct prefix
					if( thisLine!=null && thisLine.startsWith("stats,") ) {
						// then extract the relevant data
						//Logger.getLogger(this.getClass()).trace(thisLine);
						String[] vals = thisLine.split(",");
						if( vals.length!=4 )
							throw new ClusterException("Each line should only have 3 entries after 'stats,': percent cpu in use, pages per second, and percent memory used");
						synchronized(nodeInfo) {
							updateUsage(Float.valueOf(vals[1]),cpuUsage,nodeInfo.getCpuUsage());
							updateUsage(Float.valueOf(vals[2]),swapUsage,nodeInfo.getSwapUsage());
							updateUsage(Float.valueOf(vals[3]),memUsage,nodeInfo.getMemUsage());
						}
					}
					// each script prints to STDOUT every second...checking every .75 seconds should be good
					Thread.sleep(750);
					thisLine = br.readLine();
				}
				if( scriptProc!=null ) {
					scriptProc.destroy();
				}
				synchronized(nodeInfo) {
					Logger.getLogger(this.getClass()).info(nodeInfo.getNodeId().getUrl()+" Monitor thread shuttingdown");
					Thread.currentThread().setName("terminated - "+nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
				}
				
			} catch(Exception e) {
				Logger.getLogger(this.getClass()).info(StringUtils.stackTraceToString(e));
				
				ErrorClusterEvent event = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_MONITOR_DIED);
				event.setException(e);
				triggerEvent(event);
			}
		}
		
		private void updateUsage(Float value, List<Float> items, ThreeFloatUsageInfo target) {
			Float cpuNow = value;
			
			// add each column to the column trackers
			items.add(cpuNow);
			if( items.size()>(15*60) )
				items.subList(0, items.size()-(15*60)).clear();
			
			// update the averaged values in NodeInfo object of the enclosing class instance
			Float cpuAccum=0f;
			Float cpuThen=0f;
			int count=0;
			for( int i = items.size()-1; i>=0; i-- ) {
				cpuThen = items.get(i);
				cpuAccum+=cpuThen;

				if( ( items.size()<60 && i==0 ) || count==59 ) {
					target.setOne( cpuAccum/count );
					target.setFive( cpuAccum/count );
					target.setFifteen( cpuAccum/count );
				}
				if( count == (60*5)-1 ) {
					target.setFive( cpuAccum/count );
					target.setFifteen( cpuAccum/count );
				}
				if( count == (60*15)-1 ) {
					target.setFifteen( cpuAccum/count );
					break;
				}
				count++;
			}
		}
	}
	
	/**
	 * Runs an event handler on an event
	 * 
	 * @author schang
	 */
	private class EventRunner implements Runnable {

		ClusterEventHandler handler;
		public void setHandlers(ClusterEventHandler handler) {
			this.handler = handler;
		}
		
		ClusterEvent event;
		public void setEvent(ClusterEvent thisEvent) {
			event = thisEvent;
		}
		
		@Override public void run() {
			synchronized(nodeInfo) {
				Thread.currentThread().setName(nodeInfo.getNodeId().getUrl()+" "+this.getClass().getSimpleName());
			}
			try {
				handler.process(event);
			} catch( Exception e ) {
				Logger.getLogger(this.getClass()).error(StringUtils.stackTraceToString(e));
				
				ErrorClusterEvent event = (ErrorClusterEvent)ClusterFactory.newEvent(ClusterEventType.ERROR_EVENT_PROCESSING);
				event.setException(e);
				triggerEvent(event);
			}
		}		
	}
}
