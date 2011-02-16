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
package org.apache.qpid.ra.inflow;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.XASession;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message handler
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class QpidMessageHandler implements MessageListener
{
   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(QpidMessageHandler.class);

   /**
    * Trace enabled
    */
   private static boolean trace = QpidMessageHandler.log.isTraceEnabled();

   /**
    * The session
    */
   private final Session session;

   private MessageConsumer consumer;

   /**
    * The endpoint
    */
   private MessageEndpoint endpoint;

   private final QpidActivation activation;

   private boolean useLocalTx;
   
   private boolean transacted;

   private final TransactionManager tm;

   public QpidMessageHandler(final QpidActivation activation,
                                final TransactionManager tm,
                                final Session session)
   {
      this.activation = activation;
      this.session = session;
      this.tm = tm;
   }

   public void setup() throws Exception
   {
      if (QpidMessageHandler.trace)
      {
         QpidMessageHandler.log.trace("setup()");
      }

      QpidActivationSpec spec = activation.getActivationSpec();
      String selector = spec.getMessageSelector();

      // Create the message consumer
      if (activation.isTopic())
      {
         final Topic topic = (Topic) activation.getDestination();
         final String subscriptionName = spec.getSubscriptionName();
         if (spec.isSubscriptionDurable())
            consumer = session.createDurableSubscriber(topic, subscriptionName, selector, false);
         else
            consumer = session.createConsumer(topic, selector) ;
      }
      else
      {
         final Queue queue = (Queue) activation.getDestination();
         consumer = session.createConsumer(queue, selector);
      }

      // Create the endpoint, if we are transacted pass the session so it is enlisted, unless using Local TX
      MessageEndpointFactory endpointFactory = activation.getMessageEndpointFactory();
      useLocalTx = !activation.isDeliveryTransacted() && activation.getActivationSpec().isUseLocalTx();
      transacted = activation.isDeliveryTransacted() || useLocalTx ;
      if (activation.isDeliveryTransacted() && !activation.getActivationSpec().isUseLocalTx())
      {
         final XAResource xaResource = ((XASession)session).getXAResource() ;
         endpoint = endpointFactory.createEndpoint(xaResource);
      }
      else
      {
         endpoint = endpointFactory.createEndpoint(null);
      }
      consumer.setMessageListener(this);
   }

   /**
    * Stop the handler
    */
   public void teardown()
   {
      if (QpidMessageHandler.trace)
      {
         QpidMessageHandler.log.trace("teardown()");
      }

      try
      {
         if (endpoint != null)
         {
            endpoint.release();
            endpoint = null;
         }
      }
      catch (Throwable t)
      {
         QpidMessageHandler.log.debug("Error releasing endpoint " + endpoint, t);
      }

      try
      {
         consumer.close();
      }
      catch (Throwable t)
      {
         QpidMessageHandler.log.debug("Error closing core-queue consumer", t);
      }

      try
      {
         if (session != null)
         {
            session.close();
         }
      }
      catch (Throwable t)
      {
         QpidMessageHandler.log.debug("Error releasing session " + session, t);
      }
   }

   public void onMessage(final Message message)
   {
      if (QpidMessageHandler.trace)
      {
         QpidMessageHandler.log.trace("onMessage(" + message + ")");
      }

      boolean beforeDelivery = false;

      try
      {
         if (activation.getActivationSpec().getTransactionTimeout() > 0 && tm != null)
         {
            tm.setTransactionTimeout(activation.getActivationSpec().getTransactionTimeout());
         }
         endpoint.beforeDelivery(QpidActivation.ONMESSAGE);
         beforeDelivery = true;
         
         //In the transacted case the message must be acked *before* onMessage is called
         
         if (transacted)
         {
            message.acknowledge();
         }
         
         ((MessageListener)endpoint).onMessage(message);
         
         if (!transacted)
         {
            message.acknowledge();
         }
         
         try
         {
            endpoint.afterDelivery();
         }
         catch (ResourceException e)
         {
            QpidMessageHandler.log.warn("Unable to call after delivery", e);
            return;
         }
         if (useLocalTx)
         {
            session.commit();
         }
      }
      catch (Throwable e)
      {
         QpidMessageHandler.log.error("Failed to deliver message", e);
         // we need to call before/afterDelivery as a pair
         if (beforeDelivery)
         {
            try
            {
               endpoint.afterDelivery();
            }
            catch (ResourceException e1)
            {
               QpidMessageHandler.log.warn("Unable to call after delivery", e);
            }
         }
         if (useLocalTx || !activation.isDeliveryTransacted())
         {
            try
            {
               session.rollback();
            }
            catch (JMSException e1)
            {
               QpidMessageHandler.log.warn("Unable to roll local transaction back", e1);
            }
         }
      }

   }

}
