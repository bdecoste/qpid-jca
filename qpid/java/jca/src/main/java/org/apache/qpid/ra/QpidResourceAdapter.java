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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Session;
import javax.jms.XASession;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQConnectionURL;
import org.apache.qpid.client.SSLConfiguration;
import org.apache.qpid.client.XAConnectionImpl;
import org.apache.qpid.ra.inflow.QpidActivation;
import org.apache.qpid.ra.inflow.QpidActivationSpec;
import org.apache.qpid.url.URLSyntaxException;

/**
 * The resource adapter for Qpid
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class QpidResourceAdapter implements ResourceAdapter, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 4756893709825838770L;

   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(QpidResourceAdapter.class);

   /**
    * Trace enabled
    */
   private static boolean trace = log.isTraceEnabled();

   /**
    * The bootstrap context
    */
   private BootstrapContext ctx;

   /**
    * The resource adapter properties
    */
   private final QpidRAProperties raProperties;
   
   /**
    * Have the factory been configured
    */
   private final AtomicBoolean configured;

   /**
    * The activations by activation spec
    */
   private final Map<ActivationSpec, QpidActivation> activations;

   private AMQConnectionFactory defaultAMQConnectionFactory;
   
   private TransactionManager tm;

   /**
    * Constructor
    */
   public QpidResourceAdapter()
   {
      if (trace)
      {
         log.trace("constructor()");
      }

      raProperties = new QpidRAProperties();
      configured = new AtomicBoolean(false);
      activations = new ConcurrentHashMap<ActivationSpec, QpidActivation>();
   }

   public TransactionManager getTM()
   {
      return tm;
   }

   /**
    * Endpoint activation
    *
    * @param endpointFactory The endpoint factory
    * @param spec            The activation spec
    * @throws ResourceException Thrown if an error occurs
    */
   public void endpointActivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec) throws ResourceException
   {
      if (!configured.getAndSet(true))
      {
         try
         {
            setup();
         }
         catch (QpidRAException e)
         {
            throw new ResourceException("Unable to create activation", e);
         }
      }
      if (trace)
      {
         log.trace("endpointActivation(" + endpointFactory + ", " + spec + ")");
      }

      QpidActivation activation = new QpidActivation(this, endpointFactory, (QpidActivationSpec)spec);
      activations.put(spec, activation);
      activation.start();
   }

   /**
    * Endpoint deactivation
    *
    * @param endpointFactory The endpoint factory
    * @param spec            The activation spec
    */
   public void endpointDeactivation(final MessageEndpointFactory endpointFactory, final ActivationSpec spec)
   {
      if (trace)
      {
         log.trace("endpointDeactivation(" + endpointFactory + ", " + spec + ")");
      }

      QpidActivation activation = activations.remove(spec);
      if (activation != null)
      {
         activation.stop();
      }
   }

   /**
    * Get XA resources
    *
    * @param specs The activation specs
    * @return The XA resources
    * @throws ResourceException Thrown if an error occurs or unsupported
    */
   public XAResource[] getXAResources(final ActivationSpec[] specs) throws ResourceException
   {
      if (trace)
      {
         log.trace("getXAResources(" + specs + ")");
      }

      throw new ResourceException("Unsupported");
   }

   /**
    * Start
    *
    * @param ctx The bootstrap context
    * @throws ResourceAdapterInternalException
    *          Thrown if an error occurs
    */
   public void start(final BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      if (trace)
      {
         log.trace("start(" + ctx + ")");
      }
      
      locateTM();

      this.ctx = ctx;

      log.info("Qpid resource adaptor started");
   }

   /**
    * Stop
    */
   public void stop()
   {
      if (trace)
      {
         log.trace("stop()");
      }

      for (Map.Entry<ActivationSpec, QpidActivation> entry : activations.entrySet())
      {
         try
         {
            entry.getValue().stop();
         }
         catch (Exception ignored)
         {
            log.debug("Ignored", ignored);
         }
      }

      activations.clear();

      log.info("Qpid resource adapter stopped");
   }

   /**
    * Get the user name
    *
    * @return The value
    */
   public String getDefaultUserName()
   {
      if (trace)
      {
         log.trace("getUserName()");
      }

      return raProperties.getDefaultUsername();
   }

   /**
    * Set the user name
    *
    * @param userName The value
    */
   public void setUserName(final String userName)
   {
      if (trace)
      {
         log.trace("setUserName(" + userName + ")");
      }

      raProperties.setDefaultUsername(userName);
   }

   /**
    * Get the password
    *
    * @return The value
    */
   public String getDefaultPassword()
   {
      if (trace)
      {
         log.trace("getPassword()");
      }

      return raProperties.getDefaultPassword();
   }

   /**
    * Set the password
    *
    * @param password The value
    */
   public void setDefaultPassword(final String password)
   {
      if (trace)
      {
         log.trace("setPassword(****)");
      }

      raProperties.setDefaultPassword(password);
   }

   /**
    * Get the client ID
    *
    * @return The value
    */
   public String getClientID()
   {
      if (trace)
      {
         log.trace("getClientID()");
      }

      return raProperties.getClientID();
   }

   /**
    * Set the client ID
    *
    * @param clientID The client id
    */
   public void setClientID(final String clientID)
   {
      if (trace)
      {
         log.trace("setClientID(" + clientID + ")");
      }

      raProperties.setClientID(clientID);
   }

   /**
    * Get the use XA flag
    *
    * @return The value
    */
   public Boolean getUseLocalTx()
   {
      if (trace)
      {
         log.trace("getUseLocalTx()");
      }

      return raProperties.getUseLocalTx();
   }

   /**
    * Set the use XA flag
    *
    * @param localTx The value
    */
   public void setUseLocalTx(final Boolean localTx)
   {
      if (trace)
      {
         log.trace("setUseXA(" + localTx + ")");
      }

      raProperties.setUseLocalTx(localTx);
   }

   public int getSetupAttempts()
   {
      if (trace)
      {
         log.trace("getSetupAttempts()");
      }
      return raProperties.getSetupAttempts();
   }

   public void setSetupAttempts(int setupAttempts)
   {
      if (trace)
      {
         log.trace("setSetupAttempts(" + setupAttempts + ")");
      }
      raProperties.setSetupAttempts(setupAttempts);
   }

   public long getSetupInterval()
   {
      if (trace)
      {
         log.trace("getSetupInterval()");
      }
      return raProperties.getSetupInterval();
   }

   public void setSetupInterval(long interval)
   {
      if (trace)
      {
         log.trace("setSetupInterval(" + interval + ")");
      }
      raProperties.setSetupInterval(interval);
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj Object with which to compare
    * @return True if this object is the same as the obj argument; false otherwise.
    */
   public boolean equals(final Object obj)
   {
      if (trace)
      {
         log.trace("equals(" + obj + ")");
      }

      if (obj == null)
      {
         return false;
      }

      if (obj instanceof QpidResourceAdapter)
      {
         return raProperties.equals(((QpidResourceAdapter)obj).getProperties());
      }
      else
      {
         return false;
      }
   }

   /**
    * Return the hash code for the object
    *
    * @return The hash code
    */
   public int hashCode()
   {
      if (trace)
      {
         log.trace("hashCode()");
      }

      return raProperties.hashCode();
   }

   /**
    * Get the work manager
    *
    * @return The manager
    */
   public WorkManager getWorkManager()
   {
      if (trace)
      {
         log.trace("getWorkManager()");
      }

      if (ctx == null)
      {
         return null;
      }

      return ctx.getWorkManager();
   }

   public XASession createXASession(final XAConnectionImpl connection)
      throws Exception
   {
      final XASession result = connection.createXASession() ;
      if (log.isDebugEnabled())
      {
         log.debug("Using session " + result);
      }
      return result ;
   }
   
   public Session createSession(final AMQConnection connection,
                                      final int ackMode,
                                      final boolean useLocalTx,
                                      final Integer prefetchLow,
                                      final Integer prefetchHigh) throws Exception
   {
      Session result;

      if (prefetchLow == null)
      {
         result = connection.createSession(useLocalTx, ackMode) ;
      }
      else if (prefetchHigh == null)
      {
         result = connection.createSession(useLocalTx, ackMode, prefetchLow) ;
      }
      else
      {
         result = connection.createSession(useLocalTx, ackMode, prefetchHigh, prefetchLow) ;
      }

      if (log.isDebugEnabled())
      {
         log.debug("Using session " + result);
      }

      return result;

   }

   /**
    * Get the resource adapter properties
    *
    * @return The properties
    */
   protected QpidRAProperties getProperties()
   {
      if (trace)
      {
         log.trace("getProperties()");
      }

      return raProperties;
   }

   /**
    * Setup the factory
    */
   protected void setup() throws QpidRAException
   {
      defaultAMQConnectionFactory = createAMQConnectionFactory(raProperties);
   }


   public AMQConnectionFactory getDefaultAMQConnectionFactory() throws ResourceException
   {
      if (!configured.getAndSet(true))
      {
         try
         {
            setup();
         }
         catch (QpidRAException e)
         {
            throw new ResourceException("Unable to create activation", e);
         }
      }
      return defaultAMQConnectionFactory;
   }

   public AMQConnectionFactory createAMQConnectionFactory(final ConnectionFactoryProperties overrideProperties)
      throws QpidRAException
   {
      try
      {
         return createFactory(overrideProperties);
      }
      catch (final URLSyntaxException urlse)
      {
         throw new QpidRAException("Unexpected exception creating connection factory", urlse) ;
      }
   }

   public Map<String, Object> overrideConnectionParameters(final Map<String, Object> connectionParams,
                                                           final Map<String, Object> overrideConnectionParams)
   {
      Map<String, Object> map = new HashMap<String, Object>();
      if(connectionParams != null)
      {
         map.putAll(connectionParams);
      }
      if(overrideConnectionParams != null)
      {
         for (Map.Entry<String, Object> stringObjectEntry : overrideConnectionParams.entrySet())
         {
            map.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
         }
      }
      return map;
   }
   
   private void locateTM()
   {
      String locatorClasses[] = raProperties.getTransactionManagerLocatorClass().split(";");
      String locatorMethods[] = raProperties.getTransactionManagerLocatorMethod().split(";");
      
      for (int i = 0 ; i < locatorClasses.length; i++)
      {
         tm = Util.locateTM(locatorClasses[i], locatorMethods[i]);
         if (tm != null)
         {
            break;
         }
      }
      
      if (tm == null)
      {
         log.warn("It wasn't possible to lookup for a Transaction Manager through the configured properties TransactionManagerLocatorClass and TransactionManagerLocatorMethod");
         log.warn("Qpid Resource Adapter won't be able to set and verify transaction timeouts in certain cases.");
      }
      else
      {
         if (log.isDebugEnabled())
         {
            log.debug("TM located = " + tm);
         }
      }
   }
   

   private AMQConnectionFactory createFactory(final ConnectionFactoryProperties overrideProperties)
      throws URLSyntaxException, QpidRAException
   {
      final String overrideURL = overrideProperties.getConnectionURL() ;
      final String url = overrideURL != null ? overrideURL : raProperties.getConnectionURL() ;
      
      final String overrideClientID = overrideProperties.getClientID() ;
      final String clientID = (overrideClientID != null ? overrideClientID : raProperties.getClientID()) ;

      final String overrideDefaultPassword = overrideProperties.getDefaultPassword() ;
      final String defaultPassword = (overrideDefaultPassword != null ? overrideDefaultPassword : raProperties.getDefaultPassword()) ;

      final String overrideDefaultUsername = overrideProperties.getDefaultUsername() ;
      final String defaultUsername = (overrideDefaultUsername != null ? overrideDefaultUsername : raProperties.getDefaultUsername()) ;

      final String overrideHost = overrideProperties.getHost() ;
      final String host = (overrideHost != null ? overrideHost : raProperties.getHost()) ;

      final Integer overridePort = overrideProperties.getPort() ;
      final Integer port = (overridePort != null ? overridePort : raProperties.getPort()) ;

      final SSLConfiguration overrideSLlConfig = overrideProperties.getSSLConfig() ;
      final SSLConfiguration sslConfig = (overrideSLlConfig != null ? overrideSLlConfig : raProperties.getSSLConfig()) ;

      final String overridePath = overrideProperties.getPath() ;
      final String path = (overridePath != null ? overridePath : raProperties.getPath()) ;

      final AMQConnectionFactory cf ;
      if (url != null)
      {
         cf = new AMQConnectionFactory(url) ;
         
         if (host != null)
         {
            cf.setHost(host) ;
         }
         
         if (port != null)
         {
            cf.setPort(port) ;
         }
         
         if (path != null)
         {
            cf.setVirtualPath(path) ;
         }
         
         if (defaultPassword != null)
         {
            cf.setDefaultPassword(defaultPassword) ;
         }
         
         if (defaultUsername != null)
         {
            cf.setDefaultUsername(defaultUsername) ;
         }
         
         if (clientID != null)
         {
            cf.getConnectionURL().setClientName(clientID) ;
         }
      }
      else
      {
         // create a URL to force the connection details
         if ((host == null) || (port == null) || (path == null))
         {
            throw new QpidRAException("Configuration requires host/port/path if connectionURL is not specified") ;
         }
         final String username = (defaultUsername != null ? defaultUsername : "") ;
         final String password = (defaultPassword != null ? defaultPassword : "") ;
         final String client = (clientID != null ? clientID : "") ;
         
         final String newurl = AMQConnectionURL.AMQ_PROTOCOL + "://" + username +":" + password + "@" + client + "/" + path + '?' + AMQConnectionURL.OPTIONS_BROKERLIST + "='tcp://" + host + ':' + port + '\'' ;
         if (log.isDebugEnabled())
         {
            log.debug("Initialising connectionURL to " + newurl) ;
         }
         
         cf = new AMQConnectionFactory(newurl) ;
      }
      
      if (sslConfig != null)
      {
         cf.setSSLConfiguration(sslConfig) ;
      }
      
      return cf ;
   }
}
