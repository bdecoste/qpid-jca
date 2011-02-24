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
   private static final Logger _log = LoggerFactory.getLogger(QpidRAProperties.class);

   /** Use Local TX instead of XA */
   private Boolean localTx = false;
   
   
   /** Class used to locate the Transaction Manager. */
   private String transactionManagerLocatorClass ;
   
   /** Method used to locate the TM */
   private String transactionManagerLocatorMethod ;

   private static final int DEFAULT_SETUP_ATTEMPTS = 10;

   private static final long DEFAULT_SETUP_INTERVAL = 2 * 1000;

   private int setupAttempts = DEFAULT_SETUP_ATTEMPTS;

   private long setupInterval = DEFAULT_SETUP_INTERVAL;

   /**
    * Constructor
    */
   public QpidRAProperties()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("constructor()");
      }
   }

   /**
    * Get the use XA flag
    * @return The value
    */
   public Boolean getUseLocalTx()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getUseLocalTx()");
      }

      return localTx;
   }

   /**
    * Set the use XA flag
    * @param localTx The value
    */
   public void setUseLocalTx(final Boolean localTx)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setUseLocalTx(" + localTx + ")");
      }

      this.localTx = localTx;
   }

   public void setTransactionManagerLocatorClass(final String transactionManagerLocatorClass)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setTransactionManagerLocatorClass(" + transactionManagerLocatorClass + ")");
      }

      this.transactionManagerLocatorClass = transactionManagerLocatorClass;
   }

   public String getTransactionManagerLocatorClass()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getTransactionManagerLocatorClass()");
      }

      return transactionManagerLocatorClass;
   }

   public String getTransactionManagerLocatorMethod()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getTransactionManagerLocatorMethod()");
      }

      return transactionManagerLocatorMethod;
   }

   public void setTransactionManagerLocatorMethod(final String transactionManagerLocatorMethod)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setTransactionManagerLocatorMethod(" + transactionManagerLocatorMethod + ")");
      }

      this.transactionManagerLocatorMethod = transactionManagerLocatorMethod;
   }

   public int getSetupAttempts()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSetupAttempts()");
      }

      return setupAttempts;
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

      return setupInterval;
   }

   public void setSetupInterval(long setupInterval)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSetupInterval(" + setupInterval + ")");
      }

      this.setupInterval = setupInterval;
   }
   
   @Override
   public String toString()
   {
      return "QpidRAProperties[localTx=" + localTx +
            ", transactionManagerLocatorClass=" + transactionManagerLocatorClass +
            ", transactionManagerLocatorMethod=" + transactionManagerLocatorMethod +
            ", setupAttempts=" + setupAttempts +
            ", setupInterval=" + setupInterval + "]";
   }
}
