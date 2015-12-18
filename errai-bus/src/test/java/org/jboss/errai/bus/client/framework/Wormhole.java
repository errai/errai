/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.ClientMessageBus;

/**
 * Allows tests in the proper package to access private bus features.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Wormhole {

  public static Fixer changeBusEndpointUrl(ClientMessageBus bus, String newUrl) {
    String oldInEntryPoint = ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT;
    String oldOutEntryPoint = ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT;

    ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT = newUrl;
    ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT = newUrl;

    System.out.println("CHANGED ENDPOINT TO: " + newUrl);
    return new Fixer(bus, oldInEntryPoint, oldOutEntryPoint);
  }

  public static class Fixer {
    ClientMessageBus bus;
    String oldInEntryPoint;
    String oldOutEntryPoint;

    Fixer(ClientMessageBus bus, String oldInEntryPoint, String oldOutEntryPoint) {
      this.bus = bus;
      this.oldInEntryPoint = oldInEntryPoint;
      this.oldOutEntryPoint = oldOutEntryPoint;
    }

    public void fix() {
      ((ClientMessageBusImpl) bus).IN_SERVICE_ENTRY_POINT = oldInEntryPoint;
      ((ClientMessageBusImpl) bus).OUT_SERVICE_ENTRY_POINT = oldOutEntryPoint;
    }
  }
}
