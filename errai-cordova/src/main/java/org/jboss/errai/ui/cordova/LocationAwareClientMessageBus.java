/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ui.cordova;

import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.Configuration;

import java.util.logging.Logger;

/**
 * Overrides the standard ClientMessageBus implementation that throws exception when the user did not specify the endpoint
 *
 * @author Erik Jan de Wit
 */
public class LocationAwareClientMessageBus extends ClientMessageBusImpl {
  private static final Logger LOG = Logger.getLogger(LocationAwareClientMessageBus.class.getName());

  @Override
  protected String getApplicationLocation(String serviceEntryPoint) {
    Configuration configuration = getConfiguration();
    if (configuration instanceof Configuration.NotSpecified) {
      throw new IllegalArgumentException("you need to implement Configuration in order to point to the server location");
    }

    LOG.info("url end point " + configuration.getRemoteLocation());

    return super.getApplicationLocation(serviceEntryPoint);
  }
}
