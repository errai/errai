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
package org.jboss.errai.cdi.server.events;

import org.jboss.errai.cdi.client.events.AccountActivity;
import org.jboss.errai.cdi.client.events.Fraud;
import org.jboss.errai.cdi.server.api.Inbound;
import org.jboss.errai.cdi.server.api.Outbound;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 * Integrating with the CDI event subsystem.
 */
@ApplicationScoped
public class AccountService {
    @Inject @Any
    Event<Outbound> event;

	@Inject @Any
	Event<Fraud> frauds;

    public void watchActivity(@Observes @Inbound AccountActivity activity) {
        Fraud payload = new Fraud(System.currentTimeMillis());
        //event.fire(new Outbound(payload));
	    frauds.fire(payload);
    }
}
