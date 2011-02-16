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

package org.apache.qpid.ra;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;

import org.apache.qpid.client.CustomJMSXProperty;
import org.apache.qpid.common.QpidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements javax.jms.ConnectionMetaData
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class QpidRAConnectionMetaData implements ConnectionMetaData
{
   /** The logger */
   private static final Logger log = LoggerFactory.getLogger(QpidRAConnectionMetaData.class);

   /** Trace enabled */
   private static boolean trace = QpidRAConnectionMetaData.log.isTraceEnabled();
   
   private static final String PROVIDER_VERSION ;
   private static final int PROVIDER_MAJOR ;
   private static final int PROVIDER_MINOR ;

   /**
    * Constructor
    */
   public QpidRAConnectionMetaData()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("constructor()");
      }
   }

   /**
    * Get the JMS version
    * @return The version
    */
   public String getJMSVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getJMSVersion()");
      }

      return "1.1";
   }

   /**
    * Get the JMS major version
    * @return The major version
    */
   public int getJMSMajorVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getJMSMajorVersion()");
      }

      return 1;
   }

   /**
    * Get the JMS minor version
    * @return The minor version
    */
   public int getJMSMinorVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getJMSMinorVersion()");
      }

      return 1;
   }

   /**
    * Get the JMS provider name
    * @return The name
    */
   public String getJMSProviderName()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getJMSProviderName()");
      }

      return QpidProperties.getProductName() + " Resource Adapter" ;
   }

   /**
    * Get the provider version
    * @return The version
    */
   public String getProviderVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getProviderVersion()");
      }

      return PROVIDER_VERSION ;
   }

   /**
    * Get the provider major version
    * @return The version
    */
   public int getProviderMajorVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getProviderMajorVersion()");
      }

      return PROVIDER_MAJOR ;
   }

   /**
    * Get the provider minor version
    * @return The version
    */
   public int getProviderMinorVersion()
   {
      if (QpidRAConnectionMetaData.trace)
      {
         QpidRAConnectionMetaData.log.trace("getProviderMinorVersion()");
      }

      return PROVIDER_MINOR ;
   }

   /**
    * Get the JMS XPropertyNames
    * @return The names
    */
   public Enumeration<Object> getJMSXPropertyNames()
   {
       return CustomJMSXProperty.asEnumeration();
   }
   
   static
   {
	   final String version = QpidProperties.getReleaseVersion() ;
	   int major = -1 ;
	   int minor = -1 ;
	   if (version != null)
	   {
		   final int separator = version.indexOf('.') ;
		   if (separator != -1)
		   {
			   major = parseInt(version.substring(0, separator), "major") ;
			   minor = parseInt(version.substring(separator+1, version.length()), "minor") ;
		   }
	   }
	   PROVIDER_VERSION = version ;
	   PROVIDER_MAJOR = major ;
	   PROVIDER_MINOR = minor ;
   }
   
   private static int parseInt(final String value, final String name)
   {
	   try
	   {
		   return Integer.parseInt(value) ;
	   }
	   catch (final NumberFormatException nfe)
	   {
		   log.warn("Failed to parse " + name + ": " + value) ;
		   return -1 ;
	   }
   }
}
