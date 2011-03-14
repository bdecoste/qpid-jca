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
