/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.sync.test.client.ioc;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.jpa.sync.client.local.Sync;
import org.jboss.errai.jpa.sync.client.local.SyncParam;
import org.jboss.errai.jpa.sync.client.shared.SyncResponses;
import org.jboss.errai.jpa.sync.test.client.entity.SimpleEntity;

@Dependent
public class DependentScopedSyncBean {

  private long id;
  private String name;
  private SyncResponses<SimpleEntity> responses;
  private int callbackCount;

  @Sync(query = "simpleEntitiesByIdAndString",
      params = { @SyncParam(name = "id", val = "{id}"), @SyncParam(name = "string", val = "{name}"),
          @SyncParam(name = "literal", val = "literalValue") })
  private void onSyncResponse(SyncResponses<SimpleEntity> responses) {
    this.responses = responses;
    callbackCount++;
  }
  
  @PostConstruct
  private void init() {
    id = 1;
    name = "test";
  }

  public SyncResponses<SimpleEntity> getResponses() {
    return responses;
  }

  @Override
  public String toString() {
    return "DependentScopedSyncBean [id=" + id + ", name=" + name + ", responses=" + responses + "]";
  }

  public int getCallbackCount() {
    return callbackCount;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
