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

import org.apache.qpid.client.SSLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ConnectionFactoryProperties
{
   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(AMQRAMCFProperties.class);

   /**
    * Trace enabled
    */
   private static boolean trace = ConnectionFactoryProperties.log.isTraceEnabled();

   private boolean hasBeenUpdated = false;

//   /**
//    * The transport config, changing the default configured from the RA
//    */
//   private Map<String, Object> connectionParameters;
   
   private String clientID;
   
   private String connectionURL;

   private String defaultPassword;

   private String defaultUsername;

   private String host;

   private Integer port;

   private String path;
   
   private SSLConfiguration sslConfig;
   
//   public Map<String, Object> getParsedConnectionParameters()
//   {
//      return connectionParameters;
//   }
//
//   public void setParsedConnectionParameters(final Map<String, Object> connectionParameters)
//   {
//      this.connectionParameters = connectionParameters;
//      hasBeenUpdated = true;
//   }

   public String getClientID()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getClientID()");
      }
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

   public boolean isHasBeenUpdated()
   {
      return hasBeenUpdated;
   }

   public String getConnectionURL()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getConnectionURL()");
      }
      return connectionURL;
   }

   public void setConnectionURL(final String connectionURL)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setConnectionURL(" + connectionURL + ")");
      }
      hasBeenUpdated = true;
      this.connectionURL = connectionURL;
   }

   public String getDefaultPassword()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDefaultPassword()");
      }
      return defaultPassword;
   }

   public void setDefaultPassword(final String defaultPassword)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDefaultPassword(" + defaultPassword + ")");
      }
      hasBeenUpdated = true;
      this.defaultPassword = defaultPassword;
   }

   public String getDefaultUsername()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getDefaultUsername()");
      }
      return defaultUsername;
   }

   public void setDefaultUsername(final String defaultUsername)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setDefaultUsername(" + defaultUsername + ")");
      }
      hasBeenUpdated = true;
      this.defaultUsername = defaultUsername;
   }

   public String getHost()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getHost()");
      }
      return host;
   }

   public void setHost(final String host)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setHost(" + host + ")");
      }
      hasBeenUpdated = true;
      this.host = host;
   }

   public Integer getPort()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getPort()");
      }
      return port;
   }

   public void setPort(final Integer port)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setPort(" + port + ")");
      }
      hasBeenUpdated = true;
      this.port = port;
   }

   public String getPath()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getPath()");
      }
      return path;
   }

   public void setPath(final String path)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setPath(" + path + ")");
      }
      hasBeenUpdated = true;
      this.path = path;
   }

   public SSLConfiguration getSSLConfig()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getSSLConfig()");
      }
      return sslConfig;
   }
   
   public void setSSLConfig(final SSLConfiguration sslConfig)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setSSLConfig(" + sslConfig + ")");
      }
      hasBeenUpdated = true;
      this.sslConfig = sslConfig;
   }
   
   public String getKeystorePath()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getKeystorePath()");
      }
      return (sslConfig == null ? null : sslConfig.getKeystorePath()) ;
   }

   public void setKeystorePath(final String path)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setKeystorePath(" + path + ")");
      }
      if (sslConfig == null)
      {
         sslConfig = new SSLConfiguration() ;
      }
      hasBeenUpdated = true;
      sslConfig.setKeystorePath(path) ;
   }
   
   public String getKeystorePassword()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getKeystorePassword()");
      }
      return (sslConfig == null ? null : sslConfig.getKeystorePassword()) ;
   }

   public void setKeystorePassword(final String password)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setKeystorePassword(" + password + ")");
      }
      if (sslConfig == null)
      {
         sslConfig = new SSLConfiguration() ;
      }
      hasBeenUpdated = true;
      sslConfig.setKeystorePassword(password) ;
   }
   
   public String getCertType()
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("getCertType()");
      }
      return (sslConfig == null ? null : sslConfig.getCertType()) ;
   }

   public void setCertType(final String certType)
   {
      if (ConnectionFactoryProperties.trace)
      {
         ConnectionFactoryProperties.log.trace("setCertType(" + certType + ")");
      }
      if (sslConfig == null)
      {
         sslConfig = new SSLConfiguration() ;
      }
      hasBeenUpdated = true;
      sslConfig.setCertType(certType) ;
   }
}
