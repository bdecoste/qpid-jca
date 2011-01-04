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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XATopicConnection;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.spi.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hornetq.jms.referenceable.ConnectionFactoryObjectFactory;
import org.hornetq.jms.referenceable.SerializableObjectRefAddr;

/**
 * The connection factory
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision:  $
 */
public class AMQRAConnectionFactoryImpl implements AMQRAConnectionFactory
{
   /** Serial version UID */
   static final long serialVersionUID = 7981708919479859360L;

   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(AMQRAConnectionFactoryImpl.class);

   /** Trace enabled */
   private static boolean trace = AMQRAConnectionFactoryImpl.log.isTraceEnabled();

   /** The managed connection factory */
   private final AMQRAManagedConnectionFactory mcf;

   /** The connection manager */
   private ConnectionManager cm;

   /** Naming reference */
   private Reference reference;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param cm The connection manager
    */
   public AMQRAConnectionFactoryImpl(final AMQRAManagedConnectionFactory mcf, final ConnectionManager cm)
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("constructor(" + mcf + ", " + cm + ")");
      }

      this.mcf = mcf;

      if (cm == null)
      {
         // This is standalone usage, no appserver
         this.cm = new AMQRAConnectionManager();
         if (AMQRAConnectionFactoryImpl.trace)
         {
            AMQRAConnectionFactoryImpl.log.trace("Created new ConnectionManager=" + this.cm);
         }
      }
      else
      {
         this.cm = cm;
      }

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Using ManagedConnectionFactory=" + mcf + ", ConnectionManager=" + cm);
      }
   }

   /**
    * Set the reference
    * @param reference The reference
    */
   public void setReference(final Reference reference)
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("setReference(" + reference + ")");
      }

      this.reference = reference;
   }

   /**
    * Get the reference
    * @return The reference
    */
   public Reference getReference()
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("getReference()");
      }
      if (reference == null)
      {
         try
         {
            reference = new Reference(this.getClass().getCanonicalName(),
                                      new SerializableObjectRefAddr("HornetQ-CF", this),
                                      ConnectionFactoryObjectFactory.class.getCanonicalName(),
                                      null);
         }
         catch (NamingException e)
         {
            AMQRAConnectionFactoryImpl.log.error("Error while giving object Reference.", ioe);
         }
      }

      return reference;

   }

   /**
    * Create a queue connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public QueueConnection createQueueConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createQueueConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.QUEUE_CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
      }

      return s;
   }

   /**
    * Create a queue connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public QueueConnection createQueueConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createQueueConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.QUEUE_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
      }

      return s;
   }

   /**
    * Create a topic connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public TopicConnection createTopicConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createTopicConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.TOPIC_CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
      }

      return s;
   }

   /**
    * Create a topic connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public TopicConnection createTopicConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createTopicConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.TOPIC_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
      }

      return s;
   }

   /**
    * Create a connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public Connection createConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf, cm, AMQRAConnectionFactory.CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created connection: " + s);
      }

      return s;
   }

   /**
    * Create a connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public Connection createConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf, cm, AMQRAConnectionFactory.CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA queue connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XAQueueConnection createXAQueueConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXAQueueConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.XA_QUEUE_CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA  queue connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XAQueueConnection createXAQueueConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXAQueueConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.XA_QUEUE_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA topic connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XATopicConnection createXATopicConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXATopicConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.XA_TOPIC_CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA topic connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XATopicConnection createXATopicConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXATopicConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      AMQRAConnectionFactory.XA_TOPIC_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XAConnection createXAConnection() throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXAConnection()");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf, cm, AMQRAConnectionFactory.XA_CONNECTION);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created connection: " + s);
      }

      return s;
   }

   /**
    * Create a XA connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public XAConnection createXAConnection(final String userName, final String password) throws JMSException
   {
      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("createXAConnection(" + userName + ", ****)");
      }

      AMQRASessionFactoryImpl s = new AMQRASessionFactoryImpl(mcf, cm, AMQRAConnectionFactory.XA_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (AMQRAConnectionFactoryImpl.trace)
      {
         AMQRAConnectionFactoryImpl.log.trace("Created connection: " + s);
      }

      return s;
   }
}
