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
package org.apache.qpid.ra;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RA default properties - these are set in the ra.xml file
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class QpidRAProperties extends ConnectionFactoryProperties implements Serializable
{
   /** Serial version UID */
   static final long serialVersionUID = -2772367477755473248L;

   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(QpidRAProperties.class);

   /** Trace enabled */
   private static boolean trace = QpidRAProperties.log.isTraceEnabled();

   /** Use Local TX instead of XA */
   private Boolean localTx = false;
   
   
   /** Class used to locate the Transaction Manager.
    *  Using JBoss as the default locator */
   private String transactionManagerLocatorClass = "org.apache.qpid.ra.tm.JBossTransactionManagerLocator";
   
   /** Method used to locate the TM */
   private String transactionManagerLocatorMethod = "getTm";

   private static final int DEFAULT_SETUP_ATTEMPTS = 10;

   private static final long DEFAULT_SETUP_INTERVAL = 2 * 1000;

   private int setupAttempts = DEFAULT_SETUP_ATTEMPTS;

   private long setupInterval = DEFAULT_SETUP_INTERVAL;

   /**
    * Constructor
    */
   public QpidRAProperties()
   {
      if (QpidRAProperties.trace)
      {
         QpidRAProperties.log.trace("constructor()");
      }
   }

   /**
    * Get the use XA flag
    * @return The value
    */
   public Boolean getUseLocalTx()
   {
      if (QpidRAProperties.trace)
      {
         QpidRAProperties.log.trace("getUseLocalTx()");
      }

      return localTx;
   }

   /**
    * Set the use XA flag
    * @param localTx The value
    */
   public void setUseLocalTx(final Boolean localTx)
   {
      if (QpidRAProperties.trace)
      {
         QpidRAProperties.log.trace("setUseLocalTx(" + localTx + ")");
      }

      this.localTx = localTx;
   }

   public void setTransactionManagerLocatorClass(final String transactionManagerLocatorClass)
   {
      this.transactionManagerLocatorClass = transactionManagerLocatorClass;
   }

   public String getTransactionManagerLocatorClass()
   {
      return transactionManagerLocatorClass;
   }

   public String getTransactionManagerLocatorMethod()
   {
      return transactionManagerLocatorMethod;
   }

   public void setTransactionManagerLocatorMethod(final String transactionManagerLocatorMethod)
   {
      this.transactionManagerLocatorMethod = transactionManagerLocatorMethod;
   }

   public int getSetupAttempts()
   {
      return setupAttempts;
   }

   public void setSetupAttempts(int setupAttempts)
   {
      this.setupAttempts = setupAttempts;
   }

   public long getSetupInterval()
   {
      return setupInterval;
   }

   public void setSetupInterval(long setupInterval)
   {
      this.setupInterval = setupInterval;
   }
   
   @Override
   public String toString()
   {
      return "QpidRAProperties[localTx=" + localTx + "]";
   }
}
