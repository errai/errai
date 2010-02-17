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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.BooleanRoutingRule;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;


/**
 * A delegate message handler for encapsulating an endpoint around a routing rule.  This is the underlying way
 * the bus secures individual endpoints based on security rules.
 *
 * @see org.jboss.errai.bus.client.api.BooleanRoutingRule
 */
public class RuleDelegateMessageCallback implements MessageCallback {
    private MessageCallback delegate;
    private BooleanRoutingRule routingRule;

    /**
     * Creates the <tt>RuleDelegateMessageCallback</tt> instance using a given delegate and a routing rule
     *
     * @param delegate - Message callback function
     * @param rule - indicates whether or not the message should be routed.
     */
    public RuleDelegateMessageCallback(MessageCallback delegate, BooleanRoutingRule rule) {
        this.delegate = delegate;
        this.routingRule = rule;
    }

    /**
     * Callback calls the delegate if the routing rule allows it
     *
     * @param message - message to pass to the delegate callback, if called
     */
    public void callback(Message message) {
        if (routingRule.decision(message)) {
            this.delegate.callback(message);
        }
    }
}
