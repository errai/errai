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

package org.jboss.errai.demo.grocery.client.local;

/**
 * Event class to signify that an item is being edited
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class ItemEditNotifier {
  
  public static final int EDIT_EVENT = 1;
  public static final int DELETE_EVENT = 2;
  
  private int creatorId;
  private int notifierType;
  
  public ItemEditNotifier(int id, int eventType) {
    super();
      this.creatorId = id;
      this.setNotifierType(eventType);
  }
  
  public int getCreatorId() {
    return this.creatorId;
  }
  
  public void setCreatorId(int id) {
    this.creatorId = id;
  }
  
  public int getNotifierType() {
    return notifierType;
  }
  public void setNotifierType(int notifierType) {
    this.notifierType = notifierType;
  }
  
  public boolean isCreatedBy(int id) {
    if (id == this.getCreatorId()) {
      return true;
    }
    
    return false;
  }
}
