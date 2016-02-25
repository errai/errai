/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.api.annotations.IOCProducer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to test bindings to widgets of custom HasValue types.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Dependent
public class TestModelWidget extends Widget implements HasValue<TestModel> {

  @IOCProducer
  @Qual
  public static TestModelWidget create() {
    return new TestModelWidget() {
      @Override
      public boolean isQualified() {
        return true;
      }
    };
  }

  private TestModel value;

  public TestModelWidget() {
    setElement(Document.get().createDivElement());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TestModel> handler) {
    return null;
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public TestModel getValue() {
    return value;
  }

  @Override
  public void setValue(TestModel value) {
    this.value = value;
  }

  @Override
  public void setValue(TestModel value, boolean fireEvents) {
    this.value = value;
  }

  public boolean isQualified() {
    return false;
  }

}
