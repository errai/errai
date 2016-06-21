/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jsinterop.demo.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.TakesValue;

@Dependent
@Templated("IpsumListDisplay.html#entry")
public class IpsumDescriptorDisplay implements TakesValue<IpsumDescriptor>, IsElement {

  @Inject
  @DataField
  private Span name;

  @Inject
  @DataField
  private Span description;

  private IpsumDescriptor value = new IpsumDescriptor();

  @Inject
  private Event<IpsumDescriptor> clickEvent;

  @PostConstruct
  private void init() {
    getElement().setOnclick(evt -> clickEvent.fire(value));
  }

  @Override
  public void setValue(final IpsumDescriptor value) {
    this.value = value;
    name.setTextContent(value.getName());
    description.setInnerHTML(value.getDescription());
  }

  @Override
  public IpsumDescriptor getValue() {
    return value;
  }

}
