/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.demo.mobile.client.shared;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * A collection of all the latest client orientations, periodically fired from the server and observed by the clients.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class AllClientOrientations {

    private static int nextInstanceId = 0;

    private static synchronized int nextId() {
        return nextInstanceId++;
    }

    private final List<ClientOrientationEvent> clientOrientations;
    private final int instanceId;

    public AllClientOrientations(List<ClientOrientationEvent> clientOrientations) {
        this.clientOrientations = clientOrientations;
        instanceId = nextId();
    }

    public AllClientOrientations(
        @MapsTo("clientOrientations") List<ClientOrientationEvent> clientOrientations,
        @MapsTo("instanceId") int instanceId) {
        this.clientOrientations = clientOrientations;
        this.instanceId = instanceId;
    }

    public List<ClientOrientationEvent> getClientOrientations() {
        return clientOrientations;
    }

    public int getInstanceId() {
        return instanceId;
    }
}
