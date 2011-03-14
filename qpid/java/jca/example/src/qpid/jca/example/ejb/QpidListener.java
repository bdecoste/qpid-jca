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
		   @ActivationConfigProperty(propertyName = "connectionURL", propertyValue = "@broker.url@")
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
