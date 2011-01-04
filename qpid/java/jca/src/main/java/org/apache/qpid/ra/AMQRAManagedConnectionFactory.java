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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.jms.ConnectionMetaData;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.SSLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQ ManagedConectionFactory
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>.
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class AMQRAManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation
{
   /**
    * Serial version UID
    */
   static final long serialVersionUID = -1452379518562456741L;

   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(AMQRAManagedConnectionFactory.class);

   /**
    * Trace enabled
    */
   private static boolean trace = AMQRAManagedConnectionFactory.log.isTraceEnabled();

   /**
    * The resource adapter
    */
   private AMQResourceAdapter ra;

   /**
    * Connection manager
    */
   private ConnectionManager cm;

   /**
    * The managed connection factory properties
    */
   private final AMQRAMCFProperties mcfProperties;

   /**
    * Connection Factory used if properties are set
    */
   private AMQConnectionFactory connectionFactory;

   /**
    * Constructor
    */
   public AMQRAManagedConnectionFactory()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("constructor()");
      }

      ra = null;
      cm = null;
      mcfProperties = new AMQRAMCFProperties();
   }

   /**
    * Creates a Connection Factory instance
    *
    * @return javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Thrown if a connection factory cant be created
    */
   public Object createConnectionFactory() throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.debug("createConnectionFactory()");
      }

      return createConnectionFactory(new AMQRAConnectionManager());
   }

   /**
    * Creates a Connection Factory instance
    *
    * @param cxManager The connection manager
    * @return javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Thrown if a connection factory cant be created
    */
   public Object createConnectionFactory(final ConnectionManager cxManager) throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("createConnectionFactory(" + cxManager + ")");
      }

      cm = cxManager;

      AMQRAConnectionFactory cf = new AMQRAConnectionFactoryImpl(this, cm);

      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("Created connection factory: " + cf +
                                                     ", using connection manager: " +
                                                     cm);
      }

      return cf;
   }

   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    *
    * @param subject       Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return The managed connection
    * @throws ResourceException Thrown if a managed connection cant be created
    */
   public ManagedConnection createManagedConnection(final Subject subject, final ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("createManagedConnection(" + subject + ", " + cxRequestInfo + ")");
      }

      AMQRAConnectionRequestInfo cri = getCRI((AMQRAConnectionRequestInfo)cxRequestInfo);

      AMQRACredential credential = AMQRACredential.getCredential(this, subject, cri);

      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("jms credential: " + credential);
      }

      AMQRAManagedConnection mc = new AMQRAManagedConnection(this,
                                                                     cri,
                                                                     ra.getTM(),
                                                                     credential.getUserName(),
                                                                     credential.getPassword());

      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("created new managed connection: " + mc);
      }

      return mc;
   }

   /**
    * Returns a matched connection from the candidate set of connections.
    *
    * @param connectionSet The candidate connection set
    * @param subject       Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return The managed connection
    * @throws ResourceException Thrown if no managed connection cant be found
    */
   public ManagedConnection matchManagedConnections(final Set connectionSet,
                                                    final Subject subject,
                                                    final ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("matchManagedConnections(" + connectionSet +
                                                     ", " +
                                                     subject +
                                                     ", " +
                                                     cxRequestInfo +
                                                     ")");
      }

      AMQRAConnectionRequestInfo cri = getCRI((AMQRAConnectionRequestInfo)cxRequestInfo);
      AMQRACredential credential = AMQRACredential.getCredential(this, subject, cri);

      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("Looking for connection matching credentials: " + credential);
      }

      Iterator connections = connectionSet.iterator();

      while (connections.hasNext())
      {
         Object obj = connections.next();

         if (obj instanceof AMQRAManagedConnection)
         {
            AMQRAManagedConnection mc = (AMQRAManagedConnection)obj;
            ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();

            if ((mc.getUserName() == null || mc.getUserName() != null && mc.getUserName()
                                                                           .equals(credential.getUserName())) && mcf.equals(this))
            {
               if (cri.equals(mc.getCRI()))
               {
                  if (AMQRAManagedConnectionFactory.trace)
                  {
                     AMQRAManagedConnectionFactory.log.trace("Found matching connection: " + mc);
                  }

                  return mc;
               }
            }
         }
      }

      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("No matching connection was found");
      }

      return null;
   }

   /**
    * Set the log writer -- NOT SUPPORTED
    *
    * @param out The writer
    * @throws ResourceException Thrown if the writer cant be set
    */
   public void setLogWriter(final PrintWriter out) throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setLogWriter(" + out + ")");
      }
   }

   /**
    * Get the log writer -- NOT SUPPORTED
    *
    * @return The writer
    * @throws ResourceException Thrown if the writer cant be retrieved
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getLogWriter()");
      }

      return null;
   }

   /**
    * Get the resource adapter
    *
    * @return The resource adapter
    */
   public ResourceAdapter getResourceAdapter()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getResourceAdapter()");
      }

      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The resource adapter
    * @throws ResourceException Thrown if incorrect resource adapter
    */
   public void setResourceAdapter(final ResourceAdapter ra) throws ResourceException
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setResourceAdapter(" + ra + ")");
      }

      if (ra == null || !(ra instanceof AMQResourceAdapter))
      {
         throw new ResourceException("Resource adapter is " + ra);
      }

      this.ra = (AMQResourceAdapter)ra;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj Object with which to compare
    * @return True if this object is the same as the obj argument; false otherwise.
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("equals(" + obj + ")");
      }

      if (obj == null)
      {
         return false;
      }

      if (obj instanceof AMQRAManagedConnectionFactory)
      {
         AMQRAManagedConnectionFactory other = (AMQRAManagedConnectionFactory)obj;

         return mcfProperties.equals(other.getProperties()) && ra.equals(other.getResourceAdapter());
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
   @Override
   public int hashCode()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("hashCode()");
      }

      int hash = mcfProperties.hashCode();
      hash += 31 * ra.hashCode();

      return hash;
   }

   /**
    * Get the default session type
    *
    * @return The value
    */
   public String getSessionDefaultType()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getSessionDefaultType()");
      }

      return mcfProperties.getSessionDefaultType();
   }

   /**
    * Set the default session type
    *
    * @param type either javax.jms.Topic or javax.jms.Queue
    */
   public void setSessionDefaultType(final String type)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setSessionDefaultType(" + type + ")");
      }

      mcfProperties.setSessionDefaultType(type);
   }

//   /**
//    * @return the connectionParameters
//    */
//   public String getConnectionParameters()
//   {
//      return mcfProperties.getStrConnectionParameters();
//   }
//
//   public void setConnectionParameters(final String configuration)
//   {
//      mcfProperties.setConnectionParameters(configuration);
//   }

   public String getClientID()
   {
      return mcfProperties.getClientID();
   }

   public void setClientID(final String clientID)
   {
      mcfProperties.setClientID(clientID);
   }
   public String getConnectionURL()
   {
      return mcfProperties.getConnectionURL() ;
   }

   public void setConnectionURL(final String connectionURL)
   {
      mcfProperties.setConnectionURL(connectionURL);
   }

   public String getDefaultPassword()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getDefaultPassword()");
      }
      return mcfProperties.getDefaultPassword();
   }

   public void setDefaultPassword(final String defaultPassword)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setDefaultPassword(" + defaultPassword + ")");
      }
      mcfProperties.setDefaultPassword(defaultPassword);
   }

   public String getDefaultUsername()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getDefaultUsername()");
      }
      return mcfProperties.getDefaultUsername();
   }

   public void setDefaultUsername(final String defaultUsername)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setDefaultUsername(" + defaultUsername + ")");
      }
      mcfProperties.setDefaultUsername(defaultUsername);
   }

   public String getHost()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getHost()");
      }
      return mcfProperties.getHost();
   }

   public void setHost(final String host)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setHost(" + host + ")");
      }
      mcfProperties.setHost(host);
   }

   public Integer getPort()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getPort()");
      }
      return mcfProperties.getPort();
   }

   public void setPort(final Integer port)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setPort(" + port + ")");
      }
      mcfProperties.setPort(port);
   }

   public String getPath()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getPath()");
      }
      return mcfProperties.getPath();
   }

   public void setPath(final String path)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setPath(" + path + ")");
      }
      mcfProperties.setPath(path);
   }

   public SSLConfiguration getSSLConfig()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getSSLConfig()");
      }
      return mcfProperties.getSSLConfig();
   }
   
   public void setSSLConfig(final SSLConfiguration sslConfig)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setSSLConfig(" + sslConfig + ")");
      }
      mcfProperties.setSSLConfig(sslConfig);
   }
   
   public String getKeystorePath()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getKeystorePath()");
      }
      return mcfProperties.getKeystorePath();
   }

   public void setKeystorePath(final String path)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setKeystorePath(" + path + ")");
      }
      mcfProperties.setKeystorePath(path);
   }
   
   public String getKeystorePassword()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getKeystorePassword()");
      }
      return mcfProperties.getKeystorePassword();
   }

   public void setKeystorePassword(final String password)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setKeystorePassword(" + password + ")");
      }
      mcfProperties.setKeystorePassword(password);
   }
   
   public String getCertType()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getCertType()");
      }
      return mcfProperties.getCertType();
   }

   public void setCertType(final String certType)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setCertType(" + certType + ")");
      }
      mcfProperties.setCertType(certType);
   }

   /**
    * Get the useTryLock.
    *
    * @return the useTryLock.
    */
   public Integer getUseTryLock()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getUseTryLock()");
      }

      return mcfProperties.getUseTryLock();
   }

   /**
    * Set the useTryLock.
    *
    * @param useTryLock the useTryLock.
    */
   public void setUseTryLock(final Integer useTryLock)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("setUseTryLock(" + useTryLock + ")");
      }

      mcfProperties.setUseTryLock(useTryLock);
   }

   /**
    * Get the connection metadata
    *
    * @return The metadata
    */
   public ConnectionMetaData getMetaData()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getMetadata()");
      }

      return new AMQRAConnectionMetaData();
   }

   /**
    * Get the JBoss connection factory
    *
    * @return The factory
    */
   protected synchronized AMQConnectionFactory getAMQConnectionFactory() throws ResourceException
   {

      if (connectionFactory == null)
      {
         try
         {
            connectionFactory = ra.createAMQConnectionFactory(mcfProperties);
         }
         catch (final AMQRAException amqrae)
         {
            throw new ResourceException("Unexpected exception crating the connection factory", amqrae) ;
         }
      }
      return connectionFactory;
   }

   /**
    * Get the managed connection factory properties
    *
    * @return The properties
    */
   protected AMQRAMCFProperties getProperties()
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getProperties()");
      }

      return mcfProperties;
   }

   /**
    * Get a connection request info instance
    *
    * @param info The instance that should be updated; may be <code>null</code>
    * @return The instance
    */
   private AMQRAConnectionRequestInfo getCRI(final AMQRAConnectionRequestInfo info)
   {
      if (AMQRAManagedConnectionFactory.trace)
      {
         AMQRAManagedConnectionFactory.log.trace("getCRI(" + info + ")");
      }

      if (info == null)
      {
         // Create a default one
         return new AMQRAConnectionRequestInfo(ra.getProperties(), mcfProperties.getType());
      }
      else
      {
         // Fill the one with any defaults
         info.setDefaults(ra.getProperties());
         return info;
      }
   }
}
