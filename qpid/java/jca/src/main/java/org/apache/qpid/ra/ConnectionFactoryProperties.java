/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apache.qpid.ra;

import java.util.Map;

import org.hornetq.core.logging.Logger;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ConnectionFactoryProperties
{
   /**
    * The logger
    */
   private static final Logger log = Logger.getLogger(HornetQRAMCFProperties.class);

   /**
    * Trace enabled
    */
   private static boolean trace = ConnectionFactoryProperties.log.isTraceEnabled();

   private boolean hasBeenUpdated = false;

   /**
    * The transport type, changing the default configured from the RA
    */
   private String connectorClassName;

   /**
    * The transport config, changing the default configured from the RA
    */
   private Map<String, Object> connectionParameters;

   /**
    * The transport config, changing the default configured from the RA
    */
   private Map<String, Object> backupConnectionParameters;

   private String backupConnectorClassName;

   private String connectionLoadBalancingPolicyClassName;

   private String discoveryAddress;

   private Integer discoveryPort;

   private Long discoveryRefreshTimeout;

   private Long discoveryInitialWaitTimeout;

   private String clientID;

   private Integer dupsOKBatchSize;

   private Integer transactionBatchSize;

   private Long clientFailureCheckPeriod;

   private Long connectionTTL;

   private Long callTimeout;

   private Integer consumerWindowSize;

   private Integer consumerMaxRate;

   private Integer confirmationWindowSize;

   private Integer producerMaxRate;

   private Integer minLargeMessageSize;

   private Boolean blockOnAcknowledge;

   private Boolean blockOnNonDurableSend;

   private Boolean blockOnDurableSend;

   private Boolean autoGroup;

   private Boolean preAcknowledge;

   private Long retryInterval;

   private Double retryIntervalMultiplier;

   private Integer reconnectAttempts = -1;

   private Boolean failoverOnServerShutdown;

   private Boolean useGlobalPools;

   private Integer scheduledThreadPoolMaxSize;

   private Integer threadPoolMaxSize;

   /**
    * @return the transportType
    */
   public String getConnectorClassName()
   {
      return connectorClassName;
   }

   public Map<String, Object> getParsedConnectionParameters()
   {
      return connectionParameters;
   }

   public void setParsedConnectionParameters(final Map<String, Object> connectionParameters)
   {
      this.connectionParameters = connectionParameters;
      hasBeenUpdated = true;
   }

   public void setConnectorClassName(final String value)
   {
      connectorClassName = value;
      hasBeenUpdated = true;
   }

   public String getBackupConnectorClassName()
   {
      return backupConnectorClassName;
   }

   public Map<String, Object> getParsedBackupConnectionParameters()
   {
      return backupConnectionParameters;
   }

   public void setParsedBackupConnectionParameters(final Map<String, Object> backupConnectionParameters)
   {
      this.backupConnectionParameters = backupConnectionParameters;
      hasBeenUpdated = true;
   }

   public void setBackupConnectorClassName(final String backupConnectorClassName)
   {
      this.backupConnectorClassName = backupConnectorClassName;
      hasBeenUpdated = true;
   }

   public String getConnectionLoadBalancingPolicyClassName()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConnectionLoadBalancingPolicyClassName()");
      }
      hasBeenUpdated = true;
      return connectionLoadBalancingPolicyClassName;
   }

   public void setConnectionLoadBalancingPolicyClassName(final String connectionLoadBalancingPolicyClassName)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setSessionDefaultType(" + connectionLoadBalancingPolicyClassName + ")");
      }
      hasBeenUpdated = true;
      this.connectionLoadBalancingPolicyClassName = connectionLoadBalancingPolicyClassName;
   }

   public String getDiscoveryAddress()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDiscoveryAddress()");
      }
      hasBeenUpdated = true;
      return discoveryAddress;
   }

   public void setDiscoveryAddress(final String discoveryAddress)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDiscoveryAddress(" + discoveryAddress + ")");
      }
      hasBeenUpdated = true;
      this.discoveryAddress = discoveryAddress;
   }

   public Integer getDiscoveryPort()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDiscoveryPort()");
      }
      hasBeenUpdated = true;
      return discoveryPort;
   }

   public void setDiscoveryPort(final Integer discoveryPort)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDiscoveryPort(" + discoveryPort + ")");
      }
      hasBeenUpdated = true;
      this.discoveryPort = discoveryPort;
   }

   public Long getDiscoveryRefreshTimeout()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDiscoveryRefreshTimeout()");
      }
      hasBeenUpdated = true;
      return discoveryRefreshTimeout;
   }

   public void setDiscoveryRefreshTimeout(final Long discoveryRefreshTimeout)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDiscoveryRefreshTimeout(" + discoveryRefreshTimeout + ")");
      }
      hasBeenUpdated = true;
      this.discoveryRefreshTimeout = discoveryRefreshTimeout;
   }

   public Long getDiscoveryInitialWaitTimeout()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDiscoveryInitialWaitTimeout()");
      }
      hasBeenUpdated = true;
      return discoveryInitialWaitTimeout;
   }

   public void setDiscoveryInitialWaitTimeout(final Long discoveryInitialWaitTimeout)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDiscoveryInitialWaitTimeout(" + discoveryInitialWaitTimeout + ")");
      }
      hasBeenUpdated = true;
      this.discoveryInitialWaitTimeout = discoveryInitialWaitTimeout;
   }

   public String getClientID()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getClientID()");
      }
      hasBeenUpdated = true;
      return clientID;
   }

   public void setClientID(final String clientID)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setClientID(" + clientID + ")");
      }
      hasBeenUpdated = true;
      this.clientID = clientID;
   }

   /** This is for backward compatibility */
   public void setClientId(final String clientID)
   {
      setClientID(clientID);
   }

   public Integer getDupsOKBatchSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDupsOKBatchSize()");
      }
      hasBeenUpdated = true;
      return dupsOKBatchSize;
   }

   public void setDupsOKBatchSize(final Integer dupsOKBatchSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDupsOKBatchSize(" + dupsOKBatchSize + ")");
      }
      hasBeenUpdated = true;
      this.dupsOKBatchSize = dupsOKBatchSize;
   }

   public Integer getTransactionBatchSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getTransactionBatchSize()");
      }
      hasBeenUpdated = true;
      return transactionBatchSize;
   }

   public void setTransactionBatchSize(final Integer transactionBatchSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setTransactionBatchSize(" + transactionBatchSize + ")");
      }
      hasBeenUpdated = true;
      this.transactionBatchSize = transactionBatchSize;
   }

   public Long getClientFailureCheckPeriod()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getClientFailureCheckPeriod()");
      }
      hasBeenUpdated = true;
      return clientFailureCheckPeriod;
   }

   public void setClientFailureCheckPeriod(final Long clientFailureCheckPeriod)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setClientFailureCheckPeriod(" + clientFailureCheckPeriod + ")");
      }
      hasBeenUpdated = true;
      this.clientFailureCheckPeriod = clientFailureCheckPeriod;
   }

   public Long getConnectionTTL()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConnectionTTL()");
      }
      hasBeenUpdated = true;
      return connectionTTL;
   }

   public void setConnectionTTL(final Long connectionTTL)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setConnectionTTL(" + connectionTTL + ")");
      }
      hasBeenUpdated = true;
      this.connectionTTL = connectionTTL;
   }

   public Long getCallTimeout()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getCallTimeout()");
      }
      hasBeenUpdated = true;
      return callTimeout;
   }

   public void setCallTimeout(final Long callTimeout)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setCallTimeout(" + callTimeout + ")");
      }
      hasBeenUpdated = true;
      this.callTimeout = callTimeout;
   }

   public Integer getConsumerWindowSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConsumerWindowSize()");
      }
      hasBeenUpdated = true;
      return consumerWindowSize;
   }

   public void setConsumerWindowSize(final Integer consumerWindowSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setConsumerWindowSize(" + consumerWindowSize + ")");
      }
      hasBeenUpdated = true;
      this.consumerWindowSize = consumerWindowSize;
   }

   public Integer getConsumerMaxRate()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConsumerMaxRate()");
      }
      hasBeenUpdated = true;
      return consumerMaxRate;
   }

   public void setConsumerMaxRate(final Integer consumerMaxRate)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setConsumerMaxRate(" + consumerMaxRate + ")");
      }
      hasBeenUpdated = true;
      this.consumerMaxRate = consumerMaxRate;
   }

   public Integer getConfirmationWindowSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConfirmationWindowSize()");
      }
      hasBeenUpdated = true;
      return confirmationWindowSize;
   }

   public void setConfirmationWindowSize(final Integer confirmationWindowSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setConfirmationWindowSize(" + confirmationWindowSize + ")");
      }
      hasBeenUpdated = true;
      this.confirmationWindowSize = confirmationWindowSize;
   }

   public Integer getProducerMaxRate()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getProducerMaxRate()");
      }
      hasBeenUpdated = true;
      return producerMaxRate;
   }

   public void setProducerMaxRate(final Integer producerMaxRate)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setProducerMaxRate(" + producerMaxRate + ")");
      }
      hasBeenUpdated = true;
      this.producerMaxRate = producerMaxRate;
   }

   public Integer getMinLargeMessageSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getMinLargeMessageSize()");
      }
      hasBeenUpdated = true;
      return minLargeMessageSize;
   }

   public void setMinLargeMessageSize(final Integer minLargeMessageSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setMinLargeMessageSize(" + minLargeMessageSize + ")");
      }
      hasBeenUpdated = true;
      this.minLargeMessageSize = minLargeMessageSize;
   }

   public Boolean isBlockOnAcknowledge()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isBlockOnAcknowledge()");
      }
      hasBeenUpdated = true;
      return blockOnAcknowledge;
   }

   public void setBlockOnAcknowledge(final Boolean blockOnAcknowledge)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setBlockOnAcknowledge(" + blockOnAcknowledge + ")");
      }
      hasBeenUpdated = true;
      this.blockOnAcknowledge = blockOnAcknowledge;
   }

   public Boolean isBlockOnNonDurableSend()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isBlockOnNonDurableSend()");
      }
      hasBeenUpdated = true;
      return blockOnNonDurableSend;
   }

   public void setBlockOnNonDurableSend(final Boolean blockOnNonDurableSend)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setBlockOnNonDurableSend(" + blockOnNonDurableSend + ")");
      }
      hasBeenUpdated = true;
      this.blockOnNonDurableSend = blockOnNonDurableSend;
   }

   public Boolean isBlockOnDurableSend()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isBlockOnDurableSend()");
      }
      hasBeenUpdated = true;
      return blockOnDurableSend;
   }

   public void setBlockOnDurableSend(final Boolean blockOnDurableSend)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setBlockOnDurableSend(" + blockOnDurableSend + ")");
      }
      hasBeenUpdated = true;
      this.blockOnDurableSend = blockOnDurableSend;
   }

   public Boolean isAutoGroup()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isAutoGroup()");
      }
      hasBeenUpdated = true;
      return autoGroup;
   }

   public void setAutoGroup(final Boolean autoGroup)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setAutoGroup(" + autoGroup + ")");
      }
      hasBeenUpdated = true;
      this.autoGroup = autoGroup;
   }

   public Boolean isPreAcknowledge()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isPreAcknowledge()");
      }
      hasBeenUpdated = true;
      return preAcknowledge;
   }

   public void setPreAcknowledge(final Boolean preAcknowledge)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setPreAcknowledge(" + preAcknowledge + ")");
      }
      hasBeenUpdated = true;
      this.preAcknowledge = preAcknowledge;
   }

   public Long getRetryInterval()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getRetryInterval()");
      }
      hasBeenUpdated = true;
      return retryInterval;
   }

   public void setRetryInterval(final Long retryInterval)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setRetryInterval(" + retryInterval + ")");
      }
      hasBeenUpdated = true;
      this.retryInterval = retryInterval;
   }

   public Double getRetryIntervalMultiplier()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getRetryIntervalMultiplier()");
      }
      hasBeenUpdated = true;
      return retryIntervalMultiplier;
   }

   public void setRetryIntervalMultiplier(final Double retryIntervalMultiplier)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setRetryIntervalMultiplier(" + retryIntervalMultiplier + ")");
      }
      hasBeenUpdated = true;
      this.retryIntervalMultiplier = retryIntervalMultiplier;
   }

   public Integer getReconnectAttempts()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getReconnectAttempts()");
      }
      hasBeenUpdated = true;
      return reconnectAttempts;
   }

   public void setReconnectAttempts(final Integer reconnectAttempts)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setReconnectAttempts(" + reconnectAttempts + ")");
      }
      hasBeenUpdated = true;
      this.reconnectAttempts = reconnectAttempts;
   }

   public Boolean isFailoverOnServerShutdown()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isFailoverOnServerShutdown()");
      }
      hasBeenUpdated = true;
      return failoverOnServerShutdown;
   }

   public void setFailoverOnServerShutdown(final Boolean failoverOnServerShutdown)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setFailoverOnServerShutdown(" + failoverOnServerShutdown + ")");
      }
      hasBeenUpdated = true;
      this.failoverOnServerShutdown = failoverOnServerShutdown;
   }

   public Boolean isUseGlobalPools()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("isUseGlobalPools()");
      }
      hasBeenUpdated = true;
      return useGlobalPools;
   }

   public void setUseGlobalPools(final Boolean useGlobalPools)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setUseGlobalPools(" + useGlobalPools + ")");
      }
      hasBeenUpdated = true;
      this.useGlobalPools = useGlobalPools;
   }

   public Integer getScheduledThreadPoolMaxSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getScheduledThreadPoolMaxSize()");
      }
      hasBeenUpdated = true;
      return scheduledThreadPoolMaxSize;
   }

   public void setScheduledThreadPoolMaxSize(final Integer scheduledThreadPoolMaxSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setScheduledThreadPoolMaxSize(" + scheduledThreadPoolMaxSize + ")");
      }
      hasBeenUpdated = true;
      this.scheduledThreadPoolMaxSize = scheduledThreadPoolMaxSize;
   }

   public Integer getThreadPoolMaxSize()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getThreadPoolMaxSize()");
      }
      hasBeenUpdated = true;
      return threadPoolMaxSize;
   }

   public void setThreadPoolMaxSize(final Integer threadPoolMaxSize)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setThreadPoolMaxSize(" + threadPoolMaxSize + ")");
      }
      hasBeenUpdated = true;
      this.threadPoolMaxSize = threadPoolMaxSize;
   }

   public boolean isHasBeenUpdated()
   {
      return hasBeenUpdated;
   }
}
