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

package org.jboss.errai.jpa.test.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.errai.jpa.client.shared.GlobalEntityListener;

@GlobalEntityListener
public class TestingGlobalEntityListener {

  /**
   * A place to record JPA entity lifecycle events when they happen so they can
   * be verified in the test suite.
   */
  public static final List<CallbackLogEntry> CALLBACK_LOG = new ArrayList<CallbackLogEntry>();

  // ------ Lifecycle callbacks (assorted access levels to test that they all work) ------
  @PrePersist private void prePersist(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PrePersist.class)); };
  @PostPersist private void postPersist(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PostPersist.class)); };
  @PreRemove void preRemove(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PreRemove.class)); };
  @PostRemove void postRemove(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PostRemove.class)); };
  @PreUpdate protected void preUpdate(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PreUpdate.class)); };
  @PostUpdate protected void postUpdate(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PostUpdate.class)); };
  @PostLoad public void postLoad(Object entity) { CALLBACK_LOG.add(new CallbackLogEntry(entity, PostLoad.class)); };
}
