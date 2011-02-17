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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QpidRAMessageProducer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision:  $
 */
public class QpidRAMessageProducer implements MessageProducer
{
   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(QpidRAMessageProducer.class);

   /** Whether trace is enabled */
   private static boolean trace = QpidRAMessageProducer.log.isTraceEnabled();

   /** The wrapped message producer */
   protected MessageProducer producer;

   /** The session for this consumer */
   protected QpidRASession session;

   /**
    * Create a new wrapper
    * @param producer the producer
    * @param session the session
    */
   public QpidRAMessageProducer(final MessageProducer producer, final QpidRASession session)
   {
      this.producer = producer;
      this.session = session;

      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("new QpidRAMessageProducer " + this +
                                            " producer=" +
                                            producer +
                                            " session=" +
                                            session);
      }
   }

   /**
    * Close
    * @exception JMSException Thrown if an error occurs
    */
   public void close() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("close " + this);
      }
      try
      {
         closeProducer();
      }
      finally
      {
         session.removeProducer(this);
      }
   }

   /**
    * Send message
    * @param destination The destination
    * @param message The message
    * @param deliveryMode The delivery mode
    * @param priority The priority
    * @param timeToLive The time to live
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Destination destination,
                    final Message message,
                    final int deliveryMode,
                    final int priority,
                    final long timeToLive) throws JMSException
   {
      session.lock();
      try
      {
         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("send " + this +
                                               " destination=" +
                                               destination +
                                               " message=" +
                                               message +
                                               " deliveryMode=" +
                                               deliveryMode +
                                               " priority=" +
                                               priority +
                                               " ttl=" +
                                               timeToLive);
         }

         checkState();

         producer.send(destination, message, deliveryMode, priority, timeToLive);

         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Send message
    * @param destination The destination
    * @param message The message
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Destination destination, final Message message) throws JMSException
   {
      session.lock();
      try
      {
         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("send " + this + " destination=" + destination + " message=" + message);
         }

         checkState();

         producer.send(destination, message);

         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Send message
    * @param message The message
    * @param deliveryMode The delivery mode
    * @param priority The priority
    * @param timeToLive The time to live
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException
   {
      session.lock();
      try
      {
         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("send " + this +
                                               " message=" +
                                               message +
                                               " deliveryMode=" +
                                               deliveryMode +
                                               " priority=" +
                                               priority +
                                               " ttl=" +
                                               timeToLive);
         }

         checkState();

         producer.send(message, deliveryMode, priority, timeToLive);

         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Send message
    * @param message The message
    * @exception JMSException Thrown if an error occurs
    */
   public void send(final Message message) throws JMSException
   {
      session.lock();
      try
      {
         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("send " + this + " message=" + message);
         }

         checkState();

         producer.send(message);

         if (QpidRAMessageProducer.trace)
         {
            QpidRAMessageProducer.log.trace("sent " + this + " result=" + message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Get the delivery mode
    * @return The mode
    * @exception JMSException Thrown if an error occurs
    */
   public int getDeliveryMode() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getDeliveryMode()");
      }

      return producer.getDeliveryMode();
   }

   /**
    * Get the destination
    * @return The destination
    * @exception JMSException Thrown if an error occurs
    */
   public Destination getDestination() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getDestination()");
      }

      return producer.getDestination();
   }

   /**
    * Disable message id
    * @return True if disable
    * @exception JMSException Thrown if an error occurs
    */
   public boolean getDisableMessageID() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getDisableMessageID()");
      }

      return producer.getDisableMessageID();
   }

   /**
    * Disable message timestamp
    * @return True if disable
    * @exception JMSException Thrown if an error occurs
    */
   public boolean getDisableMessageTimestamp() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getDisableMessageTimestamp()");
      }

      return producer.getDisableMessageTimestamp();
   }

   /**
    * Get the priority
    * @return The priority
    * @exception JMSException Thrown if an error occurs
    */
   public int getPriority() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getPriority()");
      }

      return producer.getPriority();
   }

   /**
    * Get the time to live
    * @return The ttl
    * @exception JMSException Thrown if an error occurs
    */
   public long getTimeToLive() throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("getTimeToLive()");
      }

      return producer.getTimeToLive();
   }

   /**
    * Set the delivery mode
    * @param deliveryMode The mode
    * @exception JMSException Thrown if an error occurs
    */
   public void setDeliveryMode(final int deliveryMode) throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("setDeliveryMode(" + deliveryMode + ")");
      }

      producer.setDeliveryMode(deliveryMode);
   }

   /**
    * Set disable message id
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setDisableMessageID(final boolean value) throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("setDisableMessageID(" + value + ")");
      }

      producer.setDisableMessageID(value);
   }

   /**
    * Set disable message timestamp
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setDisableMessageTimestamp(final boolean value) throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("setDisableMessageTimestamp(" + value + ")");
      }

      producer.setDisableMessageTimestamp(value);
   }

   /**
    * Set the priority
    * @param defaultPriority The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setPriority(final int defaultPriority) throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("setPriority(" + defaultPriority + ")");
      }

      producer.setPriority(defaultPriority);
   }

   /**
    * Set the ttl
    * @param timeToLive The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setTimeToLive(final long timeToLive) throws JMSException
   {
      if (QpidRAMessageProducer.trace)
      {
         QpidRAMessageProducer.log.trace("setTimeToLive(" + timeToLive + ")");
      }

      producer.setTimeToLive(timeToLive);
   }

   /**
    * Check state
    * @exception JMSException Thrown if an error occurs
    */
   void checkState() throws JMSException
   {
      session.checkState();
   }

   /**
    * Close producer
    * @exception JMSException Thrown if an error occurs
    */
   void closeProducer() throws JMSException
   {
      producer.close();
   }
}
