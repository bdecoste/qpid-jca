QPID JCA Resource Adapter Installation/Configuration Instructions

Overview
========
The QPID Resource Adapter is a JCA 1.5 resource adapter that allows 
for JEE integration between EE applications and AMQP 0.10  message brokers. 

The adapter provides both outbound and inbound connectivity and 
exposes a variety of options to fine tune your messaging applications. Currently 
the adapter only supports C++ based brokers and has only been tested with Apache QPID C++ broker. 
Similarly, the adapter has only been configured and tested using the JBoss 5/6 platform. And is configured to
run 'out of the box' for these application servers. 

The following document explains how to configure your adapter for deployment as well as how to deploy 
the adapter in the aforementioned supported environments. 


QuickStart
==========
The following outlines the minimum steps to configure and deploy the QPID JCA resource adapter:

1) Modify the ra.xml file provided in the META-INF directory of the QPID RAR file. A set of default 
properties has already been provided that should cover a majority of the most simple use cases. 
However, two properties are required to be configured to allow the adapter to function correctly using 
XA in the JBoss environment. In the ra.xml file, look for the following properties:

    <config-property>
      <description>Transaction Manager locator class</description>
      <config-property-name>TransactionManagerLocatorClass</config-property-name>
      <config-property-type>java.lang.String</config-property-type>
      <config-property-value></config-property-value>
    </config-property>

    <config-property>
      <description>Transaction Manager locator method</description>
      <config-property-name>TransactionManagerLocatorMethod</config-property-name>
      <config-property-type>java.lang.String</config-property-type>
      <config-property-value></config-property-value>
    </config-property>

These properties are commented out due to the platform specfic requirements. To enable XA, and transactional behavior in general, you need to tell the adapter how to ifind the transaction manager. The QPID JCA resource apapter provides a transaction manager locator implementation for JBoss5/6. In order to enable this behavior, umcomment the lines above and configure them as such: 


    <config-property>
      <description>Transaction Manager locator class</description>
      <config-property-name>TransactionManagerLocatorClass</config-property-name>
      <config-property-type>java.lang.String</config-property-type>
      <config-property-value>org.apache.qpid.ra.tm.JBossTransactionManagerLocator</config-property-value>
    </config-property>

    <config-property>
      <description>Transaction Manager locator method</description>
      <config-property-name>TransactionManagerLocatorMethod</config-property-name>
      <config-property-type>java.lang.String</config-property-type>
      <config-property-value>getTm</config-property-value>
    </config-property>

This will allow the QPID resource adapter to locate the transaction manager when the adapter starts. Note, if this is not done correctly you will see the warning message


01:26:31,544 WARN  [QpidResourceAdapter] It wasn't possible to lookup for a Transaction Manager through the configured properties TransactionManagerLocatorClass and TransactionManagerLocatorMethod
01:26:31,545 WARN  [QpidResourceAdapter] Qpid Resource Adapter won't be able to set and verify transaction timeouts in certain cases.

At this point you should modify other properties to suit your environment. A more detailed explanation of these properties can be found the more extensive configuration section later in this document. 


2)Deploy the resource adapter to JBoss5/6. After configuring the ra.xml file, you can simply copy the qpid-ra-0.9.rar file to your JBoss deploy directory. Note, this can be done as a compressed file or exploded file in a similar manner as war, ejb jar and other EE deployment units. Once the adapter is deployed you should see the following message in your JBoss log/console:

01:26:31,545 INFO  [QpidResourceAdapter] Qpid resource adaptor started

The adapter is now configured and ready for use. At this point you can deploy your connection factories, MDB's and other EE artifacts.


Configuration
=============
The standard configuration mechanism for 1.5 JCA adapters is the ra.xml deployment descriptor. Like other EE based descriptors this file can be found in the META-INF directory of the provided RAR file. A majority of the properties in the ra.xml will seem familiar to anyone who has worked with QPID in a standalone environment. 

The resource adapter configuration properties provide generic properties for both inbound and outbound connectivity. These properties can be overriden when deploying managed connection factories as well as inbound activations. 





