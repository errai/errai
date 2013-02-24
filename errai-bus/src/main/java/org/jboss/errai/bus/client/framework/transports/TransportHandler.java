/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.framework.transports;

import org.jboss.errai.bus.client.api.Message;

import java.util.Collection;
import java.util.List;

/**
* @author Mike Brock
*/
public interface TransportHandler {
  public static final String EXTRA_URI_PARMS_RESOURCE = "^ExtraURIParameters";

  public void configure(Message capabilitiesMessage);

  public void start();

  public Collection<Message> stop(boolean stopAllCurrentRequests);

  public void transmit(List<Message> txMessages);

  public void handleProtocolExtension(Message message);

  public boolean isUsable();
}
