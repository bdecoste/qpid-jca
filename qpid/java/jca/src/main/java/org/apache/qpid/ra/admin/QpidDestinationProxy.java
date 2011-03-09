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

package org.apache.qpid.ra.admin;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import javax.jms.Destination;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;

import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.exchange.ExchangeDefaults;
import org.apache.qpid.framing.AMQShortString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The QpidDestinationProxy provides for allowing an administrator/developer to create and bind QPID destinations into a JNDI tree. AdminObjects are used
 * as an generic integration point rather than relying on the EE server specific API's to create destinations (queues, topics). AdminObjects and associated
 * properties are defined in the ra.xml file for a particular JCA adapter. Please see the ra.xml file for the QPID JCA resource adapter as well as the README.txt for
 * the adapter for more details.
 *
 *
 *
 */
public class QpidDestinationProxy implements Externalizable, Referenceable, Destination, Serializable
{
    private static final Logger _log = LoggerFactory.getLogger(QpidDestinationProxy.class);

    private static final String DEFAULT_QUEUE_TYPE = "QUEUE";
    private static final String DEFAULT_TOPIC_TYPE = "TOPIC";

    private String destinationAddress;
    private String destinationType;
    private Destination delegate;

    /**
     * This constructor should not only be used be de-serialisation code. Create
     * original object with the other constructor.
     */
    public QpidDestinationProxy() 
    {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException 
    {
        Reference ref = (Reference) in.readObject();

        try 
        {
            delegate = (Destination) dereference(ref);

        } catch (Exception e) 
        {
            _log.error("Failed to dereference Destination " + e.getMessage(), e);
            throw new IOException("Failed to dereference Destination: " + e, e);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException 
    {
        if (delegate == null)
        {
            _log.error("Null Destination ");
            throw new IOException("Null destination!");
        }
        
        try 
        {
            out.writeObject(((Referenceable) delegate).getReference());
        } 
        catch (NamingException e) 
        {
            _log.error("Failed to dereference Destination " + e.getMessage(), e);
            throw new IOException("Failed to dereference Destination: " + e, e);
        }
    }

    public Reference getReference() throws NamingException 
    {
    
        if(getDestinationType() != null)
        {
            if(getDestinationType().equalsIgnoreCase(DEFAULT_QUEUE_TYPE))
            {
                delegate = new AMQQueue(ExchangeDefaults.DIRECT_EXCHANGE_NAME, new AMQShortString((String) getDestinationAddress()));                
            }
            else if(getDestinationType().equalsIgnoreCase(DEFAULT_TOPIC_TYPE))
            {
                String[] keys = (getDestinationAddress()).split(",");
                AMQShortString[] bindings = new AMQShortString[keys.length];
                int i = 0;
                for (String key:keys)
                {
                    bindings[i] = new AMQShortString(key.trim());
                    i++;
                }
                delegate = new AMQTopic(ExchangeDefaults.TOPIC_EXCHANGE_NAME,bindings[0],null,bindings);

            }
            //TODO we need to deal with generic Destination 
        }

        return ((Referenceable) delegate).getReference();
    }

    private Object dereference(Reference ref) throws Exception 
    {
        ObjectFactory objFactory = (ObjectFactory) Class.forName(
                ref.getFactoryClassName()).newInstance();
        return objFactory.getObjectInstance(ref, null, null, null);
    }
    
    public void setDestinationAddress(String destinationAddress) throws Exception
    {
        this.destinationAddress = destinationAddress; 
    }
    
    public String getDestinationAddress()
    {
        return this.destinationAddress;    
    }
    
    public void setDestinationType(String destinationType)
    {
        this.destinationType = destinationType;
    }

    public String getDestinationType()
    {
        return this.destinationType;    
    }
}
