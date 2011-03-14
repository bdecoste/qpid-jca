package qpid.jca.example.web.ee5;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import javax.jms.*;
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


