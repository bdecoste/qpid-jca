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

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jboss.ejb3.annotation.ResourceAdapter;

@MessageDriven(mappedName = "jms/QpidListener", activationConfig = {
		   @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		   @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		   @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/Hello"),
		   @ActivationConfigProperty(propertyName = "connectionURL", propertyValue = "@broker.url@"),
		   @ActivationConfigProperty(propertyName = "useLocalTx", propertyValue = "false")
})
@ResourceAdapter("@rar.name@")
public class QpidListener implements MessageListener 
{
	
	@Override
	public void onMessage(Message msg) 
	{
		
		try
		{
            if(msg instanceof TextMessage)
            {
    			String content = ((TextMessage)msg).getText();
            	System.out.println("QpidListener: Received text message with contents " + content);
    		    	
            }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}

}
