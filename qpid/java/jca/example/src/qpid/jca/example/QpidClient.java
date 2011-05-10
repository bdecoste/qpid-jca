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

package qpid.jca.example.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import qpid.jca.example.ejb.QpidTest;

public class QpidClient 
{
    
    private static final String DEFAULT_JNDI = "QpidTestBean/remote";
    private static final String DEFAULT_MESSAGE = "Hello, World!"; 
    private static final Boolean USE_LOCAL_FACTORY = Boolean.FALSE;

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
        Boolean useLocalFactory = (System.getProperty("use.local") == null) ? USE_LOCAL_FACTORY : Boolean.valueOf(System.getProperty("use.local"));
        
        System.out.println("Located SLSB. Sending message with content: " + message);
        
        ejb.testQpidAdapter(message, useLocalFactory);

        System.out.println("Message sent successfully");
    }   

}
