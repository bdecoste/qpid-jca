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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Session;
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
import org.apache.qpid.client.SSLConfiguration;
import org.apache.qpid.ra.inflow.AMQActivation;
import org.apache.qpid.ra.inflow.AMQActivationSpec;
import org.apache.qpid.url.URLSyntaxException;

/**
 * The resource adapter for AMQ
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class AMQResourceAdapter implements ResourceAdapter, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 4756893709825838770L;

   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(AMQResourceAdapter.class);

   /**
    * Trace enabled
    */
   private static boolean trace = AMQResourceAdapter.log.isTraceEnabled();

   /**
    * The bootstrap context
    */
   private BootstrapContext ctx;

   /**
    * The resource adapter properties
    */
   private final AMQRAProperties raProperties;
   
//   /**
//    * The resource adapter properties before parsing
//    */
//   private String unparsedProperties;

   /**
    * Have the factory been configured
    */
   private final AtomicBoolean configured;

   /**
    * The activations by activation spec
    */
   private final Map<ActivationSpec, AMQActivation> activations;

   private AMQConnectionFactory defaultAMQConnectionFactory;
   
   private TransactionManager tm;

   /**
    * Constructor
    */
   public AMQResourceAdapter()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("constructor()");
      }

      raProperties = new AMQRAProperties();
      configured = new AtomicBoolean(false);
      activations = new ConcurrentHashMap<ActivationSpec, AMQActivation>();
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
         catch (AMQRAException e)
         {
            throw new ResourceException("Unable to create activation", e);
         }
      }
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("endpointActivation(" + endpointFactory + ", " + spec + ")");
      }

      AMQActivation activation = new AMQActivation(this, endpointFactory, (AMQActivationSpec)spec);
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("endpointDeactivation(" + endpointFactory + ", " + spec + ")");
      }

      AMQActivation activation = activations.remove(spec);
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getXAResources(" + specs + ")");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("start(" + ctx + ")");
      }
      
      locateTM();

      this.ctx = ctx;

      AMQResourceAdapter.log.info("AMQ resource adaptor started");
   }

   /**
    * Stop
    */
   public void stop()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("stop()");
      }

      for (Map.Entry<ActivationSpec, AMQActivation> entry : activations.entrySet())
      {
         try
         {
            entry.getValue().stop();
         }
         catch (Exception ignored)
         {
            AMQResourceAdapter.log.debug("Ignored", ignored);
         }
      }

      activations.clear();

      AMQResourceAdapter.log.info("AMQ resource adapter stopped");
   }
//
//   public String getConnectionParameters()
//   {
//      return unparsedProperties;
//   }
//
//   public void setConnectionParameters(final String config)
//   {
//      if (config != null)
//      {
//         this.unparsedProperties = config;
//         raProperties.setParsedConnectionParameters(Util.parseConfig(config));
//      }
//   }

   /**
    * Get the user name
    *
    * @return The value
    */
   public String getUserName()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getUserName()");
      }

      return raProperties.getUserName();
   }

   /**
    * Set the user name
    *
    * @param userName The value
    */
   public void setUserName(final String userName)
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setUserName(" + userName + ")");
      }

      raProperties.setUserName(userName);
   }

   /**
    * Get the password
    *
    * @return The value
    */
   public String getPassword()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getPassword()");
      }

      return raProperties.getPassword();
   }

   /**
    * Set the password
    *
    * @param password The value
    */
   public void setPassword(final String password)
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setPassword(****)");
      }

      raProperties.setPassword(password);
   }

   /**
    * Get the client ID
    *
    * @return The value
    */
   public String getClientID()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getClientID()");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setClientID(" + clientID + ")");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getUseLocalTx()");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setUseXA(" + localTx + ")");
      }

      raProperties.setUseLocalTx(localTx);
   }

   public int getSetupAttempts()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getSetupAttempts()");
      }
      return raProperties.getSetupAttempts();
   }

   public void setSetupAttempts(int setupAttempts)
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setSetupAttempts(" + setupAttempts + ")");
      }
      raProperties.setSetupAttempts(setupAttempts);
   }

   public long getSetupInterval()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getSetupInterval()");
      }
      return raProperties.getSetupInterval();
   }

   public void setSetupInterval(long interval)
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("setSetupInterval(" + interval + ")");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("equals(" + obj + ")");
      }

      if (obj == null)
      {
         return false;
      }

      if (obj instanceof AMQResourceAdapter)
      {
         return raProperties.equals(((AMQResourceAdapter)obj).getProperties());
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("hashCode()");
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
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getWorkManager()");
      }

      if (ctx == null)
      {
         return null;
      }

      return ctx.getWorkManager();
   }

   // TODO: Need to revisit for prefetch and modes
   public Session createSession(final AMQConnection connection,
                                      final int ackMode,
                                      final boolean deliveryTransacted,
                                      final boolean useLocalTx) throws Exception
   {
      Session result;

      // if we are CMP or BMP using local tx we ignore the ack mode as we are transactional
      if (deliveryTransacted || useLocalTx)
      {
         if (useLocalTx)
         {
            result = connection.createSession(false, ackMode);
         }
         else
         {
            result = connection.createSession(true, ackMode);
         }
      }
      else
      {
         result = connection.createSession(false, ackMode);
      }

      AMQResourceAdapter.log.debug("Using queue connection " + result);

      return result;

   }

   /**
    * Get the resource adapter properties
    *
    * @return The properties
    */
   protected AMQRAProperties getProperties()
   {
      if (AMQResourceAdapter.trace)
      {
         AMQResourceAdapter.log.trace("getProperties()");
      }

      return raProperties;
   }

   /**
    * Setup the factory
    */
   protected void setup() throws AMQRAException
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
         catch (AMQRAException e)
         {
            throw new ResourceException("Unable to create activation", e);
         }
      }
      return defaultAMQConnectionFactory;
   }

   public AMQConnectionFactory createAMQConnectionFactory(final ConnectionFactoryProperties overrideProperties)
      throws AMQRAException
   {
      final AMQConnectionFactory cf = new AMQConnectionFactory() ;
      try
      {
         setParams(cf, overrideProperties);
      }
      catch (final URLSyntaxException urlse)
      {
         throw new AMQRAException("Unexpected exception creating connection factory", urlse) ;
      }
      return cf;
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
         log.warn("AMQ Resource Adapter won't be able to set and verify transaction timeouts in certain cases.");
      }
      else
      {
         log.debug("TM located = " + tm);
      }
   }
   

   private void setParams(final AMQConnectionFactory cf,
                          final ConnectionFactoryProperties overrideProperties)
      throws URLSyntaxException
   {
      final String overrideURL = overrideProperties.getConnectionURL() ;
      final String url = overrideURL != null ? overrideURL : raProperties.getConnectionURL() ;
      if (url != null)
      {
         cf.setConnectionURLString(url) ;
      }
      
      final String overrideDefaultPassword = overrideProperties.getDefaultPassword() ;
      final String defaultPassword = (overrideDefaultPassword != null ? overrideDefaultPassword : raProperties.getDefaultPassword()) ;
      if (defaultPassword != null)
      {
         cf.setDefaultPassword(defaultPassword) ;
      }
      
      final String overrideDefaultUsername = overrideProperties.getDefaultUsername() ;
      final String defaultUsername = (overrideDefaultUsername != null ? overrideDefaultUsername : raProperties.getDefaultUsername()) ;
      if (defaultUsername != null)
      {
         cf.setDefaultUsername(defaultUsername) ;
      }
      
      final String overrideHost = overrideProperties.getHost() ;
      final String host = (overrideHost != null ? overrideHost : raProperties.getHost()) ;
      if (host != null)
      {
         cf.setHost(host) ;
      }
      
      final Integer overridePort = overrideProperties.getPort() ;
      final Integer port = (overridePort != null ? overridePort : raProperties.getPort()) ;
      if (port != null)
      {
         cf.setPort(port) ;
      }
      
      final SSLConfiguration overrideSLlConfig = overrideProperties.getSSLConfig() ;
      final SSLConfiguration sslConfig = (overrideSLlConfig != null ? overrideSLlConfig : raProperties.getSSLConfig()) ;
      if (sslConfig != null)
      {
         cf.setSSLConfiguration(sslConfig) ;
      }
      
      final String overridePath = overrideProperties.getPath() ;
      final String path = (overridePath != null ? overridePath : raProperties.getPath()) ;
      if (path != null)
      {
         cf.setVirtualPath(path) ;
      }
   }
}
