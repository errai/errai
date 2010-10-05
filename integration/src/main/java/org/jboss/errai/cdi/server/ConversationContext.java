/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.cdi.server;

import org.jboss.weld.context.bound.BoundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A conversation is used to span multiple requests, however is shorter than a
 * session. The {@link org.jboss.weld.context.bound.BoundConversationContext} uses one Map to represent a
 * request, and a second to represent the session, which are encapsulated in a
 * {@link BoundRequest}.
 * </p>
 *
 * TODO: Verify thread safety requirements
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 5, 2010
 */
public class ConversationContext implements BoundRequest {

    private Map<String, Object> requestMap = new HashMap<String, Object>();
    private Map<String, Object> sessionMap = new HashMap<String, Object>();

    /**
     * Get the current request map.
     * @return
     */
    public Map<String, Object> getRequestMap() {
        return requestMap;
    }

    /**
    * <p>
    * Get the current session map.
    * </p>
    *
    * <p>
    * A {@link BoundRequest} may be backed by a data store that only creates
    * sessions on demand. It is recommended that if the session is not created
    * on demand, or that the session has already been created (but is not
    * required by this access) that the session is returned as it allows the
    * conversation context to work more efficiently.
    * </p>
    *
    * @param create if true, then a session must be created
    * @return the session map; null may be returned if create is false
    */
    public Map<String, Object> getSessionMap(boolean create) {
        return sessionMap;
    }
}
