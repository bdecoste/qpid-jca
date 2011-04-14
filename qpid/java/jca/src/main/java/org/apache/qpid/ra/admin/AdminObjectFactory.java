package org.apache.qpid.ra.admin;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class AdminObjectFactory implements ObjectFactory 
{

    @Override
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> env) throws Exception 
    {

        Object instance = null;

        if (object instanceof Reference) 
        {

            Reference ref = (Reference) object;
            String bindingURLString;

            if (ref.getClassName().equals(QpidQueue.class.getName())) 
            {
                RefAddr addr = ref.get(QpidQueue.class.getName());
                bindingURLString = (String) addr.getContent();

                if (addr != null) 
                {
                    return new QpidQueue(new QpidBindingURL(bindingURLString));
                }
            }

            if (ref.getClassName().equals(QpidTopic.class.getName())) 
            {
                RefAddr addr = ref.get(QpidTopic.class.getName());
                bindingURLString = (String) addr.getContent();

                if (addr != null) 
                {
                    return new QpidTopic(new QpidBindingURL(bindingURLString));
                }
            }
        }

        return instance;
    }
}
