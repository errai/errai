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

import org.jboss.weld.context.bound.BoundRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 28, 2010
 */
public class ContextManager {

    private BoundRequestContext delegate;

    private ThreadLocal<Map<String, Object>> boundContext =
            new ThreadLocal<Map<String, Object>>();

    private String uuid;

    public ContextManager(String uuid, BoundRequestContext requestContext) {
        this.delegate = requestContext;
    }

    public void activateContexts(boolean active)
    {
        if(active)
        {
            boundContext.set(new HashMap<String, Object>());

            delegate.associate(boundContext.get());
            delegate.activate();
        }
        else
        {
            delegate.invalidate();            
            delegate.deactivate();
            delegate.dissociate(boundContext.get());
        }
    }
}
