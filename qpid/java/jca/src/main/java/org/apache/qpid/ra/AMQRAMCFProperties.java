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

import java.io.Serializable;

import javax.jms.Queue;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MCF default properties - these are set in the <tx-connection-factory> at the jms-ds.xml
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @version $Revision: $
 */
public class AMQRAMCFProperties extends ConnectionFactoryProperties implements Serializable
{
   /**
    * Serial version UID
    */
   static final long serialVersionUID = -5951352236582886862L;

   /**
    * The logger
    */
   private static final Logger log = LoggerFactory.getLogger(AMQRAMCFProperties.class);

   /**
    * Trace enabled
    */
   private static boolean trace = AMQRAMCFProperties.log.isTraceEnabled();

   /**
    * The queue type
    */
   private static final String QUEUE_TYPE = Queue.class.getName();

   /**
    * The topic type
    */
   private static final String TOPIC_TYPE = Topic.class.getName();

   public String strConnectionParameters;

   public String strBackupConnectionParameters;

   /**
    * The connection type
    */
   private int type = AMQRAConnectionFactory.CONNECTION;

   /**
    * Use tryLock
    */
   private Integer useTryLock;

   /**
    * Constructor
    */
   public AMQRAMCFProperties()
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("constructor()");
      }

      useTryLock = null;
   }

   /**
    * Get the connection type
    *
    * @return The type
    */
   public int getType()
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("getType()");
      }

      return type;
   }

   /**
    * @return the connectionParameters
    */
   public String getStrConnectionParameters()
   {
      return strConnectionParameters;
   }

   public void setConnectionParameters(final String configuration)
   {
      strConnectionParameters = configuration;
      setParsedConnectionParameters(Util.parseConfig(configuration));
   }

   /**
    * @return the connectionParameters
    */
   public String getBackupConnectionParameters()
   {
      return strBackupConnectionParameters;
   }

   public void setBackupConnectionParameters(final String configuration)
   {
      strBackupConnectionParameters = configuration;
      setParsedBackupConnectionParameters(Util.parseConfig(configuration));
   }

   /**
    * Set the default session type.
    *
    * @param defaultType either javax.jms.Topic or javax.jms.Queue
    */
   public void setSessionDefaultType(final String defaultType)
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("setSessionDefaultType(" + type + ")");
      }

      if (defaultType.equals(AMQRAMCFProperties.QUEUE_TYPE))
      {
         type = AMQRAConnectionFactory.QUEUE_CONNECTION;
      }
      else if (defaultType.equals(AMQRAMCFProperties.TOPIC_TYPE))
      {
         type = AMQRAConnectionFactory.TOPIC_CONNECTION;
      }
      else
      {
         type = AMQRAConnectionFactory.CONNECTION;
      }
   }

   /**
    * Get the default session type.
    *
    * @return The default session type
    */
   public String getSessionDefaultType()
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("getSessionDefaultType()");
      }

      if (type == AMQRAConnectionFactory.CONNECTION)
      {
         return "BOTH";
      }
      else if (type == AMQRAConnectionFactory.QUEUE_CONNECTION)
      {
         return AMQRAMCFProperties.TOPIC_TYPE;
      }
      else
      {
         return AMQRAMCFProperties.QUEUE_TYPE;
      }
   }

   /**
    * Get the useTryLock.
    *
    * @return the useTryLock.
    */
   public Integer getUseTryLock()
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("getUseTryLock()");
      }

      return useTryLock;
   }

   /**
    * Set the useTryLock.
    *
    * @param useTryLock the useTryLock.
    */
   public void setUseTryLock(final Integer useTryLock)
   {
      if (AMQRAMCFProperties.trace)
      {
         AMQRAMCFProperties.log.trace("setUseTryLock(" + useTryLock + ")");
      }

      this.useTryLock = useTryLock;
   }
}
