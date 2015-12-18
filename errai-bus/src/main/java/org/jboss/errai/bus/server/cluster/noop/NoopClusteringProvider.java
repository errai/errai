/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.cluster.noop;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public class NoopClusteringProvider implements ClusteringProvider {
  private static Logger log = LoggerFactory.getLogger(NoopClusteringProvider.class);

  public NoopClusteringProvider() {
    log.info("clustering support not configured.");
  }

  @Override
  public void clusterTransmit(String sessionId, String subject, String messageId) {
  }

  @Override
  public void clusterTransmitGlobal(Message message) {
  }
}
