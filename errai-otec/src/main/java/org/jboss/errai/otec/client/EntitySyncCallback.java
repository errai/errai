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

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

/**
 * @author Mike Brock
 */
public class EntitySyncCallback implements MessageCallback {
  private final EntitySyncCompletionCallback<State> callback;
  private OTEngine engine;

  public EntitySyncCallback(final OTEngine engine, final EntitySyncCompletionCallback<State> callback) {
    this.callback = callback;
    this.engine = engine;
  }

  @Override
  public void callback(final Message message) {
    final OTEntity entity
        = new OTEntityImpl<StringState>(message.get(Integer.class, "EntityID"), StringState.of(message.getValue(String.class)));

    engine.getEntityStateSpace().addEntity(entity);
    final Integer revision = message.get(Integer.class, "revision");
    entity.setRevision(revision);
    entity.resetRevisionCounterTo(revision);

    if (revision != 0) {
      entity.getState().updateHash();
    }

    MessageBuilder.createMessage()
        .toSubject("ServerOTEngineSyncService")
        .signalling()
        .withValue(entity.getId())
        .with("SyncAck", Boolean.TRUE)
        .done().sendNowWith(ErraiBus.get());

    callback.syncComplete(entity);
  }
}
