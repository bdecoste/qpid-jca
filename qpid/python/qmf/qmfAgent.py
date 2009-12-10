
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import sys
import socket
import os
import logging
from threading import Thread
from qpid.messaging import Connection, Message
from uuid import uuid4
from qmfCommon import (AMQP_QMF_TOPIC, AMQP_QMF_DIRECT, AMQP_QMF_AGENT_LOCATE, 
                       AMQP_QMF_AGENT_INDICATION, AgentId, QmfManaged)



  ##==============================================================================
  ## AGENT
  ##==============================================================================

class Agent(Thread):
    def __init__(self, vendor, product, name=None,
                 notifier=None, kwargs={}):
        Thread.__init__(self)
        self._running = False
        self.vendor = vendor
        self.product = product
        if name:
            self.name = name
        else:
            self.name = uuid4().get_urn().split(":")[2]
        self._id = AgentId(self.vendor, self.product, self.name)
        self._address = str(self._id)
        self._notifier = notifier
        self._conn = None

    def getAgentId(self):
        return AgentId(self.vendor, self.product, self.name)

    def setConnection(self, conn):
        self._conn = conn
        self._session = self._conn.session()
        self._locate_receiver = self._session.receiver(AMQP_QMF_AGENT_LOCATE)
        self._direct_receiver = self._session.receiver(AMQP_QMF_DIRECT + "/" + self._address)
        self._ind_sender = self._session.sender(AMQP_QMF_AGENT_INDICATION)
        self._running = True
        self._start()


    def _dispatch(self, msg):
        if msg.subject != "qmf4":
            logging.debug("Ignoring non-qmf message '%s'" % msg.subject)
            return

        cmap = {}
        if msg.content_type == "amqp/map":
            cmap = msg.content

        if (not msg.properties or
            not "method" in msg.properties or
            not "opcode" in msg.properties):
            logging.error("INVALID MESSAGE PROPERTIES: '%s'" % str(msg.properties))
            return

        if msg.properties["method"] == "request":
            if msg.properties["opcode"] == "agent-locate":
                if "query" in cmap:
                    query = cmap["query"]
                    if ("vendor" in query and (query["vendor"] == "*" or query["vendor"] == self.vendor) and
                        "product" in query and (query["product"] == "*" or query["product"] == self.product) and
                        "name" in query and (query["name"] == "*" or query["name"] == self.name)):
                        logging.debug("Query received for %s:%s:%s" % (self.vendor, self.product, self.name))
                        logging.debug("reply-to [%s], cid=%s" % (msg.reply_to, msg.correlation_id))
                        try:
                            tmp_snd = self.session.sender( msg.reply_to )
                            m = Message( subject="qmf4",
                                         properties={"method":"response",
                                                     "opcode":"agent"},
                                         content={"name": {"vendor":"redhat.com",
                                                           "product":"agent",
                                                           "name":"tross"}},
                                         correlation_id=msg.correlation_id)
                            tmp_snd.send(m)
                            logging.debug("reply-to [%s] sent" % msg.reply_to)
                        except e:
                            logging.error("Failed to send reply to msg '%s'" % str(e))

            else:
                logging.warning("Ignoring message with unrecognized 'opcode' value: '%s'"
                                % msg.properties["opcode"])
        else:
            logging.warning("Ignoring message with unrecognized 'method' value: '%s'" 
                            % msg.properties["method"] )



    def run(self):
        count = 0   # @todo: hack
        while self._running:
            try:
                msg = self._locate_receiver.fetch(1)
                if msg.content_type == "amqp/map":
                    self._dispatch(msg)
            except KeyboardInterrupt:
                break
            except:
                pass

            try:
                msg = self._direct_receiver.fetch(1)
                if msg.content_type == "amqp/map":
                    self._dispatch(msg)
            except KeyboardInterrupt:
                break
            except:
                pass

            count+= 1
            if count == 5:
                count = 0
                m = Message( subject="qmf4",
                             properties={"method":"indication",
                                         "opcode":"agent"},
                             content={"name": {"vendor":"redhat.com",
                                               "product":"agent",
                                               "name":"tross"}} )
                self.ind_sender.send(m)
                logging.info("Agent Indication Sent")

    
    def registerObjectClass(self, cls):
        logging.error("!!!Agent.registerObjectClass() TBD!!!")

    def registerEventClass(self, cls):
        logging.error("!!!Agent.registerEventClass() TBD!!!")

    def raiseEvent(self, qmfEvent):
        logging.error("!!!Agent.raiseEvent() TBD!!!")

    def addObject(self, qmfAgentData ):
        logging.error("!!!Agent.addObject() TBD!!!")

    def methodResponse(self, context, status, text, arguments):
        logging.error("!!!Agent.methodResponse() TBD!!!")

    def getWorkItemCount(self): 
        """ 
        Returns the count of pending WorkItems that can be retrieved.
        """
        logging.error("!!!Agent.getWorkItemCount() TBD!!!")

    def getNextWorkItem(self, timeout=None): 
        """
        Obtains the next pending work item, or None if none available. 
        """
        logging.error("!!!Agent.getNextWorkItem() TBD!!!")

    def releaseWorkItem(self, wi): 
        """
        Releases a WorkItem instance obtained by getNextWorkItem(). Called when 
        the application has finished processing the WorkItem. 
        """
        logging.error("!!!Agent.releaseWorkItem() TBD!!!")




  ##==============================================================================
  ## OBJECTS
  ##==============================================================================


class QmfAgentData(QmfManaged):
    """
    A managed data object that is owned by an agent.
    """
    def __init__(self, _agent, _schema, _props={}):
        """
        @type _agent: class Agent
        @param _agent: the agent that manages this object.
        @type _schema: class SchemaObjectClass
        @param _schema: the schema used to describe this data object
        @type _props: map of "name"=<value> pairs
        @param _props: initial values for all properties in this object
        """
        super(QmfAgentData, self).__init__(_agentId=_agent.getAgentId(), 
                                           _schema=_schema, 
                                           _props=_props)

    def destroy(self): 
        self._timestamps[QmfManaged._ts_delete] = long(time.time() * 1000)
        # @todo: publish change

    def setProperty( self, _name, _value):
        super(QmfAgentData, self).setProperty(_name, _value)
        # @todo: publish change