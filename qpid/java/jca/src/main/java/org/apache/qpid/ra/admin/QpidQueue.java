package org.apache.qpid.ra.admin;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.url.BindingURL;

public class QpidQueue extends AMQQueue 
{
    private String url;

    public QpidQueue(BindingURL binding) 
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
