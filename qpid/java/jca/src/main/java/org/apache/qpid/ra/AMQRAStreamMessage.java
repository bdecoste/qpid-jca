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
import javax.jms.StreamMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class AMQRAStreamMessage extends AMQRAMessage implements StreamMessage
{
   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(AMQRAStreamMessage.class);

   /** Whether trace is enabled */
   private static boolean trace = AMQRAStreamMessage.log.isTraceEnabled();

   /**
    * Create a new wrapper
    * @param message the message
    * @param session the session
    */
   public AMQRAStreamMessage(final StreamMessage message, final AMQRASession session)
   {
      super(message, session);

      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("constructor(" + message + ", " + session + ")");
      }
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public boolean readBoolean() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readBoolean()");
      }

      return ((StreamMessage)message).readBoolean();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public byte readByte() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readByte()");
      }

      return ((StreamMessage)message).readByte();
   }

   /**
    * Read 
    * @param value The value
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public int readBytes(final byte[] value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readBytes(" + value + ")");
      }

      return ((StreamMessage)message).readBytes(value);
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public char readChar() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readChar()");
      }

      return ((StreamMessage)message).readChar();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public double readDouble() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readDouble()");
      }

      return ((StreamMessage)message).readDouble();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public float readFloat() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readFloat()");
      }

      return ((StreamMessage)message).readFloat();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public int readInt() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readInt()");
      }

      return ((StreamMessage)message).readInt();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public long readLong() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readLong()");
      }

      return ((StreamMessage)message).readLong();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public Object readObject() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readObject()");
      }

      return ((StreamMessage)message).readObject();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public short readShort() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readShort()");
      }

      return ((StreamMessage)message).readShort();
   }

   /**
    * Read 
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public String readString() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("readString()");
      }

      return ((StreamMessage)message).readString();
   }

   /**
    * Reset 
    * @exception JMSException Thrown if an error occurs
    */
   public void reset() throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("reset()");
      }

      ((StreamMessage)message).reset();
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeBoolean(final boolean value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeBoolean(" + value + ")");
      }

      ((StreamMessage)message).writeBoolean(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeByte(final byte value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeByte(" + value + ")");
      }

      ((StreamMessage)message).writeByte(value);
   }

   /**
    * Write
    * @param value The value 
    * @param offset The offset
    * @param length The length
    * @exception JMSException Thrown if an error occurs
    */
   public void writeBytes(final byte[] value, final int offset, final int length) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeBytes(" + value + ", " + offset + ", " + length + ")");
      }

      ((StreamMessage)message).writeBytes(value, offset, length);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeBytes(final byte[] value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeBytes(" + value + ")");
      }

      ((StreamMessage)message).writeBytes(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeChar(final char value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeChar(" + value + ")");
      }

      ((StreamMessage)message).writeChar(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeDouble(final double value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeDouble(" + value + ")");
      }

      ((StreamMessage)message).writeDouble(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeFloat(final float value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeFloat(" + value + ")");
      }

      ((StreamMessage)message).writeFloat(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeInt(final int value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeInt(" + value + ")");
      }

      ((StreamMessage)message).writeInt(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeLong(final long value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeLong(" + value + ")");
      }

      ((StreamMessage)message).writeLong(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeObject(final Object value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeObject(" + value + ")");
      }

      ((StreamMessage)message).writeObject(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeShort(final short value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeShort(" + value + ")");
      }

      ((StreamMessage)message).writeShort(value);
   }

   /**
    * Write
    * @param value The value 
    * @exception JMSException Thrown if an error occurs
    */
   public void writeString(final String value) throws JMSException
   {
      if (AMQRAStreamMessage.trace)
      {
         AMQRAStreamMessage.log.trace("writeString(" + value + ")");
      }

      ((StreamMessage)message).writeString(value);
   }
}
