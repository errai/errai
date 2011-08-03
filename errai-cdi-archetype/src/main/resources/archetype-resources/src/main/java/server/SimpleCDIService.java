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
package ${package}.server;

import ${package}.client.shared.MessageEvent;
import ${package}.client.shared.ResponseEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * A very simple CDI based sevice.
 */
@ApplicationScoped
public class SimpleCDIService {
    @Inject
    private Event<ResponseEvent> responseEvent;

    public void handleMessage(@Observes MessageEvent event) {
        System.out.println("Received Message from Client: " + event.getMessage());
        responseEvent.fire(new ResponseEvent(event.getMessage() + " @ timemillis: " + System.currentTimeMillis()));
    }
}