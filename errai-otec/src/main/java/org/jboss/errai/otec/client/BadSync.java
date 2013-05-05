/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.otec.client;

/**
 * @author Mike Brock
 */
public class BadSync extends RuntimeException {
  private final int entityId;
  private final String agentId;

  public BadSync(String message, int entityId, String agentId) {
    super(message);
    this.entityId = entityId;
    this.agentId = agentId;
  }

  public int getEntityId() {
    return entityId;
  }

  public String getAgentId() {
    return agentId;
  }
}
