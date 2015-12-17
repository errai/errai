/**
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

package org.jboss.errai.ui.test.quickhandler.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.TemplateWidgetMapper;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.dom.ButtonElement;
import org.jboss.errai.ui.test.common.client.dom.Document;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;

@Templated("QuickHandlerComponent.html")
public class NonCompositeQuickHandlerComponent implements QuickHandlerComponent {

  @DataField
  private Element root = DOM.createDiv();

  @DataField
  private final AnchorElement c1 = DOM.createAnchor().cast();

  @Inject
  @DataField
  private Button c2;

  @DataField
  private ButtonElement c3 = Document.getDocument().createButtonElement();

  private boolean c0EventFired = false;
  private boolean c1EventFired = false;
  private boolean c1_dupEventFired = false;
  private boolean c2EventFired = false;
  private boolean c3EventFired = false;
  private boolean thisEventFired = false;
  private final boolean c0EventFired2 = false;


  @Override
  public AnchorElement getC1() {
    return c1;
  }

  @Override
  public Button getC2() {
    return c2;
  }

  @Override
  public ButtonElement getC3() {
    return c3;
  }

  @EventHandler("c0")
  @SinkNative(Event.ONCLICK | Event.ONFOCUS)
  private void doSomethingC0(Event e) {
    c0EventFired = true;
  }

  @EventHandler("c1")
  private void doSomethingC1(ClickEvent e) {
    c1EventFired = true;
  }

  @Override
  @EventHandler("c1")
  public void doSomethingC1_dup(ClickEvent e) {
    c1_dupEventFired = true;
  }

  @Override
  @EventHandler("c2")
  public void doSomethingC2(ClickEvent e) {
    c2EventFired = true;
  }

  @Override
  @EventHandler("c3")
  public void doSomethingC3(ClickEvent event) {
    c3EventFired = true;
  }

  @Override
  @EventHandler
  public void doSomethingOnThis(ClickEvent e) {
    thisEventFired = true;
  }

  @Override
  public boolean isC0EventFired() {
    return c0EventFired;
  }

  @Override
  public boolean isC0EventFired2() {
    return c0EventFired2;
  }

  @Override
  public boolean isC1EventFired() {
    return c1EventFired;
  }

  @Override
  public boolean isC1_dupEventFired() {
    return c1_dupEventFired;
  }

  @Override
  public boolean isC2EventFired() {
    return c2EventFired;
  }

  @Override
  public boolean isC3EventFired() {
    return c3EventFired;
  }

  @Override
  public boolean isThisEventFired() {
    return thisEventFired;
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    TemplateWidgetMapper.get(this).fireEvent(event);
  }

  public Element getRoot() {
    return root;
  }
}
