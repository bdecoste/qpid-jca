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
package org.apache.qpid.management.domain.handler.base;

import org.apache.qpid.management.domain.model.DomainModel;
import org.apache.qpid.transport.util.Logger;

/**
 * Base class for all message handlers. 
 * A message handler is an handler for a specific type of message.
 * Message type is defined by the opcode.
 * 
 * @author Andrea Gazzarini
 */
public abstract class BaseMessageHandler implements IMessageHandler
{
    /**
     * Logger used for logging.
     */
    protected final Logger _logger = Logger.get(getClass());
    
    /**
     * Managed broker domain model. 
     */
    protected DomainModel _domainModel;
    
    /**
     * Sets the broker domain model.
     * 
     * @param domainModel the broker domain model.
     */
    public void setDomainModel(DomainModel domainModel)
    {
        this._domainModel = domainModel;
    }
}