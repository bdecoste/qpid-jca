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
using System;
using System.Threading;
using log4net;
using Apache.Qpid.Buffer;
using Apache.Qpid.Client.Message;
using Apache.Qpid.Messaging;
using Apache.Qpid.Framing;

namespace Apache.Qpid.Client
{
   public class BasicMessageProducer : Closeable, IMessagePublisher
   {
      protected readonly ILog _logger = LogManager.GetLogger(typeof(BasicMessageProducer));

      /// <summary>
      /// If true, messages will not get a timestamp.
      /// </summary>
      private bool _disableTimestamps;

      /// <summary>
      /// Priority of messages created by this producer.
      /// </summary>
      private int _messagePriority;

      /// <summary>
      /// Time to live of messages. Specified in milliseconds but AMQ has 1 second resolution.
      /// </summary>
      private long _timeToLive;

      /// <summary>
      /// Delivery mode used for this producer.
      /// </summary>
      private DeliveryMode _deliveryMode;

      private bool _immediate;
      private bool _mandatory;

      string _exchangeName;
      string _routingKey;

      /// <summary>
      /// Default encoding used for messages produced by this producer.
      /// </summary>
      private string _encoding;

      /// <summary>
      /// Default encoding used for message produced by this producer.
      /// </summary>
      private string _mimeType;

      /// <summary>
      /// True if this producer was created from a transacted session
      /// </summary>
      private bool _transacted;

      private ushort _channelId;

      /// <summary>
      /// This is an id generated by the session and is used to tie individual producers to the session. This means we
      /// can deregister a producer with the session when the producer is closed. We need to be able to tie producers
      /// to the session so that when an error is propagated to the session it can close the producer (meaning that
      /// a client that happens to hold onto a producer reference will get an error if he tries to use it subsequently).
      /// </summary>
      private long _producerId;

      /// <summary>
      /// The session used to create this producer
      /// </summary>
      private AmqChannel _channel;

      public BasicMessageProducer(string exchangeName, string routingKey,
          bool transacted,
          ushort channelId,
          AmqChannel channel,
          long producerId,
          DeliveryMode deliveryMode,
          long timeToLive,
          bool immediate,
          bool mandatory,
          int priority)
      {
         _exchangeName = exchangeName;
         _routingKey = routingKey;
         _transacted = transacted;
         _channelId = channelId;
         _channel = channel;
         _producerId = producerId;
         _deliveryMode = deliveryMode;
         _timeToLive = timeToLive;
         _immediate = immediate;
         _mandatory = mandatory;
         _messagePriority = priority;

         _channel.RegisterProducer(producerId, this);
      }


      #region IMessagePublisher Members

      public DeliveryMode DeliveryMode
      {
         get
         {
            CheckNotClosed();
            return _deliveryMode;
         }
         set
         {
            CheckNotClosed();
            _deliveryMode = value;
         }
      }

      public string ExchangeName
      {
         get { return _exchangeName; }
      }

      public string RoutingKey
      {
         get { return _routingKey; }
      }

      public bool DisableMessageID
      {
         get
         {
            throw new Exception("The method or operation is not implemented.");
         }
         set
         {
            throw new Exception("The method or operation is not implemented.");
         }
      }

      public bool DisableMessageTimestamp
      {
         get
         {
            CheckNotClosed();
            return _disableTimestamps;
         }
         set
         {
            CheckNotClosed();
            _disableTimestamps = value;
         }
      }

      public int Priority
      {
         get
         {
            CheckNotClosed();
            return _messagePriority;
         }
         set
         {
            CheckNotClosed();
            if ( value < 0 || value > 9 )
            {
               throw new ArgumentOutOfRangeException("Priority of " + value + " is illegal. Value must be in range 0 to 9");
            }
            _messagePriority = value;
         }
      }

      public override void Close()
      {
         _logger.Debug("Closing producer " + this);
         Interlocked.Exchange(ref _closed, CLOSED);
         _channel.DeregisterProducer(_producerId);
      }

      public void Send(IMessage msg, DeliveryMode deliveryMode, int priority, long timeToLive)
      {
         CheckNotClosed();
         SendImpl(
            _exchangeName, 
            _routingKey, 
            (AbstractQmsMessage)msg, 
            deliveryMode, 
            priority, 
            (uint)timeToLive, 
            _mandatory,
            _immediate
            );
      }

      public void Send(IMessage msg)
      {
         CheckNotClosed();
         SendImpl(
            _exchangeName, 
            _routingKey, 
            (AbstractQmsMessage)msg, 
            _deliveryMode, 
            _messagePriority, 
            (uint)_timeToLive,
            _mandatory, 
            _immediate
            );
      }

      // This is a short-term hack (knowing that this code will be re-vamped sometime soon)
      // to facilitate publishing messages to potentially non-existent recipients.
      public void Send(IMessage msg, bool mandatory)
      {
         CheckNotClosed();
         SendImpl(
            _exchangeName, 
            _routingKey, 
            (AbstractQmsMessage)msg, 
            _deliveryMode, 
            _messagePriority, 
            (uint)_timeToLive,
            mandatory, 
            _immediate
            );
      }

      public long TimeToLive
      {
         get
         {
            CheckNotClosed();
            return _timeToLive;
         }
         set
         {
            CheckNotClosed();
            if ( value < 0 )
            {
               throw new ArgumentOutOfRangeException("Time to live must be non-negative - supplied value was " + value);
            }
            _timeToLive = value;
         }
      }

      #endregion

      public string MimeType
      {
         get
         {
            CheckNotClosed();
            return _mimeType;
         }
         set
         {
            CheckNotClosed();
            _mimeType = value;
         }
      }

      public string Encoding
      {
         get
         {
            CheckNotClosed();
            return _encoding;
         }
         set
         {
            CheckNotClosed();
            _encoding = value;
         }
      }

      public void Dispose()
      {
         Close();
      }

      #region Message Publishing

      private void SendImpl(string exchangeName, string routingKey, AbstractQmsMessage message, DeliveryMode deliveryMode, int priority, uint timeToLive, bool mandatory, bool immediate)
      {
         // todo: handle session access ticket
         AMQFrame publishFrame = BasicPublishBody.CreateAMQFrame(
            _channel.ChannelId, 0, exchangeName,
            routingKey, mandatory, immediate
            );

         // fix message properties
         if ( !_disableTimestamps )
         {
            message.Timestamp = DateTime.UtcNow.Ticks;
            if (timeToLive != 0)
            {
                message.Expiration = message.Timestamp + timeToLive;
            }
         } else
         {
            message.Expiration = 0;
         }
         message.DeliveryMode = deliveryMode;
         message.Priority = (byte)priority;

         ByteBuffer payload = message.Data;
         int payloadLength = payload.Limit;

         ContentBody[] contentBodies = CreateContentBodies(payload);
         AMQFrame[] frames = new AMQFrame[2 + contentBodies.Length];
         for ( int i = 0; i < contentBodies.Length; i++ )
         {
            frames[2 + i] = ContentBody.CreateAMQFrame(_channelId, contentBodies[i]);
         }
         if ( contentBodies.Length > 0 && _logger.IsDebugEnabled )
         {
            _logger.Debug(string.Format("Sending content body frames to {{exchangeName={0} routingKey={1}}}", exchangeName, routingKey));
         }

         // weight argument of zero indicates no child content headers, just bodies
         AMQFrame contentHeaderFrame = ContentHeaderBody.CreateAMQFrame(
            _channelId, AmqChannel.BASIC_CONTENT_TYPE, 0, 
            message.ContentHeaderProperties, (uint)payloadLength
            );
         if ( _logger.IsDebugEnabled )
         {
            _logger.Debug(string.Format("Sending content header frame to  {{exchangeName={0} routingKey={1}}}", exchangeName, routingKey));
         }

         frames[0] = publishFrame;
         frames[1] = contentHeaderFrame;
         CompositeAMQDataBlock compositeFrame = new CompositeAMQDataBlock(frames);

         lock ( _channel.Connection.FailoverMutex )
         {
            _channel.Connection.ProtocolWriter.Write(compositeFrame);
         }
      }


      /// <summary>
      /// Create content bodies. This will split a large message into numerous bodies depending on the negotiated
      /// maximum frame size.
      /// </summary>
      /// <param name="payload"></param>
      /// <returns>return the array of content bodies</returns>
      private ContentBody[] CreateContentBodies(ByteBuffer payload)
      {
         if ( payload == null )
         {
            return null;
         } else if ( payload.Remaining == 0 )
         {
            return new ContentBody[0];
         }
         // we substract one from the total frame maximum size to account for the end of frame marker in a body frame
         // (0xCE byte).
         int framePayloadMax = (int)(_channel.Connection.MaximumFrameSize - 1);
         int frameCount = CalculateContentBodyFrames(payload);
         ContentBody[] bodies = new ContentBody[frameCount];
         for ( int i = 0; i < frameCount; i++ )
         {
            int length = (payload.Remaining >= framePayloadMax)
               ? framePayloadMax : payload.Remaining;
            bodies[i] = new ContentBody(payload, (uint)length);
         }
         return bodies;
      }

      private int CalculateContentBodyFrames(ByteBuffer payload)
      {
         // we substract one from the total frame maximum size to account 
         // for the end of frame marker in a body frame
         // (0xCE byte).
         int frameCount;
         if ( (payload == null) || (payload.Remaining == 0) )
         {
            frameCount = 0;
         } else
         {
            int dataLength = payload.Remaining;
            int framePayloadMax = (int)_channel.Connection.MaximumFrameSize - 1;
            int lastFrame = ((dataLength % framePayloadMax) > 0) ? 1 : 0;
            frameCount = (int)(dataLength / framePayloadMax) + lastFrame;
         }

         return frameCount;
      }
      #endregion // Message Publishing
   }
}
