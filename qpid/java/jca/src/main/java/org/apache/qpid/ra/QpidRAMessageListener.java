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

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for a message listener
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class QpidRAMessageListener implements MessageListener
{
   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(QpidRAMessageListener.class);

   /** Whether trace is enabled */
   private static boolean trace = QpidRAMessageListener.log.isTraceEnabled();

   /** The message listener */
   private final MessageListener listener;

   /** The consumer */
   private final QpidRAMessageConsumer consumer;

   /**
    * Create a new wrapper
    * @param listener the listener
    * @param consumer the consumer
    */
   public QpidRAMessageListener(final MessageListener listener, final QpidRAMessageConsumer consumer)
   {
      if (QpidRAMessageListener.trace)
      {
         QpidRAMessageListener.log.trace("constructor(" + listener + ", " + consumer + ")");
      }

      this.listener = listener;
      this.consumer = consumer;
   }

   /**
    * On message
    * @param message The message
    */
   public void onMessage(Message message)
   {
      if (QpidRAMessageListener.trace)
      {
         QpidRAMessageListener.log.trace("onMessage(" + message + ")");
      }

      message = consumer.wrapMessage(message);
      listener.onMessage(message);
   }
}
