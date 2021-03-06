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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for a message consumer
 *
 */
public class QpidRAMessageConsumer implements MessageConsumer
{
   /** The logger */
   private static final Logger _log = LoggerFactory.getLogger(QpidRAMessageConsumer.class);

   /** The wrapped message consumer */
   protected MessageConsumer consumer;
   /** The closed flag */
   private AtomicBoolean closed = new AtomicBoolean();

   /** The session for this consumer */
   protected QpidRASession session;

   /**
    * Create a new wrapper
    * @param consumer the consumer
    * @param session the session
    */
   public QpidRAMessageConsumer(final MessageConsumer consumer, final QpidRASession session)
   {
      this.consumer = consumer;
      this.session = session;

      if (_log.isTraceEnabled())
      {
         _log.trace("new QpidRAMessageConsumer " + this +
                                            " consumer=" +
                                            Util.asString(consumer) +
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
      if (_log.isTraceEnabled())
      {
         _log.trace("close " + this);
      }
      try
      {
         closeConsumer();
      }
      finally
      {
         session.removeConsumer(this);
      }
   }

   /**
    * Check state
    * @exception JMSException Thrown if an error occurs
    */
   void checkState() throws JMSException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("checkState()");
      }
      session.checkState();
   }

   /**
    * Get message listener
    * @return The listener
    * @exception JMSException Thrown if an error occurs
    */
   public MessageListener getMessageListener() throws JMSException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getMessageListener()");
      }

      checkState();
      session.checkStrict();
      return consumer.getMessageListener();
   }

   /**
    * Set message listener
    * @param listener The listener
    * @exception JMSException Thrown if an error occurs
    */
   public void setMessageListener(final MessageListener listener) throws JMSException
   {
      session.lock();
      try
      {
         checkState();
         session.checkStrict();
         if (listener == null)
         {
            consumer.setMessageListener(null);
         }
         else
         {
            consumer.setMessageListener(wrapMessageListener(listener));
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Get message selector
    * @return The selector
    * @exception JMSException Thrown if an error occurs
    */
   public String getMessageSelector() throws JMSException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getMessageSelector()");
      }

      checkState();
      return consumer.getMessageSelector();
   }

   /**
    * Receive
    * @return The message
    * @exception JMSException Thrown if an error occurs
    */
   public Message receive() throws JMSException
   {
      session.lock();
      try
      {
         if (_log.isTraceEnabled())
         {
            _log.trace("receive " + this);
         }

         checkState();
         // Make an explicit start check otherwise qpid starts the dispatcher
         Message message = (session.isStarted() ? consumer.receive() : null);

         if (_log.isTraceEnabled())
         {
            _log.trace("received " + this + " result=" + Util.asString(message));
         }

         if (message == null)
         {
            return null;
         }
         else
         {
            return wrapMessage(message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Receive
    * @param timeout The timeout value
    * @return The message
    * @exception JMSException Thrown if an error occurs
    */
   public Message receive(final long timeout) throws JMSException
   {
      session.lock();
      try
      {
         if (_log.isTraceEnabled())
         {
            _log.trace("receive " + this + " timeout=" + timeout);
         }

         checkState();
         // Make an explicit start check otherwise qpid starts the dispatcher
         Message message = (session.isStarted() ? consumer.receive(timeout) : null);

         if (_log.isTraceEnabled())
         {
            _log.trace("received " + this + " result=" + Util.asString(message));
         }

         if (message == null)
         {
            return null;
         }
         else
         {
            return wrapMessage(message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Receive
    * @return The message
    * @exception JMSException Thrown if an error occurs
    */
   public Message receiveNoWait() throws JMSException
   {
      session.lock();
      try
      {
         if (_log.isTraceEnabled())
         {
            _log.trace("receiveNoWait " + this);
         }

         checkState();
         // Make an explicit start check otherwise qpid starts the dispatcher
         Message message = (session.isStarted() ? consumer.receiveNoWait() : null);

         if (_log.isTraceEnabled())
         {
            _log.trace("received " + this + " result=" + Util.asString(message));
         }

         if (message == null)
         {
            return null;
         }
         else
         {
            return wrapMessage(message);
         }
      }
      finally
      {
         session.unlock();
      }
   }

   /**
    * Close consumer
    * @exception JMSException Thrown if an error occurs
    */
   void closeConsumer() throws JMSException
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("closeConsumer()");
      }

      if (!closed.getAndSet(true))
      {
         consumer.close();
      }
   }

   /**
    * Wrap message
    * @param message The message to be wrapped
    * @return The wrapped message
    */
   Message wrapMessage(final Message message)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("wrapMessage(" + Util.asString(message) + ")");
      }

      if (message instanceof BytesMessage)
      {
         return new QpidRABytesMessage((BytesMessage)message, session);
      }
      else if (message instanceof MapMessage)
      {
         return new QpidRAMapMessage((MapMessage)message, session);
      }
      else if (message instanceof ObjectMessage)
      {
         return new QpidRAObjectMessage((ObjectMessage)message, session);
      }
      else if (message instanceof StreamMessage)
      {
         return new QpidRAStreamMessage((StreamMessage)message, session);
      }
      else if (message instanceof TextMessage)
      {
         return new QpidRATextMessage((TextMessage)message, session);
      }
      return new QpidRAMessage(message, session);
   }

   /**
    * Wrap message listener
    * @param listener The listener to be wrapped
    * @return The wrapped listener
    */
   MessageListener wrapMessageListener(final MessageListener listener)
   {
      if (_log.isTraceEnabled())
      {
         _log.trace("getMessageSelector()");
      }

      return new QpidRAMessageListener(listener, this);
   }
}
