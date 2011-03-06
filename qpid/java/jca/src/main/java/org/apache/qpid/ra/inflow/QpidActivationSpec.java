/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.ra.inflow;

import javax.jms.Session;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.qpid.ra.ConnectionFactoryProperties;
import org.apache.qpid.ra.QpidResourceAdapter;

/**
 * The activation spec
 * These properties are set on the MDB ActivactionProperties
 * 
 */
public class QpidActivationSpec extends ConnectionFactoryProperties implements ActivationSpec
{
   private static final int DEFAULT_MAX_SESSION = 15;

   /** The logger */
   private static final Logger _log = LoggerFactory.getLogger(QpidActivationSpec.class);

   /** The resource adapter */
   private QpidResourceAdapter ra;

   /** The destination */
   private String destination;

   /** The destination type */
   private String destinationType;

   /** The message selector */
   private String messageSelector;

   /** The acknowledgement mode */
   private int acknowledgeMode;

   /** The subscription durability */
   private boolean subscriptionDurability;

   /** The subscription name */
   private String subscriptionName;

   /** The maximum number of sessions */
   private Integer maxSession;

   /** Transaction timeout */
   private Integer transactionTimeout;

   /** prefetch low */
   private Integer prefetchLow;

   /** prefetch high */
   private Integer prefetchHigh;

   private boolean useJNDI = true;

   /* use local tx instead of XA*/
   private Boolean localTx;

   // undefined by default, default is specified at the RA level in QpidRAProperties
   private Integer setupAttempts;
   
   // undefined by default, default is specified at the RA level in QpidRAProperties
   private Long setupInterval;

   /**
    * Constructor
    */
   public QpidActivationSpec()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("constructor()");
      }

      acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
      maxSession = DEFAULT_MAX_SESSION;
      transactionTimeout = 0;
   }

   /**
    * Get the resource adapter
    * @return The resource adapter
    */
   public ResourceAdapter getResourceAdapter()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getResourceAdapter()");
      }

      return ra;
   }

   /**
    * @return the useJNDI
    */
   public boolean isUseJNDI()
   {
      return useJNDI;
   }

   /**
    * @param value the useJNDI to set
    */
   public void setUseJNDI(final boolean value)
   {
      useJNDI = value;
   }

   /**
    * Set the resource adapter
    * @param ra The resource adapter
    * @exception ResourceException Thrown if incorrect resource adapter
    */
   public void setResourceAdapter(final ResourceAdapter ra) throws ResourceException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setResourceAdapter(" + ra + ")");
      }

      if (ra == null || !(ra instanceof QpidResourceAdapter))
      {
         throw new ResourceException("Resource adapter is " + ra);
      }

      this.ra = (QpidResourceAdapter)ra;
   }

   /**
    * Get the destination
    * @return The value
    */
   public String getDestination()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getDestination()");
      }

      return destination;
   }

   /**
    * Set the destination
    * @param value The value
    */
   public void setDestination(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setDestination(" + value + ")");
      }

      destination = value;
   }

   /**
    * Get the destination type
    * @return The value
    */
   public String getDestinationType()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getDestinationType()");
      }

      return destinationType;
   }

   /**
    * Set the destination type
    * @param value The value
    */
   public void setDestinationType(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setDestinationType(" + value + ")");
      }

      destinationType = value;
   }

   /**
    * Get the message selector
    * @return The value
    */
   public String getMessageSelector()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getMessageSelector()");
      }

      return messageSelector;
   }

   /**
    * Set the message selector
    * @param value The value
    */
   public void setMessageSelector(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setMessageSelector(" + value + ")");
      }

      messageSelector = value;
   }

   /**
    * Get the acknowledge mode
    * @return The value
    */
   public String getAcknowledgeMode()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getAcknowledgeMode()");
      }

      if (Session.DUPS_OK_ACKNOWLEDGE == acknowledgeMode)
      {
         return "Dups-ok-acknowledge";
      }
      else
      {
         return "Auto-acknowledge";
      }
   }

   /**
    * Set the acknowledge mode
    * @param value The value
    */
   public void setAcknowledgeMode(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setAcknowledgeMode(" + value + ")");
      }

      if ("DUPS_OK_ACKNOWLEDGE".equalsIgnoreCase(value) || "Dups-ok-acknowledge".equalsIgnoreCase(value))
      {
         acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
      }
      else if ("AUTO_ACKNOWLEDGE".equalsIgnoreCase(value) || "Auto-acknowledge".equalsIgnoreCase(value))
      {
         acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported acknowledgement mode " + value);
      }
   }

   /**
    * @return the acknowledgement mode
    */
   public int getAcknowledgeModeInt()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getAcknowledgeMode()");
      }

      return acknowledgeMode;
   }

   /**
    * Get the subscription durability
    * @return The value
    */
   public String getSubscriptionDurability()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSubscriptionDurability()");
      }

      if (subscriptionDurability)
      {
         return "Durable";
      }
      else
      {
         return "NonDurable";
      }
   }

   /**
    * Set the subscription durability
    * @param value The value
    */
   public void setSubscriptionDurability(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSubscriptionDurability(" + value + ")");
      }

      subscriptionDurability = "Durable".equals(value);
   }

   /**
    * Get the status of subscription durability
    * @return The value
    */
   public boolean isSubscriptionDurable()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("isSubscriptionDurable()");
      }

      return subscriptionDurability;
   }

   /**
    * Get the subscription name
    * @return The value
    */
   public String getSubscriptionName()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSubscriptionName()");
      }

      return subscriptionName;
   }

   /**
    * Set the subscription name
    * @param value The value
    */
   public void setSubscriptionName(final String value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSubscriptionName(" + value + ")");
      }

      subscriptionName = value;
   }

   /**
    * Get the number of max session
    * @return The value
    */
   public Integer getMaxSession()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getMaxSession()");
      }

      if (maxSession == null)
      {
         return DEFAULT_MAX_SESSION;
      }

      return maxSession;
   }

   /**
    * Set the number of max session
    * @param value The value
    */
   public void setMaxSession(final Integer value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setMaxSession(" + value + ")");
      }

      maxSession = value;
   }

   /**
    * Get the transaction timeout
    * @return The value
    */
   public Integer getTransactionTimeout()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getTransactionTimeout()");
      }

      return transactionTimeout;
   }

   /**
    * Set the transaction timeout
    * @param value The value
    */
   public void setTransactionTimeout(final Integer value)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setTransactionTimeout(" + value + ")");
      }

      transactionTimeout = value;
   }

   /**
    * Get the prefetch low
    * @return The value
    */
   public Integer getPrefetchLow()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getPrefetchLow()");
      }

      return prefetchLow;
   }

   /**
    * Set the prefetch low
    * @param value The value
    */
   public void setPrefetchLow(final Integer prefetchLow)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setPrefetchLow(" + prefetchLow + ")");
      }

      this.prefetchLow = prefetchLow;
   }

   /**
    * Get the prefetch high
    * @return The value
    */
   public Integer getPrefetchHigh()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getPrefetchHigh()");
      }

      return prefetchHigh;
   }

   /**
    * Set the prefetch high
    * @param value The value
    */
   public void setPrefetchHigh(final Integer prefetchHigh)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setPrefetchHigh(" + prefetchHigh + ")");
      }

      this.prefetchHigh = prefetchHigh;
   }

   public Boolean isUseLocalTx()
   {
      if (localTx == null)
      {
         return ra.getUseLocalTx();
      }
      else
      {
         return localTx;
      }
   }

   public void setUseLocalTx(final Boolean localTx)
   {
      this.localTx = localTx;
   }

   public int getSetupAttempts()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSetupAttempts()");
      }

      if (setupAttempts == null)
      {
         return ra.getSetupAttempts();
      }
      else
      {
         return setupAttempts;
      }
   }

   public void setSetupAttempts(int setupAttempts)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSetupAttempts(" + setupAttempts + ")");
      }

      this.setupAttempts = setupAttempts;
   }

   public long getSetupInterval()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSetupInterval()");
      }

      if (setupInterval == null)
      {
         return ra.getSetupInterval();
      }
      else
      {
         return setupInterval;
      }
   }

   public void setSetupInterval(long setupInterval)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSetupInterval(" + setupInterval + ")");
      }

      this.setupInterval = setupInterval;
   }

   /**
    * Validate
    * @exception InvalidPropertyException Thrown if a validation exception occurs
    */
   public void validate() throws InvalidPropertyException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("validate()");
      }

      if (destination == null || destination.trim().equals(""))
      {
         throw new InvalidPropertyException("Destination is mandatory");
      }
   }

   /**
    * Get a string representation
    * @return The value
    */
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(QpidActivationSpec.class.getName()).append('(');
      buffer.append("ra=").append(ra);
      buffer.append(" destination=").append(destination);
      buffer.append(" destinationType=").append(destinationType);
      if (messageSelector != null)
      {
         buffer.append(" selector=").append(messageSelector);
      }
      buffer.append(" ack=").append(getAcknowledgeMode());
      buffer.append(" durable=").append(subscriptionDurability);
      buffer.append(" clientID=").append(getClientID());
      if (subscriptionName != null)
      {
         buffer.append(" subscription=").append(subscriptionName);
      }
      buffer.append(" user=").append(getDefaultUsername());
      if (getDefaultPassword() != null)
      {
         buffer.append(" password=").append("****");
      }
      buffer.append(" maxSession=").append(maxSession);
      if (prefetchLow != null)
      {
         buffer.append(" prefetchLow=").append(prefetchLow);
      }
      if (prefetchHigh != null)
      {
         buffer.append(" prefetchHigh=").append(prefetchHigh);
      }
      buffer.append(')');
      return buffer.toString();
   }
}
