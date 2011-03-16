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

package qpid.jca.example.ejb;

import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

@Stateless
public class QpidTestBean implements QpidTestRemote, QpidTestLocal
{
    private static final Logger log = Logger.getLogger(QpidTestBean.class.getName());

    @Resource(mappedName="java:QpidJMSXA")	
    private ConnectionFactory connectionFactory;
    
    @Resource(mappedName="queue/Hello")
    private Destination destination;

    public void testQpidAdapter(final String content) throws Exception
    {
        javax.jms.Connection connection = null;
        Session session = null;
        
        log.info("Sending message to MDB with content " + content);

        try
        {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(content);
            messageProducer.send(message);

        }
        catch(Exception e)
        {
           throw e;
        }
        finally
        {
            if(session != null)
            {
            	try
            	{
                	session.close();            		
            	}
            	catch(Exception ignore){}
            }
            
            if(connection != null)
            {
            	try
            	{
            		connection.close();
            	}
            	catch(Exception ignore){}
            }
        }
	}
        
	
}
