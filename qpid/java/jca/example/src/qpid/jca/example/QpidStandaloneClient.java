package qpid.jca.example.client;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import java.util.Properties;

public class QpidStandaloneClient 
{
    
    private static final String DEFAULT_JNDI = "QpidConnectionFactory";
    private static final String DEFAULT_DEST_NAME = "queue/Hello";
    private static final String DEFAULT_MESSAGE = "Hello, World!"; 

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception 
    {
        Properties props = new Properties();
        InitialContext context = null; 
        ConnectionFactory connectionFactory = null;
        Session session = null;
        Connection connection = null;
        Destination destination = null; 
        try
        {
            String cfName = (System.getProperty("qpid.cf.name") == null) ? DEFAULT_JNDI : System.getProperty("qpid.cf.name");
            String destName = (System.getProperty("qpid.dest.name") == null) ? DEFAULT_DEST_NAME : System.getProperty("qpid.dest.name");  
            String content = (System.getProperty("qpid.message") == null) ? DEFAULT_MESSAGE : System.getProperty("qpid.message");  
            
            context = new InitialContext();
            connectionFactory = (ConnectionFactory)context.lookup(cfName);
            
            System.out.println("Acquired QpidConnectionFactory from JNDI");

            connection = connectionFactory.createConnection();
            
            System.out.println("Created connection from QpidConnectionFactory ");

            
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            destination = (Destination)context.lookup(destName);

            MessageProducer messageProducer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(content);
            messageProducer.send(message);
            
             
            
        }
        finally
        {
            if(context != null)
            {
                try
                {
                    context.close();                    
                }
                catch(Exception ignore){}
            }
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

