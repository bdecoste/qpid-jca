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
package org.apache.qpid.ra.inflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
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

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQDestination;
import org.apache.qpid.client.BasicMessageConsumer;
import org.apache.qpid.client.BasicMessageProducer;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.ra.AMQResourceAdapter;
import org.apache.qpid.ra.Util;

/**
 * The activation.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class AMQActivation
{
   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(AMQActivation.class);

   /**
    * Trace enabled
    */
   private static boolean trace = AMQActivation.log.isTraceEnabled();

   /**
    * The onMessage method
    */
   public static final Method ONMESSAGE;

   /**
    * The resource adapter
    */
   private final AMQResourceAdapter ra;

   /**
    * The activation spec
    */
   private final AMQActivationSpec spec;

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

   /** The name of the temporary subscription name that all the sessions will share */
   private AMQShortString topicTemporaryQueue;

   private final List<AMQMessageHandler> handlers = new ArrayList<AMQMessageHandler>();

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
   public AMQActivation(final AMQResourceAdapter ra,
                            final MessageEndpointFactory endpointFactory,
                            final AMQActivationSpec spec) throws ResourceException
   {
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("constructor(" + ra + ", " + endpointFactory + ", " + spec + ")");
      }

      this.ra = ra;
      this.endpointFactory = endpointFactory;
      this.spec = spec;
      try
      {
         isDeliveryTransacted = endpointFactory.isDeliveryTransacted(AMQActivation.ONMESSAGE);
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
   public AMQActivationSpec getActivationSpec()
   {
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("getActivationSpec()");
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
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("getMessageEndpointFactory()");
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
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("isDeliveryTransacted()");
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
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("getWorkManager()");
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
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("isTopic()");
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
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("start()");
      }
      deliveryActive.set(true);
      ra.getWorkManager().scheduleWork(new SetupActivation());
   }

   /**
    * @return the topicTemporaryQueue
    */
   public AMQShortString getTopicTemporaryQueue()
   {
      return topicTemporaryQueue;
   }

   /**
    * @param topicTemporaryQueue the topicTemporaryQueue to set
    */
   public void setTopicTemporaryQueue(AMQShortString topicTemporaryQueue)
   {
      this.topicTemporaryQueue = topicTemporaryQueue;
   }

   /**
    * Stop the activation
    */
   public void stop()
   {
      if (AMQActivation.trace)
      {
         AMQActivation.log.trace("stop()");
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
      AMQActivation.log.debug("Setting up " + spec);
      setupCF();

      setupDestination();
      for (int i = 0; i < spec.getMaxSession(); i++)
      {
         Session session = null;

         try
         {
            session = setupSession();
            AMQMessageHandler handler = new AMQMessageHandler(this, ra.getTM(), session, i);
            handler.setup();
            handlers.add(handler);
         }
         catch (Exception e)
         {
            if (session != null)
            {
               session.close();
            }
            
            throw e;
         }
      }

      AMQActivation.log.debug("Setup complete " + this);
   }

   /**
    * Teardown the activation
    */
   protected synchronized void teardown()
   {
      AMQActivation.log.debug("Tearing down " + spec);

      for (AMQMessageHandler handler : handlers)
      {
         handler.teardown();
      }
      if (spec.isHasBeenUpdated())
      {
//         factory.close();   KEV
         factory = null;
      }
      AMQActivation.log.debug("Tearing down complete " + this);
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

   /**
    * Setup a session
    * @return The connection
    * @throws Exception Thrown if an error occurs
    */
   protected Session setupSession() throws Exception
   {
      Session result = null;

      try
      {
         result = ra.createSession(factory.getServerLocator().createSessionFactory(),
                                   spec.getAcknowledgeModeInt(),
                                   spec.getUser(),
                                   spec.getPassword(),
                                   ra.getPreAcknowledge(),
                                   ra.getDupsOKBatchSize(),
                                   ra.getTransactionBatchSize(),
                                   isDeliveryTransacted,
                                   spec.isUseLocalTx(),
                                   spec.getTransactionTimeout());

         AMQActivation.log.debug("Using queue connection " + result);

         return result;
      }
      catch (Throwable t)
      {
         try
         {
            if (result != null)
            {
               result.close();
            }
         }
         catch (Exception e)
         {
            AMQActivation.log.trace("Ignored error closing connection", e);
         }
         if (t instanceof Exception)
         {
            throw (Exception)t;
         }
         throw new RuntimeException("Error configuring connection", t);
      }
   }

   public AMQShortString getAddress()
   {
      return destination.getSimpleAddress();
   }

   protected void setupDestination() throws Exception
   {

      String destinationName = spec.getDestination();

      if (spec.isUseJNDI())
      {
         Context ctx = new InitialContext();
         AMQActivation.log.debug("Using context " + ctx.getEnvironment() + " for " + spec);
         if (AMQActivation.trace)
         {
            AMQActivation.log.trace("setupDestination(" + ctx + ")");
         }

         String destinationTypeString = spec.getDestinationType();
         if (destinationTypeString != null && !destinationTypeString.trim().equals(""))
         {
            AMQActivation.log.debug("Destination type defined as " + destinationTypeString);

            Class<?> destinationType;
            if (Topic.class.getName().equals(destinationTypeString))
            {
               destinationType = Topic.class;
               isTopic = true;
            }
            else
            {
               destinationType = Queue.class;
            }

            AMQActivation.log.debug("Retrieving destination " + destinationName +
                                        " of type " +
                                        destinationType.getName());
            try
            {
               destination = (AMQDestination)Util.lookup(ctx, destinationName, destinationType);
            }
            catch (Exception e)
            {
               if (destinationName == null)
               {
                  throw e;
               }
               // If there is no binding on naming, we will just create a new instance
               if (isTopic)
               {
                  destination = (AMQDestination)AMQJMSClient.createTopic(destinationName.substring(destinationName.lastIndexOf('/') + 1));
               }
               else
               {
                  destination = (AMQDestination)AMQJMSClient.createQueue(destinationName.substring(destinationName.lastIndexOf('/') + 1));
               }
            }
         }
         else
         {
            AMQActivation.log.debug("Destination type not defined");
            AMQActivation.log.debug("Retrieving destination " + destinationName +
                                        " of type " +
                                        Destination.class.getName());

            destination = (AMQDestination)Util.lookup(ctx, destinationName, Destination.class);
            if (destination instanceof Topic)
            {
               isTopic = true;
            }
         }
      }
      else
      {
         if (Topic.class.getName().equals(spec.getDestinationType()))
         {
            destination = (AMQDestination)AMQJMSClient.createTopic(spec.getDestination());
            isTopic = true;
         }
         else
         {
            destination = (AMQDestination)AMQJMSClient.createQueue(spec.getDestination());
         }
      }

      AMQActivation.log.debug("Got destination " + destination + " from " + destinationName);
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
      buffer.append(AMQActivation.class.getName()).append('(');
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
         log.warn("Failure in AMQ activation " + spec, failure);
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
               log.info("Reconnected with AMQ");            
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
//      KEV - investigate qpid alternative if necessary
//      return (failure instanceof AMQException && ((AMQException)failure).getCode() == AMQException.QUEUE_DOES_NOT_EXIST) ;
      return false ;
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
