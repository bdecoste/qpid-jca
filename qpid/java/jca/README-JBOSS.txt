Qpid JCA Resource Adapter 

JBoss Installation and Configuration Instructions

Overview
========
    The QPID Resource Adapter is a JCA 1.5 resource adapter that allows for JEE 
integration between EE applications and AMQP 0.10  message brokers. 

    The adapter provides both outbound and inbound connectivity and exposes a 
variety of options to fine tune your messaging applications. Currently the adapter 
only supports C++ based brokers and has only been tested with Apache QPID C++ broker. 
Similarly, the adapter has only been configured and tested using the JBoss 5.1.x and 6.x platforms. 

    The following document explains how to configure your adapter for deployment as well as how to deploy 
the adapter in the aforementioned supported environments. 


Deployment
==========
    To deploy the Qpid JCA adapter for either JBoss 5.1.x or 6.x application 
servers, simply copy the qpid-ra-<version>.rar file to your JBoss deploy directory. 
By default this can be found at JBOSS_ROOT/server/<server-name>/deploy, where JBOSS_ROOT 
denotes the root directory of your JBoss installation. A successful adapter installation 
will be accompanied with a console/log message resembling the following:

    INFO  [QpidResourceAdapter] Qpid resource adaptor started

    At this point the adapter is deployed and ready for use. 
    

Configuration
=============
    The standard configuration mechanism for 1.5 JCA adapters is the ra.xml 
deployment descriptor. Like other EE based descriptors this file can be found 
in the META-INF directory of the provided EE artifact (ie .rar file). A majority 
of the properties in the ra.xml will seem familiar to anyone who has worked with 
Apache Qpid in a standalone environment. A reasonable set of configuration defaults 
have been provided. 

    The resource adapter configuration properties provide generic properties for both 
inbound and outbound connectivity. These properties can be overridden when deploying 
managed connection factories as well as inbound activations using the standard JBoss
configuration artifacts, the *-ds.xml file and MDB activation configuration. A sample
*-ds.xml file, qpid-jca-ds.xml, can be found in your Qpid JCA resource adapter directory.

Notes
=============
    While JBoss JCA provides a configuration flag to allow for resource adapters
to be used outside the application server, currently the Qpid JCA adapter does
not support this configuration. As a result, the adapter must be used within the 
context of the application server.


