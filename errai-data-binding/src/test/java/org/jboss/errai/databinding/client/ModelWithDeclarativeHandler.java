/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeEvent;
import org.jboss.errai.ui.shared.api.annotations.PropertyChangeHandler;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Bindable
public class ModelWithDeclarativeHandler {

  private String str;
  // Tests that property name is based on methods.
  private boolean bool1;

  private final List<PropertyChangeEvent<?>> observedEvents = new ArrayList<>();

  public String getStr() {
    return str;
  }

  public void setStr(final String str) {
    this.str = str;
  }

  public boolean isBool() {
    return bool1;
  }

  public void setBool(final boolean bool) {
    this.bool1 = bool;
  }

  public List<PropertyChangeEvent<?>> observedEvents() {
    return observedEvents;
  }

  @PropertyChangeHandler("str")
  public void onStrChange(final PropertyChangeEvent<String> event) {
    observedEvents.add(event);
  }

  @PropertyChangeHandler("bool")
  public void onBoolChange(final PropertyChangeEvent<Boolean> event) {
    observedEvents.add(event);
  }

  @PropertyChangeHandler
  public void onAnyChange(final PropertyChangeEvent<?> event) {
    observedEvents.add(event);
  }

}
