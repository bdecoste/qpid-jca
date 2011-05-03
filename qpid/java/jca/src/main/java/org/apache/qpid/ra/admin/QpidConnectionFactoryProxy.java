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
import java.net.URISyntaxException;

import javax.jms.ConnectionFactory;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.ObjectFactory;

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.ra.QpidRAConnectionFactoryImpl;
import org.apache.qpid.ra.QpidRAManagedConnectionFactory;
import org.apache.qpid.ra.QpidResourceAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 */
public class QpidConnectionFactoryProxy implements Externalizable, Referenceable, ConnectionFactory, Serializable
{
    private static final Logger _log = LoggerFactory.getLogger(QpidDestinationProxy.class);

    private String connectionURL;
    private ConnectionFactory delegate;

    /**
     * This constructor should not only be used be de-serialisation code. Create
     * original object with the other constructor.
     */
    public QpidConnectionFactoryProxy() 
    {
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException 
    {
        Reference ref = (Reference) in.readObject();

        try 
        {
            delegate = (ConnectionFactory) dereference(ref);

        } catch (Exception e) 
        {
            _log.error("Failed to dereference ConnectionFactory " + e.getMessage(), e);
            throw new IOException("Failed to dereference ConnectionFactory: " + e.getMessage());
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException 
    {
        if (delegate == null)
        {
            _log.error("Null Destination ");
            throw new IOException("Null ConnectionFactory!");
        }
        
        try 
        {
            out.writeObject(((Referenceable) delegate).getReference());
        } 
        catch (NamingException e) 
        {
            _log.error("Failed to dereference ConnectionFactory " + e.getMessage(), e);
            throw new IOException("Failed to dereference ConnectionFactory: " + e.getMessage());
        }
    }

    @Override
    public Reference getReference() throws NamingException 
    {
        try
        {
            delegate = new AMQConnectionFactory(getConnectionURL());
            /*
            QpidResourceAdapter ra = new QpidResourceAdapter();
            QpidRAManagedConnectionFactory mcf = new QpidRAManagedConnectionFactory();
            mcf.setResourceAdapter(ra);
            mcf.setConnectionURL(getConnectionURL());
            delegate = new QpidRAConnectionFactoryImpl(mcf, null);
            */ 
            return ((Referenceable) delegate).getReference();
        }
        catch(Exception e)
        {
            throw new NamingException(e.getMessage());
        }
    }       
    private Object dereference(Reference ref) throws Exception 
    {
        ObjectFactory objFactory = (ObjectFactory) Class.forName(
                ref.getFactoryClassName()).newInstance();
        return objFactory.getObjectInstance(ref, null, null, null);
    }
    
    public void setConnectionURL(final String connectionURL)
    {
        this.connectionURL = connectionURL;
    }
    public String getConnectionURL()
    {
        return this.connectionURL;
    }

  /**
    * Create a connection
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public Connection createConnection() throws JMSException
   {
       return delegate.createConnection();   
   }

   /**
    * Create a connection
    * @param userName The user name
    * @param password The password
    * @return The connection
    * @exception JMSException Thrown if the operation fails
    */
   public Connection createConnection(final String userName, final String password) throws JMSException
   {
      return delegate.createConnection(userName, password);
   }

}

