package org.apache.qpid.ra.admin;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.url.BindingURL;

public class QpidTopic extends AMQTopic 
{
    private String url;

    public QpidTopic(BindingURL binding) 
    {
        super(binding);
        this.url = binding.getURL();
    }

    @Override
    public Reference getReference() throws NamingException 
    {
        return new Reference(this.getClass().getName(), new StringRefAddr(this.getClass().getName(), toURL()),
                AdminObjectFactory.class.getName(), null);
    }

    @Override
    public String toURL() 
    {
        return url;
    }

}
