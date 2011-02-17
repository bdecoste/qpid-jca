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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQDestination;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.client.XAConnectionImpl;
import org.apache.qpid.protocol.AMQConstant;
import org.apache.qpid.ra.QpidResourceAdapter;
import org.apache.qpid.ra.Util;

/**
 * The activation.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class QpidActivation
{
   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(QpidActivation.class);

   /**
    * Trace enabled
    */
   private static boolean trace = QpidActivation.log.isTraceEnabled();

   /**
    * The onMessage method
    */
   public static final Method ONMESSAGE;

   /**
    * The resource adapter
    */
   private final QpidResourceAdapter ra;

   /**
    * The activation spec
    */
   private final QpidActivationSpec spec;

   /**
    * The message endpoint factory
    */
   private final MessageEndpointFactory endpointFactory;

   /**
    * Whether delivery is active
    */
   private final AtomicBoolean deliveryActive = new AtomicBoolean(false);

   /**
    * The destination type
    */
   private boolean isTopic = false;

   /**
    * Is the delivery transacted
    */
   private boolean isDeliveryTransacted;

   private AMQDestination destination;

   private final List<QpidMessageHandler> handlers = new ArrayList<QpidMessageHandler>();

   private AMQConnectionFactory factory;

   // Whether we are in the failure recovery loop
   private AtomicBoolean inFailure = new AtomicBoolean(false);
   
   static
   {
      try
      {
         ONMESSAGE = MessageListener.class.getMethod("onMessage", new Class[] { Message.class });
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Constructor
    *
    * @param ra              The resource adapter
    * @param endpointFactory The endpoint factory
    * @param spec            The activation spec
    * @throws ResourceException Thrown if an error occurs
    */
   public QpidActivation(final QpidResourceAdapter ra,
                            final MessageEndpointFactory endpointFactory,
                            final QpidActivationSpec spec) throws ResourceException
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("constructor(" + ra + ", " + endpointFactory + ", " + spec + ")");
      }

      this.ra = ra;
      this.endpointFactory = endpointFactory;
      this.spec = spec;
      try
      {
         isDeliveryTransacted = endpointFactory.isDeliveryTransacted(QpidActivation.ONMESSAGE);
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
   }

   /**
    * Get the activation spec
    *
    * @return The value
    */
   public QpidActivationSpec getActivationSpec()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("getActivationSpec()");
      }

      return spec;
   }

   /**
    * Get the message endpoint factory
    *
    * @return The value
    */
   public MessageEndpointFactory getMessageEndpointFactory()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("getMessageEndpointFactory()");
      }

      return endpointFactory;
   }

   /**
    * Get whether delivery is transacted
    *
    * @return The value
    */
   public boolean isDeliveryTransacted()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("isDeliveryTransacted()");
      }

      return isDeliveryTransacted;
   }

   /**
    * Get the work manager
    *
    * @return The value
    */
   public WorkManager getWorkManager()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("getWorkManager()");
      }

      return ra.getWorkManager();
   }

   /**
    * Is the destination a topic
    *
    * @return The value
    */
   public boolean isTopic()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("isTopic()");
      }

      return isTopic;
   }

   /**
    * Start the activation
    *
    * @throws ResourceException Thrown if an error occurs
    */
   public void start() throws ResourceException
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("start()");
      }
      deliveryActive.set(true);
      ra.getWorkManager().scheduleWork(new SetupActivation());
   }

   /**
    * Stop the activation
    */
   public void stop()
   {
      if (QpidActivation.trace)
      {
         QpidActivation.log.trace("stop()");
      }

      deliveryActive.set(false);
      teardown();
   }

   /**
    * Setup the activation
    *
    * @throws Exception Thrown if an error occurs
    */
   protected synchronized void setup() throws Exception
   {
      QpidActivation.log.debug("Setting up " + spec);
      setupCF();

      setupDestination();
      for (int i = 0; i < spec.getMaxSession(); i++)
      {
         Session session = null;

         try
         {
            final boolean useLocalTx = spec.isUseLocalTx() ;
            final AMQConnection connection ;
            if (isDeliveryTransacted && !useLocalTx)
            {
               final XAConnectionImpl xaConnection = (XAConnectionImpl)factory.createXAConnection() ;
               session = ra.createXASession(xaConnection) ;
               connection = xaConnection ;
            }
            else
            {
               connection = (AMQConnection)factory.createConnection() ;
               session = ra.createSession(connection,
                     spec.getAcknowledgeModeInt(),
                     useLocalTx,
                     spec.getPrefetchLow(),
                     spec.getPrefetchHigh());
            }

            QpidActivation.log.debug("Using queue connection " + session);
            QpidMessageHandler handler = new QpidMessageHandler(this, ra.getTM(), connection, session);
            handler.setup();
            handlers.add(handler);
         }
         catch (Exception e)
         {
            if (session != null)
            {
               try
               {
                  session.close();
               }
               catch (Exception e2)
               {
                  QpidActivation.log.trace("Ignored error closing session", e2);
               }
            }
            
            throw e;
         }
      }

      QpidActivation.log.debug("Setup complete " + this);
   }

   /**
    * Teardown the activation
    */
   protected synchronized void teardown()
   {
      QpidActivation.log.debug("Tearing down " + spec);

      for (QpidMessageHandler handler : handlers)
      {
         handler.teardown();
      }
      if (spec.isHasBeenUpdated())
      {
         factory = null;
      }
      QpidActivation.log.debug("Tearing down complete " + this);
   }

   protected void setupCF() throws Exception
   {
      if (spec.isHasBeenUpdated())
      {
         factory = ra.createAMQConnectionFactory(spec);
      }
      else
      {
         factory = ra.getDefaultAMQConnectionFactory();
      }
   }

   public Destination getDestination()
   {
      return destination;
   }

   protected void setupDestination() throws Exception
   {

      String destinationName = spec.getDestination();

      if (spec.isUseJNDI())
      {
         Context ctx = new InitialContext();
         QpidActivation.log.debug("Using context " + ctx.getEnvironment() + " for " + spec);
         if (QpidActivation.trace)
         {
            QpidActivation.log.trace("setupDestination(" + ctx + ")");
         }

         String destinationTypeString = spec.getDestinationType();
         if (destinationTypeString != null && !destinationTypeString.trim().equals(""))
         {
            QpidActivation.log.debug("Destination type defined as " + destinationTypeString);

            Class<? extends AMQDestination> destinationType;
            if (Topic.class.getName().equals(destinationTypeString))
            {
               destinationType = AMQTopic.class;
               isTopic = true;
            }
            else
            {
               destinationType = AMQQueue.class;
            }

            QpidActivation.log.debug("Retrieving destination " + destinationName +
                                        " of type " +
                                        destinationType.getName());
            destination = Util.lookup(ctx, destinationName, destinationType);
         }
         else
         {
            QpidActivation.log.debug("Destination type not defined");
            QpidActivation.log.debug("Retrieving destination " + destinationName +
                                        " of type " +
                                        Destination.class.getName());

            destination = Util.lookup(ctx, destinationName, AMQDestination.class);
            isTopic = (destination instanceof Topic) ;
         }
      }
      else
      {
         destination = (AMQDestination)AMQDestination.createDestination(spec.getDestination());
         isTopic = (destination instanceof Topic) ;
      }

      QpidActivation.log.debug("Got destination " + destination + " from " + destinationName);
   }

   /**
    * Get a string representation
    *
    * @return The value
    */
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(QpidActivation.class.getName()).append('(');
      buffer.append("spec=").append(spec.getClass().getName());
      buffer.append(" mepf=").append(endpointFactory.getClass().getName());
      buffer.append(" active=").append(deliveryActive.get());
      if (spec.getDestination() != null)
      {
         buffer.append(" destination=").append(spec.getDestination());
      }
      buffer.append(" transacted=").append(isDeliveryTransacted);
      buffer.append(')');
      return buffer.toString();
   }

   /**
    * Handles any failure by trying to reconnect
    * 
    * @param failure the reason for the failure
    */
   public void handleFailure(Throwable failure)
   {
      if(doesNotExist(failure))
      {
         log.info("awaiting topic/queue creation " + getActivationSpec().getDestination());
      }
      else
      {
         log.warn("Failure in Qpid activation " + spec, failure);
      }
      int reconnectCount = 0;
      int setupAttempts = spec.getSetupAttempts();
      long setupInterval = spec.getSetupInterval();
      
      // Only enter the failure loop once
      if (inFailure.getAndSet(true))
         return;
      try
      {
         while (deliveryActive.get() && (setupAttempts == -1 || reconnectCount < setupAttempts))
         {
            teardown();

            try
            {
               Thread.sleep(setupInterval);
            }
            catch (InterruptedException e)
            {
               log.debug("Interrupted trying to reconnect " + spec, e);
               break;
            }

            log.info("Attempting to reconnect " + spec);
            try
            {
               setup();
               log.info("Reconnected with Qpid");            
               break;
            }
            catch (Throwable t)
            {
               if(doesNotExist(failure))
               {
                  log.info("awaiting topic/queue creation " + getActivationSpec().getDestination());
               }
               else
               {
                  log.error("Unable to reconnect " + spec, t);
               }
            }
            ++reconnectCount;
         }
      }
      finally
      {
         // Leaving failure recovery loop
         inFailure.set(false);
      }
   }
   
   /**
    * Check to see if the failure represents a missing endpoint
    * @param failure The failure.
    * @return true if it represents a missing endpoint, false otherwise
    */
   private boolean doesNotExist(final Throwable failure)
   {
      return (failure instanceof AMQException) && (((AMQException)failure).getErrorCode() == AMQConstant.NOT_FOUND) ;
   }

   /**
    * Handles the setup
    */
   private class SetupActivation implements Work
   {
      public void run()
      {
         try
         {
            setup();
         }
         catch (Throwable t)
         {
            handleFailure(t);
         }
      }

      public void release()
      {
      }
   }
}
