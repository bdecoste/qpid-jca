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

import javax.jms.Session;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;

import org.apache.qpid.jms.ConnectionURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection request information
 *
 */
public class QpidRAConnectionRequestInfo implements ConnectionRequestInfo
{
   /** The logger */
   private static final Logger _log = LoggerFactory.getLogger(QpidRAConnectionRequestInfo.class);

   /** The user name */
   private String userName;

   /** The password */
   private String password;

   /** The client id */
   private String clientID;

   /** The type */
   private final int type;

   /** Use transactions */
   private final boolean transacted;

   /** The acknowledge mode */
   private final int acknowledgeMode;

   /**
    * Constructor
    * @param ra The resource adapter.
    * @param type The connection type
    * @throws ResourceException 
    */
   public QpidRAConnectionRequestInfo(final QpidResourceAdapter ra, final int type)
      throws ResourceException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("constructor(" + ra + ")");
      }

      final QpidRAProperties properties = ra.getProperties() ;
      if (properties.getConnectionURL() != null)
      {
         final ConnectionURL connectionURL = ra.getDefaultAMQConnectionFactory().getConnectionURL() ;
         userName = connectionURL.getUsername();
         password = connectionURL.getPassword();
         clientID = connectionURL.getClientName();
      }
      else
      {
         userName = ra.getDefaultUserName();
         password = ra.getDefaultPassword();
         clientID = ra.getClientID();
      }
      this.type = type;
      transacted = true;
      acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
   }

   /**
    * Constructor
    * @param type The connection type
    */
   public QpidRAConnectionRequestInfo(final int type)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("constructor(" + type + ")");
      }

      this.type = type;
      transacted = true;
      acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
   }

   /**
    * Constructor
    * @param transacted Use transactions
    * @param acknowledgeMode The acknowledge mode
    * @param type The connection type
    */
   public QpidRAConnectionRequestInfo(final boolean transacted, final int acknowledgeMode, final int type)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("constructor(" + transacted +
                                                  ", " +
                                                  acknowledgeMode +
                                                  ", " +
                                                  type +
                                                  ")");
      }

      this.transacted = transacted;
      this.acknowledgeMode = acknowledgeMode;
      this.type = type;
   }

   /**
    * Fill in default values if they are missing
    * @param connectionURL The connection URL
    */
   public void setDefaults(final ConnectionURL connectionURL)
   {
      if (userName == null)
      {
         userName = connectionURL.getUsername();
      }
      if (password == null)
      {
         password = connectionURL.getPassword();
      }
      if (clientID == null)
      {
         clientID = connectionURL.getClientName();
      }
   }
   
   /**
    * Fill in default values if they are missing
    * @param ra The resource adapter
    * @throws ResourceException 
    */
   public void setDefaults(final QpidResourceAdapter ra)
      throws ResourceException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setDefaults(" + ra + ")");
      }
      
      final QpidRAProperties properties = ra.getProperties() ;
      if (properties.getConnectionURL() != null)
      {
         setDefaults(ra.getDefaultAMQConnectionFactory().getConnectionURL()) ;
      }
      else
      {
         if (userName == null)
         {
            userName = ra.getDefaultUserName();
         }
         if (password == null)
         {
            password = ra.getDefaultPassword();
         }
         if (clientID == null)
         {
            clientID = ra.getClientID();
         }
      }
   }

   /**
    * Get the user name
    * @return The value
    */
   public String getUserName()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getUserName()");
      }

      return userName;
   }

   /**
    * Set the user name
    * @param userName The value
    */
   public void setUserName(final String userName)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setUserName(" + userName + ")");
      }

      this.userName = userName;
   }

   /**
    * Get the password
    * @return The value
    */
   public String getPassword()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getPassword()");
      }

      return password;
   }

   /**
    * Set the password
    * @param password The value
    */
   public void setPassword(final String password)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setPassword(****)");
      }

      this.password = password;
   }

   /**
    * Get the client id
    * @return The value
    */
   public String getClientID()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getClientID()");
      }

      return clientID;
   }

   /**
    * Set the client id
    * @param clientID The value
    */
   public void setClientID(final String clientID)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("setClientID(" + clientID + ")");
      }

      this.clientID = clientID;
   }

   /**
    * Get the connection type
    * @return The type
    */
   public int getType()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getType()");
      }

      return type;
   }

   /**
    * Use transactions
    * @return True if transacted; otherwise false
    */
   public boolean isTransacted()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("isTransacted() " + transacted);
      }

      return transacted;
   }

   /**
    * Get the acknowledge mode
    * @return The mode
    */
   public int getAcknowledgeMode()
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getAcknowledgeMode()");
      }

      return acknowledgeMode;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * @param obj Object with which to compare
    * @return True if this object is the same as the obj argument; false otherwise.
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (obj == null)
      {
         return false;
      }

      if (obj instanceof QpidRAConnectionRequestInfo)
      {
         QpidRAConnectionRequestInfo you = (QpidRAConnectionRequestInfo)obj;
         return Util.compare(userName, you.getUserName()) && Util.compare(password, you.getPassword()) &&
                Util.compare(clientID, you.getClientID()) &&
                type == you.getType() &&
                transacted == you.isTransacted() &&
                acknowledgeMode == you.getAcknowledgeMode();
      }
      else
      {
         return false;
      }
   }

   /**
    * Return the hash code for the object
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      int hash = 7;

      hash += 31 * hash + (userName != null ? userName.hashCode() : 0);
      hash += 31 * hash + (password != null ? password.hashCode() : 0);
      hash += 31 * hash + Integer.valueOf(type).hashCode();
      hash += 31 * hash + (transacted ? 1 : 0);
      hash += 31 * hash + Integer.valueOf(acknowledgeMode).hashCode();

      return hash;
   }
   
   @Override
   public String toString()
   {
      return "QpidRAConnectionRequestInfo[type=" + type +
         ", transacted=" + transacted + ", acknowledgeMode=" + acknowledgeMode +
         ", clientID=" + clientID + ", userName=" + userName + ", password=" + password + "]";
   }
}
