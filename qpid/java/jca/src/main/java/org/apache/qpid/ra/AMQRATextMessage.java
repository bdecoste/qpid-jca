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

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class AMQRATextMessage extends AMQRAMessage implements TextMessage
{
   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(AMQRATextMessage.class);

   /** Whether trace is enabled */
   private static boolean trace = AMQRATextMessage.log.isTraceEnabled();

   /**
    * Create a new wrapper
    * @param message the message
    * @param session the session
    */
   public AMQRATextMessage(final TextMessage message, final AMQRASession session)
   {
      super(message, session);

      if (AMQRATextMessage.trace)
      {
         AMQRATextMessage.log.trace("constructor(" + message + ", " + session + ")");
      }
   }

   /**
    * Get text
    * @return The text
    * @exception JMSException Thrown if an error occurs
    */
   public String getText() throws JMSException
   {
      if (AMQRATextMessage.trace)
      {
         AMQRATextMessage.log.trace("getText()");
      }

      return ((TextMessage)message).getText();
   }

   /**
    * Set text
    * @param string The text
    * @exception JMSException Thrown if an error occurs
    */
   public void setText(final String string) throws JMSException
   {
      if (AMQRATextMessage.trace)
      {
         AMQRATextMessage.log.trace("setText(" + string + ")");
      }

      ((TextMessage)message).setText(string);
   }
}
