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
package org.apache.qpid.management.wsdm;

import java.util.Date;
import java.util.UUID;

import javax.management.MBeanAttributeInfo;
import javax.xml.namespace.QName;

import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.WsrfConstants;
import org.apache.qpid.management.Names;
import org.w3c.dom.Element;

/**
 * Test case for Web Service Resource Properties interfaces.
 * Those interfaces are defined on http://docs.oasis-open.org/wsrf/wsrf-ws_resource_properties-1.2-spec-os.pdf
 * (Web Services Resource Properties 1.2 - (WS-ResourceProperties).
 * For a better explanation see chapter 5 of the specification above.
 * 
 * @author Andrea Gazzarini
 */
public class GetMultipleResourcePropertiesTestCase extends BaseWsDmAdapterTestCase
{
	/**
	 * Tests the GetMultipleResourceProperties interface when the request contains 
	 * an unknwon target resource.
	 * 
	 * <br>precondition : the GetMultipleResourceProperties request contains an unknwon resource.
	 * <br>postcondition : a SoapFault is thrown and the corresponding detail contains an 
	 * 								UnknownResourceFault element.
	 */
	public void testGetMultipleResourcePropertiesKO_WithUnknownResourceFault() throws Exception
	{
		try 
		{
			_resourceClient.getEndpointReference().removeParameter(Names.RESOURCE_ID_QNAME);
			_resourceClient.getEndpointReference().addParameter(Names.RESOURCE_ID_QNAME,"lablabalbal");			
			
			_resourceClient.getMultipleResourceProperties(new QName[]{});
		} catch(SoapFault expected)
		{
			assertEquals(
					WsrfConstants.RESOURCE_UNKNOWN_QNAME.getLocalPart(),
					expected.getDetail().getLocalName());
		}
	}
	
	/**
	 * Test the WS-RP GetResourceProperties interface of the WS-DM adapter.
	 * 
	 * <br>precondition : a ws resource exists and is registered. 
	 * <br>postcondition : Properties are correctly returned according to WSRP interface and they (their value)
	 * 								are matching with corresponding MBean properties.
	 */
	public void testGetMultipleResourcePropertiesOK() throws Exception
	{
		MBeanAttributeInfo [] attributesMetadata = _mbeanInfo.getAttributes();
		QName[] names = new QName[attributesMetadata.length];
		
		int index = 0;
		for (MBeanAttributeInfo attributeMetadata : _mbeanInfo.getAttributes())
		{
			QName qname = new QName(Names.NAMESPACE_URI,attributeMetadata.getName(),Names.PREFIX);
			names[index++] = qname;
		}
		
		Element[] properties =_resourceClient.getMultipleResourceProperties(names);
		for (Element element : properties)
		{
			String name = element.getLocalName();
			Object value = _managementServer.getAttribute(_resourceObjectName, name);
			if ("Name".equals(name))
			{
				assertEquals(
						value,
						element.getTextContent());
			} else if ("Durable".equals(name))
			{
				assertEquals(
						value,
						Boolean.valueOf(element.getTextContent()));				
			} else if ("ExpireTime".equals(name))
			{
				assertEquals(
						value,
						new Date(Long.valueOf(element.getTextContent())));								
			} else if ("MsgTotalEnqueues".equals(name))
			{
				assertEquals(
						value,
						Long.valueOf(element.getTextContent()));								
			} else if ("ConsumerCount".equals(name))
			{
				assertEquals(
						value,
						Integer.valueOf(element.getTextContent()));								
			}else if ("VhostRef".equals(name))
			{
				assertEquals(
						value,
						UUID.fromString(element.getTextContent()));								
			}
		}
	}
}
