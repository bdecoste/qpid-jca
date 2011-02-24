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
   private static final Logger _log = LoggerFactory.getLogger(ConnectionFactoryProperties.class);

   private boolean hasBeenUpdated = false;

   private String clientID;
   
   private String connectionURL;

   private String defaultPassword;

   private String defaultUsername;

   private String host;

   private Integer port;

   private String path;
   
   private SSLConfiguration sslConfig;
   
   public String getClientID()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getClientID()");
      }
      return clientID;
   }

   public void setClientID(final String clientID)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setClientID(" + clientID + ")");
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
      if (_log.isTraceEnabled())
      {
         _log.trace("getConnectionURL()");
      }
      return connectionURL;
   }

   public void setConnectionURL(final String connectionURL)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setConnectionURL(" + connectionURL + ")");
      }
      hasBeenUpdated = true;
      this.connectionURL = connectionURL;
   }

   public String getDefaultPassword()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getDefaultPassword()");
      }
      return defaultPassword;
   }

   public void setDefaultPassword(final String defaultPassword)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setDefaultPassword(" + defaultPassword + ")");
      }
      hasBeenUpdated = true;
      this.defaultPassword = defaultPassword;
   }

   public String getDefaultUsername()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getDefaultUsername()");
      }
      return defaultUsername;
   }

   public void setDefaultUsername(final String defaultUsername)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setDefaultUsername(" + defaultUsername + ")");
      }
      hasBeenUpdated = true;
      this.defaultUsername = defaultUsername;
   }

   public String getHost()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getHost()");
      }
      return host;
   }

   public void setHost(final String host)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setHost(" + host + ")");
      }
      hasBeenUpdated = true;
      this.host = host;
   }

   public Integer getPort()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getPort()");
      }
      return port;
   }

   public void setPort(final Integer port)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setPort(" + port + ")");
      }
      hasBeenUpdated = true;
      this.port = port;
   }

   public String getPath()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getPath()");
      }
      return path;
   }

   public void setPath(final String path)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setPath(" + path + ")");
      }
      hasBeenUpdated = true;
      this.path = path;
   }

   public SSLConfiguration getSSLConfig()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getSSLConfig()");
      }
      return sslConfig;
   }
   
   public void setSSLConfig(final SSLConfiguration sslConfig)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setSSLConfig(" + sslConfig + ")");
      }
      hasBeenUpdated = true;
      this.sslConfig = sslConfig;
   }
   
   public String getKeystorePath()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getKeystorePath()");
      }
      return (sslConfig == null ? null : sslConfig.getKeystorePath()) ;
   }

   public void setKeystorePath(final String path)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setKeystorePath(" + path + ")");
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
      if (_log.isTraceEnabled())
      {
         _log.trace("getKeystorePassword()");
      }
      return (sslConfig == null ? null : sslConfig.getKeystorePassword()) ;
   }

   public void setKeystorePassword(final String password)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setKeystorePassword(" + password + ")");
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
      if (_log.isTraceEnabled())
      {
         _log.trace("getCertType()");
      }
      return (sslConfig == null ? null : sslConfig.getCertType()) ;
   }

   public void setCertType(final String certType)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setCertType(" + certType + ")");
      }
      if (sslConfig == null)
      {
         sslConfig = new SSLConfiguration() ;
      }
      hasBeenUpdated = true;
      sslConfig.setCertType(certType) ;
   }
}
