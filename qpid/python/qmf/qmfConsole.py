#
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
import os
import logging
import platform
import time
import Queue
from threading import Thread
from threading import Lock
from threading import currentThread
from threading import Condition

from qpid.messaging import *

from qmfCommon import (AMQP_QMF_DIRECT, AMQP_QMF_NAME_SEPARATOR, AMQP_QMF_AGENT_INDICATION,
                       AMQP_QMF_AGENT_LOCATE)
from qmfCommon import AgentId



# global flag that indicates which thread (if any) is
# running the console callback 
_callback_thread=None




##==============================================================================
## Sequence Manager  
##==============================================================================

class _Mailbox(object):
    """
    Virtual base class for all Mailbox-like objects
    """
    def __init__(self):
        self._msgs = []
        self._cv = Condition()
        self._waiting = False

    def deliver(self, obj):
        self._cv.acquire()
        try:
            self._msgs.append(obj)
            # if was empty, notify waiters
            if len(self._msgs) == 1:
                self._cv.notify()
        finally:
            self._cv.release()

    def fetch(self, timeout=None):
        self._cv.acquire()
        try:
            if len(self._msgs):
                return self._msgs.pop()
            self._cv.wait(timeout)
            if len(self._msgs):
                return self._msgs.pop()
            return None
        finally:
            self._cv.release()



class SequencedWaiter(object):
    """ 
    Manage sequence numbers for asynchronous method calls. 
    Allows the caller to associate a generic piece of data with a unique sequence
    number."""

    def __init__(self):
        self.lock     = Lock()
        self.sequence = 1L
        self.pending  = {}


    def allocate(self):
        """ 
        Reserve a sequence number.
        
        @rtype: long
        @return: a unique nonzero sequence number.
        """
        self.lock.acquire()
        try:
            seq = self.sequence
            self.sequence = self.sequence + 1
            self.pending[seq] = _Mailbox()
        finally:
            self.lock.release()
        logging.debug( "sequence %d allocated" % seq)
        return seq


    def put_data(self, seq, new_data):
        seq = long(seq)
        logging.debug( "putting data [%r] to seq %d..." % (new_data, seq) )
        self.lock.acquire()
        try:
            if seq in self.pending:
                self.pending[seq].deliver(new_data)
            else:
                logging.error( "seq %d not found!" % seq )
        finally:
            self.lock.release()



    def get_data(self, seq, timeout=None):
        """ 
        Release a sequence number reserved using the reserve method.  This must
        be called when the sequence is no longer needed.
        
        @type seq: int
        @param seq: a sequence previously allocated by calling reserve().
        @rtype: any
        @return: the data originally associated with the reserved sequence number.
        """
        seq = long(seq)
        logging.debug( "getting data for seq=%d" % seq)
        mbox = None
        self.lock.acquire()
        try:
            if seq in self.pending:
                mbox = self.pending[seq]
        finally:
            self.lock.release()

        # Note well: pending list is unlocked, so we can wait.
        # we reference mbox locally, so it will not be released
        # until we are done.

        if mbox:
            d = mbox.fetch(timeout)
            logging.debug( "seq %d fetched %r!" % (seq, d) )
            return d

        logging.debug( "seq %d not found!" % seq )
        return None


    def release(self, seq):
        """
        Release the sequence, and its mailbox
        """
        seq = long(seq)
        logging.debug( "releasing seq %d" % seq )
        self.lock.acquire()
        try:
            if seq in self.pending:
                del self.pending[seq]
        finally:
            self.lock.release()



#class ObjectProxy(QmfObject):
class ObjectProxy(object):
    """
    A local representation of a QmfObject that is managed by a remote agent.  
    """
    def __init__(self, agent, cls, kwargs={}):
        """
        @type agent: qmfConsole.AgentProxy
        @param agent: Agent that manages this object.
        @type cls: qmfCommon.SchemaObjectClass
        @param cls: Schema that describes the class.
        @type kwargs: dict
        @param kwargs: ??? supported keys ???
        """
        # QmfObject.__init__(self, cls, kwargs)
        self._agent = agent

    # def update(self):
    def refresh(self, timeout = None):
        """
        Called to re-fetch the current state of the object from the agent.  This updates
        the contents of the object to their most current values.

        @rtype: bool
        @return: True if refresh succeeded.  Refresh may fail if agent does not respond.
        """
        if not self._agent:
            raise Exception("No Agent associated with this object")
        newer = self._agent.get_object(Query({"object_id":object_id}), timeout)
        if newer == None:
            logging.error("Failed to retrieve object %s from agent %s" % (str(self), str(self._agent)))
            raise Exception("Failed to retrieve object %s from agent %s" % (str(self), str(self._agent)))
        self.mergeUpdate(newer)  ### ??? in Rafi's console.py::Object Class

    ### def _merge_update(self, newerObject):
    ### ??? in Rafi's console.py::Object Class


    ### def is_deleted(self):
    ### ??? in Rafi's console.py::Object Class

    def key(self): pass




class AgentProxy(object):
    """
    A local representation of a remote agent managed by this console.
    """
    def __init__(self, name):
        """
        @type name: AgentId
        @param name: uniquely identifies this agent in the AMQP domain.
        """
        if not name or not isinstance(name, AgentId):
            raise Exception( "Attempt to create an Agent without supplying a valid agent name." );

        self._name = name
        self._address = AMQP_QMF_DIRECT + AMQP_QMF_NAME_SEPARATOR + str(name)
        self._console = None
        self._sender = None
        self._packages = [] # list of package names known to this agent
        self._classes = {}  # dict [key:class] of classes known to this agent
        self._subscriptions = [] # list of active standing subscriptions for this agent
        self._exists = False  # true when Agent Announce is received from this agent
        logging.debug( "Created AgentProxy with address: [%s]" % self._address )


    def key(self):
        return str(self._name)

    
    def _send_msg(self, msg):
        """
        Low-level routine to asynchronously send a message to this agent.
        """
        msg.reply_to = self._console.address()
        handle = self._console._req_correlation.allocate()
        if handle == 0:
            raise Exception("Can not allocate a sequence id!")
        msg.correlation_id = str(handle)
        self._sender.send(msg)
        return handle



    def _fetch_reply_msg(self, handle, timeout=None):
        """
        Low-level routine to wait for an expected reply from the agent.
        """
        if handle == 0:
            raise Exception("Invalid handle")
        msg = self._console._req_correlation.get_data( handle, timeout )
        if not msg:
            logging.debug("timed out waiting for reply message")
        self._console._req_correlation.release( handle )
        return msg


    def get_packages(self):
        """
        Return a list of the names of all packages known to this agent.
        """
        return self._packages[:]

    def get_classes(self):
        """
        Return a dictionary [key:class] of classes known to this agent.
        """
        return self._classes[:]

    def get_objects(self, query, kwargs={}):
        """
        Return a list of objects that satisfy the given query.

        @type query: dict, or qmfCommon.Query
        @param query: filter for requested objects
        @type kwargs: dict
        @param kwargs: ??? used to build match selector and query ???
        @rtype: list
        @return: list of matching objects, or None.
        """
        pass

    def get_object(self, query, kwargs={}):
        """
        Get one object - query is expected to match only one object.
        ??? Recommended: explicit timeout param, default None ???

        @type query: dict, or qmfCommon.Query
        @param query: filter for requested objects
        @type kwargs: dict
        @param kwargs: ??? used to build match selector and query ???
        @rtype: qmfConsole.ObjectProxy
        @return: one matching object, or none
        """
        pass


    def create_subscription(self, query):
        """
        Factory for creating standing subscriptions based on a given query.

        @type query: qmfCommon.Query object
        @param query: determines the list of objects for which this subscription applies
        @rtype: qmfConsole.Subscription
        @returns: an object representing the standing subscription.
        """
        pass

    def __repr__(self):
        return self._address
    
    def __str__(self):
        return self.__repr__()



  ##==============================================================================
  ## CONSOLE
  ##==============================================================================



class WorkItem(object):
    """
    Describes an event that has arrived at the Console for the
    application to process.  The Notifier is invoked when one or 
    more of these WorkItems become available for processing.
    """
    #
    # Enumeration of the types of WorkItems produced by the Console
    #
    AGENT_ADDED = 1
    AGENT_DELETED = 2
    NEW_PACKAGE = 3
    NEW_CLASS = 4
    OBJECT_UPDATE = 5
    EVENT_RECEIVED = 7
    AGENT_HEARTBEAT = 8

    def __init__(self, kind, kwargs={}):
        """
        Used by the Console to create a work item.
        
        @type kind: int
        @param kind: work item type
        """
        self._kind = kind
        self._param_map = kwargs


    def getType(self):
        return self._kind

    def getParams(self):
        return self._param_map



class Notifier(object):
    """
    Virtual base class that defines a call back which alerts the application that
    a QMF Console notification is pending.
    """
    def console_indication(self):
        """
        Called when one or more console items are ready for the console application to process.
        This method may be called by the internal console management thread.  Its purpose is to
        indicate that the console application should process pending items.
        """
        pass




class Console(Thread):
    """
    A Console manages communications to a collection of agents on behalf of an application.
    """
    def __init__(self, name=None, notifier=None, kwargs={}):
        """
        @type name: str
        @param name: identifier for this console.  Must be unique.
        @type notifier: qmfConsole.Notifier
        @param notifier: invoked when events arrive for processing.
        @type kwargs: dict
        @param kwargs: ??? Unused
        """
        Thread.__init__(self)
        self._name = name
        if not self._name:
            self._name = "qmfc-%s.%d" % (platform.node(), os.getpid())
        self._address = AMQP_QMF_DIRECT + AMQP_QMF_NAME_SEPARATOR + self._name
        self._notifier = notifier
        self._conn = None
        self._session = None
        # dict of "agent-direct-address":AgentProxy entries
        self._agent_map = {}
        self._agent_map_lock = Lock()
        self._direct_recvr = None
        self._announce_recvr = None
        self._locate_sender = None
        self._schema_cache = {}
        self._req_correlation = SequencedWaiter()
        self._operational = False
        self._agent_discovery = False
        # lock out run() thread
        self._cv = Condition()
        # for passing WorkItems to the application
        self._work_q = Queue.Queue()
        ## Old stuff below???
        #self._broker_list = []
        #self.impl = qmfengine.Console()
        #self._event = qmfengine.ConsoleEvent()
        ##self._cv = Condition()
        ##self._sync_count = 0
        ##self._sync_result = None
        ##self._select = {}
        ##self._cb_cond = Condition()



    def destroy(self, timeout=None):
        """
        Must be called before the Console is deleted.  
        Frees up all resources and shuts down all background threads.

        @type timeout: float
        @param timeout: maximum time in seconds to wait for all background threads to terminate.  Default: forever.
        """
        logging.debug("Destroying Console...")
        if self._conn:
            self.remove_connection(self._conn, timeout)
        logging.debug("Console Destroyed")



    def add_connection(self, conn):
        """
        Add a AMQP connection to the console.  The console will setup a session over the
        connection.  The console will then broadcast an Agent Locate Indication over
        the session in order to discover present agents.

        @type conn: qpid.messaging.Connection
        @param conn: the connection to the AMQP messaging infrastructure.
        """
        if self._conn:
            raise Exception( "Multiple connections per Console not supported." );
        self._conn = conn
        self._session = conn.session(name=self._name)
        self._direct_recvr = self._session.receiver(self._address)
        self._announce_recvr = self._session.receiver(AMQP_QMF_AGENT_INDICATION)
        self._locate_sender = self._session.sender(AMQP_QMF_AGENT_LOCATE)
        #
        # Now that receivers are created, fire off the receive thread...
        #
        self._operational = True
        self.start()



    def remove_connection(self, conn, timeout=None):
        """
        Remove an AMQP connection from the console.  Un-does the add_connection() operation,
        and releases any agents and sessions associated with the connection.

        @type conn: qpid.messaging.Connection
        @param conn: connection previously added by add_connection()
        """
        if self._conn and conn and conn != self._conn:
            logging.error( "Attempt to delete unknown connection: %s" % str(conn))

        # tell connection thread to shutdown
        self._operational = False
        if self.isAlive():
            # kick my thread to wake it up
            logging.debug("Making temp sender for [%s]" % self._address)
            tmp_sender = self._session.sender(self._address)
            try:
                msg = Message(subject="qmf4",
                              properties={"method":"request",
                                          "opcode":"console-ping"},
                              content={"data":"ignore"})
                tmp_sender.send( msg, sync=True )
            except SendError, e:
                logging.error(str(e))
            logging.debug("waiting for console receiver thread to exit")
            self.join(timeout)
            if self.isAlive():
                logging.error( "Console thread '%s' is hung..." % self.getName() )
        self._direct_recvr.close()
        self._announce_recvr.close()
        self._locate_sender.close()
        self._session = None
        self._conn = None
        logging.debug("console connection removal complete")


    def address(self):
        """
        The AMQP address this Console is listening to.
        """
        return self._address


    def create_agent( self, agent_name ):
        """
        Factory to create/retrieve an agent for this console
        """
        if not isinstance(agent_name, AgentId):
            raise TypeError("agent_name must be an instance of AgentId")

        agent = AgentProxy(agent_name)

        self._agent_map_lock.acquire()
        try:
            if agent_name in self._agent_map:
                return self._agent_map[agent_name]

            agent._console = self
            agent._sender = self._session.sender(agent._address)
            self._agent_map[agent_name] = agent
        finally:
            self._agent_map_lock.release()

        return agent



    def destroy_agent( self, agent ):
        """
        Undoes create.
        """
        if not isinstance(agent, AgentProxy):
            raise TypeError("agent must be an instance of AgentProxy")

        self._agent_map_lock.acquire()
        try:
            if agent._name in self._agent_map:
                del self._agent_map[agent._name]
        finally:
            self._agent_map_lock.release()




    def find_agent(self, agent_name, timeout=None ):
        """
        Given the name of a particular agent, return an AgentProxy representing 
        that agent.  Return None if the agent does not exist.
        """
        self._agent_map_lock.acquire()
        try:
            if agent_name in self._agent_map:
                return self._agent_map[agent_name]
        finally:
            self._agent_map_lock.release()

        new_agent = self.create_agent(agent_name)
        msg = Message(subject="qmf4",
                      properties={"method":"request",
                                  "opcode":"agent-locate"},
                      content={"query": {"vendor" : agent_name.vendor(),
                                         "product" : agent_name.product(),
                                         "name" : agent_name.name()}})
        handle = new_agent._send_msg(msg)
        if handle == 0:
            raise Exception("Failed to send Agent locate message to agent %s" % str(agent_name))

        msg = new_agent._fetch_reply_msg(handle, timeout)
        if not msg:
            logging.debug("Unable to contact agent '%s' - no reply." % agent_name)
            self.destroy_agent(new_agent)
            return None
        # @todo - for now, dump the message
        logging.info( "agent-locate reply received for %s" % agent_name)
        return new_agent



    def run(self):
        global _callback_thread
        #
        # @todo KAG Rewrite when api supports waiting on multiple receivers
        #
        while self._operational:

            qLen = self._work_q.qsize()

            try:
                msg = self._announce_recvr.fetch(timeout = 0)
                if msg:
                    self._rcv_announce(msg)
            except:
                pass

            try:
                msg = self._direct_recvr.fetch(timeout = 0)
                if msg:
                    self._rcv_direct(msg)
            except:
                pass

            # try:
            #     logging.error("waiting for next rcvr...")
            #     rcvr = self._session.next_receiver()
            # except:
            #     logging.error("exception during next_receiver()")

            # logging.error("rcvr=%s" % str(rcvr))


            if qLen == 0 and self._work_q.qsize() and self._notifier:
                # work queue went non-empty, kick
                # the application...

                _callback_thread = currentThread()
                logging.info("Calling console indication")
                self._notifier.console_indication()
                _callback_thread = None

            while self._operational and \
                    self._announce_recvr.pending() == 0 and \
                    self._direct_recvr.pending():
                time.sleep(0.5)

        logging.debug("Shutting down Console thread")



    # called by run() thread ONLY
    #
    def _rcv_announce(self, msg):
        """
        PRIVATE: Process a message received on the announce receiver
        """
        logging.info( "Announce message received!" )
        if msg.subject != "qmf4":
            logging.debug("Ignoring non-qmf message '%s'" % msg.subject)
            return

        amap = {}
        if msg.content_type == "amqp/map":
            amap = msg.content

        if (not msg.properties or
            not "method" in msg.properties or
            not "opcode" in msg.properties):
            logging.error("INVALID MESSAGE PROPERTIES: '%s'" % str(msg.properties))
            return

        if msg.properties["method"] == "indication":
            # agent indication
            if msg.properties["opcode"] == "agent":
                if "name" in amap:
                    if self._agent_discovery:
                        ind = amap["name"]
                        if "vendor" in ind and "product" in ind and "name" in ind:

                            agent = self.create_agent(AgentId( ind["vendor"],
                                                               ind["product"],
                                                               ind["name"] ))
                            if not agent._exists:
                                # new agent
                                agent._exists = True
                                logging.info("AGENT_ADDED for %s" % agent)
                                wi = WorkItem(WorkItem.AGENT_ADDED,
                                              {"agent": agent})
                                self._work_q.put(wi)
            else:
                logging.warning("Ignoring message with unrecognized 'opcode' value: '%s'"
                                % msg.properties["opcode"])
        else:
            logging.warning("Ignoring message with unrecognized 'method' value: '%s'" 
                            % msg.properties["method"] )




    # called by run() thread ONLY
    #
    def _rcv_direct(self, msg):
        """
        PRIVATE: Process a message sent to my direct receiver
        """
        logging.info( "direct message received!" )
        if msg.correlation_id:
            self._req_correlation.put_data(msg.correlation_id, msg)



    def enable_agent_discovery(self):
        """
        Called to enable the asynchronous Agent Discovery process.
        Once enabled, AGENT_ADD work items can arrive on the WorkQueue.
        """
        if not self._agent_discovery:
            self._agent_discovery = True
            msg = Message(subject="qmf4",
                          properties={"method":"request",
                                      "opcode":"agent-locate"},
                          content={"query": {"vendor": "*",
                                             "product": "*",
                                             "name": "*"}})
            self._locate_sender.send(msg)



    def disable_agent_discovery(self):
        """
        Called to disable the async Agent Discovery process enabled by
        calling enable_agent_discovery()
        """
        self._agent_discovery = False



    def get_workitem_count(self):
        """
        Returns the count of pending WorkItems that can be retrieved.
        """
        return self._work_q.qsize()



    def get_next_workitem(self, timeout=None):
        """
        Returns the next pending work item, or None if none available.
        @todo: subclass and return an Empty event instead.
        """
        try:
            wi = self._work_q.get(True, timeout)
        except Queue.Empty:
            return None
        return wi


    def release_workitem(self, wi):
        """
        Return a WorkItem to the Console when it is no longer needed.
        @todo: call Queue.task_done() - only 2.5+

        @type wi: class qmfConsole.WorkItem
        @param wi: work item object to return.
        """
        pass



    # def get_packages(self):
    #     plist = []
    #     for i in range(self.impl.packageCount()):
    #         plist.append(self.impl.getPackageName(i))
    #     return plist
    
    
    # def get_classes(self, package, kind=CLASS_OBJECT):
    #     clist = []
    #     for i in range(self.impl.classCount(package)):
    #         key = self.impl.getClass(package, i)
    #         class_kind = self.impl.getClassKind(key)
    #         if class_kind == kind:
    #             if kind == CLASS_OBJECT:
    #                 clist.append(SchemaObjectClass(None, None, {"impl":self.impl.getObjectClass(key)}))
    #             elif kind == CLASS_EVENT:
    #                 clist.append(SchemaEventClass(None, None, {"impl":self.impl.getEventClass(key)}))
    #     return clist
    
    
    # def bind_package(self, package):
    #     return self.impl.bindPackage(package)
    
    
    # def bind_class(self, kwargs = {}):
    #     if "key" in kwargs:
    #         self.impl.bindClass(kwargs["key"])
    #     elif "package" in kwargs:
    #         package = kwargs["package"]
    #         if "class" in kwargs:
    #             self.impl.bindClass(package, kwargs["class"])
    #         else:
    #             self.impl.bindClass(package)
    #     else:
    #         raise Exception("Argument error: invalid arguments, use 'key' or 'package'[,'class']")
    
    
    # def get_agents(self, broker=None):
    #     blist = []
    #     if broker:
    #         blist.append(broker)
    #     else:
    #         self._cv.acquire()
    #         try:
    #             # copy while holding lock
    #             blist = self._broker_list[:]
    #         finally:
    #             self._cv.release()

    #     agents = []
    #     for b in blist:
    #         for idx in range(b.impl.agentCount()):
    #             agents.append(AgentProxy(b.impl.getAgent(idx), b))

    #     return agents
    
    
    # def get_objects(self, query, kwargs = {}):
    #     timeout = 30
    #     agent = None
    #     temp_args = kwargs.copy()
    #     if type(query) == type({}):
    #         temp_args.update(query)

    #     if "_timeout" in temp_args:
    #         timeout = temp_args["_timeout"]
    #         temp_args.pop("_timeout")

    #     if "_agent" in temp_args:
    #         agent = temp_args["_agent"]
    #         temp_args.pop("_agent")

    #     if type(query) == type({}):
    #         query = Query(temp_args)

    #     self._select = {}
    #     for k in temp_args.iterkeys():
    #         if type(k) == str:
    #             self._select[k] = temp_args[k]

    #     self._cv.acquire()
    #     try:
    #         self._sync_count = 1
    #         self._sync_result = []
    #         broker = self._broker_list[0]
    #         broker.send_query(query.impl, None, agent)
    #         self._cv.wait(timeout)
    #         if self._sync_count == 1:
    #             raise Exception("Timed out: waiting for query response")
    #     finally:
    #         self._cv.release()

    #     return self._sync_result
    
    
    # def get_object(self, query, kwargs = {}):
    #     '''
    #     Return one and only one object or None.
    #     '''
    #     objs = objects(query, kwargs)
    #     if len(objs) == 1:
    #         return objs[0]
    #     else:
    #         return None


    # def first_object(self, query, kwargs = {}):
    #     '''
    #     Return the first of potentially many objects.
    #     '''
    #     objs = objects(query, kwargs)
    #     if objs:
    #         return objs[0]
    #     else:
    #         return None


    # # Check the object against select to check for a match
    # def _select_match(self, object):
    #     schema_props = object.properties()
    #     for key in self._select.iterkeys():
    #         for prop in schema_props:
    #             if key == p[0].name() and self._select[key] != p[1]:
    #                 return False
    #     return True


    # def _get_result(self, list, context):
    #     '''
    #     Called by Broker proxy to return the result of a query.
    #     '''
    #     self._cv.acquire()
    #     try:
    #         for item in list:
    #             if self._select_match(item):
    #                 self._sync_result.append(item)
    #         self._sync_count -= 1
    #         self._cv.notify()
    #     finally:
    #         self._cv.release()


    # def start_sync(self, query): pass
    
    
    # def touch_sync(self, sync): pass
    
    
    # def end_sync(self, sync): pass
    
    


#     def start_console_events(self):
#         self._cb_cond.acquire()
#         try:
#             self._cb_cond.notify()
#         finally:
#             self._cb_cond.release()


#     def _do_console_events(self):
#         '''
#         Called by the Console thread to poll for events.  Passes the events
#         onto the ConsoleHandler associated with this Console.  Is called
#         periodically, but can also be kicked by Console.start_console_events().
#         '''
#         count = 0
#         valid = self.impl.getEvent(self._event)
#         while valid:
#             count += 1
#             try:
#                 if self._event.kind == qmfengine.ConsoleEvent.AGENT_ADDED:
#                     logging.debug("Console Event AGENT_ADDED received")
#                     if self._handler:
#                         self._handler.agent_added(AgentProxy(self._event.agent, None))
#                 elif self._event.kind == qmfengine.ConsoleEvent.AGENT_DELETED:
#                     logging.debug("Console Event AGENT_DELETED received")
#                     if self._handler:
#                         self._handler.agent_deleted(AgentProxy(self._event.agent, None))
#                 elif self._event.kind == qmfengine.ConsoleEvent.NEW_PACKAGE:
#                     logging.debug("Console Event NEW_PACKAGE received")
#                     if self._handler:
#                         self._handler.new_package(self._event.name)
#                 elif self._event.kind == qmfengine.ConsoleEvent.NEW_CLASS:
#                     logging.debug("Console Event NEW_CLASS received")
#                     if self._handler:
#                         self._handler.new_class(SchemaClassKey(self._event.classKey))
#                 elif self._event.kind == qmfengine.ConsoleEvent.OBJECT_UPDATE:
#                     logging.debug("Console Event OBJECT_UPDATE received")
#                     if self._handler:
#                         self._handler.object_update(ConsoleObject(None, {"impl":self._event.object}),
#                                                     self._event.hasProps, self._event.hasStats)
#                 elif self._event.kind == qmfengine.ConsoleEvent.EVENT_RECEIVED:
#                     logging.debug("Console Event EVENT_RECEIVED received")
#                 elif self._event.kind == qmfengine.ConsoleEvent.AGENT_HEARTBEAT:
#                     logging.debug("Console Event AGENT_HEARTBEAT received")
#                     if self._handler:
#                         self._handler.agent_heartbeat(AgentProxy(self._event.agent, None), self._event.timestamp)
#                 elif self._event.kind == qmfengine.ConsoleEvent.METHOD_RESPONSE:
#                     logging.debug("Console Event METHOD_RESPONSE received")
#                 else:
#                     logging.debug("Console thread received unknown event: '%s'" % str(self._event.kind))
#             except e:
#                 print "Exception caught in callback thread:", e
#             self.impl.popEvent()
#             valid = self.impl.getEvent(self._event)
#         return count





# class Broker(ConnectionHandler):
#     #   attr_reader :impl :conn, :console, :broker_bank
#     def __init__(self, console, conn):
#         self.broker_bank = 1
#         self.console = console
#         self.conn = conn
#         self._session = None
#         self._cv = Condition()
#         self._stable = None
#         self._event = qmfengine.BrokerEvent()
#         self._xmtMessage = qmfengine.Message()
#         self.impl = qmfengine.BrokerProxy(self.console.impl)
#         self.console.impl.addConnection(self.impl, self)
#         self.conn.add_conn_handler(self)
#         self._operational = True
    
    
#     def shutdown(self):
#         logging.debug("broker.shutdown() called.")
#         self.console.impl.delConnection(self.impl)
#         self.conn.del_conn_handler(self)
#         if self._session:
#             self.impl.sessionClosed()
#             logging.debug("broker.shutdown() sessionClosed done.")
#             self._session.destroy()
#             logging.debug("broker.shutdown() session destroy done.")
#             self._session = None
#         self._operational = False
#         logging.debug("broker.shutdown() done.")


#     def wait_for_stable(self, timeout = None):
#         self._cv.acquire()
#         try:
#             if self._stable:
#                 return
#             if timeout:
#                 self._cv.wait(timeout)
#                 if not self._stable:
#                     raise Exception("Timed out: waiting for broker connection to become stable")
#             else:
#                 while not self._stable:
#                     self._cv.wait()
#         finally:
#             self._cv.release()


#     def send_query(self, query, ctx, agent):
#         agent_impl = None
#         if agent:
#             agent_impl = agent.impl
#         self.impl.sendQuery(query, ctx, agent_impl)
#         self.conn.kick()


#     def _do_broker_events(self):
#         count = 0
#         valid = self.impl.getEvent(self._event)
#         while valid:
#             count += 1
#             if self._event.kind == qmfengine.BrokerEvent.BROKER_INFO:
#                 logging.debug("Broker Event BROKER_INFO received");
#             elif self._event.kind == qmfengine.BrokerEvent.DECLARE_QUEUE:
#                 logging.debug("Broker Event DECLARE_QUEUE received");
#                 self.conn.impl.declareQueue(self._session.handle, self._event.name)
#             elif self._event.kind == qmfengine.BrokerEvent.DELETE_QUEUE:
#                 logging.debug("Broker Event DELETE_QUEUE received");
#                 self.conn.impl.deleteQueue(self._session.handle, self._event.name)
#             elif self._event.kind == qmfengine.BrokerEvent.BIND:
#                 logging.debug("Broker Event BIND received");
#                 self.conn.impl.bind(self._session.handle, self._event.exchange, self._event.name, self._event.bindingKey)
#             elif self._event.kind == qmfengine.BrokerEvent.UNBIND:
#                 logging.debug("Broker Event UNBIND received");
#                 self.conn.impl.unbind(self._session.handle, self._event.exchange, self._event.name, self._event.bindingKey)
#             elif self._event.kind == qmfengine.BrokerEvent.SETUP_COMPLETE:
#                 logging.debug("Broker Event SETUP_COMPLETE received");
#                 self.impl.startProtocol()
#             elif self._event.kind == qmfengine.BrokerEvent.STABLE:
#                 logging.debug("Broker Event STABLE received");
#                 self._cv.acquire()
#                 try:
#                     self._stable = True
#                     self._cv.notify()
#                 finally:
#                     self._cv.release()
#             elif self._event.kind == qmfengine.BrokerEvent.QUERY_COMPLETE:
#                 result = []
#                 for idx in range(self._event.queryResponse.getObjectCount()):
#                     result.append(ConsoleObject(None, {"impl":self._event.queryResponse.getObject(idx), "broker":self}))
#                 self.console._get_result(result, self._event.context)
#             elif self._event.kind == qmfengine.BrokerEvent.METHOD_RESPONSE:
#                 obj = self._event.context
#                 obj._method_result(MethodResponse(self._event.methodResponse()))
            
#             self.impl.popEvent()
#             valid = self.impl.getEvent(self._event)
        
#         return count
    
    
#     def _do_broker_messages(self):
#         count = 0
#         valid = self.impl.getXmtMessage(self._xmtMessage)
#         while valid:
#             count += 1
#             logging.debug("Broker: sending msg on connection")
#             self.conn.impl.sendMessage(self._session.handle, self._xmtMessage)
#             self.impl.popXmt()
#             valid = self.impl.getXmtMessage(self._xmtMessage)
        
#         return count
    
    
#     def _do_events(self):
#         while True:
#             self.console.start_console_events()
#             bcnt = self._do_broker_events()
#             mcnt = self._do_broker_messages()
#             if bcnt == 0 and mcnt == 0:
#                 break;
    
    
#     def conn_event_connected(self):
#         logging.debug("Broker: Connection event CONNECTED")
#         self._session = Session(self.conn, "qmfc-%s.%d" % (socket.gethostname(), os.getpid()), self)
#         self.impl.sessionOpened(self._session.handle)
#         self._do_events()
    
    
#     def conn_event_disconnected(self, error):
#         logging.debug("Broker: Connection event DISCONNECTED")
#         pass
    
    
#     def conn_event_visit(self):
#         self._do_events()


#     def sess_event_session_closed(self, context, error):
#         logging.debug("Broker: Session event CLOSED")
#         self.impl.sessionClosed()
    
    
#     def sess_event_recv(self, context, message):
#         logging.debug("Broker: Session event MSG_RECV")
#         if not self._operational:
#             logging.warning("Unexpected session event message received by Broker proxy: context='%s'" % str(context))
#         self.impl.handleRcvMessage(message)
#         self._do_events()


