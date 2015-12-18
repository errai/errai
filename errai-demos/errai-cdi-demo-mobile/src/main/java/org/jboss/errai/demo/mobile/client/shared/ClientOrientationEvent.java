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

package org.jboss.errai.demo.mobile.client.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

/**
 * @author edewit@redhat.com
 */
@Portable
public class ClientOrientationEvent extends OrientationEvent {

  private String clientId;
  private long timestamp;

  public ClientOrientationEvent(String clientId, OrientationEvent event) {
    this(clientId, System.currentTimeMillis(), event.getX(), event.getY(), event.getZ());
  }

  public ClientOrientationEvent(@MapsTo("clientId") String clientId, @MapsTo("timestamp") long timestamp,
        @MapsTo("x") double x,
        @MapsTo("y") double y, @MapsTo("z") double z) {
    super(x, y, z);
    this.clientId = clientId;
    this.timestamp = timestamp;
  }

  public String getClientId() {
    return clientId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "ClientOrientationEvent{" + "clientId='" + clientId + '\'' + '}';
  }
}
