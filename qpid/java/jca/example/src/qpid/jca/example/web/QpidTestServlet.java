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

package qpid.jca.example.web;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class QpidTestServlet extends HttpServlet
{
	private static final String DEFAULT_MESSAGE = "Hello from QPID JCA!";
	
    private ConnectionFactory connectionFactory;
    private Destination destination;

    public void init(ServletConfig config) throws ServletException
    {
        
        InitialContext context = null;

        try
        {
            context = new InitialContext(); 
            
            String param = config.getInitParameter("connectionFactory");
            connectionFactory = (ConnectionFactory)context.lookup(param);
            param = config.getInitParameter("destination");
            destination = (Destination)context.lookup(param);
            
        }
        catch(Exception e)
        {
           throw new ServletException(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if(context != null)
                    context.close();
            }
            catch(Exception ignore){}
            
        }
        
    }
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
        Connection connection = null;
        Session session = null;
        
        try
        {
        	String content = (req.getParameter("message") == null) ? DEFAULT_MESSAGE : req.getParameter("message");
        	connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer messageProducer = session.createProducer(destination);

            TextMessage message = session.createTextMessage(content);
            messageProducer.send(message);

            resp.getOutputStream().println("Sent message with content '" + content + "'");
            resp.getOutputStream().flush();
            
        }
        catch(Exception e)
        {
           throw new ServletException(e.getMessage(), e);
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


