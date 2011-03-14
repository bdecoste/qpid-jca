package qpid.jca.example.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import qpid.jca.example.ejb.QpidTest;

public class QpidClient 
{
    
    private static final String DEFAULT_JNDI = "QpidTestBean/remote";
    private static final String DEFAULT_MESSAGE = "Hello, World!"; 

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception 
    {
        Properties props = new Properties();
        Context context = new InitialContext();
         
        String ejbName = (System.getProperty("qpid.ejb.name") == null) ? DEFAULT_JNDI : System.getProperty("qpid.ejb.name");

        QpidTest ejb = (QpidTest)context.lookup(ejbName);
        System.out.println("Found SLSB " + ejbName);
       
        String message = (System.getProperty("qpid.message") == null) ? DEFAULT_MESSAGE : System.getProperty("qpid.message");  

        System.out.println("Located SLSB. Sending message with content: " + message);
        ejb.testQpidAdapter(message);
        System.out.println("Message sent successfully");
    }   

}
