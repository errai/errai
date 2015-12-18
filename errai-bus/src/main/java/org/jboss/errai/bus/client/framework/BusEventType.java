/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;

/**
 * An enumeration which represents the logical states that the bus can be in.
 *
 * @author Jonathan Fuerth
 * @author Christian Sadilek
 */
public enum BusEventType {
  ASSOCIATING {
    @Override
    public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
      l.busAssociating(e);
    }
  },
  DISASSOCIATING {
    @Override
    public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
      l.busDisassociating(e);
    }
  },
  ONLINE {
    @Override
    public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
      l.busOnline(e);
    }
  },
  OFFLINE {
    @Override
    public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
      l.busOffline(e);
    }
  };

  public abstract void deliverTo(BusLifecycleListener l, BusLifecycleEvent e);
}
