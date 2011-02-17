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

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XATopicConnection;
import javax.naming.BinaryRefAddr;
import javax.naming.Reference;
import javax.resource.spi.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connection factory
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision:  $
 */
public class QpidRAConnectionFactoryImpl implements QpidRAConnectionFactory
{
   /** Serial version UID */
   static final long serialVersionUID = 7981708919479859360L;

   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(QpidRAConnectionFactoryImpl.class);

   /** Trace enabled */
   private static boolean trace = QpidRAConnectionFactoryImpl.log.isTraceEnabled();

   /** The managed connection factory */
   private final QpidRAManagedConnectionFactory mcf;

   /** The connection manager */
   private ConnectionManager cm;

   /** Naming reference */
   private Reference reference;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param cm The connection manager
    */
   public QpidRAConnectionFactoryImpl(final QpidRAManagedConnectionFactory mcf, final ConnectionManager cm)
   {
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("constructor(" + mcf + ", " + cm + ")");
      }

      this.mcf = mcf;

      if (cm == null)
      {
         // This is standalone usage, no appserver
         this.cm = new QpidRAConnectionManager();
         if (QpidRAConnectionFactoryImpl.trace)
         {
            QpidRAConnectionFactoryImpl.log.trace("Created new ConnectionManager=" + this.cm);
         }
      }
      else
      {
         this.cm = cm;
      }

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Using ManagedConnectionFactory=" + mcf + ", ConnectionManager=" + cm);
      }
   }

   /**
    * Set the reference
    * @param reference The reference
    */
   public void setReference(final Reference reference)
   {
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("setReference(" + reference + ")");
      }

      this.reference = reference;
   }

   /**
    * Get the reference
    * @return The reference
    */
   public Reference getReference()
   {
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("getReference()");
      }
      if (reference == null)
      {
         try
         {
            reference = new Reference(this.getClass().getCanonicalName(),
                                      new BinaryRefAddr("QPID-CF", Util.serialize(this)),
                                      ConnectionFactoryObjectFactory.class.getCanonicalName(),
                                      null);
         }
         catch (final IOException ioe)
         {
            QpidRAConnectionFactoryImpl.log.error("Error while giving object Reference.", ioe);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createQueueConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.QUEUE_CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createQueueConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.QUEUE_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createTopicConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.TOPIC_CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createTopicConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.TOPIC_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf, cm, QpidRAConnectionFactory.CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf, cm, QpidRAConnectionFactory.CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXAQueueConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.XA_QUEUE_CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXAQueueConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.XA_QUEUE_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created queue connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXATopicConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.XA_TOPIC_CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXATopicConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf,
                                                                      cm,
                                                                      QpidRAConnectionFactory.XA_TOPIC_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created topic connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXAConnection()");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf, cm, QpidRAConnectionFactory.XA_CONNECTION);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created connection: " + s);
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
      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("createXAConnection(" + userName + ", ****)");
      }

      QpidRASessionFactoryImpl s = new QpidRASessionFactoryImpl(mcf, cm, QpidRAConnectionFactory.XA_CONNECTION);
      s.setUserName(userName);
      s.setPassword(password);

      if (QpidRAConnectionFactoryImpl.trace)
      {
         QpidRAConnectionFactoryImpl.log.trace("Created connection: " + s);
      }

      return s;
   }
}
