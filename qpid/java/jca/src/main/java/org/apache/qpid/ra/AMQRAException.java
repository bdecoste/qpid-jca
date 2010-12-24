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


/**
 * AMQ Resource Adapter exception.
 */
public class AMQRAException extends Exception
{
   /**
    * The serial version uid for this serializable class.
    */
   private static final long serialVersionUID = -8428168579371716101L;

   /**
    * Create a default AMQ ra exception.
    */
   public AMQRAException()
   {
      super();
   }

   /**
    * Create an AMQ ra exception with a specific message.
    * @param message The message associated with this exception.
    */
   public AMQRAException(final String message)
   {
      super(message);
   }

   /**
    * Create an AMQ ra exception with a specific cause.
    * @param cause The cause associated with this exception.
    */
   public AMQRAException(final Throwable cause)
   {
      super(cause);
   }

   /**
    * Create an AMQ ra exception with a specific message and cause.
    * @param message The message associated with this exception.
    * @param cause The cause associated with this exception.
    */
   public AMQRAException(final String message, final Throwable cause)
   {
      super(message, cause);
   }
}
